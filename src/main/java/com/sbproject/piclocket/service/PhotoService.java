package com.sbproject.piclocket.service;

import com.sbproject.piclocket.dto.CreateUploadRequest;
import com.sbproject.piclocket.dto.CreateUploadResponse;
import com.sbproject.piclocket.dto.PhotoResponse;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import com.sbproject.piclocket.security.UserContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final long MAX_UPLOADS_PER_DAY = 5;

    private final PhotoRepository photoRepository;
    private final S3PresignedUrlService s3PresignedUrlService;
    private final S3ObjectService s3ObjectService;
    private final UserContextService userContextService;

    public PhotoService(PhotoRepository photoRepository, S3PresignedUrlService s3PresignedUrlService, S3ObjectService s3ObjectService, UserContextService userContextService) {
        this.photoRepository = photoRepository;
        this.s3PresignedUrlService = s3PresignedUrlService;
        this.s3ObjectService = s3ObjectService;
        this.userContextService = userContextService;
    }

    /**
     * Fetches presigned URL and returns it to the client
     *
     * @param request contains photo metadata we will store in DB
     * @return {@link CreateUploadResponse} contains the url for direct upload and photo id
     */
    public CreateUploadResponse createUploadRequest(CreateUploadRequest request) {
        UUID photoId = UUID.randomUUID();
        String userId = userContextService.getCurrentUserId();
        Instant now = Instant.now();

        // Confirm file size is within our limits
        validateFileSize(request.fileSizeBytes());

        // Confirm we haven't exhausted out max uploads for the day
        validateDailyUploadLimit(userId, now);

        Instant expiresAt = now.plus(1, ChronoUnit.DAYS); // set to auto delete after 1 day

        String s3Key = "v1/users/%s/photos/%s/original".formatted(userId, photoId);

        String uploadUrl = s3PresignedUrlService.generateUploadUrl(s3Key, request.contentType());

        Photo photo = Photo.builder()
                .photoId(photoId)
                .userId(userId)
                .filename(request.filename())
                .s3Key(s3Key)
                .status(PhotoStatus.PENDING_UPLOAD)
                .contentType(request.contentType())
                .fileSizeBytes(request.fileSizeBytes())
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(expiresAt)
                .build();

        photoRepository.save(photo);

        log.info(
                "Created upload request for photoId={}, status={}",
                photoId,
                PhotoStatus.PENDING_UPLOAD
        );

        return new CreateUploadResponse(photoId, uploadUrl);
    }

    /**
     * Checks whether or not a photo upload has successfully been completed and adjusts state accordingly
     * If successful, state is updated to UPLOADED
     *
     * @param photoId the unique identifier of a photo
     */
    public void completeUpload(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photo not found for id=%s".formatted(photoId)
                ));

        boolean photoExists = s3ObjectService.objectExists(photo.getS3Key());

        if (!photoExists) {
            log.warn("Upload completion requested but S3 object was not found for photoId={}", photoId);

            throw new IllegalStateException(
                    "Photo upload has not completed for id=%s".formatted(photoId)
            );
        }

        Instant now = Instant.now();

        photo.markUploaded(now);
        photoRepository.save(photo);

        log.info("Upload completed for photoId={}, status={}", photoId, PhotoStatus.UPLOADED);
    }

    /**
     * Retrieves photos that have successfully uploaded to S3.
     *
     * @return a list of {@link PhotoResponse}
     */
    public List<PhotoResponse> getUploadedPhotos() {
        String userId = userContextService.getCurrentUserId();

        return photoRepository.findByUserIdAndStatus(userId, PhotoStatus.UPLOADED)
                .stream()
                .map(photo -> new PhotoResponse(
                        photo.getPhotoId(),
                        photo.getStatus(),
                        s3PresignedUrlService.generateDownloadUrl(photo.getS3Key())
                ))
                .toList();
    }

    /**
     * Delete a photo from the repository and S3.
     *
     * @param photoId the unique identifier of the photo to be deleted
     */
    @Transactional
    public void deletePhoto(UUID photoId) {
        String userId = userContextService.getCurrentUserId();

        Photo photo = photoRepository.findByPhotoIdAndUserId(photoId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Photo not found"
                ));

        s3ObjectService.deleteObject(photo.getS3Key());

        photoRepository.delete(photo);
    }

    private void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of 5 MB"
            );
        }
    }

    private void validateDailyUploadLimit(String userId, Instant now) {
        Instant today = now.truncatedTo(ChronoUnit.DAYS);
        Instant tomorrow = today.plus(1, ChronoUnit.DAYS);

        long uploadsToday = photoRepository.countByUserIdAndCreatedAtBetween(
                userId,
                today,
                tomorrow
        );

        if (uploadsToday >= MAX_UPLOADS_PER_DAY) {
            throw new IllegalStateException("Daily upload limit reached");
        }
    }
}

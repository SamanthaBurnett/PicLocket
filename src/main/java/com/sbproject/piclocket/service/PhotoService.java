package com.sbproject.piclocket.service;

import com.sbproject.piclocket.dto.CreateUploadRequest;
import com.sbproject.piclocket.dto.CreateUploadResponse;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
    private static final String DEMO_USER_ID = "demo-user-id";
    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public CreateUploadResponse createUploadRequest(CreateUploadRequest request) {
        UUID photoId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(1, ChronoUnit.DAYS); // set to auto delete after 1 day

        String s3Key = "v1/users/%s/photos/%s/original".formatted(DEMO_USER_ID, photoId);

        Photo photo = Photo.builder()
                .photoId(photoId)
                .userId(DEMO_USER_ID)
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
                "Created upload request for photoId={}, userId={}, status={}",
                photoId,
                DEMO_USER_ID,
                PhotoStatus.PENDING_UPLOAD
        );

        String mockUploadedUrl = "https://mock-s3.local/upload/%s".formatted(photoId);

        return new CreateUploadResponse(photoId, mockUploadedUrl);
    }
}

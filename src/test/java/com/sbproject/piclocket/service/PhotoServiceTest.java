package com.sbproject.piclocket.service;

import com.sbproject.piclocket.dto.CreateUploadRequest;
import com.sbproject.piclocket.dto.CreateUploadResponse;
import com.sbproject.piclocket.dto.PhotoResponse;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import com.sbproject.piclocket.security.UserContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    private static final String DEMO_USER_ID = "demo-user-id";
    private static final String FILENAME = "beach.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final String S3_KEY_TEMPLATE = "v1/users/%s/photos/%s/original";
    private static final String PRESIGNED_UPLOAD_URL = "https://presigned-url.test/upload";
    private static final String PRESIGNED_DOWNLOAD_URL = "https://presigned-url.test/download";
    private static final long FILE_SIZE_BYTES = 5_000_000L;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    @Mock
    private S3ObjectService s3ObjectService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private PhotoService photoService;

    @Test
    void createUploadRequest_validRequest_returnsUploadResponse() {
        CreateUploadRequest request = new CreateUploadRequest(
                FILENAME,
                CONTENT_TYPE,
                FILE_SIZE_BYTES
        );

        when(userContextService.getCurrentUserId()).thenReturn(DEMO_USER_ID);
        when(s3PresignedUrlService.generateUploadUrl(anyString(), anyString())).thenReturn(PRESIGNED_UPLOAD_URL);

        CreateUploadResponse response = photoService.createUploadRequest(request);

        ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
        verify(photoRepository).save(photoCaptor.capture());

        Photo savedPhoto = photoCaptor.getValue();

        assertThat(savedPhoto.getPhotoId()).isNotNull();
        assertThat(savedPhoto.getUserId()).isEqualTo(DEMO_USER_ID);
        assertThat(savedPhoto.getFilename()).isEqualTo(FILENAME);
        assertThat(savedPhoto.getS3Key()).isEqualTo(S3_KEY_TEMPLATE.formatted(DEMO_USER_ID, savedPhoto.getPhotoId()));
        assertThat(savedPhoto.getStatus()).isEqualTo(PhotoStatus.PENDING_UPLOAD);
        assertThat(savedPhoto.getContentType()).isEqualTo(CONTENT_TYPE);
        assertThat(savedPhoto.getFileSizeBytes()).isEqualTo(FILE_SIZE_BYTES);
        assertThat(savedPhoto.getCreatedAt()).isNotNull();
        assertThat(savedPhoto.getUpdatedAt()).isNotNull();
        assertThat(savedPhoto.getExpiresAt()).isNotNull();

        verify(s3PresignedUrlService).generateUploadUrl(savedPhoto.getS3Key(), CONTENT_TYPE);

        assertThat(response.photoId()).isEqualTo(savedPhoto.getPhotoId());
        assertThat(response.uploadUrl()).contains(PRESIGNED_UPLOAD_URL);
    }

    @Test
    void createUploadRequest_fileTooBig_throwsException() {
        CreateUploadRequest request = new CreateUploadRequest(
                FILENAME,
                CONTENT_TYPE,
                5 * 1024 * 1024 + 1L
        );

        assertThatThrownBy(() -> photoService.createUploadRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File size exceeds maximum allowed size of 5 MB");
    }

    @Test
    void createUploadRequest_dailyLimitExceeded_throwsException() {
        CreateUploadRequest request = new CreateUploadRequest(
                FILENAME,
                CONTENT_TYPE,
                FILE_SIZE_BYTES
        );

        when(userContextService.getCurrentUserId()).thenReturn(DEMO_USER_ID);
        when(photoRepository.countByUserIdAndCreatedAtBetween(eq(DEMO_USER_ID), any(), any())).thenReturn(6L);

        assertThatThrownBy(() -> photoService.createUploadRequest(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Daily upload limit reached");
    }

    @Test
    void getUploadedPhotos_uploadedPhotosExist_returnsUploadedPhotoResponses() {
        UUID photoId = UUID.randomUUID();
        Photo uploadedPhoto = createUploadedPhoto(photoId);

        when(userContextService.getCurrentUserId()).thenReturn(DEMO_USER_ID);
        when(photoRepository.findByUserIdAndStatus(DEMO_USER_ID, PhotoStatus.UPLOADED)).thenReturn(List.of(uploadedPhoto));
        when(s3PresignedUrlService.generateDownloadUrl(uploadedPhoto.getS3Key())).thenReturn(PRESIGNED_DOWNLOAD_URL);

        List<PhotoResponse> response = photoService.getUploadedPhotos();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().photoId()).isEqualTo(photoId);
        assertThat(response.getFirst().status()).isEqualTo(PhotoStatus.UPLOADED);
        assertThat(response.getFirst().downloadUrl()).isEqualTo(PRESIGNED_DOWNLOAD_URL);

        verify(userContextService).getCurrentUserId();
        verify(photoRepository).findByUserIdAndStatus(DEMO_USER_ID, PhotoStatus.UPLOADED);
        verify(s3PresignedUrlService).generateDownloadUrl(uploadedPhoto.getS3Key());
    }

    @Test
    void deletePhoto_photoExistsForUser_deletesS3ObjectAndMetadata() {
        UUID photoId = UUID.randomUUID();
        Photo uploadedPhoto = createUploadedPhoto(photoId);

        when(userContextService.getCurrentUserId()).thenReturn(DEMO_USER_ID);
        when(photoRepository.findByPhotoIdAndUserId(photoId, DEMO_USER_ID))
                .thenReturn(Optional.of(uploadedPhoto));

        photoService.deletePhoto(photoId);

        verify(s3ObjectService).deleteObject(uploadedPhoto.getS3Key());
        verify(photoRepository).delete(uploadedPhoto);
    }

    @Test
    void deletePhoto_photoDoesNotExistForUser_throwsExceptionAndDoesNotDelete() {
        UUID photoId = UUID.randomUUID();

        when(userContextService.getCurrentUserId()).thenReturn(DEMO_USER_ID);
        when(photoRepository.findByPhotoIdAndUserId(photoId, DEMO_USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.deletePhoto(photoId))
                .isInstanceOf(ResponseStatusException.class);

        verify(s3ObjectService, never()).deleteObject(anyString());
        verify(photoRepository, never()).delete(any(Photo.class));
    }


    private Photo createUploadedPhoto(UUID photoId) {
        return Photo.builder()
                .photoId(photoId)
                .userId(DEMO_USER_ID)
                .filename(FILENAME)
                .s3Key(S3_KEY_TEMPLATE.formatted(DEMO_USER_ID, photoId))
                .status(PhotoStatus.UPLOADED)
                .contentType(CONTENT_TYPE)
                .fileSizeBytes(FILE_SIZE_BYTES)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86_400))
                .build();
    }
}
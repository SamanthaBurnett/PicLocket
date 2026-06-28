package com.sbproject.piclocket.service;

import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoCleanupServiceTest {

    private static final String USER_ID = "demo-user-id";
    private static final String FILENAME = "beach.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final long FILE_SIZE_BYTES = 5_000_000L;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private S3ObjectService s3ObjectService;

    @InjectMocks
    private PhotoCleanupService photoCleanupService;

    @Test
    void deleteExpiredPhotos_whenExpiredPhotosExist_deletesS3ObjectsAndMetadata() {
        Photo photoOne = createExpiredPhoto(UUID.randomUUID());
        Photo photoTwo = createExpiredPhoto(UUID.randomUUID());

        when(photoRepository.findByExpiresAtBefore(any(Instant.class)))
                .thenReturn(List.of(photoOne, photoTwo));

        photoCleanupService.deleteExpiredPhotos();

        verify(s3ObjectService).deleteObject(photoOne.getS3Key());
        verify(s3ObjectService).deleteObject(photoTwo.getS3Key());

        verify(photoRepository).delete(photoOne);
        verify(photoRepository).delete(photoTwo);
    }

    @Test
    void deleteExpiredPhotos_whenNoExpiredPhotosExist_doesNothing() {
        when(photoRepository.findByExpiresAtBefore(any(Instant.class)))
                .thenReturn(List.of());

        photoCleanupService.deleteExpiredPhotos();

        verify(s3ObjectService, never()).deleteObject(any());
        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void deleteExpiredPhotos_queriesForPhotosExpiredBeforeNow() {
        when(photoRepository.findByExpiresAtBefore(any(Instant.class)))
                .thenReturn(List.of());

        photoCleanupService.deleteExpiredPhotos();

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(photoRepository).findByExpiresAtBefore(instantCaptor.capture());

        assertThat(instantCaptor.getValue()).isBeforeOrEqualTo(Instant.now());
    }

    private Photo createExpiredPhoto(UUID photoId) {
        return Photo.builder()
                .photoId(photoId)
                .userId(USER_ID)
                .filename(FILENAME)
                .s3Key("v1/users/%s/photos/%s/original".formatted(USER_ID, photoId))
                .status(PhotoStatus.UPLOADED)
                .contentType(CONTENT_TYPE)
                .fileSizeBytes(FILE_SIZE_BYTES)
                .createdAt(Instant.now().minusSeconds(7_200))
                .updatedAt(Instant.now().minusSeconds(7_200))
                .uploadedAt(Instant.now().minusSeconds(7_000))
                .expiresAt(Instant.now().minusSeconds(3_600))
                .build();
    }
}
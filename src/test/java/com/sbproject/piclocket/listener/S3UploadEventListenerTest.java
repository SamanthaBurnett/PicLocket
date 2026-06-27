package com.sbproject.piclocket.listener;

import com.sbproject.piclocket.dto.s3.S3UploadEventParser;
import com.sbproject.piclocket.dto.s3.UploadedPhotoEvent;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3UploadEventListenerTest {

    @Mock
    private S3UploadEventParser parser;

    @Mock
    private PhotoRepository photoRepository;

    private S3UploadEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new S3UploadEventListener(parser, photoRepository);
    }

    @Test
    void receiveMessage_photoPendingUpload_marksUploaded() throws Exception {
        UploadedPhotoEvent event = new UploadedPhotoEvent(
                "v1/users/demo-user-success/photos/photo-123/original.jpg"
        );

        Photo photo = Photo.builder()
                .photoId(UUID.randomUUID())
                .s3Key(event.objectKey())
                .status(PhotoStatus.PENDING_UPLOAD)
                .build();

        when(parser.parse("message")).thenReturn(Optional.of(event));
        when(photoRepository.findByS3Key(event.objectKey()))
                .thenReturn(Optional.of(photo));

        listener.receiveMessage("message");

        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.UPLOADED);
        assertThat(photo.getUploadedAt()).isNotNull();

        verify(photoRepository).save(photo);
    }

    @Test
    void receiveMessage_photoAlreadyUploaded_skipsProcessing() throws Exception {
        UploadedPhotoEvent event = new UploadedPhotoEvent(
                "v1/users/demo-user-success/photos/photo-123/original.jpg"
        );

        Photo photo = Photo.builder()
                .photoId(UUID.randomUUID())
                .s3Key(event.objectKey())
                .status(PhotoStatus.UPLOADED)
                .uploadedAt(Instant.parse("2026-06-28T00:00:00Z"))
                .build();

        when(parser.parse("message")).thenReturn(Optional.of(event));
        when(photoRepository.findByS3Key(event.objectKey()))
                .thenReturn(Optional.of(photo));

        listener.receiveMessage("message");

        assertThat(photo.getStatus()).isEqualTo(PhotoStatus.UPLOADED);
        assertThat(photo.getUploadedAt())
                .isEqualTo(Instant.parse("2026-06-28T00:00:00Z"));

        verify(photoRepository, never()).save(any(Photo.class));
    }
}

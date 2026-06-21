package com.sbproject.piclocket.service;

import com.sbproject.piclocket.dto.CreateUploadRequest;
import com.sbproject.piclocket.dto.CreateUploadResponse;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    @InjectMocks
    private PhotoService photoService;

    @Test
    void createUploadRequest_validRequest_returns200() {
        CreateUploadRequest request = new CreateUploadRequest(
                "beach.jpg",
                "image/jpeg",
                5000000L
        );

        String presignedUploadUrl = "https://presigned-url.test/upload";

        when(s3PresignedUrlService.generateUploadUrl(anyString(), anyString())).thenReturn(presignedUploadUrl);

        CreateUploadResponse response = photoService.createUploadRequest(request);

        ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
        verify(photoRepository).save(photoCaptor.capture());

        Photo savedPhoto = photoCaptor.getValue();

        assertThat(savedPhoto.getPhotoId()).isNotNull();
        assertThat(savedPhoto.getUserId()).isEqualTo("demo-user-id");
        assertThat(savedPhoto.getFilename()).isEqualTo("beach.jpg");
        assertThat(savedPhoto.getS3Key()).contains("v1/users/demo-user-id/photos/");
        assertThat(savedPhoto.getS3Key()).contains(savedPhoto.getPhotoId().toString());
        assertThat(savedPhoto.getStatus()).isEqualTo(PhotoStatus.PENDING_UPLOAD);
        assertThat(savedPhoto.getContentType()).isEqualTo("image/jpeg");
        assertThat(savedPhoto.getFileSizeBytes()).isEqualTo(5000000L);
        assertThat(savedPhoto.getExpiresAt()).isNotNull();

        verify(s3PresignedUrlService).generateUploadUrl(savedPhoto.getS3Key(), "image/jpeg");

        assertThat(response.photoId()).isEqualTo(savedPhoto.getPhotoId());
        assertThat(response.uploadUrl()).contains(presignedUploadUrl);
    }
}
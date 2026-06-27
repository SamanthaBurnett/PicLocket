package com.sbproject.piclocket.listener;

import com.sbproject.piclocket.dto.s3.S3UploadEventParser;
import com.sbproject.piclocket.dto.s3.UploadedPhotoEvent;
import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import com.sbproject.piclocket.repository.PhotoRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Listener for S3 upload events delivered through SQS.
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class S3UploadEventListener {

    private final S3UploadEventParser parser;
    private final PhotoRepository photoRepository;

    @SqsListener("${pic-locket.sqs.queue}")
    public void receiveMessage(String message) {
        try {
            Optional<UploadedPhotoEvent> event = parser.parse(message);

            if (event.isEmpty()) {
                log.info("Ignoring non S3 created event");
                return;
            }

            UploadedPhotoEvent uploadedPhoto = event.get();

            Photo photo = photoRepository.findByS3Key(uploadedPhoto.objectKey())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Photo not found for S3 key."
                                    ));

            // Make uploading idempotent
            if (photo.getStatus() == PhotoStatus.UPLOADED) {
                log.info("Skipping duplicate upload event");
                return;
            }

            photo.markUploaded(Instant.now());
            photoRepository.save(photo);

            log.info("Photo {} marked as uploaded.", photo.getPhotoId());
        } catch (Exception e) {
            log.error("Failed to parse S3 event.");
        }
    }
}

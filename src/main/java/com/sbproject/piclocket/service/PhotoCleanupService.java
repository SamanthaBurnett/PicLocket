package com.sbproject.piclocket.service;

import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class PhotoCleanupService {

    private final PhotoRepository photoRepository;
    private final S3ObjectService s3ObjectService;

    @Transactional
    public void deleteExpiredPhotos() {
        Instant now = Instant.now();

        List<Photo> expiredPhotos = photoRepository.findByExpiresAtBefore(now);

        if (expiredPhotos.isEmpty()) {
            log.info("No expired photos found for cleanup.");
            return;
        }

        for (Photo photo: expiredPhotos) {
            s3ObjectService.deleteObject(photo.getS3Key());
            photoRepository.delete(photo);
        }

        log.info("Deleted all expired photos.");
    }
}

package com.sbproject.piclocket.scheduler;

import com.sbproject.piclocket.service.PhotoCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class PhotoCleanupScheduler {

    private final PhotoCleanupService photoCleanupService;

    @Scheduled(fixedDelayString = "${pic-locket.cleanup.fixed-delay-ms}")
    public void cleanExpiredPhotos() {
        log.info("Starting expired photo cleanup.");
        photoCleanupService.deleteExpiredPhotos();
    }
}

package com.sbproject.piclocket.scheduler;

import com.sbproject.piclocket.service.PhotoCleanupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhotoCleanupSchedulerTest {

    @Mock
    private PhotoCleanupService photoCleanupService;

    @InjectMocks
    private PhotoCleanupScheduler photoCleanupScheduler;

    @Test
    void cleansExpiredPhotos_callsCleanupService() {
        photoCleanupScheduler.cleanExpiredPhotos();

        verify(photoCleanupService).deleteExpiredPhotos();
    }
}
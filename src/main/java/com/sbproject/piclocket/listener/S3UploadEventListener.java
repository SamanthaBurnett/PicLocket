package com.sbproject.piclocket.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener for S3 upload events delivered through SQS.
 */
@Slf4j
@Component
public class S3UploadEventListener {

    @SqsListener("${pic-locket.sqs.queue}")
    public void receiveMessage(String message) {
        log.info("Received S3 upload event message={}", message);
    }
}

package com.sbproject.piclocket.service;

import com.sbproject.piclocket.config.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Handles interactions with S3 objects.
 * Currently supports verifying whether an object exists in the configured S3 bucket.
 */
@Service
public class S3ObjectService {

    private static final Logger log = LoggerFactory.getLogger(S3ObjectService.class);

    private final AwsProperties awsProperties;
    private final S3Client s3Client;

    public S3ObjectService(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        this.s3Client = S3Client.builder()
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public boolean objectExists(String s3Key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(awsProperties.s3().bucket())
                .key(s3Key)
                .build();

        try {
            s3Client.headObject(request);
            log.info("Verified S3 object exists for key={}", s3Key);
            return true;
        } catch (NoSuchKeyException exception) {
            return false;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                log.warn("No S3 object found for key={}", s3Key);
                return false;
            }

            log.error("Failed to verify S3 object for key={}, statusCode={}", s3Key, exception.statusCode());
            throw exception;
        }
    }
}

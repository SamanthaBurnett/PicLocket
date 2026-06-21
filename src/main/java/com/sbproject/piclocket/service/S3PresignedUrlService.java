package com.sbproject.piclocket.service;

import com.sbproject.piclocket.config.AwsProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

/**
 * This class generates timed URLs to allow for direct uploads to S3.
 */
@Service
public class S3PresignedUrlService {

    // Upload URLs must be short-lived. If a client waits too long, they must request a new one
    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(15);

    private final AwsProperties awsProperties;
    private final S3Presigner s3Presigner;

    public S3PresignedUrlService(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;

        // Generates temporary URLs to allow clients to upload directly to S3
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Generates presigned url string so client can do direct upload to S3
     *
     * @param s3Key represents the path to the upload
     * @param contentType the type of object we plan to upload (i.e. mime type)
     * @return String representing presigned url
     */
    public String generateUploadUrl(String s3Key, String contentType) {
        // Defines the object the client is allowed to upload to S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.s3().bucket())
                .key(s3Key)
                .contentType(contentType)
                .build();

        // Defines how the presigned URL should be generated including its expiration time
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        // AWS authorizes the upload request and generates the url to be used for direct uploads by the client
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        URL uploadUrl = presignedRequest.url();

        return uploadUrl.toString();
    }
}

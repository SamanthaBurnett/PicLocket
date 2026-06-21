package com.sbproject.piclocket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Photo {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID photoId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoStatus status;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    // Upload requested
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // When photo is updated
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // When s3 confirms upload is complete
    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    // When photo should expire
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

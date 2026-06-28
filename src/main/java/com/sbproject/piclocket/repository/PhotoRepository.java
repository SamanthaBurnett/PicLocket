package com.sbproject.piclocket.repository;

import com.sbproject.piclocket.model.Photo;
import com.sbproject.piclocket.model.PhotoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findByUserIdAndStatus(String userId, PhotoStatus status);

    long countByUserIdAndCreatedAtBetween(String userId, Instant start, Instant end);

    Optional<Photo> findByS3Key(String s3Key);

    Optional<Photo> findByPhotoIdAndUserId(UUID photoId, String userId);

    List<Photo> findByExpiresAtBefore(Instant now);
}

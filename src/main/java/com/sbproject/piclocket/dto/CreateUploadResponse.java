package com.sbproject.piclocket.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateUploadResponse(
        UUID photoId,
        String uploadUrl
) {
}

package com.sbproject.piclocket.dto;

import com.sbproject.piclocket.model.PhotoStatus;

import java.util.UUID;

public record PhotoResponse(
        UUID photoId,
        PhotoStatus status
) {
}

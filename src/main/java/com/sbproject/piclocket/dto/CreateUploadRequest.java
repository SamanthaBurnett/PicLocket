package com.sbproject.piclocket.dto;

public record CreateUploadRequest(
        String filename,
        String contentType,
        Long fileSizeBytes
) {
}

package com.sbproject.piclocket.dto.s3;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class S3UploadEventParser {

    private final ObjectMapper mapper;

    public S3UploadEventParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<UploadedPhotoEvent> parse(String message) throws IOException {
        JsonNode root = mapper.readTree(message);
        JsonNode records = root.path("Records");

        if (!records.isArray() || records.isEmpty()) {
            return Optional.empty();
        }

        JsonNode firstRecord = records.get(0);

        String objectKey = firstRecord
                .path("s3")
                .path("object")
                .path("key")
                .asString();

        if (objectKey.isBlank()) return Optional.empty();

        return Optional.of(new UploadedPhotoEvent(objectKey));
    }
}

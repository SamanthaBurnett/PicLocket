package com.sbproject.piclocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pic-locket.cors")
public record CorsProperties(
        String allowedOrigin
) {
}

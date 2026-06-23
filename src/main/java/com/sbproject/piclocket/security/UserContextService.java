package com.sbproject.piclocket.security;

import org.springframework.stereotype.Service;

/**
 * Provides access to the user associated with the current request.
 * This currently returns a fixed development user until JWT-based authentication is added.
 */
@Service
public class UserContextService {

    private static final String DEVELOPMENT_USER_ID = "demo-user-id";

    public String getCurrentUserId() {
        return DEVELOPMENT_USER_ID;
    }
}

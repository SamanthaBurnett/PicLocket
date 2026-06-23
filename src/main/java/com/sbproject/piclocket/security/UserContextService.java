package com.sbproject.piclocket.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Provides access to the user associated with the current request.
 */
@Service
public class UserContextService {

    /**
     * Retrieves the authenticated user id from the security context.
     *
     * @return authenticated user id
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication.getPrincipal().toString();
    }
}

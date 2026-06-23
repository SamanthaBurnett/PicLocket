package com.sbproject.piclocket.controller;

import com.sbproject.piclocket.security.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * For local development.
 * Generates JWTs for testing purposes.
 */
@RestController
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/dev/token")
    public String generateToken(@RequestParam String userId) {
        return jwtService.generateToken(userId);
    }
}

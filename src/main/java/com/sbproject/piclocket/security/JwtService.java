package com.sbproject.piclocket.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Handles JWT parsing and validation.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generates JWT for a user.
     * Note: this is for local development and testing purposes.
     *
     * @param userId unique identifier of a user
     * @return signed JWT for user
     */
    public String generateToken(String userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES))
                )
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a JWT and extracts the claims.
     *
     * @param token JWT without the Bearer prefix
     * @return token claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts user id.
     * @param token JWT without the Bearer prefix
     * @return authenticated user id
     */
    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }
}

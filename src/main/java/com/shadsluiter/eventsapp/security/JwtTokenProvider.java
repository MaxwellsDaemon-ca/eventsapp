package com.shadsluiter.eventsapp.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Provides functionality for generating, parsing, and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    /**
     * Generates a secret key for signing the JWT based on the configured secret.
     * 
     * @return the generated secret key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for the given authentication object.
     * 
     * @param authentication the authenticated user's credentials
     * @return a signed JWT token string
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Extracts the username from a given JWT token.
     * 
     * @param token the JWT token string
     * @return the username (subject) stored in the token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates the authenticity and expiration of a JWT token.
     * 
     * @param authToken the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            // Token is invalid or expired
            return false;
        }
    }
}

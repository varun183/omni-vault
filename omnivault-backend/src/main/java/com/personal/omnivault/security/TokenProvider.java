package com.personal.omnivault.security;

import com.personal.omnivault.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * Provides utilities for generating, validating, and processing JWT tokens.
 * Handles token creation, parsing, and authentication based on JWT claims.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Generates an access token for a user.
     *
     * @param userPrincipal The user principal for whom to generate the token
     * @return A JWT access token
     */
    public String generateAccessToken(UserPrincipal userPrincipal) {
        return generateToken(userPrincipal, jwtProperties.getAccessTokenExpirationMs());
    }

    /**
     * Internal method to generate a JWT token with specified expiration.
     *
     * @param userPrincipal The user principal for whom to generate the token
     * @param expirationMs Token expiration time in milliseconds
     * @return A JWT token
     */
    private String generateToken(UserPrincipal userPrincipal, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Creates a signing key from the JWT secret.
     *
     * @return A cryptographic key for signing JWT tokens
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token The JWT token
     * @return The UUID of the user
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Validates a JWT token.
     * Checks token integrity, expiration, and signature.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        } catch (Exception e) {
            log.error("JWT token validation error", e);
        }
        return false;
    }

    /**
     * Creates an Authentication object from a JWT token.
     * Loads user details and creates an authentication token
     * for Spring Security context.
     *
     * @param token The JWT token
     * @return An Authentication object representing the user
     */
    public Authentication getAuthenticationFromToken(String token) {
        UUID userId = getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserById(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
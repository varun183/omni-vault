package com.personal.omnivault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JSON Web Token (JWT) authentication.
 * Manages JWT-related settings such as secret key and token expiration times.
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;
}
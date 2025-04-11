package com.omnivault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for email-related settings.
 * Manages email verification parameters such as base URL,
 * token and OTP expiration times, and OTP length.
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
public class EmailProperties {

    private String from;
    private Verification verification = new Verification();

    /**
     * Nested configuration class for email verification settings.
     */
    @Data
    public static class Verification {
        private String baseUrl;
        private int tokenExpiry = 1440; // 24 hours in minutes
        private int otpExpiry = 10;     // 10 minutes
        private int otpLength = 6;
    }
}
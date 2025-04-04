package com.personal.omnivault.config;

import com.personal.omnivault.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * Configuration class for scheduled background tasks.
 * Provides automated maintenance tasks for the application,
 * such as cleaning up expired verification tokens.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * Scheduled task to clean up expired verification tokens.
     * Runs daily at midnight to remove verification tokens
     * that have passed their expiration date.
     * This helps maintain database cleanliness and prevents
     * accumulation of stale verification tokens.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired verification tokens");
        verificationTokenRepository.deleteAllExpiredTokens(ZonedDateTime.now());
    }
}
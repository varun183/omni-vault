package com.personal.omnivault.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@EnableJpaAuditing
public class TestJpaConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> Optional.of(UUID.randomUUID());
    }

    @Bean
    public SesClient sesClient() {
        // Create a mock SesClient for testing
        return SesClient.builder()
                .build();
    }
}
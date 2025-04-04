package com.personal.omnivault.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuration class for creating an S3Presigner bean.
 * Provides a configurable S3Presigner for generating pre-signed URLs
 * for S3 or S3-compatible storage services. The configuration is
 * conditionally enabled based on application properties.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3PresignerConfig {

    private final AwsS3Config s3Config;

    /**
     * Creates an S3Presigner bean for generating pre-signed URLs.
     *
     * Configures the presigner with:
     * - AWS credentials
     * - Specified region
     * - Optional custom endpoint for S3-compatible services
     *
     * @return Configured S3Presigner or null if S3 is disabled
     */
    @Bean
    public S3Presigner s3Presigner() {
        if (!s3Config.isEnabled()) {
            return null;
        }

        software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder builder = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
                .region(Region.of(s3Config.getRegion()));

        // Optional endpoint configuration (for MinIO, LocalStack, etc.)
        if (s3Config.getEndpointUrl() != null && !s3Config.getEndpointUrl().isEmpty()) {
            builder.endpointOverride(URI.create(s3Config.getEndpointUrl()));
        }

        return builder.build();
    }
}
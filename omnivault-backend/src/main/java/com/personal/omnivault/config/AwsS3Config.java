package com.personal.omnivault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsS3Config {
    private boolean enabled;
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;
    private String endpointUrl;
    private boolean pathStyleAccessEnabled;
    private long maxSizeBytes;
    private long urlExpirationSeconds;

    @Bean
    public S3Client s3Client() {
        if (!enabled) {
            return null;
        }

        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region));

        // Optional endpoint configuration (for MinIO, LocalStack, etc.)
        if (endpointUrl != null && !endpointUrl.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpointUrl));
            // Path-style is needed for some S3-compatible services
            if (pathStyleAccessEnabled) {
                builder.serviceConfiguration(c -> c.pathStyleAccessEnabled(true));
            }
        }

        return builder.build();
    }
}
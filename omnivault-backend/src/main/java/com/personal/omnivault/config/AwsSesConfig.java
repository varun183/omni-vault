package com.personal.omnivault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "aws.ses")
@Data
public class AwsSesConfig {
    private boolean enabled;
    private String accessKey;
    private String secretKey;
    private String region;
    private String source;
    private String replyTo;
    private boolean sandboxMode;
    private List<String> verifiedRecipients;

    @Bean
    public SesClient sesClient() {
        if (!enabled) {
            return null;
        }

        return SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }
}
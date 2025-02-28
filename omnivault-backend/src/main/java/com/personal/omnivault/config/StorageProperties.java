package com.personal.omnivault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.storage")
@Data
public class StorageProperties {
    private String location;
    private Map<String, Integer> maxSize;
}
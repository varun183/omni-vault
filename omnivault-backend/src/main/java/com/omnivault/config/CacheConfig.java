package com.omnivault.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;

/**
 * Configuration class for setting up application-wide caching using Caffeine.
 * Enables caching and configures cache settings such as expiration,
 * initial capacity, and maximum size. Defines named caches for
 * different types of content and entities.
 */
@Configuration
@EnableCaching
public class CacheConfig {


    /**
     * Configures the base Caffeine cache settings.
     * Sets up default cache properties including:
     * - Expiration after 10 minutes of access
     * - Initial capacity of 50 entries
     * - Maximum of 500 entries
     *
     * @return Configured Caffeine cache builder
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .initialCapacity(50)
                .maximumSize(500);
    }


    /**
     * Creates a CacheManager with predefined named caches.
     * Configures specific caches for various application entities:
     * - contents
     * - folders
     * - tags
     * - users
     * - contentsByFolder
     * - contentsByTag
     * - contentsByType
     * - recentContents
     * - popularContents
     *
     * @param caffeine The base Caffeine cache configuration
     * @return Configured CacheManager with named caches
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        cacheManager.setCacheNames(Arrays.asList(
                "contents",
                "folders",
                "tags",
                "users",
                "contentsByFolder",
                "contentsByTag",
                "contentsByType",
                "recentContents",
                "popularContents"
        ));
        return cacheManager;
    }
}
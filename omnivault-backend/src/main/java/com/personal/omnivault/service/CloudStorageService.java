package com.personal.omnivault.service;

import com.personal.omnivault.domain.model.ContentType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
/**
 * Interface for cloud storage operations
 */
public interface CloudStorageService {

    /**
     * Check if the cloud storage service is enabled
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Store a file in cloud storage
     * @param file The file to store
     * @param userId The owner's user ID
     * @param contentType The type of content being stored
     * @param filename The original filename
     * @return The storage key/path in cloud storage
     */
    String storeFile(MultipartFile file, UUID userId, ContentType contentType, String filename);

    /**
     * Load a file from cloud storage as a Resource
     * @param key The storage key/path
     * @return The file as a Resource
     */
    Resource loadFileAsResource(String key);

    /**
     * Generate a presigned URL for temporary access to a file
     * @param key The storage key/path
     * @return A presigned URL string
     */
    String generatePresignedUrl(String key);

    /**
     * Delete a file from cloud storage
     * @param key The storage key/path
     */
    void deleteFile(String key);

    /**
     * Check if a file exists in cloud storage
     * @param key The storage key/path
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String key);
}
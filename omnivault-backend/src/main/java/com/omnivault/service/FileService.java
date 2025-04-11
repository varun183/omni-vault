package com.omnivault.service;

import com.omnivault.domain.model.ContentType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Service interface for file storage and management operations.
 * Provides methods for initializing storage, storing files, generating thumbnails,
 * loading resources, and performing file-related operations.
 */
public interface FileService {

    /**
     * Initialize storage directories
     */
    void init();

    /**
     * Store a file and return its storage path
     *
     * @param file The file to store
     * @param userId The owner's user ID
     * @param contentType The type of content being stored
     * @return The storage path
     */
    String storeFile(MultipartFile file, UUID userId, ContentType contentType);

    /**
     * Generate a thumbnail for an image or video
     *
     * @param storagePath The storage path of the original file
     * @param contentType The type of content
     * @return The path to the generated thumbnail
     */
    String generateThumbnail(String storagePath, ContentType contentType);

    /**
     * Load a file as a Resource
     *
     * @param storagePath The storage path of the file
     * @return The file as a Resource
     */
    Resource loadFileAsResource(String storagePath);

    /**
     * Get the absolute file path from a storage path
     *
     * @param storagePath The storage path of the file
     * @return The absolute file path
     */
    Path getPath(String storagePath);

    /**
     * Delete a file
     *
     * @param storagePath The storage path of the file to delete
     */
    void deleteFile(String storagePath);

    /**
     * Check if a file exists
     *
     * @param storagePath The storage path of the file
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String storagePath);

    /**
     * Get the Content-Type of a file
     *
     * @param storagePath The storage path of the file
     * @return The Content-Type
     */
    String getContentType(String storagePath);

    /**
     * Detects the MIME type of  file using multiple methods
     *
     * @param file The uploaded file
     * @param originalFilename The original filename
     * @return The detected MIME type
     */
    String detectMimeType(MultipartFile file, String originalFilename);
}
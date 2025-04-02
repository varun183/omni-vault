package com.personal.omnivault.domain.model;

/**
 * Enum representing the storage location of content
 */
public enum StorageLocation {
    /**
     * Content is stored locally on the server
     */
    LOCAL,

    /**
     * Content is stored in cloud storage (S3)
     */
    CLOUD
}
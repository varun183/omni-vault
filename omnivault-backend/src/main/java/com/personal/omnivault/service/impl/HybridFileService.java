package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.domain.model.StorageLocation;
import com.personal.omnivault.exception.FileStorageException;
import com.personal.omnivault.service.CloudStorageService;
import com.personal.omnivault.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

/**
 * A service that combines local file storage and cloud storage
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class HybridFileService implements FileService {

    private final FileServiceImpl localFileService;
    private final CloudStorageService cloudStorageService;

    @Override
    public void init() {
        localFileService.init();
    }

    @Override
    public String storeFile(MultipartFile file, UUID userId, ContentType contentType) {
        return storeFile(file, userId, contentType, StorageLocation.LOCAL);
    }

    public String storeFile(MultipartFile file, UUID userId, ContentType contentType, StorageLocation storageLocation) {
        // If cloud storage is requested but not enabled, fall back to local storage
        if (storageLocation == StorageLocation.CLOUD && !cloudStorageService.isEnabled()) {
            log.warn("Cloud storage requested but not enabled, falling back to local storage");
            storageLocation = StorageLocation.LOCAL;
        }

        if (storageLocation == StorageLocation.CLOUD) {
            try {
                return cloudStorageService.storeFile(file, userId, contentType,
                        file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed");
            } catch (Exception e) {
                log.error("Error storing file in cloud, falling back to local storage", e);
                return localFileService.storeFile(file, userId, contentType);
            }
        } else {
            return localFileService.storeFile(file, userId, contentType);
        }
    }

    @Override
    public String generateThumbnail(String storagePath, ContentType contentType) {
        return generateThumbnail(storagePath, contentType, StorageLocation.LOCAL);
    }

    public String generateThumbnail(String storagePath, ContentType contentType, StorageLocation storageLocation) {
        // For now, we'll generate thumbnails in the same storage location as the original file
        // This could be enhanced to allow different storage locations for thumbnails
        if (storageLocation == StorageLocation.CLOUD) {
            // If cloud storage is not enabled, fall back to local
            if (!cloudStorageService.isEnabled()) {
                log.warn("Cloud storage requested for thumbnail but not enabled, cannot generate thumbnail");
                return null;
            }

            // For simplicity, not implementing cloud thumbnail generation yet
            log.warn("Cloud thumbnail generation not fully implemented");
            return null;
        } else {
            return localFileService.generateThumbnail(storagePath, contentType);
        }
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        return loadFileAsResource(storagePath, StorageLocation.LOCAL);
    }

    public Resource loadFileAsResource(String storagePath, StorageLocation storageLocation) {
        if (storageLocation == StorageLocation.CLOUD) {
            if (!cloudStorageService.isEnabled()) {
                log.warn("Attempted to load cloud file but cloud storage is not enabled");
                throw new FileStorageException("Cloud storage is not enabled");
            }

            try {
                return cloudStorageService.loadFileAsResource(storagePath);
            } catch (Exception e) {
                log.error("Error loading file from cloud storage", e);
                throw new FileStorageException("Failed to load file from cloud storage", e);
            }
        } else {
            return localFileService.loadFileAsResource(storagePath);
        }
    }

    @Override
    public Path getPath(String storagePath) {
        // Only works for local storage
        return localFileService.getPath(storagePath);
    }

    @Override
    public void deleteFile(String storagePath) {
        deleteFile(storagePath, StorageLocation.LOCAL);
    }

    public void deleteFile(String storagePath, StorageLocation storageLocation) {
        if (storageLocation == StorageLocation.CLOUD) {
            if (!cloudStorageService.isEnabled()) {
                log.warn("Attempted to delete cloud file but cloud storage is not enabled");
                return; // Silently ignore as the file doesn't exist anyway
            }

            try {
                cloudStorageService.deleteFile(storagePath);
            } catch (Exception e) {
                log.error("Error deleting file from cloud storage", e);
                // Don't rethrow to avoid breaking the user experience
            }
        } else {
            localFileService.deleteFile(storagePath);
        }
    }

    @Override
    public boolean fileExists(String storagePath) {
        return fileExists(storagePath, StorageLocation.LOCAL);
    }

    public boolean fileExists(String storagePath, StorageLocation storageLocation) {
        if (storageLocation == StorageLocation.CLOUD) {
            if (!cloudStorageService.isEnabled()) {
                return false;
            }

            try {
                return cloudStorageService.fileExists(storagePath);
            } catch (Exception e) {
                log.error("Error checking if file exists in cloud storage", e);
                return false;
            }
        } else {
            return localFileService.fileExists(storagePath);
        }
    }

    @Override
    public String getContentType(String storagePath) {
        return localFileService.getContentType(storagePath);
    }

    @Override
    public String detectMimeType(MultipartFile file, String originalFilename) {
        return localFileService.detectMimeType(file, originalFilename);
    }

    /**
     * Generate a presigned URL for a file in cloud storage
     * @param storagePath The storage path
     * @param storageLocation The storage location
     * @return A presigned URL or null if not in cloud storage
     */
    public String generatePresignedUrl(String storagePath, StorageLocation storageLocation) {
        if (storageLocation == StorageLocation.CLOUD) {
            if (!cloudStorageService.isEnabled()) {
                log.warn("Attempted to generate presigned URL but cloud storage is not enabled");
                return null;
            }

            try {
                return cloudStorageService.generatePresignedUrl(storagePath);
            } catch (Exception e) {
                log.error("Error generating presigned URL", e);
                return null;
            }
        }
        return null;
    }

    /**
     * Move a file from local storage to cloud storage
     * @param storagePath The local storage path
     * @param userId The user ID
     * @param contentType The content type
     * @return The new cloud storage path
     */
    public String moveToCloud(String storagePath, UUID userId, ContentType contentType) {
        if (!cloudStorageService.isEnabled()) {
            throw new FileStorageException("Cloud storage is not enabled");
        }

        // Load the file from local storage
        Resource resource = localFileService.loadFileAsResource(storagePath);

        try {
            // Create a temporary file to upload to S3
            MultipartFile multipartFile = new CustomMultipartFile(
                    resource.getInputStream(),
                    resource.contentLength(),
                    resource.getFilename());

            // Upload to cloud
            String cloudPath = cloudStorageService.storeFile(
                    multipartFile,
                    userId,
                    contentType,
                    resource.getFilename());

            // Delete the local copy
            localFileService.deleteFile(storagePath);

            return cloudPath;
        } catch (Exception e) {
            throw new FileStorageException("Failed to move file to cloud storage", e);
        }
    }

    /**
     * Move a file from cloud storage to local storage
     * @param storagePath The cloud storage path
     * @param userId The user ID
     * @param contentType The content type
     * @return The new local storage path
     */
    public String moveToLocal(String storagePath, UUID userId, ContentType contentType) {
        if (!cloudStorageService.isEnabled()) {
            throw new FileStorageException("Cloud storage is not enabled");
        }

        // Load the file from cloud storage
        Resource resource = cloudStorageService.loadFileAsResource(storagePath);

        try {
            // Create a temporary file to store locally
            MultipartFile multipartFile = new CustomMultipartFile(
                    resource.getInputStream(),
                    resource.contentLength(),
                    resource.getFilename());

            // Store locally
            String localPath = localFileService.storeFile(
                    multipartFile,
                    userId,
                    contentType);

            // Delete the cloud copy
            cloudStorageService.deleteFile(storagePath);

            return localPath;
        } catch (Exception e) {
            throw new FileStorageException("Failed to move file to local storage", e);
        }
    }
}
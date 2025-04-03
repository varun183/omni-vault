package com.personal.omnivault.service.impl;

import com.personal.omnivault.config.StorageProperties;
import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.exception.FileStorageException;
import com.personal.omnivault.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling file storage operations.
 * Extracts file functionality from FileStorageService to make it more modular.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final StorageProperties storageProperties;
    private final Tika tika = new Tika();
    private Path rootLocation;

    @PostConstruct
    public void initialize() {
        init();
    }

    /**
     * Initialize storage directories
     */
    public void init() {
        rootLocation = Paths.get(storageProperties.getLocation());
        try {
            Files.createDirectories(rootLocation);
            log.info("Created root storage directory: {}", rootLocation);

            // Create subdirectories for each content type
            for (ContentType contentType : ContentType.values()) {
                Path typeDir = rootLocation.resolve(contentType.name().toLowerCase());
                Files.createDirectories(typeDir);
                log.info("Created storage directory for {}: {}", contentType, typeDir);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage", e);
        }
    }

    /**
     * Store a file and return its storage path
     *
     * @param file        The file to store
     * @param userId      The owner's user ID
     * @param contentType The type of content being stored
     * @return The storage path
     */
    public String storeFile(MultipartFile file, UUID userId, ContentType contentType) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Validate file size based on content type
        validateFileSize(file, contentType);

        // Clean and create a safe filename, handling potential null
        String originalFilename = file.getOriginalFilename();
        // Safely clean the filename or use default if null
        if (originalFilename != null) {
            originalFilename = org.springframework.util.StringUtils.cleanPath(originalFilename);
        }
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unnamed_file";
        }

        // Generate a unique storage path
        String storagePath = generateStoragePath(userId, contentType, originalFilename);
        Path targetPath = getPath(storagePath);
        File tempFile = null;

        try {
            // Make sure the target directory exists
            Files.createDirectories(targetPath.getParent());

            // First save to temp file to avoid partial writes
            tempFile = File.createTempFile("upload_", "_temp");

            // Copy the file
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // Then move to final location
            Files.copy(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file {} at {}", originalFilename, targetPath);
            return storagePath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new FileStorageException("Failed to store file " + originalFilename, e);
        } finally {
            // Clean up temp file and handle the result of delete()
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Generate a thumbnail for an image or video
     *
     * @param storagePath The storage path of the original file
     * @param contentType The type of content
     * @return The path to the generated thumbnail
     */
    public String generateThumbnail(String storagePath, ContentType contentType) {
        Path sourcePath = getPath(storagePath);
        File sourceFile = sourcePath.toFile();
        String filename = sourcePath.getFileName().toString();

        return generateThumbnailFromFile(sourceFile, contentType, filename);
    }

    public String generateThumbnailFromFile(File sourceFile, ContentType contentType, String filename) {
        try {
            // Generate thumbnail filename
            String thumbnailPath = getThumbnailPath(filename);
            Path targetPath = getPath(thumbnailPath);

            // Create parent directories if they don't exist
            Files.createDirectories(targetPath.getParent());

            // For now, we'll only handle image thumbnails - video would require additional libraries
            if (contentType == ContentType.IMAGE) {
                BufferedImage originalImage = ImageIO.read(sourceFile);
                if (originalImage == null) {
                    log.warn("Could not read image file: {}", sourceFile);
                    return null;
                }

                // Create thumbnail (max 200x200 while preserving aspect ratio)
                int maxSize = 200;
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                int width = originalWidth;
                int height = originalHeight;

                if (originalWidth > maxSize || originalHeight > maxSize) {
                    if (originalWidth > originalHeight) {
                        width = maxSize;
                        height = (int) ((double) originalHeight / originalWidth * maxSize);
                    } else {
                        height = maxSize;
                        width = (int) ((double) originalWidth / originalHeight * maxSize);
                    }
                }

                // Create scaled image
                Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                thumbnail.getGraphics().drawImage(scaledImage, 0, 0, null);

                // Save thumbnail
                String extension = FilenameUtils.getExtension(filename).toLowerCase();
                ImageIO.write(thumbnail, extension, targetPath.toFile());

                log.info("Generated thumbnail for {} at {}", sourceFile, targetPath);
                return thumbnailPath;
            }

            return null;
        } catch (IOException e) {
            log.error("Failed to generate thumbnail for {}", sourceFile, e);
            return null;
        }
    }


    /**
     * Load a file as a Resource
     *
     * @param storagePath The storage path of the file
     * @return The file as a Resource
     */
    public Resource loadFileAsResource(String storagePath) {
        try {
            Path file = getPath(storagePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + storagePath);
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new FileStorageException("File not found " + storagePath, e);
        }
    }

    /**
     * Get the absolute file path from a storage path
     *
     * @param storagePath The storage path of the file
     * @return The absolute file path
     */
    public Path getPath(String storagePath) {
        return rootLocation.resolve(storagePath);
    }

    /**
     * Delete a file
     *
     * @param storagePath The storage path of the file to delete
     */
    public void deleteFile(String storagePath) {
        if (storagePath == null) {
            return;
        }

        try {
            Path file = getPath(storagePath);
            Files.deleteIfExists(file);
            log.info("Deleted file: {}", file);

            // Try to delete thumbnail if it exists
            String thumbnailPath = getThumbnailPath(storagePath);
            Path thumbnailFile = getPath(thumbnailPath);
            if (Files.deleteIfExists(thumbnailFile)) {
                log.info("Deleted thumbnail: {}", thumbnailFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storagePath, e);
        }
    }

    /**
     * Check if a file exists
     *
     * @param storagePath The storage path of the file
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String storagePath) {
        if (storagePath == null) {
            return false;
        }
        Path file = getPath(storagePath);
        return Files.exists(file);
    }

    /**
     * Get the Content-Type of a file
     *
     * @param storagePath The storage path of the file
     * @return The Content-Type
     */
    public String getContentType(String storagePath) {
        try {
            Path file = getPath(storagePath);
            return tika.detect(file.toFile());
        } catch (IOException e) {
            log.error("Failed to detect content type for file: {}", storagePath, e);
            return "application/octet-stream";
        }
    }

    /**
     * Detects the MIME type of  file using multiple methods
     *
     * @param file             The uploaded file
     * @param originalFilename The original filename
     * @return The detected MIME type
     */
    public String detectMimeType(MultipartFile file, String originalFilename) {
        // First, try to use the MIME type provided by the client
        String mimeType = file.getContentType();

        // If no MIME type is provided it's the generic "application/octet-stream",
        // try to detect it based on file extension
        if (mimeType == null || mimeType.equals("application/octet-stream")) {
            // Handle potential null originalFilename
            if (originalFilename != null) {
                String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();

                // Use a HashMap instead of Map.of() to avoid the limitation on number of entries
                Map<String, String> mimeTypeMap = getMimeTypeMap();

                if (mimeTypeMap.containsKey(extension)) {
                    mimeType = mimeTypeMap.get(extension);
                }
            }
        }

        // If still not determined, try Apache Tika for more accurate detection
        if (mimeType == null || mimeType.equals("application/octet-stream")) {
            File tempFile = null;
            try {
                // Create a temporary file to use with Tika
                String safeName = originalFilename != null ? originalFilename : "unknown";
                tempFile = File.createTempFile("detect-mime-", "-" + FilenameUtils.getExtension(safeName));
                file.transferTo(tempFile);

                // Use Tika to detect the MIME type
                mimeType = tika.detect(tempFile);

                // Clean up (handle the boolean result)
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            } catch (IOException e) {
                log.warn("Failed to detect MIME type using Tika: {}", e.getMessage());
                // Ensure cleanup in case of exception
                if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                    log.warn("Failed to delete temporary file after error: {}", tempFile.getAbsolutePath());
                }
            }
        }

        // Default to octet-stream if all detection methods fail
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    private static Map<String, String> getMimeTypeMap() {
        Map<String, String> mimeTypeMap = new HashMap<>();

        // Text files
        mimeTypeMap.put("txt", "text/plain");
        mimeTypeMap.put("log", "text/plain");
        mimeTypeMap.put("csv", "text/csv");
        mimeTypeMap.put("md", "text/markdown");
        mimeTypeMap.put("json", "application/json");
        mimeTypeMap.put("xml", "application/xml");
        mimeTypeMap.put("html", "text/html");
        mimeTypeMap.put("css", "text/css");
        mimeTypeMap.put("js", "application/javascript");

        // Document files
        mimeTypeMap.put("pdf", "application/pdf");
        mimeTypeMap.put("doc", "application/msword");
        mimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypeMap.put("xls", "application/vnd.ms-excel");
        mimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return mimeTypeMap;
    }

    // Helper methods

    private String generateStoragePath(UUID userId, ContentType contentType, String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueId = UUID.randomUUID().toString();

        return String.format("%s/%s/%s.%s",
                contentType.name().toLowerCase(),
                userId.toString(),
                uniqueId,
                extension);
    }

    private String getThumbnailPath(String storagePath) {
        String directory = FilenameUtils.getPath(storagePath);
        String basename = FilenameUtils.getBaseName(storagePath);
        String extension = FilenameUtils.getExtension(storagePath);

        return String.format("%sthumbnails/%s_thumb.%s", directory, basename, extension);
    }

    private void validateFileSize(MultipartFile file, ContentType contentType) {
        Integer maxSizeMb = storageProperties.getMaxSize().getOrDefault(
                contentType.name().toLowerCase(),
                storageProperties.getMaxSize().getOrDefault("other", 25)
        );

        long maxSizeBytes = maxSizeMb * 1024L * 1024L;

        if (file.getSize() > maxSizeBytes) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed size of %d MB for %s files",
                            maxSizeMb, contentType)
            );
        }
    }
}
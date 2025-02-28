package com.personal.omnivault.service.impl;

import com.personal.omnivault.config.StorageProperties;
import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.exception.FileStorageException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements StorageService {

    private final StorageProperties storageProperties;
    private final Tika tika = new Tika();
    private Path rootLocation;

    @PostConstruct
    public void initialize() {
        init();
    }

    @Override
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

    @Override
    public String store(MultipartFile file, UUID userId, ContentType contentType) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Validate file size based on content type
        validateFileSize(file, contentType);

        // Clean and create a safe filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Generate a unique storage path
        String storagePath = generateStoragePath(userId, contentType, originalFilename);
        Path targetPath = getPath(storagePath);

        try {
            // Make sure the target directory exists
            Files.createDirectories(targetPath.getParent());

            // Copy the file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored file {} at {}", originalFilename, targetPath);
            return storagePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public String generateThumbnail(String storagePath, ContentType contentType) {
        if (contentType != ContentType.IMAGE && contentType != ContentType.VIDEO) {
            return null;
        }

        try {
            Path sourcePath = getPath(storagePath);

            // Generate thumbnail filename
            String thumbnailPath = getThumbnailPath(storagePath);
            Path targetPath = getPath(thumbnailPath);

            // Create parent directories if they don't exist
            Files.createDirectories(targetPath.getParent());

            // For now, we'll only handle image thumbnails - video would require additional libraries
            if (contentType == ContentType.IMAGE) {
                BufferedImage originalImage = ImageIO.read(sourcePath.toFile());
                if (originalImage == null) {
                    log.warn("Could not read image file: {}", sourcePath);
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
                String extension = FilenameUtils.getExtension(sourcePath.getFileName().toString()).toLowerCase();
                ImageIO.write(thumbnail, extension, targetPath.toFile());

                log.info("Generated thumbnail for {} at {}", sourcePath, targetPath);
                return thumbnailPath;
            }

            return null;
        } catch (IOException e) {
            log.error("Failed to generate thumbnail for {}", storagePath, e);
            return null;
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path file = getPath(storagePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "path", storagePath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "path", storagePath);
        }
    }

    @Override
    public Path getPath(String storagePath) {
        return rootLocation.resolve(storagePath);
    }

    @Override
    public void delete(String storagePath) {
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

    @Override
    public boolean exists(String storagePath) {
        if (storagePath == null) {
            return false;
        }
        Path file = getPath(storagePath);
        return Files.exists(file);
    }

    @Override
    public String getContentType(String filename) {
        try {
            File file = getPath(filename).toFile();
            return tika.detect(file);
        } catch (IOException e) {
            log.error("Failed to detect content type for file: {}", filename, e);
            return "application/octet-stream";
        }
    }

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
package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.service.FileService;
import com.personal.omnivault.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Implementation of StorageService that delegates to the FileService
 * This maintains backward compatibility while using our new modular approach
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements StorageService {

    private final FileService fileService;

    @Override
    public void init() {
        fileService.init();
    }

    @Override
    public String store(MultipartFile file, UUID userId, ContentType contentType) {
        return fileService.storeFile(file, userId, contentType);
    }

    @Override
    public String generateThumbnail(String storagePath, ContentType contentType) {
        return fileService.generateThumbnail(storagePath, contentType);
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        return fileService.loadFileAsResource(storagePath);
    }

    @Override
    public Path getPath(String storagePath) {
        return fileService.getPath(storagePath);
    }

    @Override
    public void delete(String storagePath) {
        fileService.deleteFile(storagePath);
    }

    @Override
    public boolean exists(String storagePath) {
        return fileService.fileExists(storagePath);
    }

    @Override
    public String getContentType(String filename) {
        return fileService.getContentType(filename);
    }
}
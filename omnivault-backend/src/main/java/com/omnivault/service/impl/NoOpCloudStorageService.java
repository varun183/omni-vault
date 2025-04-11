package com.omnivault.service.impl;

import com.omnivault.domain.model.ContentType;
import com.omnivault.exception.FileStorageException;
import com.omnivault.service.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpCloudStorageService implements CloudStorageService {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String storeFile(MultipartFile file, UUID userId, ContentType contentType, String filename) {
        throw new FileStorageException("Cloud storage is not enabled");
    }

    @Override
    public Resource loadFileAsResource(String key) {
        throw new FileStorageException("Cloud storage is not enabled");
    }

    @Override
    public String generatePresignedUrl(String key) {
        throw new FileStorageException("Cloud storage is not enabled");
    }

    @Override
    public void deleteFile(String key) {
        throw new FileStorageException("Cloud storage is not enabled");
    }

    @Override
    public boolean fileExists(String key) {
        return false;
    }
}
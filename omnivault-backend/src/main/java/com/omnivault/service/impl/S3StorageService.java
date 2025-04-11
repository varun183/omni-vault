package com.omnivault.service.impl;

import com.omnivault.config.AwsS3Config;
import com.omnivault.domain.model.ContentType;
import com.omnivault.exception.FileStorageException;
import com.omnivault.service.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3StorageService implements CloudStorageService {

    private final AwsS3Config s3Config;
    private final S3Client s3Client;

    @Override
    public boolean isEnabled() {
        return s3Config.isEnabled();
    }

    @Override
    public String storeFile(MultipartFile file, UUID userId, ContentType contentType, String filename) {
        if (!s3Config.isEnabled()) {
            throw new FileStorageException("S3 storage is not enabled");
        }

        if (file.getSize() > s3Config.getMaxSizeBytes()) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed size of %d bytes", s3Config.getMaxSizeBytes())
            );
        }

        try {
            // Generate a unique storage path
            String key = generateS3Key(userId, contentType, filename);

            // Upload the file to S3
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file to S3: {}", key);

            return key;
        } catch (IOException ex) {
            log.error("Failed to upload file to S3", ex);
            throw new FileStorageException("Failed to store file in S3", ex);
        } catch (S3Exception ex) {
            log.error("S3 error during file upload", ex);
            throw new FileStorageException("S3 error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            byte[] content = s3Object.readAllBytes();

            return new ByteArrayResource(content);
        } catch (IOException ex) {
            log.error("Failed to read file from S3", ex);
            throw new FileStorageException("Failed to read file from S3", ex);
        } catch (S3Exception ex) {
            log.error("S3 error during file download", ex);
            throw new FileStorageException("S3 error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String generatePresignedUrl(String key) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(s3Config.getUrlExpirationSeconds()))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(key)
                            .build())
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            URL url = presignedRequest.url();

            return url.toString();
        } catch (Exception ex) {
            log.error("Failed to generate presigned URL", ex);
            throw new FileStorageException("Failed to generate presigned URL", ex);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted file from S3: {}", key);
        } catch (S3Exception ex) {
            log.error("S3 error during file deletion", ex);
            throw new FileStorageException("S3 error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            log.error("S3 error checking if file exists", ex);
            return false;
        }
    }

    private String generateS3Key(UUID userId, ContentType contentType, String filename) {
        String sanitizedFilename = sanitizeFilename(filename);
        String uniqueId = UUID.randomUUID().toString();

        return String.format("%s/%s/%s-%s",
                contentType.name().toLowerCase(),
                userId.toString(),
                uniqueId,
                sanitizedFilename);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        // Remove path traversal attempts and normalize
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
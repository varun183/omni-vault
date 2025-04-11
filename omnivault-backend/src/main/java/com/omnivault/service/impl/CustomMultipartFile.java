package com.omnivault.service.impl;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A custom implementation of MultipartFile that wraps an InputStream
 * Used for converting between storage types
 */
public class CustomMultipartFile implements MultipartFile {
    private final InputStream inputStream;
    private final long size;
    private final String name;
    private final String contentType;
    private byte[] bytes;

    public CustomMultipartFile(@NonNull InputStream inputStream, long size, String name) {
        this(inputStream, size, name, null);
    }

    public CustomMultipartFile(@NonNull InputStream inputStream, long size, String name, String contentType) {
        this.inputStream = inputStream;
        this.size = size;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        if (bytes == null) {
            bytes = inputStream.readAllBytes();
        }
        return bytes;
    }

    @Override
    @NonNull
    public InputStream getInputStream()  {
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }
        return inputStream;
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (InputStream in = getInputStream()) {
            java.nio.file.Files.copy(in, dest.toPath());
        }
    }
}
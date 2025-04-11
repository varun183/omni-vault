package com.omnivault.util;

import com.omnivault.domain.dto.response.ContentDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling file responses
 */
public class FileResponseUtils {

    private static final long CACHE_DURATION_HOURS = 24;

    private FileResponseUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a ResponseEntity for a file resource
     *
     * @param resource The file resource to send
     * @param content The content metadata
     * @return ResponseEntity configured with appropriate headers
     */
    public static ResponseEntity<Resource> createFileResponse(Resource resource, ContentDTO content) {
        String contentType = content.getMimeType();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .contentType(contentType != null ?
                        MediaType.parseMediaType(contentType) :
                        MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.maxAge(CACHE_DURATION_HOURS, TimeUnit.HOURS));

        // Use "inline" for images, videos, and PDFs so they display in browser
        if (shouldUseInlineDisposition(contentType)) {
            responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + content.getOriginalFilename() + "\"");
        } else {
            // Use "attachment" for other file types to force download
            responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + content.getOriginalFilename() + "\"");
        }

        return responseBuilder.body(resource);
    }

    /**
     * Creates a ResponseEntity for a thumbnail resource
     *
     * @param resource The thumbnail resource to send
     * @return ResponseEntity configured with appropriate headers for a thumbnail
     */
    public static ResponseEntity<Resource> createThumbnailResponse(Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .cacheControl(CacheControl.maxAge(CACHE_DURATION_HOURS, TimeUnit.HOURS))
                .body(resource);
    }

    /**
     * Determines if the content type should use inline disposition
     *
     * @param contentType The MIME type of the content
     * @return true if the content should use inline disposition
     */
    private static boolean shouldUseInlineDisposition(String contentType) {
        return contentType != null &&
                (contentType.startsWith("image/") ||
                        contentType.startsWith("video/") ||
                        contentType.equals("application/pdf"));
    }
}
package com.omnivault.util;

import com.omnivault.domain.model.ContentType;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for content type detection and metadata management.
 * Centralizes document identification logic across the application.
 */
@Slf4j
@Component
public class ContentTypeUtils {

    // Common file type extensions grouped by content type
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "tiff", "ico");

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "mpg", "mpeg");

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            // Common document types
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            // Text files
            "txt", "text", "log", "rtf", "csv", "tsv", "md", "markdown",
            // Data formats
            "json", "xml", "yaml", "yml", "toml", "ini", "cfg", "conf",
            // Code files
            "java", "py", "rb", "php", "c", "cpp", "h", "cs", "js", "jsx", "ts", "tsx", "go", "rs", "swift",
            // Office formats
            "odt", "ods", "odp", "odg", "odf",
            // Archive formats
            "zip", "rar", "7z", "tar", "gz");

    // Common MIME types mapped to content types
    private static final Map<String, ContentType> MIME_TYPE_MAP = createMimeTypeMap();






    private static Map<String, ContentType> createMimeTypeMap() {
        Map<String, ContentType> map = new HashMap<>();

        // Images
        map.put("image/jpeg", ContentType.IMAGE);
        map.put("image/png", ContentType.IMAGE);
        map.put("image/gif", ContentType.IMAGE);
        map.put("image/svg+xml", ContentType.IMAGE);
        map.put("image/webp", ContentType.IMAGE);

        // Videos
        map.put("video/mp4", ContentType.VIDEO);
        map.put("video/mpeg", ContentType.VIDEO);
        map.put("video/quicktime", ContentType.VIDEO);
        map.put("video/x-msvideo", ContentType.VIDEO);
        map.put("video/webm", ContentType.VIDEO);

        // Documents
        map.put("application/pdf", ContentType.DOCUMENT);
        map.put("application/msword", ContentType.DOCUMENT);
        map.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ContentType.DOCUMENT);
        map.put("application/vnd.ms-excel", ContentType.DOCUMENT);
        map.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ContentType.DOCUMENT);
        map.put("application/vnd.ms-powerpoint", ContentType.DOCUMENT);
        map.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ContentType.DOCUMENT);

        // Text content
        map.put("text/plain", ContentType.DOCUMENT);
        map.put("text/csv", ContentType.DOCUMENT);
        map.put("text/html", ContentType.DOCUMENT);
        map.put("text/markdown", ContentType.DOCUMENT);
        map.put("application/json", ContentType.DOCUMENT);
        map.put("application/xml", ContentType.DOCUMENT);

        return map;
    }

    public static ContentType determineContentType(String filename, String mimeType) {
        // If we have a MIME type, try to use that first
        if (mimeType != null && MIME_TYPE_MAP.containsKey(mimeType)) {
            return MIME_TYPE_MAP.get(mimeType);
        }

        // Fall back to extension-based detection
        if (filename != null) {
            String extension = FilenameUtils.getExtension(filename).toLowerCase();

            if (IMAGE_EXTENSIONS.contains(extension)) {
                return ContentType.IMAGE;
            }

            if (VIDEO_EXTENSIONS.contains(extension)) {
                return ContentType.VIDEO;
            }

            if (DOCUMENT_EXTENSIONS.contains(extension)) {
                return ContentType.DOCUMENT;
            }
        }

        // Default fallback
        return ContentType.OTHER;
    }







}
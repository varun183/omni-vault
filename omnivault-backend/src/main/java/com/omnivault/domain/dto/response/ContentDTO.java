package com.omnivault.domain.dto.response;

import com.omnivault.domain.model.ContentType;
import com.omnivault.domain.model.StorageLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about a content item in OmniVault")
public class ContentDTO {
    @Schema(
            description = "Unique identifier for the content",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
            description = "Title of the content",
            example = "Summer Vacation Notes",
            maxLength = 255
    )
    private String title;

    @Schema(
            description = "Optional description of the content",
            example = "Detailed notes and memories from my summer vacation"
    )
    private String description;

    @Schema(
            description = "Type of content",
            example = "TEXT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ContentType contentType;

    @Schema(
            description = "ID of the folder containing this content",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID folderId;

    @Schema(
            description = "Name of the folder containing this content",
            example = "Personal Notes"
    )
    private String folderName;

    @Schema(
            description = "Size of the content in bytes",
            example = "1024"
    )
    private Long sizeBytes;

    @Schema(
            description = "MIME type of the content",
            example = "text/plain"
    )
    private String mimeType;

    @Schema(
            description = "Internal storage path of the content",
            example = "text/a94af379-b06f-4006-b6ca-3833c24273df/notes.txt"
    )
    private String storagePath;

    @Schema(
            description = "Storage location of the content",
            example = "LOCAL"
    )
    private StorageLocation storageLocation;

    @Schema(
            description = "Original filename of the uploaded content",
            example = "summer_notes.txt"
    )
    private String originalFilename;

    @Schema(
            description = "Path to the content's thumbnail",
            example = "thumbnails/summer_notes_thumb.jpg"
    )
    private String thumbnailPath;

    @Schema(
            description = "Storage location of the thumbnail",
            example = "LOCAL"
    )
    private StorageLocation thumbnailStorageLocation;

    @Schema(
            description = "Indicates if the content is marked as a favorite",
            example = "true"
    )
    private boolean favorite;

    @Schema(
            description = "Number of times the content has been viewed",
            example = "42"
    )
    private Integer viewCount;

    @Schema(
            description = "Additional metadata associated with the content",
            example = "{\"source\": \"Personal Journal\", \"mood\": \"Happy\"}"
    )
    private Map<String, Object> metadata;

    @Schema(
            description = "Tags associated with the content"
    )
    private List<TagDTO> tags;

    @Schema(
            description = "Timestamp when the content was created",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime createdAt;

    @Schema(
            description = "Timestamp when the content was last updated",
            example = "2024-04-08T15:45:22Z"
    )
    private ZonedDateTime updatedAt;

    // Type-specific content fields
    @Schema(
            description = "Text content (for TEXT type)",
            example = "Today was an amazing day..."
    )
    private String textContent;

    @Schema(
            description = "URL (for LINK type)",
            example = "https://example.com/summer-memories"
    )
    private String url;

    @Schema(
            description = "Preview image path for links",
            example = "links/preview/summer_memories.jpg"
    )
    private String previewImagePath;

    @Schema(
            description = "Pre-signed URL for cloud-stored content",
            example = "https://s3.amazonaws.com/bucket/path/to/content"
    )
    private String presignedUrl;

    @Schema(
            description = "Expiration timestamp for the pre-signed URL",
            example = "1712587200000"
    )
    private Long presignedUrlExpiresAt;

    @Schema(
            description = "Pre-signed URL for thumbnail",
            example = "https://s3.amazonaws.com/bucket/path/to/thumbnail"
    )
    private String thumbnailPresignedUrl;
}
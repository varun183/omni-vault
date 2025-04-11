package com.omnivault.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "contents")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a content item in the OmniVault system")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
            description = "Unique identifier for the content",
            example = "550e8400-e29b-41d4-a716-446655440000",
            pattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    )
    private UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    @Schema(
            description = "Title of the content",
            example = "My Project Notes",
            maxLength = 255
    )
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(
            description = "Optional description of the content",
            example = "Detailed notes about my ongoing project"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    @Schema(
            description = "Type of content",
            example = "TEXT"
    )
    private ContentType contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    @JsonIgnore
    @Schema(description = "Folder containing this content")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @Schema(description = "User who owns this content")
    private User user;

    @Column(name = "size_bytes")
    @Schema(
            description = "Size of the content in bytes",
            example = "1024",
            minimum = "0"
    )
    private Long sizeBytes;

    @Column(name = "mime_type")
    @Schema(
            description = "MIME type of the content",
            example = "text/plain"
    )
    private String mimeType;

    @Column(name = "storage_path", columnDefinition = "TEXT")
    @Schema(
            description = "Path where the content is stored",
            example = "text/a94af379-b06f-4006-b6ca-3833c24273df/my-notes.txt"
    )
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_location", nullable = false)
    @Schema(
            description = "Location where the content is stored",
            example = "LOCAL"
    )
    private StorageLocation storageLocation = StorageLocation.LOCAL;

    @Column(name = "original_filename", columnDefinition = "TEXT")
    @Schema(
            description = "Original filename of the uploaded content",
            example = "project-notes.txt"
    )
    private String originalFilename;

    @Column(name = "thumbnail_path", columnDefinition = "TEXT")
    @Schema(
            description = "Path to the thumbnail (for image/video content)",
            example = "thumbnails/a94af379-b06f-4006-b6ca-3833c24273df/thumbnail.jpg"
    )
    private String thumbnailPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_storage_location")
    @Schema(
            description = "Storage location of the thumbnail",
            example = "LOCAL"
    )
    private StorageLocation thumbnailStorageLocation = StorageLocation.LOCAL;

    @Column(name = "is_favorite")
    @Schema(
            description = "Indicates if the content is marked as a favorite",
            example = "false"
    )
    private boolean favorite;

    @Column(name = "view_count")
    @Schema(
            description = "Number of times the content has been viewed",
            example = "42",
            minimum = "0"
    )
    private Integer viewCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    @Schema(
            description = "Additional metadata for the content",
            example = "{\"source\": \"Personal Project\", \"tags\": [\"work\", \"development\"]}"
    )
    private Map<String, Object> metadata;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "content_tags",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    @Schema(description = "Tags associated with the content")
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at")
    @Schema(
            description = "Timestamp when the content was created",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(
            description = "Timestamp when the content was last updated",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(
            description = "Version number for optimistic locking",
            example = "0"
    )
    private Long version = 0L;

    // Existing methods with added documentation
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = ZonedDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
        if (storageLocation == null) {
            storageLocation = StorageLocation.LOCAL;
        }
        if (thumbnailStorageLocation == null && thumbnailPath != null) {
            thumbnailStorageLocation = StorageLocation.LOCAL;
        }
    }

    @Schema(description = "Increments the view count for the content")
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }

    @Schema(description = "Adds a tag to the content")
    public void addTag(Tag tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);

        if (tag.getContents() == null) {
            tag.setContents(new HashSet<>());
        }
        tag.getContents().add(this);
    }

    @Schema(description = "Removes a tag from the content")
    public void removeTag(Tag tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }

        if (tag.getContents() != null) {
            tag.getContents().remove(this);
        }
    }
}
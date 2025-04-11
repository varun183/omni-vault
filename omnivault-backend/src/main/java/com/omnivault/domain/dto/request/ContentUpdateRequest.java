package com.omnivault.domain.dto.request;

import com.omnivault.domain.model.StorageLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating content")
public class ContentUpdateRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
            description = "Updated title for the content",
            example = "Updated Meeting Notes",
            maxLength = 255
    )
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(
            description = "Updated description for the content",
            example = "Revised notes from team meeting",
            maxLength = 1000
    )
    private String description;

    @Schema(
            description = "ID of the folder to move the content to",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID folderId;

    @Schema(
            description = "Flag to toggle favorite status"
    )
    private Boolean favorite;

    @Schema(
            description = "List of existing tag IDs to associate with the content"
    )
    private List<UUID> tagIds;

    @Schema(
            description = "List of new tag names to create and associate with the content"
    )
    private List<String> newTags;

    @Schema(
            description = "Additional metadata for the content",
            example = "{\"source\": \"Updated Source\", \"importance\": \"medium\"}"
    )
    private Map<String, Object> metadata;

    @Schema(
            description = "Preferred storage location for the content"
    )
    private StorageLocation storageLocation;

    @Schema(
            description = "Updated text content (for text-based content)"
    )
    private String textContent;

    @Schema(
            description = "Updated URL (for link-based content)"
    )
    private String url;
}
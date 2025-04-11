package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request for creating text-based content")
public class TextContentCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
            description = "Title of the text content",
            example = "Meeting Notes",
            maxLength = 255
    )
    private String title;

    @Schema(
            description = "Optional description for the content",
            example = "Notes from weekly team meeting"
    )
    private String description;

    @Schema(
            description = "ID of the folder to place the content in",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID folderId;

    @NotBlank(message = "Text content is required")
    @Schema(
            description = "Actual text content",
            example = "Today we discussed project milestones and upcoming sprint planning."
    )
    private String textContent;

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
            example = "{\"source\": \"Team Meeting\", \"importance\": \"high\"}"
    )
    private Map<String, Object> metadata;
}

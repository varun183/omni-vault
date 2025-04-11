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
@Schema(description = "Request for creating link-based content")
public class LinkContentCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
            description = "Title of the link content",
            example = "Interesting Article on AI",
            maxLength = 255
    )
    private String title;

    @Schema(
            description = "Optional description for the content",
            example = "A fascinating read about recent AI developments"
    )
    private String description;

    @Schema(
            description = "ID of the folder to place the content in",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID folderId;

    @NotBlank(message = "URL is required")
    @Schema(
            description = "Full URL of the link",
            example = "https://example.com/ai-article"
    )
    private String url;

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
            example = "{\"source\": \"Tech Blog\", \"readTime\": \"5 min\"}"
    )
    private Map<String, Object> metadata;
}
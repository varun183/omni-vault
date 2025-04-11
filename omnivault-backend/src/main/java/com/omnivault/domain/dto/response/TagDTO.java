package com.omnivault.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about a tag in OmniVault")
public class TagDTO {
    @Schema(
            description = "Unique identifier for the tag",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
            description = "Name of the tag",
            example = "Important",
            maxLength = 50
    )
    private String name;

    @Schema(
            description = "Hex color code for the tag",
            example = "#FF5733",
            pattern = "^#[0-9A-Fa-f]{6}$"
    )
    private String color;

    @Schema(
            description = "Number of content items with this tag",
            example = "10"
    )
    private int contentCount;

    @Schema(
            description = "Timestamp when the tag was created",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime createdAt;

    @Schema(
            description = "Timestamp when the tag was last updated",
            example = "2024-04-08T15:45:22Z"
    )
    private ZonedDateTime updatedAt;
}
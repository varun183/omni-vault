package com.omnivault.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about a folder in OmniVault")
public class FolderDTO {
    @Schema(
            description = "Unique identifier for the folder",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
            description = "Name of the folder",
            example = "Personal Documents",
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Optional description of the folder",
            example = "Contains all my important personal documents"
    )
    private String description;

    @Schema(
            description = "ID of the parent folder (null for root folders)",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID parentId;

    @Schema(
            description = "Full path of the folder in the folder hierarchy",
            example = "/Personal/Documents"
    )
    private String path;

    @Schema(
            description = "Number of content items in this folder",
            example = "15"
    )
    private int contentCount;

    @Schema(
            description = "Number of subfolders within this folder",
            example = "3"
    )
    private int subfolderCount;

    @Schema(
            description = "List of immediate subfolders"
    )
    private List<FolderDTO> subfolders;

    @Schema(
            description = "Timestamp when the folder was created",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime createdAt;

    @Schema(
            description = "Timestamp when the folder was last updated",
            example = "2024-04-08T15:45:22Z"
    )
    private ZonedDateTime updatedAt;
}
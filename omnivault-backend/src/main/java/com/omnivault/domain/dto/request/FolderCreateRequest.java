package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new folder")
public class FolderCreateRequest {
    @NotBlank(message = "Folder name is required")
    @Size(min = 1, max = 100, message = "Folder name must be between 1 and 100 characters")
    @Schema(
            description = "Name of the folder",
            example = "Work Documents",
            minLength = 1,
            maxLength = 100
    )
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(
            description = "Optional description for the folder",
            example = "Contains all work-related documents",
            maxLength = 500
    )
    private String description;

    @Schema(
            description = "ID of the parent folder (if this is a subfolder)",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID parentId;
}
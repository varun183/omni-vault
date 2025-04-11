package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag creation request")
public class TagCreateRequest {
    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    @Schema(
            description = "Name of the tag",
            example = "Important",
            minLength = 1,
            maxLength = 50
    )
    private String name;

    @Schema(
            description = "Hex color code for the tag",
            example = "#FF5733",
            pattern = "^#[0-9A-Fa-f]{6}$"
    )
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #FF5733)")
    private String color;
}
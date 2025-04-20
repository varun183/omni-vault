package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating user profile")
public class ProfileUpdateRequest {
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Schema(
            description = "Updated first name",
            example = "John",
            maxLength = 50
    )
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Schema(
            description = "Updated last name",
            example = "Doe",
            maxLength = 50
    )
    private String lastName;
}
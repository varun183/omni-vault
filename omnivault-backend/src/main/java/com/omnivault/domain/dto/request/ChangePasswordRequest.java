package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for changing user password")
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    @Schema(
            description = "User's current password",
            example = "OldSecurePassword123!"
    )
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(
            description = "New password to replace the current password",
            example = "NewSecurePassword456!",
            minLength = 8,
            maxLength = 100
    )
    private String newPassword;
}
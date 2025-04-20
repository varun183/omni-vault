package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for deleting user account")
public class DeleteAccountRequest {
    @NotBlank(message = "Password confirmation is required")
    @Schema(
            description = "User's current password to confirm account deletion",
            example = "SecurePassword123!"
    )
    private String password;
}
package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user login credentials")
public class LoginRequest {
    @NotBlank(message = "Username or email is required")
    @Schema(
            description = "Username or email for authentication",
            example = "johndoe@example.com",
            minLength = 3,
            maxLength = 100
    )
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "User's password",
            example = "SecurePassword123!",
            minLength = 8,
            maxLength = 100
    )
    private String password;
}
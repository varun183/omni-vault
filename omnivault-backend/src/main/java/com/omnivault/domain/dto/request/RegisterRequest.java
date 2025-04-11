package com.omnivault.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "User registration request details")
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(
            description = "User's unique username",
            example = "johndoe",
            minLength = 3,
            maxLength = 50
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(
            description = "User's email address",
            example = "john.doe@example.com",
            pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(
            description = "User's password",
            example = "StrongP@ssw0rd!",
            minLength = 8,
            maxLength = 100
    )
    private String password;

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Schema(
            description = "User's first name",
            example = "John",
            maxLength = 50
    )
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Schema(
            description = "User's last name",
            example = "Doe",
            maxLength = 50
    )
    private String lastName;
}
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
@Schema(description = "User information returned after authentication")
public class UserDTO {
    @Schema(
            description = "Unique identifier for the user",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
            description = "Username",
            example = "johndoe",
            maxLength = 50
    )
    private String username;

    @Schema(
            description = "User's email address",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "User's first name",
            example = "John",
            maxLength = 50
    )
    private String firstName;

    @Schema(
            description = "User's last name",
            example = "Doe",
            maxLength = 50
    )
    private String lastName;

    @Schema(
            description = "Timestamp when the user account was created",
            example = "2024-04-07T12:34:56Z"
    )
    private ZonedDateTime createdAt;
}
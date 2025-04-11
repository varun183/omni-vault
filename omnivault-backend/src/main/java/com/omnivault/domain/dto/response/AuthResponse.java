package com.omnivault.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing user and token information")
public class AuthResponse {
    @Schema(
            description = "JWT access token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Refresh token for obtaining new access tokens",
            example = "r3fr3sh_t0k3n_c0d3"
    )
    private String refreshToken;

    @Schema(
            description = "Type of token (typically 'Bearer')",
            example = "Bearer"
    )
    private String tokenType;

    @Schema(
            description = "Token expiration time in seconds",
            example = "3600"
    )
    private Long expiresIn;

    @Schema(
            description = "Authenticated user details"
    )
    private UserDTO user;
}
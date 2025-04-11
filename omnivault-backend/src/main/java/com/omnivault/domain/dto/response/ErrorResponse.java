package com.omnivault.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder  // Change from @Builder to @SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response for API exceptions")
public class ErrorResponse {
    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Resource not found")
    private String message;

    @Schema(description = "Request path or context", example = "/api/contents/123")
    private String path;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;
}
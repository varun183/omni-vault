package com.omnivault.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Types of content that can be stored in OmniVault")
public enum ContentType {
    @Schema(description = "Plain text content like notes or documents")
    TEXT,

    @Schema(description = "Web links or URLs")
    LINK,

    @Schema(description = "Image files")
    IMAGE,

    @Schema(description = "Video files")
    VIDEO,

    @Schema(description = "Document files like PDFs, Word docs")
    DOCUMENT,

    @Schema(description = "Other file types not covered by specific categories")
    OTHER
}
package com.omnivault.controller;

import com.omnivault.domain.dto.request.TagCreateRequest;
import com.omnivault.domain.dto.response.TagDTO;
import com.omnivault.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Endpoints for managing content tags")
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "Get all tags",
            description = "Retrieves all tags for the current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @Operation(
            summary = "Get tag details",
            description = "Retrieves details of a specific tag"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TagDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content)
    })
    @GetMapping("/{tagId}")
    public ResponseEntity<TagDTO> getTag(
            @Parameter(description = "ID of the tag", required = true)
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(tagService.getTag(tagId));
    }

    @Operation(
            summary = "Create a new tag",
            description = "Creates a new tag for the current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag created successfully",
                    content = @Content(schema = @Schema(implementation = TagDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid tag creation request",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TagDTO> createTag(
            @Valid @RequestBody TagCreateRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @Operation(
            summary = "Update tag",
            description = "Updates an existing tag's details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag updated successfully",
                    content = @Content(schema = @Schema(implementation = TagDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content)
    })
    @PutMapping("/{tagId}")
    public ResponseEntity<TagDTO> updateTag(
            @Parameter(description = "ID of the tag to update", required = true)
            @PathVariable UUID tagId,
            @Valid @RequestBody TagCreateRequest request) {
        return ResponseEntity.ok(tagService.updateTag(tagId, request));
    }

    @Operation(
            summary = "Delete tag",
            description = "Deletes a tag from the user's account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content)
    })
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID of the tag to delete", required = true)
            @PathVariable UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search tags",
            description = "Searches tags by name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags found successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> searchTags(
            @Parameter(description = "Search term to find matching tags", required = true)
            @RequestParam String query) {
        return ResponseEntity.ok(tagService.searchTags(query));
    }
}
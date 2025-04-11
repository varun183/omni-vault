package com.omnivault.controller;

import com.omnivault.domain.dto.request.ContentUpdateRequest;
import com.omnivault.domain.dto.request.LinkContentCreateRequest;
import com.omnivault.domain.dto.request.TextContentCreateRequest;
import com.omnivault.domain.dto.response.ContentDTO;
import com.omnivault.domain.model.ContentType;
import com.omnivault.domain.model.StorageLocation;
import com.omnivault.service.ContentService;
import com.omnivault.util.FileResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Endpoints for managing content items")
public class ContentController {

    private final ContentService contentService;

    @Operation(
            summary = "Get all content",
            description = "Retrieves all content items for the current user with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getAllContent(pageable));
    }

    @Operation(
            summary = "Get content by ID",
            description = "Retrieves a specific content item by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content item retrieved",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDTO> getContent(
            @Parameter(description = "ID of the content to retrieve", required = true)
            @PathVariable UUID contentId) {
        return ResponseEntity.ok(contentService.getContent(contentId));
    }

    @Operation(
            summary = "Get content by folder",
            description = "Retrieves all content items within a specific folder with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content)
    })
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<Page<ContentDTO>> getContentByFolder(
            @Parameter(description = "ID of the folder", required = true)
            @PathVariable UUID folderId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByFolder(folderId, pageable));
    }

    @Operation(
            summary = "Get content by type",
            description = "Retrieves all content items of a specific type with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/type/{contentType}")
    public ResponseEntity<Page<ContentDTO>> getContentByType(
            @Parameter(description = "Type of content (TEXT, LINK, IMAGE, VIDEO, DOCUMENT, OTHER)", required = true)
            @PathVariable ContentType contentType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByType(contentType, pageable));
    }

    @Operation(
            summary = "Get content by tag",
            description = "Retrieves all content items with a specific tag with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content)
    })
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Page<ContentDTO>> getContentByTag(
            @Parameter(description = "ID of the tag", required = true)
            @PathVariable UUID tagId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByTag(tagId, pageable));
    }

    @Operation(
            summary = "Get favorite content",
            description = "Retrieves all favorite content items with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of favorite content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/favorites")
    public ResponseEntity<Page<ContentDTO>> getFavoriteContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getFavoriteContent(pageable));
    }

    @Operation(
            summary = "Get recent content",
            description = "Retrieves recently added or updated content items with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of recent content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/recent")
    public ResponseEntity<Page<ContentDTO>> getRecentContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getRecentContent(pageable));
    }

    @Operation(
            summary = "Get popular content",
            description = "Retrieves the most viewed content items"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of popular content items retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContentDTO.class))))
    })
    @GetMapping("/popular")
    public ResponseEntity<List<ContentDTO>> getPopularContent() {
        return ResponseEntity.ok(contentService.getPopularContent());
    }

    @Operation(
            summary = "Create text content",
            description = "Creates a new text note content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text content created successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PostMapping("/text")
    public ResponseEntity<ContentDTO> createTextContent(
            @Valid @RequestBody TextContentCreateRequest request) {
        return ResponseEntity.ok(contentService.createTextContent(request));
    }

    @Operation(
            summary = "Create link content",
            description = "Creates a new link content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link content created successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PostMapping("/link")
    public ResponseEntity<ContentDTO> createLinkContent(
            @Valid @RequestBody LinkContentCreateRequest request) {
        return ResponseEntity.ok(contentService.createLinkContent(request));
    }

    @Operation(
            summary = "Upload file content",
            description = "Uploads a file and creates a new file-based content item",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or request data",
                    content = @Content)
    })
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentDTO> createFileContent(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Title for the content")
            @RequestParam(required = false) String title,
            @Parameter(description = "Description for the content")
            @RequestParam(required = false) String description,
            @Parameter(description = "ID of the folder to place content in")
            @RequestParam(required = false) UUID folderId,
            @Parameter(description = "List of tag IDs to associate with the content")
            @RequestParam(required = false) List<UUID> tagIds,
            @Parameter(description = "List of new tag names to create and associate with the content")
            @RequestParam(required = false) List<String> newTags,
            @Parameter(description = "Storage location (CLOUD or LOCAL)")
            @RequestParam(required = false, defaultValue = "CLOUD") StorageLocation storageLocation) {
        return ResponseEntity.ok(contentService.createFileContent(
                file, title, description, folderId, tagIds, newTags, storageLocation));
    }

    @Operation(
            summary = "Update content",
            description = "Updates an existing content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content updated successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @PutMapping("/{contentId}")
    public ResponseEntity<ContentDTO> updateContent(
            @Parameter(description = "ID of the content to update", required = true)
            @PathVariable UUID contentId,
            @Valid @RequestBody ContentUpdateRequest request) {
        return ResponseEntity.ok(contentService.updateContent(contentId, request));
    }

    @Operation(
            summary = "Toggle favorite status",
            description = "Toggles the favorite status of a content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite status toggled successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @PutMapping("/{contentId}/favorite")
    public ResponseEntity<ContentDTO> toggleFavorite(
            @Parameter(description = "ID of the content to toggle favorite status", required = true)
            @PathVariable UUID contentId) {
        return ResponseEntity.ok(contentService.toggleFavorite(contentId));
    }

    @Operation(
            summary = "Update content tags",
            description = "Updates the tags associated with a content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content tags updated successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @PutMapping("/{contentId}/tags")
    public ResponseEntity<ContentDTO> updateContentTags(
            @Parameter(description = "ID of the content to update tags for", required = true)
            @PathVariable UUID contentId,
            @Parameter(description = "List of tag IDs to associate with the content")
            @RequestParam(required = false) List<UUID> tagIds,
            @Parameter(description = "List of new tag names to create and associate with the content")
            @RequestParam(required = false) List<String> newTags) {
        return ResponseEntity.ok(contentService.updateContentTags(contentId, tagIds, newTags));
    }

    @Operation(
            summary = "Move content storage",
            description = "Moves a content item between storage locations (LOCAL or CLOUD)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content moved successfully",
                    content = @Content(schema = @Schema(implementation = ContentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid storage location",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @PutMapping("/{contentId}/storage")
    public ResponseEntity<ContentDTO> moveContentStorage(
            @Parameter(description = "ID of the content to move", required = true)
            @PathVariable UUID contentId,
            @Parameter(description = "Target storage location (CLOUD or LOCAL)", required = true)
            @RequestParam StorageLocation targetLocation) {
        return ResponseEntity.ok(contentService.moveContentStorage(contentId, targetLocation));
    }

    @Operation(
            summary = "Delete content",
            description = "Deletes a content item and its associated file if any"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(
            @Parameter(description = "ID of the content to delete", required = true)
            @PathVariable UUID contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get content file",
            description = "Downloads the file associated with a content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "Content or file not found",
                    content = @Content)
    })
    @GetMapping("/{contentId}/file")
    public ResponseEntity<Resource> getContentFile(
            @Parameter(description = "ID of the content to download file for", required = true)
            @PathVariable UUID contentId) {
        Resource resource = contentService.getContentFile(contentId);
        ContentDTO content = contentService.getContent(contentId);
        return FileResponseUtils.createFileResponse(resource, content);
    }

    @Operation(
            summary = "Get content cloud URL",
            description = "Generates a pre-signed URL for cloud-stored content"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL generated successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Content is not stored in cloud",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @GetMapping("/{contentId}/cloud-url")
    public ResponseEntity<Map<String, String>> getContentPresignedUrl(
            @Parameter(description = "ID of the content to get URL for", required = true)
            @PathVariable UUID contentId) {
        String url = contentService.getContentPresignedUrl(contentId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(
            summary = "Get content thumbnail",
            description = "Retrieves the thumbnail for an image or video content item"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thumbnail retrieved successfully",
                    content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "404", description = "Content or thumbnail not found",
                    content = @Content)
    })
    @GetMapping("/{contentId}/thumbnail")
    public ResponseEntity<Resource> getContentThumbnail(
            @Parameter(description = "ID of the content to retrieve thumbnail for", required = true)
            @PathVariable UUID contentId) {
        Resource resource = contentService.getContentThumbnail(contentId);
        return FileResponseUtils.createThumbnailResponse(resource);
    }

    @Operation(
            summary = "Get cloud URL for content thumbnail",
            description = "Generates a pre-signed URL for a cloud-stored thumbnail"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thumbnail URL generated successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Thumbnail is not stored in cloud",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Content not found",
                    content = @Content)
    })
    @GetMapping("/{contentId}/thumbnail-url")
    public ResponseEntity<Map<String, String>> getThumbnailPresignedUrl(
            @Parameter(description = "ID of the content to get thumbnail URL for", required = true)
            @PathVariable UUID contentId) {
        String url = contentService.getThumbnailPresignedUrl(contentId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(
            summary = "Search content",
            description = "Searches content across multiple fields with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching content items retrieved",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ContentDTO>> searchContent(
            @Parameter(description = "Search term to find matching content", required = true)
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.searchContent(query, pageable));
    }
}
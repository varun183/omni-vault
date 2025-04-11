package com.omnivault.controller;

import com.omnivault.domain.dto.request.FolderCreateRequest;
import com.omnivault.domain.dto.response.FolderDTO;
import com.omnivault.service.FolderService;
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
@RequestMapping("/folders")
@RequiredArgsConstructor
@Tag(name = "Folders", description = "Endpoints for managing folder structure")
public class FolderController {

    private final FolderService folderService;

    @Operation(
            summary = "Get root folders",
            description = "Retrieves all root-level folders for the current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Root folders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/root")
    public ResponseEntity<List<FolderDTO>> getRootFolders() {
        return ResponseEntity.ok(folderService.getRootFolders());
    }

    @Operation(
            summary = "Get subfolders",
            description = "Retrieves all subfolders within a specific parent folder"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subfolders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Parent folder not found",
                    content = @Content)
    })
    @GetMapping("/{folderId}/subfolders")
    public ResponseEntity<List<FolderDTO>> getSubfolders(
            @Parameter(description = "ID of the parent folder", required = true)
            @PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getSubfolders(folderId));
    }

    @Operation(
            summary = "Get folder details",
            description = "Retrieves details of a specific folder"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FolderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content)
    })
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderDTO> getFolder(
            @Parameter(description = "ID of the folder", required = true)
            @PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getFolder(folderId));
    }

    @Operation(
            summary = "Create a new folder",
            description = "Creates a new folder for the current user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder created successfully",
                    content = @Content(schema = @Schema(implementation = FolderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid folder creation request",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(
            @Valid @RequestBody FolderCreateRequest request) {
        return ResponseEntity.ok(folderService.createFolder(request));
    }

    @Operation(
            summary = "Update folder",
            description = "Updates an existing folder's details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder updated successfully",
                    content = @Content(schema = @Schema(implementation = FolderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content)
    })
    @PutMapping("/{folderId}")
    public ResponseEntity<FolderDTO> updateFolder(
            @Parameter(description = "ID of the folder to update", required = true)
            @PathVariable UUID folderId,
            @Valid @RequestBody FolderCreateRequest request) {
        return ResponseEntity.ok(folderService.updateFolder(folderId, request));
    }

    @Operation(
            summary = "Delete folder",
            description = "Deletes a folder and its contents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content)
    })
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "ID of the folder to delete", required = true)
            @PathVariable UUID folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search folders",
            description = "Searches folders by name or description"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folders found successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<FolderDTO>> searchFolders(
            @Parameter(description = "Search term to find matching folders", required = true)
            @RequestParam String query) {
        return ResponseEntity.ok(folderService.searchFolders(query));
    }
}
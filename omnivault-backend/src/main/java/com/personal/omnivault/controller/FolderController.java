package com.personal.omnivault.controller;

import com.personal.omnivault.domain.dto.request.FolderCreateRequest;
import com.personal.omnivault.domain.dto.response.FolderDTO;
import com.personal.omnivault.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/root")
    public ResponseEntity<List<FolderDTO>> getRootFolders() {
        return ResponseEntity.ok(folderService.getRootFolders());
    }

    @GetMapping("/{folderId}/subfolders")
    public ResponseEntity<List<FolderDTO>> getSubfolders(@PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getSubfolders(folderId));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderDTO> getFolder(@PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getFolder(folderId));
    }

    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(@Valid @RequestBody FolderCreateRequest request) {
        return ResponseEntity.ok(folderService.createFolder(request));
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderDTO> updateFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody FolderCreateRequest request) {
        return ResponseEntity.ok(folderService.updateFolder(folderId, request));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FolderDTO>> searchFolders(@RequestParam String query) {
        return ResponseEntity.ok(folderService.searchFolders(query));
    }
}
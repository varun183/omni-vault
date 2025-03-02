package com.personal.omnivault.controller;

import com.personal.omnivault.domain.dto.request.ContentUpdateRequest;
import com.personal.omnivault.domain.dto.request.LinkContentCreateRequest;
import com.personal.omnivault.domain.dto.request.TextContentCreateRequest;
import com.personal.omnivault.domain.dto.response.ContentDTO;
import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getAllContent(pageable));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDTO> getContent(@PathVariable UUID contentId) {
        return ResponseEntity.ok(contentService.getContent(contentId));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<Page<ContentDTO>> getContentByFolder(
            @PathVariable UUID folderId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByFolder(folderId, pageable));
    }

    @GetMapping("/type/{contentType}")
    public ResponseEntity<Page<ContentDTO>> getContentByType(
            @PathVariable ContentType contentType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByType(contentType, pageable));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Page<ContentDTO>> getContentByTag(
            @PathVariable UUID tagId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getContentByTag(tagId, pageable));
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<ContentDTO>> getFavoriteContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getFavoriteContent(pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<ContentDTO>> getRecentContent(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.getRecentContent(pageable));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ContentDTO>> getPopularContent() {
        return ResponseEntity.ok(contentService.getPopularContent());
    }

    @PostMapping("/text")
    public ResponseEntity<ContentDTO> createTextContent(@Valid @RequestBody TextContentCreateRequest request) {
        return ResponseEntity.ok(contentService.createTextContent(request));
    }

    @PostMapping("/link")
    public ResponseEntity<ContentDTO> createLinkContent(@Valid @RequestBody LinkContentCreateRequest request) {
        return ResponseEntity.ok(contentService.createLinkContent(request));
    }

    @PostMapping("/file")
    public ResponseEntity<ContentDTO> createFileContent(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) List<UUID> tagIds,
            @RequestParam(required = false) List<String> newTags) {
        return ResponseEntity.ok(contentService.createFileContent(file, title, description, folderId, tagIds, newTags));
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentDTO> updateContent(
            @PathVariable UUID contentId,
            @Valid @RequestBody ContentUpdateRequest request) {
        return ResponseEntity.ok(contentService.updateContent(contentId, request));
    }

    @PutMapping("/{contentId}/favorite")
    public ResponseEntity<ContentDTO> toggleFavorite(@PathVariable UUID contentId) {
        return ResponseEntity.ok(contentService.toggleFavorite(contentId));
    }

    @PutMapping("/{contentId}/tags")
    public ResponseEntity<ContentDTO> updateContentTags(
            @PathVariable UUID contentId,
            @RequestParam(required = false) List<UUID> tagIds,
            @RequestParam(required = false) List<String> newTags) {
        return ResponseEntity.ok(contentService.updateContentTags(contentId, tagIds, newTags));
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{contentId}/file")
    public ResponseEntity<Resource> getContentFile(@PathVariable UUID contentId) {
        Resource resource = contentService.getContentFile(contentId);
        String contentType = contentService.getContent(contentId).getMimeType();

        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        contentService.getContent(contentId).getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{contentId}/thumbnail")
    public ResponseEntity<Resource> getContentThumbnail(@PathVariable UUID contentId) {
        Resource resource = contentService.getContentThumbnail(contentId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ContentDTO>> searchContent(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contentService.searchContent(query, pageable));
    }
}
package com.personal.omnivault.controller;

import com.personal.omnivault.domain.dto.request.TagCreateRequest;
import com.personal.omnivault.domain.dto.response.TagDTO;
import com.personal.omnivault.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagDTO> getTag(@PathVariable UUID tagId) {
        return ResponseEntity.ok(tagService.getTag(tagId));
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagCreateRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable UUID tagId,
            @Valid @RequestBody TagCreateRequest request) {
        return ResponseEntity.ok(tagService.updateTag(tagId, request));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String query) {
        return ResponseEntity.ok(tagService.searchTags(query));
    }
}
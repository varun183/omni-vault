package com.omnivault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnivault.domain.dto.request.TagCreateRequest;
import com.omnivault.domain.dto.response.TagDTO;
import com.omnivault.exception.GlobalExceptionHandler;
import com.omnivault.exception.ResourceNotFoundException;
import com.omnivault.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private ObjectMapper objectMapper;
    private UUID testTagId;
    private TagDTO tagDTO;
    private TagCreateRequest tagCreateRequest;
    private List<TagDTO> allTags;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testTagId = UUID.randomUUID();

        tagDTO = TagDTO.builder()
                .id(testTagId)
                .name("Test Tag")
                .color("#FF5733")
                .contentCount(5)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        TagDTO secondTagDTO = TagDTO.builder()
                .id(UUID.randomUUID())
                .name("Another Tag")
                .color("#33FF57")
                .contentCount(3)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        allTags = Arrays.asList(tagDTO, secondTagDTO);

        tagCreateRequest = TagCreateRequest.builder()
                .name("New Tag")
                .color("#3357FF")
                .build();
    }

    @Test
    @DisplayName("Should get all tags")
    void getAllTags_Success() throws Exception {
        // Given
        when(tagService.getAllTags()).thenReturn(allTags);

        // When & Then
        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testTagId.toString())))
                .andExpect(jsonPath("$[0].name", is("Test Tag")))
                .andExpect(jsonPath("$[0].color", is("#FF5733")))
                .andExpect(jsonPath("$[0].contentCount", is(5)))
                .andExpect(jsonPath("$[1].name", is("Another Tag")));

        verify(tagService).getAllTags();
    }

    @Test
    @DisplayName("Should get a tag by ID")
    void getTag_Success() throws Exception {
        // Given
        when(tagService.getTag(testTagId)).thenReturn(tagDTO);

        // When & Then
        mockMvc.perform(get("/tags/{tagId}", testTagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testTagId.toString())))
                .andExpect(jsonPath("$.name", is("Test Tag")))
                .andExpect(jsonPath("$.color", is("#FF5733")))
                .andExpect(jsonPath("$.contentCount", is(5)));

        verify(tagService).getTag(testTagId);
    }

    @Test
    @DisplayName("Should handle tag not found")
    void getTag_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(tagService.getTag(nonExistentId)).thenThrow(
                new ResourceNotFoundException("Tag", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/tags/{tagId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Tag not found")));

        verify(tagService).getTag(nonExistentId);
    }

    @Test
    @DisplayName("Should create a tag successfully")
    void createTag_Success() throws Exception {
        // Given
        TagDTO newTagDTO = TagDTO.builder()
                .id(UUID.randomUUID())
                .name("New Tag")
                .color("#3357FF")
                .contentCount(0)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(tagService.createTag(any(TagCreateRequest.class))).thenReturn(newTagDTO);

        // When & Then
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Tag")))
                .andExpect(jsonPath("$.color", is("#3357FF")))
                .andExpect(jsonPath("$.contentCount", is(0)));

        verify(tagService).createTag(any(TagCreateRequest.class));
    }

    @Test
    @DisplayName("Should update a tag successfully")
    void updateTag_Success() throws Exception {
        // Given
        TagCreateRequest updateRequest = TagCreateRequest.builder()
                .name("Updated Tag")
                .color("#5733FF")
                .build();

        TagDTO updatedTagDTO = TagDTO.builder()
                .id(testTagId)
                .name("Updated Tag")
                .color("#5733FF")
                .contentCount(5)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(tagService.updateTag(eq(testTagId), any(TagCreateRequest.class))).thenReturn(updatedTagDTO);

        // When & Then
        mockMvc.perform(put("/tags/{tagId}", testTagId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testTagId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Tag")))
                .andExpect(jsonPath("$.color", is("#5733FF")));

        verify(tagService).updateTag(eq(testTagId), any(TagCreateRequest.class));
    }

    @Test
    @DisplayName("Should delete a tag successfully")
    void deleteTag_Success() throws Exception {
        // Given
        doNothing().when(tagService).deleteTag(testTagId);

        // When & Then
        mockMvc.perform(delete("/tags/{tagId}", testTagId))
                .andExpect(status().isOk());

        verify(tagService).deleteTag(testTagId);
    }

    @Test
    @DisplayName("Should search tags successfully")
    void searchTags_Success() throws Exception {
        // Given
        when(tagService.searchTags("test")).thenReturn(Collections.singletonList(tagDTO));

        // When & Then
        mockMvc.perform(get("/tags/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Tag")));

        verify(tagService).searchTags("test");
    }

    @Test
    @DisplayName("Should validate tag creation request")
    void createTag_ValidationError() throws Exception {
        // Given
        TagCreateRequest invalidRequest = TagCreateRequest.builder()
                .name("") // Empty name
                .color("invalid-color") // Invalid color format
                .build();

        // When & Then
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.color").exists());

        verify(tagService, never()).createTag(any(TagCreateRequest.class));
    }
}
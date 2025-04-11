package com.omnivault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnivault.domain.dto.request.FolderCreateRequest;
import com.omnivault.domain.dto.response.FolderDTO;
import com.omnivault.exception.GlobalExceptionHandler;
import com.omnivault.exception.ResourceNotFoundException;
import com.omnivault.service.FolderService;
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
class FolderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FolderService folderService;

    @InjectMocks
    private FolderController folderController;

    private ObjectMapper objectMapper;
    private UUID testFolderId;
    private UUID parentFolderId;
    private FolderDTO rootFolderDTO;
    private FolderDTO subFolderDTO;
    private FolderCreateRequest folderCreateRequest;
    private List<FolderDTO> rootFolders;
    private List<FolderDTO> subFolders;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(folderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testFolderId = UUID.randomUUID();
        parentFolderId = UUID.randomUUID();

        rootFolderDTO = FolderDTO.builder()
                .id(parentFolderId)
                .name("Root Folder")
                .description("This is a root folder")
                .parentId(null)
                .path("/Root Folder")
                .contentCount(5)
                .subfolderCount(2)
                .subfolders(Collections.emptyList())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        subFolderDTO = FolderDTO.builder()
                .id(testFolderId)
                .name("Sub Folder")
                .description("This is a subfolder")
                .parentId(parentFolderId)
                .path("/Root Folder/Sub Folder")
                .contentCount(3)
                .subfolderCount(0)
                .subfolders(Collections.emptyList())
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        folderCreateRequest = FolderCreateRequest.builder()
                .name("New Folder")
                .description("New folder description")
                .parentId(parentFolderId)
                .build();

        rootFolders = List.of(rootFolderDTO);
        subFolders = List.of(subFolderDTO);
    }

    @Test
    @DisplayName("Should get all root folders")
    void getRootFolders_Success() throws Exception {
        // Given
        when(folderService.getRootFolders()).thenReturn(rootFolders);

        // When & Then
        mockMvc.perform(get("/folders/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(parentFolderId.toString())))
                .andExpect(jsonPath("$[0].name", is("Root Folder")))
                .andExpect(jsonPath("$[0].description", is("This is a root folder")))
                .andExpect(jsonPath("$[0].parentId").doesNotExist())
                .andExpect(jsonPath("$[0].contentCount", is(5)))
                .andExpect(jsonPath("$[0].subfolderCount", is(2)));

        verify(folderService).getRootFolders();
    }

    @Test
    @DisplayName("Should get all subfolders for a parent folder")
    void getSubfolders_Success() throws Exception {
        // Given
        when(folderService.getSubfolders(parentFolderId)).thenReturn(subFolders);

        // When & Then
        mockMvc.perform(get("/folders/{folderId}/subfolders", parentFolderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testFolderId.toString())))
                .andExpect(jsonPath("$[0].name", is("Sub Folder")))
                .andExpect(jsonPath("$[0].description", is("This is a subfolder")))
                .andExpect(jsonPath("$[0].parentId", is(parentFolderId.toString())))
                .andExpect(jsonPath("$[0].contentCount", is(3)))
                .andExpect(jsonPath("$[0].subfolderCount", is(0)));

        verify(folderService).getSubfolders(parentFolderId);
    }

    @Test
    @DisplayName("Should get a folder by ID")
    void getFolder_Success() throws Exception {
        // Given
        when(folderService.getFolder(testFolderId)).thenReturn(subFolderDTO);

        // When & Then
        mockMvc.perform(get("/folders/{folderId}", testFolderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testFolderId.toString())))
                .andExpect(jsonPath("$.name", is("Sub Folder")))
                .andExpect(jsonPath("$.description", is("This is a subfolder")))
                .andExpect(jsonPath("$.parentId", is(parentFolderId.toString())))
                .andExpect(jsonPath("$.path", is("/Root Folder/Sub Folder")))
                .andExpect(jsonPath("$.contentCount", is(3)))
                .andExpect(jsonPath("$.subfolderCount", is(0)));

        verify(folderService).getFolder(testFolderId);
    }

    @Test
    @DisplayName("Should handle folder not found")
    void getFolder_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(folderService.getFolder(nonExistentId)).thenThrow(
                new ResourceNotFoundException("Folder", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/folders/{folderId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Folder not found")));

        verify(folderService).getFolder(nonExistentId);
    }

    @Test
    @DisplayName("Should create a folder successfully")
    void createFolder_Success() throws Exception {
        // Given
        FolderDTO newFolderDTO = FolderDTO.builder()
                .id(UUID.randomUUID())
                .name("New Folder")
                .description("New folder description")
                .parentId(parentFolderId)
                .path("/Root Folder/New Folder")
                .contentCount(0)
                .subfolderCount(0)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(folderService.createFolder(any(FolderCreateRequest.class))).thenReturn(newFolderDTO);

        // When & Then
        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Folder")))
                .andExpect(jsonPath("$.description", is("New folder description")))
                .andExpect(jsonPath("$.parentId", is(parentFolderId.toString())))
                .andExpect(jsonPath("$.path", is("/Root Folder/New Folder")))
                .andExpect(jsonPath("$.contentCount", is(0)))
                .andExpect(jsonPath("$.subfolderCount", is(0)));

        verify(folderService).createFolder(any(FolderCreateRequest.class));
    }

    @Test
    @DisplayName("Should update a folder successfully")
    void updateFolder_Success() throws Exception {
        // Given
        FolderCreateRequest updateRequest = FolderCreateRequest.builder()
                .name("Updated Folder")
                .description("Updated folder description")
                .parentId(null) // Moving to root
                .build();

        FolderDTO updatedFolderDTO = FolderDTO.builder()
                .id(testFolderId)
                .name("Updated Folder")
                .description("Updated folder description")
                .parentId(null)
                .path("/Updated Folder")
                .contentCount(3)
                .subfolderCount(0)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(folderService.updateFolder(eq(testFolderId), any(FolderCreateRequest.class))).thenReturn(updatedFolderDTO);

        // When & Then
        mockMvc.perform(put("/folders/{folderId}", testFolderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testFolderId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Folder")))
                .andExpect(jsonPath("$.description", is("Updated folder description")))
                .andExpect(jsonPath("$.parentId").doesNotExist())
                .andExpect(jsonPath("$.path", is("/Updated Folder")));

        verify(folderService).updateFolder(eq(testFolderId), any(FolderCreateRequest.class));
    }

    @Test
    @DisplayName("Should delete a folder successfully")
    void deleteFolder_Success() throws Exception {
        // Given
        doNothing().when(folderService).deleteFolder(testFolderId);

        // When & Then
        mockMvc.perform(delete("/folders/{folderId}", testFolderId))
                .andExpect(status().isOk());

        verify(folderService).deleteFolder(testFolderId);
    }

    @Test
    @DisplayName("Should search folders successfully")
    void searchFolders_Success() throws Exception {
        // Given
        when(folderService.searchFolders("folder")).thenReturn(Arrays.asList(rootFolderDTO, subFolderDTO));

        // When & Then
        mockMvc.perform(get("/folders/search")
                        .param("query", "folder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Root Folder")))
                .andExpect(jsonPath("$[1].name", is("Sub Folder")));

        verify(folderService).searchFolders("folder");
    }

    @Test
    @DisplayName("Should validate folder creation request")
    void createFolder_ValidationError() throws Exception {
        // Given
        FolderCreateRequest invalidRequest = FolderCreateRequest.builder()
                .name("") // Empty name
                .build();

        // When & Then
        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verify(folderService, never()).createFolder(any(FolderCreateRequest.class));
    }
}
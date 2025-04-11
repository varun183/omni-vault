package com.omnivault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnivault.domain.dto.request.ContentUpdateRequest;
import com.omnivault.domain.dto.request.TextContentCreateRequest;
import com.omnivault.domain.dto.response.ContentDTO;
import com.omnivault.domain.dto.response.TagDTO;
import com.omnivault.domain.model.ContentType;
import com.omnivault.domain.model.StorageLocation;
import com.omnivault.exception.GlobalExceptionHandler;
import com.omnivault.exception.ResourceNotFoundException;
import com.omnivault.service.ContentService;
import com.omnivault.util.FileResponseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentController contentController;

    private ObjectMapper objectMapper;
    private UUID contentId;
    private UUID folderId;
    private UUID tagId;
    private ContentDTO contentDTO;
    private List<ContentDTO> contentList;
    private TextContentCreateRequest textContentRequest;
    private ContentUpdateRequest updateRequest;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(contentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        contentId = UUID.randomUUID();
        folderId = UUID.randomUUID();
        tagId = UUID.randomUUID();

        TagDTO tagDTO = TagDTO.builder()
                .id(tagId)
                .name("Test Tag")
                .color("#FF5733")
                .build();

        contentDTO = ContentDTO.builder()
                .id(contentId)
                .title("Test Content")
                .description("This is a test content")
                .contentType(ContentType.TEXT)
                .folderId(folderId)
                .folderName("Test Folder")
                .textContent("This is the text content body")
                .storageLocation(StorageLocation.LOCAL)
                .tags(Collections.singletonList(tagDTO))
                .favorite(false)
                .viewCount(0)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        ContentDTO secondContent = ContentDTO.builder()
                .id(UUID.randomUUID())
                .title("Another Content")
                .description("This is another test content")
                .contentType(ContentType.LINK)
                .url("https://example.com")
                .storageLocation(StorageLocation.LOCAL)
                .favorite(true)
                .viewCount(5)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        contentList = Arrays.asList(contentDTO, secondContent);

        textContentRequest = TextContentCreateRequest.builder()
                .title("New Text Content")
                .description("New text content description")
                .folderId(folderId)
                .textContent("This is new text content")
                .tagIds(Collections.singletonList(tagId))
                .newTags(Collections.singletonList("New Tag"))
                .build();

        updateRequest = ContentUpdateRequest.builder()
                .title("Updated Content")
                .description("Updated description")
                .folderId(folderId)
                .favorite(true)
                .tagIds(Collections.singletonList(tagId))
                .newTags(Collections.singletonList("New Tag"))
                .textContent("Updated text content")
                .build();
    }

    @Test
    @DisplayName("Should get all content with pagination")
    void getAllContent_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(contentService.getAllContent(any(Pageable.class)))
                .thenReturn(new PageImpl<>(contentList, pageable, contentList.size()));

        // When & Then
        mockMvc.perform(get("/contents")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(contentId.toString())))
                .andExpect(jsonPath("$.content[0].title", is("Test Content")))
                .andExpect(jsonPath("$.content[0].contentType", is("TEXT")))
                .andExpect(jsonPath("$.content[1].title", is("Another Content")))
                .andExpect(jsonPath("$.content[1].contentType", is("LINK")));

        verify(contentService).getAllContent(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get content by ID")
    void getContent_Success() throws Exception {
        // Given
        when(contentService.getContent(contentId)).thenReturn(contentDTO);

        // When & Then
        mockMvc.perform(get("/contents/{contentId}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(contentId.toString())))
                .andExpect(jsonPath("$.title", is("Test Content")))
                .andExpect(jsonPath("$.description", is("This is a test content")))
                .andExpect(jsonPath("$.contentType", is("TEXT")))
                .andExpect(jsonPath("$.folderId", is(folderId.toString())))
                .andExpect(jsonPath("$.tags[0].id", is(tagId.toString())))
                .andExpect(jsonPath("$.textContent", is("This is the text content body")));

        verify(contentService).getContent(contentId);
    }

    @Test
    @DisplayName("Should handle content not found")
    void getContent_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(contentService.getContent(nonExistentId)).thenThrow(
                new ResourceNotFoundException("Content", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/contents/{contentId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Content not found")));

        verify(contentService).getContent(nonExistentId);
    }

    @Test
    @DisplayName("Should get content by folder")
    void getContentByFolder_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(contentService.getContentByFolder(eq(folderId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(contentDTO), pageable, 1));

        // When & Then
        mockMvc.perform(get("/contents/folder/{folderId}", folderId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(contentId.toString())))
                .andExpect(jsonPath("$.content[0].folderId", is(folderId.toString())));

        verify(contentService).getContentByFolder(eq(folderId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get content by type")
    void getContentByType_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(contentService.getContentByType(eq(ContentType.TEXT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(contentDTO), pageable, 1));

        // When & Then
        mockMvc.perform(get("/contents/type/{contentType}", "TEXT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].contentType", is("TEXT")));

        verify(contentService).getContentByType(eq(ContentType.TEXT), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get content by tag")
    void getContentByTag_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(contentService.getContentByTag(eq(tagId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(contentDTO), pageable, 1));

        // When & Then
        mockMvc.perform(get("/contents/tag/{tagId}", tagId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].tags[0].id", is(tagId.toString())));

        verify(contentService).getContentByTag(eq(tagId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should create text content")
    void createTextContent_Success() throws Exception {
        // Given
        when(contentService.createTextContent(any(TextContentCreateRequest.class))).thenReturn(contentDTO);

        // When & Then
        mockMvc.perform(post("/contents/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textContentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Content")))
                .andExpect(jsonPath("$.contentType", is("TEXT")))
                .andExpect(jsonPath("$.textContent", is("This is the text content body")));

        verify(contentService).createTextContent(any(TextContentCreateRequest.class));
    }

    @Test
    @DisplayName("Should update content")
    void updateContent_Success() throws Exception {
        // Given
        ContentDTO updatedContent = contentDTO.toBuilder()
                .title("Updated Content")
                .description("Updated description")
                .favorite(true)
                .textContent("Updated text content")
                .build();

        when(contentService.updateContent(eq(contentId), any(ContentUpdateRequest.class))).thenReturn(updatedContent);

        // When & Then
        mockMvc.perform(put("/contents/{contentId}", contentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(contentId.toString())))
                .andExpect(jsonPath("$.title", is("Updated Content")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.favorite", is(true)))
                .andExpect(jsonPath("$.textContent", is("Updated text content")));

        verify(contentService).updateContent(eq(contentId), any(ContentUpdateRequest.class));
    }

    @Test
    @DisplayName("Should toggle favorite status")
    void toggleFavorite_Success() throws Exception {
        // Given
        ContentDTO favoritedContent = contentDTO.toBuilder().favorite(true).build();
        when(contentService.toggleFavorite(contentId)).thenReturn(favoritedContent);

        // When & Then
        mockMvc.perform(put("/contents/{contentId}/favorite", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(contentId.toString())))
                .andExpect(jsonPath("$.favorite", is(true)));

        verify(contentService).toggleFavorite(contentId);
    }

    @Test
    @DisplayName("Should delete content")
    void deleteContent_Success() throws Exception {
        // Given
        doNothing().when(contentService).deleteContent(contentId);

        // When & Then
        mockMvc.perform(delete("/contents/{contentId}", contentId))
                .andExpect(status().isOk());

        verify(contentService).deleteContent(contentId);
    }

    @Test
    @DisplayName("Should get content file")
    void getContentFile_Success() throws Exception {
        // Given
        Resource resource = new ByteArrayResource("test file content".getBytes());
        when(contentService.getContentFile(contentId)).thenReturn(resource);
        when(contentService.getContent(contentId)).thenReturn(contentDTO);

        // Use MockedStatic to mock the static method in FileResponseUtils
        try (MockedStatic<FileResponseUtils> mockedStatic = Mockito.mockStatic(FileResponseUtils.class)) {
            ResponseEntity<Resource> responseEntity = ResponseEntity.ok(resource);
            mockedStatic.when(() -> FileResponseUtils.createFileResponse(any(), any())).thenReturn(responseEntity);

            // When & Then
            mockMvc.perform(get("/contents/{contentId}/file", contentId))
                    .andExpect(status().isOk());

            verify(contentService).getContentFile(contentId);
            verify(contentService).getContent(contentId);
            mockedStatic.verify(() -> FileResponseUtils.createFileResponse(any(), any()));
        }
    }

    @Test
    @DisplayName("Should search content")
    void searchContent_Success() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String searchQuery = "test";
        when(contentService.searchContent(eq(searchQuery), any(Pageable.class)))
                .thenReturn(new PageImpl<>(contentList, pageable, contentList.size()));

        // When & Then
        mockMvc.perform(get("/contents/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Test Content")))
                .andExpect(jsonPath("$.content[1].title", is("Another Content")));

        verify(contentService).searchContent(eq(searchQuery), any(Pageable.class));
    }

    @Test
    @DisplayName("Should upload file content")
    void createFileContent_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test file content".getBytes());

        when(contentService.createFileContent(
                any(), any(), any(), any(), any(), any(), any())).thenReturn(contentDTO);

        // When & Then
        mockMvc.perform(multipart("/contents/file")
                        .file(file)
                        .param("title", "Test Content")
                        .param("description", "This is a test content")
                        .param("storageLocation", "LOCAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Content")));

        verify(contentService).createFileContent(
                any(), any(), any(), any(), any(), any(), any());
    }
}
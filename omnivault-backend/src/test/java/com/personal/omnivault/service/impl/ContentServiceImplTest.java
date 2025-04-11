package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.dto.request.TextContentCreateRequest;
import com.personal.omnivault.domain.dto.response.ContentDTO;
import com.personal.omnivault.domain.model.*;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.ContentRepository;
import com.personal.omnivault.repository.LinkContentRepository;
import com.personal.omnivault.repository.TextContentRepository;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.ContentEntityService;
import com.personal.omnivault.service.FolderService;
import com.personal.omnivault.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private TextContentRepository textContentRepository;

    @Mock
    private LinkContentRepository linkContentRepository;

    @Mock
    private AuthService authService;

    @Mock
    private FolderService folderService;

    @Mock
    private TagService tagService;

    @Mock
    private ContentEntityService contentEntityService;

    @Mock
    private HybridFileService fileService;

    @InjectMocks
    private ContentServiceImpl contentService;

    private User testUser;
    private Folder testFolder;
    private Content textContent;
    private Content linkContent;
    private TextContent textContentEntity;
    private Tag testTag;
    private TextContentCreateRequest textContentRequest;
    private UUID contentId;
    private UUID folderId;
    private UUID tagId;

    @BeforeEach
    void setup() {
        contentId = UUID.randomUUID();
        folderId = UUID.randomUUID();
        tagId = UUID.randomUUID();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        testFolder = Folder.builder()
                .id(folderId)
                .name("Test Folder")
                .user(testUser)
                .build();

        testTag = Tag.builder()
                .id(tagId)
                .name("Test Tag")
                .color("#FF5733")
                .user(testUser)
                .build();

        textContent = Content.builder()
                .id(contentId)
                .title("Test Text Content")
                .description("This is test text content")
                .contentType(ContentType.TEXT)
                .folder(testFolder)
                .user(testUser)
                .favorite(false)
                .viewCount(0)
                .storageLocation(StorageLocation.LOCAL)
                .tags(new HashSet<>(Collections.singletonList(testTag)))
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        textContentEntity = TextContent.builder()
                .contentId(contentId)
                .content(textContent)
                .textContent("This is the text content body")
                .build();

        linkContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Link Content")
                .description("This is test link content")
                .contentType(ContentType.LINK)
                .folder(testFolder)
                .user(testUser)
                .favorite(false)
                .viewCount(0)
                .storageLocation(StorageLocation.LOCAL)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        textContentRequest = TextContentCreateRequest.builder()
                .title("New Text Content")
                .description("New text content description")
                .folderId(folderId)
                .textContent("This is new text content")
                .tagIds(Collections.singletonList(tagId))
                .newTags(Collections.singletonList("New Tag"))
                .build();
    }

    @Test
    @DisplayName("Should get content by ID")
    void getContent() {
        // Given
        when(contentEntityService.getContentEntity(contentId)).thenReturn(textContent);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        ContentDTO result = contentService.getContent(contentId);

        // Then
        verify(contentEntityService).getContentEntity(contentId);
        verify(textContentRepository).findByContentId(contentId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(contentId);
        assertThat(result.getTitle()).isEqualTo("Test Text Content");
        assertThat(result.getDescription()).isEqualTo("This is test text content");
        assertThat(result.getContentType()).isEqualTo(ContentType.TEXT);
        assertThat(result.getFolderId()).isEqualTo(folderId);
        assertThat(result.getTextContent()).isEqualTo("This is the text content body");
        assertThat(result.getTags()).hasSize(1);
        assertThat(result.getTags().getFirst().getName()).isEqualTo("Test Tag");
    }

    @Test
    @DisplayName("Should get all content with pagination")
    void getAllContent() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Content> contentList = Arrays.asList(textContent, linkContent);
        Page<Content> contentPage = new PageImpl<>(contentList, pageable, contentList.size());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(contentRepository.findAllByUser(testUser, pageable)).thenReturn(contentPage);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        Page<ContentDTO> result = contentService.getAllContent(pageable);

        // Then
        verify(authService).getCurrentUser();
        verify(contentRepository).findAllByUser(testUser, pageable);
        verify(textContentRepository).findByContentId(contentId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Text Content");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Test Link Content");
    }

    @Test
    @DisplayName("Should get content by folder with pagination")
    void getContentByFolder() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Content> contentList = Collections.singletonList(textContent);
        Page<Content> contentPage = new PageImpl<>(contentList, pageable, contentList.size());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderService.getFolderEntity(folderId)).thenReturn(testFolder);
        when(contentRepository.findAllByUserAndFolder(testUser, testFolder, pageable)).thenReturn(contentPage);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        Page<ContentDTO> result = contentService.getContentByFolder(folderId, pageable);

        // Then
        verify(authService).getCurrentUser();
        verify(folderService).getFolderEntity(folderId);
        verify(contentRepository).findAllByUserAndFolder(testUser, testFolder, pageable);
        verify(textContentRepository).findByContentId(contentId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Text Content");
        assertThat(result.getContent().getFirst().getFolderId()).isEqualTo(folderId);
    }

    @Test
    @DisplayName("Should get content by tag with pagination")
    void getContentByTag() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Content> contentList = Collections.singletonList(textContent);
        Page<Content> contentPage = new PageImpl<>(contentList, pageable, contentList.size());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(tagService.getTagEntity(tagId)).thenReturn(testTag);
        when(contentRepository.findAllByUserAndTagId(testUser, tagId, pageable)).thenReturn(contentPage);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        Page<ContentDTO> result = contentService.getContentByTag(tagId, pageable);

        // Then
        verify(authService).getCurrentUser();
        verify(tagService).getTagEntity(tagId);
        verify(contentRepository).findAllByUserAndTagId(testUser, tagId, pageable);
        verify(textContentRepository).findByContentId(contentId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Text Content");
        assertThat(result.getContent().getFirst().getTags()).hasSize(1);
        assertThat(result.getContent().getFirst().getTags().getFirst().getId()).isEqualTo(tagId);
    }

    @Test
    @DisplayName("Should create text content")
    void createTextContent() {
        // Given
        Set<Tag> existingTags = Collections.singleton(testTag);
        Set<Tag> newTags = Collections.singleton(Tag.builder()
                .id(UUID.randomUUID())
                .name("New Tag")
                .color("#33FF57")
                .user(testUser)
                .build());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderService.getFolderEntity(folderId)).thenReturn(testFolder);
        when(contentRepository.save(any(Content.class))).thenReturn(textContent);
        when(textContentRepository.save(any(TextContent.class))).thenReturn(textContentEntity);
        when(tagService.getTagsByIds(Collections.singletonList(tagId))).thenReturn(existingTags);
        when(tagService.findOrCreateTags(Collections.singletonList("New Tag"))).thenReturn(newTags);

        // When
        ContentDTO result = contentService.createTextContent(textContentRequest);

        // Then
        verify(authService).getCurrentUser();
        verify(folderService).getFolderEntity(folderId);

        ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
        verify(contentRepository, times(2)).save(contentCaptor.capture());

        Content capturedContent = contentCaptor.getAllValues().getFirst();
        assertThat(capturedContent.getTitle()).isEqualTo("New Text Content");
        assertThat(capturedContent.getDescription()).isEqualTo("New text content description");
        assertThat(capturedContent.getContentType()).isEqualTo(ContentType.TEXT);
        assertThat(capturedContent.getFolder()).isEqualTo(testFolder);
        assertThat(capturedContent.getUser()).isEqualTo(testUser);

        ArgumentCaptor<TextContent> textContentCaptor = ArgumentCaptor.forClass(TextContent.class);
        verify(textContentRepository).save(textContentCaptor.capture());

        TextContent capturedTextContent = textContentCaptor.getValue();
        assertThat(capturedTextContent.getTextContent()).isEqualTo("This is new text content");

        verify(tagService).getTagsByIds(eq(Collections.singletonList(tagId)));
        verify(tagService).findOrCreateTags(eq(Collections.singletonList("New Tag")));

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Text Content");
        assertThat(result.getContentType()).isEqualTo(ContentType.TEXT);
    }

    @Test
    @DisplayName("Should toggle favorite status")
    void toggleFavorite() {
        // Given
        Content unfavorited = textContent.toBuilder().favorite(false).build();
        Content favorited = textContent.toBuilder().favorite(true).build();

        when(contentEntityService.getContentEntity(contentId)).thenReturn(unfavorited);
        when(contentRepository.save(any(Content.class))).thenReturn(favorited);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        ContentDTO result = contentService.toggleFavorite(contentId);

        // Then
        verify(contentEntityService).getContentEntity(contentId);

        ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
        verify(contentRepository).save(contentCaptor.capture());

        Content capturedContent = contentCaptor.getValue();
        assertThat(capturedContent.isFavorite()).isTrue();

        assertThat(result).isNotNull();
        assertThat(result.isFavorite()).isTrue();
    }

    @Test
    @DisplayName("Should delete content")
    void deleteContent() {
        // Given
        when(contentEntityService.getContentEntity(contentId)).thenReturn(textContent);
        doNothing().when(contentRepository).delete(any(Content.class));

        // When
        contentService.deleteContent(contentId);

        // Then
        verify(contentEntityService).getContentEntity(contentId);
        verify(contentRepository).delete(textContent);
    }

    @Test
    @DisplayName("Should search content")
    void searchContent() {
        // Given
        Pageable pageable = Pageable.unpaged();
        List<Content> contentList = Collections.singletonList(textContent);
        Page<Content> contentPage = new PageImpl<>(contentList, pageable, contentList.size());
        String searchTerm = "test";

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(contentRepository.fullTextSearchContents(testUser.getId(), searchTerm, pageable)).thenReturn(contentPage);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        Page<ContentDTO> result = contentService.searchContent(searchTerm, pageable);

        // Then
        verify(authService).getCurrentUser();
        verify(contentRepository).fullTextSearchContents(testUser.getId(), searchTerm, pageable);
        verify(textContentRepository).findByContentId(contentId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Text Content");
    }

    @Test
    @DisplayName("Should update content tags")
    void updateContentTags() {
        // Given
        List<UUID> newTagIds = Collections.singletonList(UUID.randomUUID());
        List<String> newTagNames = Collections.singletonList("Another Tag");
        Set<Tag> existingTags = new HashSet<>();
        Set<Tag> newTags = Collections.singleton(Tag.builder()
                .id(UUID.randomUUID())
                .name("Another Tag")
                .color("#33FF57")
                .user(testUser)
                .build());

        when(contentEntityService.getContentEntity(contentId)).thenReturn(textContent);
        when(tagService.getTagsByIds(newTagIds)).thenReturn(existingTags);
        when(tagService.findOrCreateTags(newTagNames)).thenReturn(newTags);
        when(contentRepository.save(any(Content.class))).thenReturn(textContent);
        when(textContentRepository.findByContentId(contentId)).thenReturn(Optional.of(textContentEntity));

        // When
        ContentDTO result = contentService.updateContentTags(contentId, newTagIds, newTagNames);

        // Then
        verify(contentEntityService).getContentEntity(contentId);
        verify(tagService).getTagsByIds(newTagIds);
        verify(tagService).findOrCreateTags(newTagNames);

        ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
        verify(contentRepository).save(contentCaptor.capture());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(contentId);
    }

    @Test
    @DisplayName("Should handle non-existent content")
    void getContent_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(contentEntityService.getContentEntity(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Content", "id", nonExistentId));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> contentService.getContent(nonExistentId));

        verify(contentEntityService).getContentEntity(nonExistentId);
        verify(textContentRepository, never()).findByContentId(any());
    }

    @Test
    @DisplayName("Should increment view count")
    void incrementViewCount() {
        // Given
        Content updatedContent = textContent.toBuilder().viewCount(1).build();

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(textContent));
        when(contentRepository.save(any(Content.class))).thenReturn(updatedContent);

        // When
        contentService.incrementViewCount(contentId);

        // Then
        verify(contentRepository).findById(contentId);

        ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
        verify(contentRepository).save(contentCaptor.capture());

        Content capturedContent = contentCaptor.getValue();
        assertThat(capturedContent.getViewCount()).isEqualTo(1);
    }
}
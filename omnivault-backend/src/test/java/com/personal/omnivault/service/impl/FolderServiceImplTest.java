package com.personal.omnivault.service.impl;

import com.personal.omnivault.domain.dto.request.FolderCreateRequest;
import com.personal.omnivault.domain.dto.response.FolderDTO;
import com.personal.omnivault.domain.model.Folder;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.FolderRepository;
import com.personal.omnivault.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceImplTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private FolderServiceImpl folderService;

    private User testUser;
    private Folder rootFolder;
    private Folder subFolder;
    private FolderCreateRequest createRequest;
    private FolderCreateRequest subFolderRequest;

    @BeforeEach
    void setup() {
        UUID userId = UUID.randomUUID();
        UUID rootFolderId = UUID.randomUUID();
        UUID subFolderId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        rootFolder = Folder.builder()
                .id(rootFolderId)
                .name("Root Folder")
                .description("This is a root folder")
                .parent(null)
                .user(testUser)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        subFolder = Folder.builder()
                .id(subFolderId)
                .name("Sub Folder")
                .description("This is a subfolder")
                .parent(rootFolder)
                .user(testUser)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        createRequest = FolderCreateRequest.builder()
                .name("New Folder")
                .description("New folder description")
                .parentId(null)
                .build();

        subFolderRequest = FolderCreateRequest.builder()
                .name("New Sub Folder")
                .description("New subfolder description")
                .parentId(rootFolderId)
                .build();
    }

    @Test
    @DisplayName("Should get all root folders")
    void getRootFolders() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.findAllByUserAndParentIsNull(testUser)).thenReturn(List.of(rootFolder));
        when(folderRepository.countContentsByFolderId(rootFolder.getId())).thenReturn(5);
        when(folderRepository.countSubfoldersByFolderId(rootFolder.getId())).thenReturn(2);

        // When
        List<FolderDTO> result = folderService.getRootFolders();

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).findAllByUserAndParentIsNull(testUser);
        verify(folderRepository).countContentsByFolderId(rootFolder.getId());
        verify(folderRepository).countSubfoldersByFolderId(rootFolder.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(rootFolder.getId());
        assertThat(result.getFirst().getName()).isEqualTo(rootFolder.getName());
        assertThat(result.getFirst().getDescription()).isEqualTo(rootFolder.getDescription());
        assertThat(result.getFirst().getParentId()).isNull();
        assertThat(result.getFirst().getContentCount()).isEqualTo(5);
        assertThat(result.getFirst().getSubfolderCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get all subfolders for a parent folder")
    void getSubfolders() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.findByIdAndUser(rootFolder.getId(), testUser)).thenReturn(Optional.of(rootFolder));
        when(folderRepository.findAllByUserAndParentId(testUser, rootFolder.getId())).thenReturn(List.of(subFolder));
        when(folderRepository.countContentsByFolderId(subFolder.getId())).thenReturn(3);
        when(folderRepository.countSubfoldersByFolderId(subFolder.getId())).thenReturn(0);

        // When
        List<FolderDTO> result = folderService.getSubfolders(rootFolder.getId());

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).findByIdAndUser(rootFolder.getId(), testUser);
        verify(folderRepository).findAllByUserAndParentId(testUser, rootFolder.getId());
        verify(folderRepository).countContentsByFolderId(subFolder.getId());
        verify(folderRepository).countSubfoldersByFolderId(subFolder.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(subFolder.getId());
        assertThat(result.getFirst().getName()).isEqualTo(subFolder.getName());
        assertThat(result.getFirst().getDescription()).isEqualTo(subFolder.getDescription());
        assertThat(result.getFirst().getParentId()).isEqualTo(rootFolder.getId());
        assertThat(result.getFirst().getContentCount()).isEqualTo(3);
        assertThat(result.getFirst().getSubfolderCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should get a folder by ID")
    void getFolder() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.findByIdAndUser(rootFolder.getId(), testUser)).thenReturn(Optional.of(rootFolder));
        when(folderRepository.countContentsByFolderId(rootFolder.getId())).thenReturn(5);
        when(folderRepository.countSubfoldersByFolderId(rootFolder.getId())).thenReturn(2);

        // When
        FolderDTO result = folderService.getFolder(rootFolder.getId());

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).findByIdAndUser(rootFolder.getId(), testUser);
        verify(folderRepository).countContentsByFolderId(rootFolder.getId());
        verify(folderRepository).countSubfoldersByFolderId(rootFolder.getId());

        assertThat(result.getId()).isEqualTo(rootFolder.getId());
        assertThat(result.getName()).isEqualTo(rootFolder.getName());
        assertThat(result.getDescription()).isEqualTo(rootFolder.getDescription());
        assertThat(result.getParentId()).isNull();
        assertThat(result.getContentCount()).isEqualTo(5);
        assertThat(result.getSubfolderCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw exception when folder not found")
    void getFolder_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.findByIdAndUser(nonExistentId, testUser)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> folderService.getFolder(nonExistentId));

        assertThat(exception.getMessage()).contains("Folder not found");
    }

    @Test
    @DisplayName("Should create a root folder successfully")
    void createFolder_Root() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.existsByNameAndParentIsNullAndUser("New Folder", testUser)).thenReturn(false);

        Folder newFolder = Folder.builder()
                .id(UUID.randomUUID())
                .name("New Folder")
                .description("New folder description")
                .user(testUser)
                .parent(null)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(folderRepository.save(any(Folder.class))).thenReturn(newFolder);
        when(folderRepository.countContentsByFolderId(newFolder.getId())).thenReturn(0);
        when(folderRepository.countSubfoldersByFolderId(newFolder.getId())).thenReturn(0);

        // When
        FolderDTO result = folderService.createFolder(createRequest);

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).existsByNameAndParentIsNullAndUser("New Folder", testUser);

        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(folderCaptor.capture());
        Folder capturedFolder = folderCaptor.getValue();
        assertThat(capturedFolder.getName()).isEqualTo("New Folder");
        assertThat(capturedFolder.getDescription()).isEqualTo("New folder description");
        assertThat(capturedFolder.getUser()).isEqualTo(testUser);
        assertThat(capturedFolder.getParent()).isNull();

        assertThat(result.getName()).isEqualTo(newFolder.getName());
        assertThat(result.getDescription()).isEqualTo(newFolder.getDescription());
        assertThat(result.getParentId()).isNull();
    }

    @Test
    @DisplayName("Should create a subfolder successfully")
    void createFolder_SubFolder() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.existsByNameAndParentIdAndUser("New Sub Folder", rootFolder.getId(), testUser)).thenReturn(false);
        when(folderRepository.findByIdAndUser(rootFolder.getId(), testUser)).thenReturn(Optional.of(rootFolder));

        Folder newSubFolder = Folder.builder()
                .id(UUID.randomUUID())
                .name("New Sub Folder")
                .description("New subfolder description")
                .user(testUser)
                .parent(rootFolder)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        when(folderRepository.save(any(Folder.class))).thenReturn(newSubFolder);
        when(folderRepository.countContentsByFolderId(newSubFolder.getId())).thenReturn(0);
        when(folderRepository.countSubfoldersByFolderId(newSubFolder.getId())).thenReturn(0);

        // When
        FolderDTO result = folderService.createFolder(subFolderRequest);

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).existsByNameAndParentIdAndUser("New Sub Folder", rootFolder.getId(), testUser);
        verify(folderRepository).findByIdAndUser(rootFolder.getId(), testUser);

        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(folderCaptor.capture());
        Folder capturedFolder = folderCaptor.getValue();
        assertThat(capturedFolder.getName()).isEqualTo("New Sub Folder");
        assertThat(capturedFolder.getDescription()).isEqualTo("New subfolder description");
        assertThat(capturedFolder.getUser()).isEqualTo(testUser);
        assertThat(capturedFolder.getParent()).isEqualTo(rootFolder);

        assertThat(result.getName()).isEqualTo(newSubFolder.getName());
        assertThat(result.getDescription()).isEqualTo(newSubFolder.getDescription());
        assertThat(result.getParentId()).isEqualTo(rootFolder.getId());
    }

    @Test
    @DisplayName("Should throw exception when folder name already exists at root level")
    void createFolder_RootNameExists() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.existsByNameAndParentIsNullAndUser("New Folder", testUser)).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> folderService.createFolder(createRequest));

        assertThat(exception.getMessage()).contains("A folder with this name already exists at the root level");
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    @DisplayName("Should throw exception when subfolder name already exists in parent folder")
    void createFolder_SubFolderNameExists() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.existsByNameAndParentIdAndUser("New Sub Folder", rootFolder.getId(), testUser)).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> folderService.createFolder(subFolderRequest));

        assertThat(exception.getMessage()).contains("A folder with this name already exists in the parent folder");
        verify(folderRepository, never()).findByIdAndUser(any(), any());
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    @DisplayName("Should throw exception when parent folder not found")
    void createFolder_ParentNotFound() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.existsByNameAndParentIdAndUser("New Sub Folder", rootFolder.getId(), testUser)).thenReturn(false);
        when(folderRepository.findByIdAndUser(rootFolder.getId(), testUser)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> folderService.createFolder(subFolderRequest));

        assertThat(exception.getMessage()).contains("Parent folder not found");
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    @DisplayName("Should search folders successfully")
    void searchFolders() {
        // Given
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(folderRepository.searchFolders(eq(testUser), eq("folder"))).thenReturn(Arrays.asList(rootFolder, subFolder));
        when(folderRepository.countContentsByFolderId(any(UUID.class))).thenReturn(3);
        when(folderRepository.countSubfoldersByFolderId(any(UUID.class))).thenReturn(1);

        // When
        List<FolderDTO> result = folderService.searchFolders("folder");

        // Then
        verify(authService).getCurrentUser();
        verify(folderRepository).searchFolders(testUser, "folder");
        verify(folderRepository, times(2)).countContentsByFolderId(any(UUID.class));
        verify(folderRepository, times(2)).countSubfoldersByFolderId(any(UUID.class));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Root Folder", "Sub Folder");
    }
}
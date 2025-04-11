package com.personal.omnivault.repository;

import com.personal.omnivault.config.TestJpaConfig;
import com.personal.omnivault.domain.model.Content;
import com.personal.omnivault.domain.model.ContentType;
import com.personal.omnivault.domain.model.Folder;
import com.personal.omnivault.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class FolderRepositoryTest {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentRepository contentRepository;

    private User testUser;
    private Folder rootFolder;
    private Folder subFolder;

    @BeforeEach
    void setup() {
        folderRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .emailVerified(true)
                .enabled(true)
                .build();

        testUser = userRepository.save(testUser);

        // Create root folder
        rootFolder = Folder.builder()
                .name("Root Folder")
                .description("This is a root folder")
                .parent(null)
                .user(testUser)
                .build();

        rootFolder = folderRepository.save(rootFolder);

        // Create subfolder
        subFolder = Folder.builder()
                .name("Sub Folder")
                .description("This is a subfolder")
                .parent(rootFolder)
                .user(testUser)
                .build();

        subFolder = folderRepository.save(subFolder);

        // Create some content in the subfolder
        Content content = Content.builder()
                .title("Test Content")
                .description("Test Content Description")
                .contentType(ContentType.TEXT)
                .folder(subFolder)
                .user(testUser)
                .build();

        contentRepository.save(content);
    }

    @Test
    @DisplayName("Should find all root folders for a user")
    void findAllByUserAndParentIsNull() {
        // When
        List<Folder> rootFolders = folderRepository.findAllByUserAndParentIsNull(testUser);

        // Then
        assertThat(rootFolders).hasSize(1);
        assertThat(rootFolders.getFirst().getName()).isEqualTo("Root Folder");
        assertThat(rootFolders.getFirst().getParent()).isNull();
    }

    @Test
    @DisplayName("Should find all subfolders for a parent folder")
    void findAllByUserAndParentId() {
        // When
        List<Folder> subFolders = folderRepository.findAllByUserAndParentId(testUser, rootFolder.getId());

        // Then
        assertThat(subFolders).hasSize(1);
        assertThat(subFolders.getFirst().getName()).isEqualTo("Sub Folder");
        assertThat(subFolders.getFirst().getParent().getId()).isEqualTo(rootFolder.getId());
    }

    @Test
    @DisplayName("Should find folder by ID and user")
    void findByIdAndUser() {
        // When
        Optional<Folder> foundFolder = folderRepository.findByIdAndUser(rootFolder.getId(), testUser);

        // Then
        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getName()).isEqualTo("Root Folder");
    }

    @Test
    @DisplayName("Should check if folder exists by name, parent, and user")
    void existsByNameAndParentIdAndUser() {
        // When
        boolean exists = folderRepository.existsByNameAndParentIdAndUser("Sub Folder", rootFolder.getId(), testUser);
        boolean doesNotExist = folderRepository.existsByNameAndParentIdAndUser("Nonexistent Folder", rootFolder.getId(), testUser);

        // Then
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    @DisplayName("Should check if root folder exists by name and user")
    void existsByNameAndParentIsNullAndUser() {
        // When
        boolean exists = folderRepository.existsByNameAndParentIsNullAndUser("Root Folder", testUser);
        boolean doesNotExist = folderRepository.existsByNameAndParentIsNullAndUser("Nonexistent Folder", testUser);

        // Then
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    @DisplayName("Should search folders by name or description")
    void searchFolders() {
        // When
        List<Folder> foldersByName = folderRepository.searchFolders(testUser, "Root");
        List<Folder> foldersByDescription = folderRepository.searchFolders(testUser, "subfolder");

        // Then
        assertThat(foldersByName).hasSize(1);
        assertThat(foldersByName.getFirst().getName()).isEqualTo("Root Folder");

        assertThat(foldersByDescription).hasSize(1);
        assertThat(foldersByDescription.getFirst().getName()).isEqualTo("Sub Folder");
    }

    @Test
    @DisplayName("Should count contents by folder ID")
    void countContentsByFolderId() {
        // When
        int count = folderRepository.countContentsByFolderId(subFolder.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count subfolders by parent folder ID")
    void countSubfoldersByFolderId() {
        // When
        int count = folderRepository.countSubfoldersByFolderId(rootFolder.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }
}
package com.omnivault.repository;

import com.omnivault.config.TestJpaConfig;
import com.omnivault.domain.model.Content;
import com.omnivault.domain.model.ContentType;
import com.omnivault.domain.model.Tag;
import com.omnivault.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentRepository contentRepository;

    private User testUser;
    private Tag workTag;
    private Tag personalTag;
    private Content content;

    @BeforeEach
    void setup() {
        tagRepository.deleteAll();
        contentRepository.deleteAll();
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

        // Create tags
        workTag = Tag.builder()
                .name("Work")
                .color("#FF5733")
                .user(testUser)
                .build();

        personalTag = Tag.builder()
                .name("Personal")
                .color("#33FF57")
                .user(testUser)
                .build();

        workTag = tagRepository.save(workTag);
        personalTag = tagRepository.save(personalTag);

        // Create content with tag
        content = Content.builder()
                .title("Test Content")
                .description("Test Content Description")
                .contentType(ContentType.TEXT)
                .user(testUser)
                .build();

        content = contentRepository.save(content);

        // Add tag to content
        content.addTag(workTag);
        content = contentRepository.save(content);
    }

    @Test
    @DisplayName("Should find all tags for a user")
    void findAllByUser() {
        // When
        List<Tag> tags = tagRepository.findAllByUser(testUser);

        // Then
        assertThat(tags).hasSize(2);
        assertThat(tags).extracting("name").containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    @DisplayName("Should find tag by ID and user")
    void findByIdAndUser() {
        // When
        Optional<Tag> foundTag = tagRepository.findByIdAndUser(workTag.getId(), testUser);

        // Then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("Work");
    }

    @Test
    @DisplayName("Should find tag by name and user")
    void findByNameAndUser() {
        // When
        Optional<Tag> foundTag = tagRepository.findByNameAndUser("Work", testUser);

        // Then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getColor()).isEqualTo("#FF5733");
    }

    @Test
    @DisplayName("Should find tags by names and user")
    void findByNameInAndUser() {
        // When
        Set<Tag> foundTags = tagRepository.findByNameInAndUser(Arrays.asList("Work", "Personal"), testUser);

        // Then
        assertThat(foundTags).hasSize(2);
        assertThat(foundTags).extracting("name").containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    @DisplayName("Should find tags by IDs and user")
    void findByIdInAndUser() {
        // When
        Set<Tag> foundTags = tagRepository.findByIdInAndUser(Arrays.asList(workTag.getId(), personalTag.getId()), testUser);

        // Then
        assertThat(foundTags).hasSize(2);
        assertThat(foundTags).extracting("name").containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    @DisplayName("Should check if tag exists by name and user")
    void existsByNameAndUser() {
        // When
        boolean exists = tagRepository.existsByNameAndUser("Work", testUser);
        boolean doesNotExist = tagRepository.existsByNameAndUser("Nonexistent", testUser);

        // Then
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    @DisplayName("Should search tags by name")
    void searchTags() {
        // When
        List<Tag> foundTags = tagRepository.searchTags(testUser, "per");

        // Then
        assertThat(foundTags).hasSize(1);
        assertThat(foundTags.getFirst().getName()).isEqualTo("Personal");
    }

    @Test
    @DisplayName("Should count contents by tag ID")
    void countContentsByTagId() {
        // When
        int workTagCount = tagRepository.countContentsByTagId(workTag.getId());
        int personalTagCount = tagRepository.countContentsByTagId(personalTag.getId());

        // Then
        assertThat(workTagCount).isEqualTo(1);
        assertThat(personalTagCount).isEqualTo(0);
    }
}
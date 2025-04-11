package com.omnivault.repository;

import com.omnivault.config.TestJpaConfig;
import com.omnivault.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .emailVerified(true)
                .enabled(true)
                .build();

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by username or email")
    void findByUsernameOrEmail() {
        // When
        Optional<User> foundByUsername = userRepository.findByUsernameOrEmail("testuser", "testuser");
        Optional<User> foundByEmail = userRepository.findByUsernameOrEmail("test@example.com", "test@example.com");

        // Then
        assertThat(foundByUsername).isPresent();
        assertThat(foundByEmail).isPresent();
        assertThat(foundByUsername.get().getUsername()).isEqualTo(foundByEmail.get().getUsername());
    }

    @Test
    @DisplayName("Should check if username exists")
    void existsByUsername() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        boolean doesNotExist = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean doesNotExist = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }
}
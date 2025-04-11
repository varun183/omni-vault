package com.omnivault.repository;

import com.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username The unique username to search for
     * @return An Optional containing the User if found, otherwise empty
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email The unique email address to search for
     * @return An Optional containing the User if found, otherwise empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by either their username or email address.
     * This method allows authentication using either username or email.
     *
     * @param username The username to search for
     * @param email The email address to search for
     * @return An Optional containing the User if found, otherwise empty
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check for existence
     * @return true if the username is already taken, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email address already exists in the database.
     *
     * @param email The email address to check for existence
     * @return true if the email is already registered, false otherwise
     */
    boolean existsByEmail(String email);
}
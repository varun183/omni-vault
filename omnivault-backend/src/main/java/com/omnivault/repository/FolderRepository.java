package com.omnivault.repository;

import com.omnivault.domain.model.Folder;
import com.omnivault.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    /**
     * Retrieves all root-level folders (folders without a parent) for a specific user.
     *
     * @param user The user whose root folders are being retrieved
     * @return A list of root-level folders for the user
     */
    List<Folder> findAllByUserAndParentIsNull(User user);

    /**
     * Retrieves all subfolders of a specific parent folder for a given user.
     *
     * @param user The user who owns the folders
     * @param parentId The unique identifier of the parent folder
     * @return A list of subfolders within the specified parent folder
     */
    List<Folder> findAllByUserAndParentId(User user, UUID parentId);

    /**
     * Finds a specific folder by its ID and associated user.
     *
     * @param id The unique identifier of the folder
     * @param user The user who owns the folder
     * @return An Optional containing the folder if found, otherwise empty
     */
    Optional<Folder> findByIdAndUser(UUID id, User user);

    /**
     * Checks if a folder with the given name already exists within a specific parent folder for a user.
     *
     * @param name The name of the folder to check
     * @param parentId The unique identifier of the parent folder
     * @param user The user who owns the folder
     * @return true if a folder with the name exists in the specified parent folder, false otherwise
     */
    boolean existsByNameAndParentIdAndUser(String name, UUID parentId, User user);

    /**
     * Checks if a root-level folder with the given name already exists for a user.
     *
     * @param name The name of the folder to check
     * @param user The user who owns the folder
     * @return true if a root-level folder with the name exists, false otherwise
     */
    boolean existsByNameAndParentIsNullAndUser(String name, User user);

    /**
     * Searches for folders belonging to a user that match the given search term.
     * The search is case-insensitive and matches against folder name or description.
     *
     * @param user The user whose folders are being searched
     * @param searchTerm The term to search for in folder names and descriptions
     * @return A list of folders matching the search criteria
     */
    @Query("SELECT f FROM Folder f WHERE f.user = ?1 AND (LOWER(f.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(f.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    List<Folder> searchFolders(User user, String searchTerm);

    /**
     * Counts the number of content items within a specific folder.
     *
     * @param folderId The unique identifier of the folder
     * @return The total number of content items in the folder
     */
    @Query("SELECT COUNT(c) FROM Content c WHERE c.folder.id = ?1")
    int countContentsByFolderId(UUID folderId);

    /**
     * Counts the number of subfolders within a specific folder.
     *
     * @param folderId The unique identifier of the parent folder
     * @return The total number of subfolders
     */
    @Query("SELECT COUNT(f) FROM Folder f WHERE f.parent.id = ?1")
    int countSubfoldersByFolderId(UUID folderId);

    /**
     * Deletes all folders associated with a specific user.
     *
     * @param user The user whose folders are to be deleted
     */
    void deleteByUser(User user);
}
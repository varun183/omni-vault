package com.omnivault.service;

import com.omnivault.domain.dto.request.FolderCreateRequest;
import com.omnivault.domain.dto.response.FolderDTO;
import com.omnivault.domain.model.Folder;

import java.util.List;
import java.util.UUID;

public interface FolderService {

    /**
     * Get all root folders for the current user
     *
     * @return List of root folders
     */
    List<FolderDTO> getRootFolders();

    /**
     * Get all subfolders of a folder
     *
     * @param folderId The parent folder ID
     * @return List of subfolders
     */
    List<FolderDTO> getSubfolders(UUID folderId);

    /**
     * Get folder by ID
     *
     * @param folderId The folder ID
     * @return The folder DTO
     */
    FolderDTO getFolder(UUID folderId);

    /**
     * Get folder entity by ID
     *
     * @param folderId The folder ID
     * @return The folder entity
     */
    Folder getFolderEntity(UUID folderId);

    /**
     * Create a new folder
     *
     * @param request The folder creation request
     * @return The created folder DTO
     */
    FolderDTO createFolder(FolderCreateRequest request);

    /**
     * Update a folder
     *
     * @param folderId The folder ID
     * @param request The folder update request
     * @return The updated folder DTO
     */
    FolderDTO updateFolder(UUID folderId, FolderCreateRequest request);

    /**
     * Delete a folder
     *
     * @param folderId The folder ID
     */
    void deleteFolder(UUID folderId);

    /**
     * Search folders by name or description
     *
     * @param searchTerm The search term
     * @return List of matching folders
     */
    List<FolderDTO> searchFolders(String searchTerm);
}
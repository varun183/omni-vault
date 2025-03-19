// src/services/fileService.js
import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";

/**
 * Service for handling file-related operations
 */
const fileService = {
  /**
   * Fetches a file with authentication header and returns a blob URL
   * @param {string} contentId - The ID of the content to fetch
   * @returns {Promise<string>} A blob URL to the file
   */
  fetchFileWithAuth: async (contentId) => {
    const cacheKey = `file_${contentId}`;

    // Try cache first
    const cachedUrl = apiCache.get(cacheKey);
    if (cachedUrl) {
      return cachedUrl;
    }

    try {
      const response = await axiosInstance.get(`/contents/${contentId}/file`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(response.data);

      // Cache for a short time (2 minutes)
      apiCache.set(cacheKey, url, 2 * 60 * 1000);

      return url;
    } catch (error) {
      console.error("Error fetching file:", error);
      return null;
    }
  },

  /**
   * Fetches a thumbnail with authentication header and returns a blob URL
   * @param {string} contentId - The ID of the content to fetch thumbnail for
   * @returns {Promise<string>} A blob URL to the thumbnail
   */
  fetchThumbnailWithAuth: async (contentId) => {
    const cacheKey = `thumbnail_${contentId}`;

    // Try cache first
    const cachedUrl = apiCache.get(cacheKey);
    if (cachedUrl) {
      return cachedUrl;
    }

    try {
      const response = await axiosInstance.get(
        `/contents/${contentId}/thumbnail`,
        {
          responseType: "blob",
        }
      );
      const url = URL.createObjectURL(response.data);

      // Cache thumbnails longer (5 minutes)
      apiCache.set(cacheKey, url, 5 * 60 * 1000);

      return url;
    } catch (error) {
      console.error("Error fetching thumbnail:", error);
      return null;
    }
  },

  /**
   * Downloads a file with proper filename
   * @param {string} contentId - The ID of the content to download
   * @param {string} filename - The filename to use for download
   * @returns {Promise<void>}
   */
  downloadFile: async (contentId, filename) => {
    try {
      const response = await axiosInstance.get(`/contents/${contentId}/file`, {
        responseType: "blob",
      });

      // Create a blob URL and trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename || "download");
      document.body.appendChild(link);
      link.click();
      link.remove();

      // Clean up
      setTimeout(() => {
        window.URL.revokeObjectURL(url);
      }, 100);
    } catch (error) {
      console.error("Error downloading file:", error);
    }
  },

  /**
   * Gets a direct file URL (useful for iframes)
   * @param {string} contentId - The ID of the content
   * @returns {string} URL with auth token appended
   */
  getFileUrl: (contentId) => {
    const baseUrl = `${axiosInstance.defaults.baseURL}/contents/${contentId}/file`;
    const token = localStorage.getItem("access_token");
    return token ? `${baseUrl}?token=${token}` : baseUrl;
  },

  /**
   * Gets a direct thumbnail URL
   * @param {string} contentId - The ID of the content
   * @returns {string} URL with auth token appended
   */
  getThumbnailUrl: (contentId) => {
    const baseUrl = `${axiosInstance.defaults.baseURL}/contents/${contentId}/thumbnail`;
    const token = localStorage.getItem("access_token");
    return token ? `${baseUrl}?token=${token}` : baseUrl;
  },

  /**
   * Fetches multiple thumbnails in parallel and returns a map of URLs
   * @param {Array<string>} contentIds - Array of content IDs
   * @returns {Promise<Object>} Map of content IDs to blob URLs
   */
  fetchBatchThumbnails: async (contentIds) => {
    if (!contentIds || contentIds.length === 0) return {};

    // Create cache key for this batch
    const cacheKey = `thumbnails_batch_${contentIds.sort().join("_")}`;

    // Try cache first
    const cachedResults = apiCache.get(cacheKey);
    if (cachedResults) return cachedResults;

    // If not in cache, fetch individually
    const results = {};
    await Promise.all(
      contentIds.map(async (id) => {
        try {
          const url = await fileService.fetchThumbnailWithAuth(id);
          if (url) {
            results[id] = url;
          }
        } catch (error) {
          console.log(`Error fetching thumbnail for ${id}:`, error.message);
        }
      })
    );

    // Cache the batch result - shorter TTL for thumbnails
    apiCache.set(cacheKey, results, 2 * 60 * 1000); // 2 minutes
    return results;
  },
};

export default fileService;

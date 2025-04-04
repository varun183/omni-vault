// src/services/fileService.js
import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";
import contentService from "./contentService";

/**
 * Service for handling file-related operations
 */
const fileService = {
  /**
   * Fetches a file with authentication header and returns a blob URL or presigned URL
   * @param {string} contentId - The ID of the content to fetch
   * @param {object} options - Optional parameters
   * @returns {Promise<string>} A URL to the file (blob or presigned)
   */
  fetchFileWithAuth: async (contentId, options = {}) => {
    const cacheKey = `file_${contentId}`;

    // Try cache first
    const cachedUrl = apiCache.get(cacheKey);
    if (cachedUrl) {
      return cachedUrl;
    }

    try {
      // First check if a cloud URL is available for this content
      if (options.checkCloud !== false) {
        try {
          const response = await axiosInstance.get(
            `/contents/${contentId}/cloud-url`
          );
          if (response.data && response.data.url) {
            const presignedUrl = response.data.url;
            // Cache for a short time
            apiCache.set(cacheKey, presignedUrl, 10 * 60 * 1000); // 10 minutes
            return presignedUrl;
          }
        } catch (err) {
          // If not cloud storage or error, fall back to direct fetch
          // This is normal for local storage, so we just continue silently
          console.log("fetchFilewithAuth error", err);
        }
      }

      // Proceed with regular file fetch for local storage
      const response = await axiosInstance.get(`/contents/${contentId}/file`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(response.data);

      // Cache for a short time
      apiCache.set(cacheKey, url, 2 * 60 * 1000); // 2 minutes

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
      // First check if the content has a thumbnail
      const content = await contentService.getContent(contentId);
      if (!content.thumbnailPath) {
        return null; // No thumbnail available
      }

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

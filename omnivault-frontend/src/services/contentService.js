import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";

const contentService = {
  getAllContent: async (page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents?page=${page}&size=${size}&sort=createdAt,desc`
    );
    return response.data;
  },

  getContent: async (contentId) => {
    const cacheKey = `content_${contentId}`;

    // Try to get from cache first and check if it's fresh (less than 5 minutes old)
    const cachedContent = apiCache.get(cacheKey);
    if (cachedContent && cachedContent._timestamp) {
      const now = Date.now();
      const ageInMinutes = (now - cachedContent._timestamp) / (1000 * 60);

      // Use cache if it's less than 5 minutes old
      if (ageInMinutes < 5) {
        return cachedContent;
      }
    }

    // If not in cache or stale, fetch from API
    const response = await axiosInstance.get(`/contents/${contentId}`);

    // Add timestamp to the response data
    const responseWithTimestamp = {
      ...response.data,
      _timestamp: Date.now(),
    };

    // Store in cache before returning
    apiCache.set(cacheKey, responseWithTimestamp);
    return responseWithTimestamp;
  },

  getContentByType: async (contentType, page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/type/${contentType}?page=${page}&size=${size}`
    );
    return response.data;
  },

  getContentByTag: async (tagId, page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/tag/${tagId}?page=${page}&size=${size}`
    );
    return response.data;
  },

  getFavorites: async (page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/favorites?page=${page}&size=${size}`
    );
    return response.data;
  },

  getRecent: async (page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/recent?page=${page}&size=${size}`
    );
    return response.data;
  },

  getPopular: async () => {
    const response = await axiosInstance.get("/contents/popular");
    return response.data;
  },

  createTextContent: async (textContentData) => {
    const response = await axiosInstance.post(
      "/contents/text",
      textContentData
    );
    return response.data;
  },

  createLinkContent: async (linkContentData) => {
    const response = await axiosInstance.post(
      "/contents/link",
      linkContentData
    );
    return response.data;
  },

  uploadFile: async (file, title, description, folderId, tagIds, newTags) => {
    const formData = new FormData();
    formData.append("file", file);

    if (title) formData.append("title", title);
    if (description) formData.append("description", description);
    if (folderId) formData.append("folderId", folderId);

    if (tagIds && tagIds.length > 0) {
      tagIds.forEach((tagId) => formData.append("tagIds", tagId));
    }

    if (newTags && newTags.length > 0) {
      newTags.forEach((tag) => formData.append("newTags", tag));
    }

    const response = await axiosInstance.post("/contents/file", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return response.data;
  },

  updateContent: async (contentId, contentData) => {
    // Invalidate cache for this content
    apiCache.remove(`content_${contentId}`);
    // Also clear any thumbnail caches that might include this content
    apiCache.clear(`thumbnails_`);

    const response = await axiosInstance.put(
      `/contents/${contentId}`,
      contentData
    );
    return response.data;
  },

  toggleFavorite: async (contentId) => {
    const response = await axiosInstance.put(`/contents/${contentId}/favorite`);
    return response.data;
  },

  deleteContent: async (contentId) => {
    await axiosInstance.delete(`/contents/${contentId}`);
  },

  searchContent: async (query, page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/search?query=${query}&page=${page}&size=${size}`
    );
    return response.data;
  },

  getFileUrl: (contentId) => {
    const baseUrl = `${axiosInstance.defaults.baseURL}/contents/${contentId}/file`;
    const token = localStorage.getItem("access_token");
    // Return URL with auth token for direct browser access (like in iframes)
    return token ? `${baseUrl}?token=${token}` : baseUrl;
  },

  getThumbnailUrl: (contentId) => {
    const baseUrl = `${axiosInstance.defaults.baseURL}/contents/${contentId}/thumbnail`;
    const token = localStorage.getItem("access_token");
    // Return URL with auth token for direct browser access
    return token ? `${baseUrl}?token=${token}` : baseUrl;
  },

  fetchBatchThumbnails: async (contentIds) => {
    if (!contentIds || contentIds.length === 0) return {};

    // Create cache key for this batch
    const cacheKey = `thumbnails_${contentIds.sort().join("_")}`;

    // Try cache first
    const cachedResults = apiCache.get(cacheKey);
    if (cachedResults) return cachedResults;

    // If not in cache, fetch individually
    const results = {};
    await Promise.all(
      contentIds.map(async (id) => {
        try {
          // First check if the content item has a thumbnailPath
          const contentResponse = await axiosInstance.get(`/contents/${id}`);
          const contentItem = contentResponse.data;

          // Only try to fetch thumbnail if it actually has one
          if (contentItem.thumbnailPath) {
            try {
              const response = await axiosInstance.get(
                `/contents/${id}/thumbnail`,
                {
                  responseType: "blob",
                }
              );
              results[id] = URL.createObjectURL(response.data);
            } catch (thumbnailError) {
              console.log(`Thumbnail not available for ${id}`);
              // Don't store an error in results, just skip this one
            }
          } else {
            console.log(`Content ${id} has no thumbnail path`);
          }
        } catch (error) {
          console.log(
            `Error fetching content or thumbnail for ${id}:`,
            error.message
          );
        }
      })
    );

    // Cache the batch result - shorter TTL for thumbnails
    apiCache.set(cacheKey, results, 2 * 60 * 1000); // 2 minutes
    return results;
  },

  // These methods remain the same for programmatic access
  fetchFileWithAuth: async (contentId) => {
    try {
      const response = await axiosInstance.get(`/contents/${contentId}/file`, {
        responseType: "blob",
      });
      return URL.createObjectURL(response.data);
    } catch (error) {
      console.error("Error fetching file:", error);
      return null;
    }
  },

  // Method to check if a document can be viewed in browser
  canViewDocumentInBrowser: (mimeType) => {
    if (!mimeType) return false;

    const viewableTypes = [
      "application/pdf",
      "text/plain",
      "text/html",
      "text/csv",
      "text/markdown",
      "image/svg+xml",
    ];

    return viewableTypes.includes(mimeType) || mimeType.startsWith("text/");
  },

  fetchThumbnailWithAuth: async (contentId) => {
    try {
      const response = await axiosInstance.get(
        `/contents/${contentId}/thumbnail`,
        {
          responseType: "blob",
        }
      );
      return URL.createObjectURL(response.data);
    } catch (error) {
      console.error("Error fetching thumbnail:", error);
      return null;
    }
  },

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
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Error downloading file:", error);
    }
  },
};

export default contentService;

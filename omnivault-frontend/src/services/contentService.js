import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";
import fileService from "./fileService";

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
  getFolderContent: async (folderId, page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/folder/${folderId}?page=${page}&size=${size}`
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
    return fileService.getFileUrl(contentId);
  },

  getThumbnailUrl: (contentId) => {
    return fileService.getThumbnailUrl(contentId);
  },

  fetchBatchThumbnails: async (contentIds) => {
    return fileService.fetchBatchThumbnails(contentIds);
  },

  // These methods remain the same for programmatic access
  fetchFileWithAuth: async (contentId) => {
    return fileService.fetchFileWithAuth(contentId);
  },

  fetchThumbnailWithAuth: async (contentId) => {
    return fileService.fetchThumbnailWithAuth(contentId);
  },

  downloadFile: async (contentId, filename) => {
    return fileService.downloadFile(contentId, filename);
  },
};

export default contentService;

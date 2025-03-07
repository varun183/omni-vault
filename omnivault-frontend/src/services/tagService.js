import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";

const tagService = {
  getAllTags: async () => {
    const response = await axiosInstance.get("/tags");
    return response.data;
  },

  getTag: async (tagId) => {
    const cacheKey = `tag_${tagId}`;

    // Try to get from cache first
    const cachedTag = apiCache.get(cacheKey);
    if (cachedTag) {
      return cachedTag;
    }

    // Not in cache, fetch from API
    const response = await axiosInstance.get(`/tags/${tagId}`);

    // Store in cache before returning
    apiCache.set(cacheKey, response.data);
    return response.data;
  },

  createTag: async (tagData) => {
    const response = await axiosInstance.post("/tags", tagData);
    return response.data;
  },

  updateTag: async (tagId, tagData) => {
    const response = await axiosInstance.put(`/tags/${tagId}`, tagData);
    return response.data;
  },

  deleteTag: async (tagId) => {
    await axiosInstance.delete(`/tags/${tagId}`);
  },

  searchTags: async (query) => {
    const response = await axiosInstance.get(`/tags/search?query=${query}`);
    return response.data;
  },
};

export default tagService;

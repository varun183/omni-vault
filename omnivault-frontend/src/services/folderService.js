import axiosInstance from "./axiosInstance";
import { apiCache } from "../utils/apiCache";

const folderService = {
  getRootFolders: async () => {
    const response = await axiosInstance.get("/folders/root");
    return response.data;
  },

  getFolder: async (folderId) => {
    const cacheKey = `folder_${folderId}`;

    // Try to get from cache first
    const cachedFolder = apiCache.get(cacheKey);
    if (cachedFolder) {
      return cachedFolder;
    }

    // Not in cache, fetch from API
    const response = await axiosInstance.get(`/folders/${folderId}`);

    // Store in cache before returning
    apiCache.set(cacheKey, response.data);
    return response.data;
  },

  getSubfolders: async (folderId) => {
    const response = await axiosInstance.get(`/folders/${folderId}/subfolders`);
    return response.data;
  },

  createFolder: async (folderData) => {
    const response = await axiosInstance.post("/folders", folderData);
    return response.data;
  },

  updateFolder: async (folderId, folderData) => {
    const response = await axiosInstance.put(
      `/folders/${folderId}`,
      folderData
    );
    return response.data;
  },

  deleteFolder: async (folderId) => {
    await axiosInstance.delete(`/folders/${folderId}`);
  },

  searchFolders: async (query) => {
    const response = await axiosInstance.get(`/folders/search?query=${query}`);
    return response.data;
  },
};

export default folderService;

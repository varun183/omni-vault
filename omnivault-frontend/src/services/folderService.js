import axiosInstance from "./axiosInstance";

const folderService = {
  getRootFolders: async () => {
    const response = await axiosInstance.get("/folders/root");
    return response.data;
  },

  getFolder: async (folderId) => {
    const response = await axiosInstance.get(`/folders/${folderId}`);
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

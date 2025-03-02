import axiosInstance from "./axiosInstance";

const tagService = {
  getAllTags: async () => {
    const response = await axiosInstance.get("/tags");
    return response.data;
  },

  getTag: async (tagId) => {
    const response = await axiosInstance.get(`/tags/${tagId}`);
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

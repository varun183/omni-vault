import axiosInstance from "./axiosInstance";

const contentService = {
  getAllContent: async (page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents?page=${page}&size=${size}&sort=createdAt,desc`
    );
    return response.data;
  },

  getContent: async (contentId) => {
    const response = await axiosInstance.get(`/contents/${contentId}`);
    return response.data;
  },

  getFolderContent: async (folderId, page = 0, size = 10) => {
    const response = await axiosInstance.get(
      `/contents/folder/${folderId}?page=${page}&size=${size}`
    );
    return response.data;
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
    return `${axiosInstance.defaults.baseURL}/contents/${contentId}/file`;
  },

  getThumbnailUrl: (contentId) => {
    return `${axiosInstance.defaults.baseURL}/contents/${contentId}/thumbnail`;
  },
};

export default contentService;

import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import contentService from "../../services/contentService";

export const getAllContent = createAsyncThunk(
  "content/getAllContent",
  async ({ page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getAllContent(page, size);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to get content"
      );
    }
  }
);

export const getContent = createAsyncThunk(
  "content/getContent",
  async (contentId, { rejectWithValue }) => {
    try {
      return await contentService.getContent(contentId);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to get content"
      );
    }
  }
);

export const getFolderContent = createAsyncThunk(
  "content/getFolderContent",
  async ({ folderId, page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getFolderContent(folderId, page, size);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to get folder content"
      );
    }
  }
);

export const createTextContent = createAsyncThunk(
  "content/createTextContent",
  async (contentData, { rejectWithValue }) => {
    try {
      return await contentService.createTextContent(contentData);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to create text content"
      );
    }
  }
);

export const createLinkContent = createAsyncThunk(
  "content/createLinkContent",
  async (contentData, { rejectWithValue }) => {
    try {
      return await contentService.createLinkContent(contentData);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to create link content"
      );
    }
  }
);

export const uploadFile = createAsyncThunk(
  "content/uploadFile",
  async (
    { file, title, description, folderId, tagIds, newTags },
    { rejectWithValue }
  ) => {
    try {
      return await contentService.uploadFile(
        file,
        title,
        description,
        folderId,
        tagIds,
        newTags
      );
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to upload file"
      );
    }
  }
);

export const updateContent = createAsyncThunk(
  "content/updateContent",
  async ({ contentId, contentData }, { rejectWithValue }) => {
    try {
      return await contentService.updateContent(contentId, contentData);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to update content"
      );
    }
  }
);

export const toggleFavorite = createAsyncThunk(
  "content/toggleFavorite",
  async (contentId, { rejectWithValue }) => {
    try {
      return await contentService.toggleFavorite(contentId);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to toggle favorite"
      );
    }
  }
);

export const deleteContent = createAsyncThunk(
  "content/deleteContent",
  async (contentId, { rejectWithValue }) => {
    try {
      await contentService.deleteContent(contentId);
      return contentId;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to delete content"
      );
    }
  }
);

export const searchContent = createAsyncThunk(
  "content/searchContent",
  async ({ query, page, size }, { rejectWithValue }) => {
    try {
      return await contentService.searchContent(query, page, size);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to search content"
      );
    }
  }
);

const initialState = {
  contents: [],
  currentContent: null,
  totalElements: 0,
  totalPages: 0,
  loading: false,
  error: null,
  searchResults: null,
};

const contentSlice = createSlice({
  name: "content",
  initialState,
  reducers: {
    clearContentError: (state) => {
      state.error = null;
    },
    clearCurrentContent: (state) => {
      state.currentContent = null;
    },
    clearSearchResults: (state) => {
      state.searchResults = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Get All Content
      .addCase(getAllContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getAllContent.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getAllContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Content
      .addCase(getContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getContent.fulfilled, (state, action) => {
        state.loading = false;
        state.currentContent = action.payload;
      })
      .addCase(getContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Folder Content
      .addCase(getFolderContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getFolderContent.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getFolderContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Create Content
      .addCase(createTextContent.fulfilled, (state, action) => {
        state.contents.unshift(action.payload);
      })
      .addCase(createLinkContent.fulfilled, (state, action) => {
        state.contents.unshift(action.payload);
      })
      .addCase(uploadFile.fulfilled, (state, action) => {
        state.contents.unshift(action.payload);
      })

      // Update Content
      .addCase(updateContent.fulfilled, (state, action) => {
        const updated = action.payload;

        if (state.currentContent && state.currentContent.id === updated.id) {
          state.currentContent = updated;
        }

        const index = state.contents.findIndex((c) => c.id === updated.id);
        if (index !== -1) {
          state.contents[index] = updated;
        }
      })

      // Toggle Favorite
      .addCase(toggleFavorite.fulfilled, (state, action) => {
        const updated = action.payload;

        if (state.currentContent && state.currentContent.id === updated.id) {
          state.currentContent = updated;
        }

        const index = state.contents.findIndex((c) => c.id === updated.id);
        if (index !== -1) {
          state.contents[index] = updated;
        }
      })

      // Delete Content
      .addCase(deleteContent.fulfilled, (state, action) => {
        const deletedId = action.payload;

        if (state.currentContent && state.currentContent.id === deletedId) {
          state.currentContent = null;
        }

        state.contents = state.contents.filter((c) => c.id !== deletedId);
      })

      // Search Content
      .addCase(searchContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchContent.fulfilled, (state, action) => {
        state.loading = false;
        state.searchResults = action.payload;
      })
      .addCase(searchContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearContentError, clearCurrentContent, clearSearchResults } =
  contentSlice.actions;
export default contentSlice.reducer;

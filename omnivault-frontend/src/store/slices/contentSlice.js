import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import contentService from "../../services/contentService";
import { apiCache } from "../../utils/apiCache";
import logger from "../../services/loggerService";

const handleAsyncError = (
  error,
  rejectWithValue,
  customMessage,
  context = {}
) => {
  logger.error(customMessage, error, context);
  return rejectWithValue(error.response?.data?.message || customMessage);
};

export const getAllContent = createAsyncThunk(
  "content/getAllContent",
  async ({ page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getAllContent(page, size);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to get content", {
        page,
        size,
      });
    }
  }
);

export const getContent = createAsyncThunk(
  "content/getContent",
  async (contentId, { rejectWithValue }) => {
    try {
      return await contentService.getContent(contentId);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to get content", {
        contentId,
      });
    }
  }
);

export const getFolderContent = createAsyncThunk(
  "content/getFolderContent",
  async ({ folderId, page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getFolderContent(folderId, page, size);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to get folder content",
        { folderId, page, size }
      );
    }
  }
);

export const getContentByType = createAsyncThunk(
  "content/getContentByType",
  async ({ contentType, page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getContentByType(contentType, page, size);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        `Failed to get ${contentType} content`,
        { contentType, page, size }
      );
    }
  }
);

export const getContentByTag = createAsyncThunk(
  "content/getContentByTag",
  async ({ tagId, page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getContentByTag(tagId, page, size);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to get content for tag",
        { tagId, page, size }
      );
    }
  }
);

export const getFavoriteContent = createAsyncThunk(
  "content/getFavoriteContent",
  async ({ page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getFavorites(page, size);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to get favorite content",
        { page, size }
      );
    }
  }
);

export const getRecentContent = createAsyncThunk(
  "content/getRecentContent",
  async ({ page, size }, { rejectWithValue }) => {
    try {
      return await contentService.getRecent(page, size);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to get recent content",
        { page, size }
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
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to create text content",
        { contentData }
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
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to create link content",
        { contentData }
      );
    }
  }
);

export const uploadFile = createAsyncThunk(
  "content/uploadFile",
  async (
    { file, title, description, folderId, tagIds, newTags, storageLocation },
    { rejectWithValue }
  ) => {
    try {
      return await contentService.uploadFile(
        file,
        title,
        description,
        folderId,
        tagIds,
        newTags,
        storageLocation
      );
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to upload file", {
        title,
        description,
        folderId,
        tagIds,
        storageLocation,
      });
    }
  }
);

export const updateContent = createAsyncThunk(
  "content/updateContent",
  async ({ contentId, contentData }, { rejectWithValue }) => {
    try {
      apiCache.remove(`content_${contentId}`);
      apiCache.clear("thumbnails_");
      return await contentService.updateContent(contentId, contentData);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to update content",
        { contentId, contentData }
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
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to toggle favorite",
        { contentId }
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
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to delete content",
        { contentId }
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
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to search content",
        { query, page, size }
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
  contentMap: {},
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
      });
    builder
      .addCase(getAllContent.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;

        // Add to content map
        action.payload.content.forEach((item) => {
          state.contentMap[item.id] = item;
        });
      })
      .addCase(getAllContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Content
      .addCase(getContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      });
    builder
      .addCase(getContent.fulfilled, (state, action) => {
        state.loading = false;
        state.currentContent = action.payload;
        state.contentMap[action.payload.id] = action.payload;
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

      // Get Content By Type
      .addCase(getContentByType.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getContentByType.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getContentByType.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Content By Tag
      .addCase(getContentByTag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getContentByTag.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getContentByTag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Favorite Content
      .addCase(getFavoriteContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getFavoriteContent.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getFavoriteContent.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Recent Content
      .addCase(getRecentContent.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getRecentContent.fulfilled, (state, action) => {
        state.loading = false;
        state.contents = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(getRecentContent.rejected, (state, action) => {
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

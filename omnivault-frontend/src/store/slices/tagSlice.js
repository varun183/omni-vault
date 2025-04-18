import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import tagService from "../../services/tagService";
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

export const getAllTags = createAsyncThunk(
  "tags/getAllTags",
  async (_, { rejectWithValue }) => {
    try {
      return await tagService.getAllTags();
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to get tags");
    }
  }
);

export const getTag = createAsyncThunk(
  "tags/getTag",
  async (tagId, { rejectWithValue }) => {
    try {
      return await tagService.getTag(tagId);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to get tag", {
        tagId,
      });
    }
  }
);

export const createTag = createAsyncThunk(
  "tags/createTag",
  async (tagData, { rejectWithValue }) => {
    try {
      return await tagService.createTag(tagData);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to create tag", {
        tagData,
      });
    }
  }
);

export const updateTag = createAsyncThunk(
  "tags/updateTag",
  async ({ tagId, tagData }, { rejectWithValue }) => {
    try {
      return await tagService.updateTag(tagId, tagData);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to update tag", {
        tagId,
        tagData,
      });
    }
  }
);

export const deleteTag = createAsyncThunk(
  "tags/deleteTag",
  async (tagId, { rejectWithValue }) => {
    try {
      await tagService.deleteTag(tagId);
      return tagId;
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to delete tag", {
        tagId,
      });
    }
  }
);

export const searchTags = createAsyncThunk(
  "tags/searchTags",
  async (query, { rejectWithValue }) => {
    try {
      return await tagService.searchTags(query);
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to search tags", {
        query,
      });
    }
  }
);

const initialState = {
  tags: [],
  currentTag: null,
  loading: false,
  error: null,
  searchResults: [],
};

const tagSlice = createSlice({
  name: "tags",
  initialState,
  reducers: {
    clearTagError: (state) => {
      state.error = null;
    },
    clearTagSearchResults: (state) => {
      state.searchResults = [];
    },
    clearCurrentTag: (state) => {
      state.currentTag = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Get All Tags
      .addCase(getAllTags.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getAllTags.fulfilled, (state, action) => {
        state.loading = false;
        state.tags = action.payload;
      })
      .addCase(getAllTags.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        logger.error("Failed to load all tags", action.payload);
      })

      // Get Tag
      .addCase(getTag.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getTag.fulfilled, (state, action) => {
        state.loading = false;
        state.currentTag = action.payload;
      })
      .addCase(getTag.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        logger.error("Failed to load tag", action.payload);
      })

      // Create Tag
      .addCase(createTag.fulfilled, (state, action) => {
        state.tags.push(action.payload);
      })

      // Update Tag
      .addCase(updateTag.fulfilled, (state, action) => {
        const index = state.tags.findIndex((t) => t.id === action.payload.id);
        if (index !== -1) {
          state.tags[index] = action.payload;
        }

        if (state.currentTag && state.currentTag.id === action.payload.id) {
          state.currentTag = action.payload;
        }
      })

      // Delete Tag
      .addCase(deleteTag.fulfilled, (state, action) => {
        state.tags = state.tags.filter((t) => t.id !== action.payload);

        if (state.currentTag && state.currentTag.id === action.payload) {
          state.currentTag = null;
        }
      })

      // Search Tags
      .addCase(searchTags.pending, (state) => {
        state.loading = true;
      })
      .addCase(searchTags.fulfilled, (state, action) => {
        state.loading = false;
        state.searchResults = action.payload;
      })
      .addCase(searchTags.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        logger.error("Failed to search tags", action.payload);
      });
  },
});

export const { clearTagError, clearTagSearchResults, clearCurrentTag } =
  tagSlice.actions;
export default tagSlice.reducer;

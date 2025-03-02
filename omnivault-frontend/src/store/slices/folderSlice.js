import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import folderService from "../../services/folderService";

export const getRootFolders = createAsyncThunk(
  "folders/getRootFolders",
  async (_, { rejectWithValue }) => {
    try {
      return await folderService.getRootFolders();
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to get folders"
      );
    }
  }
);

export const getFolder = createAsyncThunk(
  "folders/getFolder",
  async (folderId, { rejectWithValue }) => {
    try {
      return await folderService.getFolder(folderId);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to get folder"
      );
    }
  }
);

export const createFolder = createAsyncThunk(
  "folders/createFolder",
  async (folderData, { rejectWithValue }) => {
    try {
      return await folderService.createFolder(folderData);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to create folder"
      );
    }
  }
);

export const updateFolder = createAsyncThunk(
  "folders/updateFolder",
  async ({ folderId, folderData }, { rejectWithValue }) => {
    try {
      return await folderService.updateFolder(folderId, folderData);
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to update folder"
      );
    }
  }
);

export const deleteFolder = createAsyncThunk(
  "folders/deleteFolder",
  async (folderId, { rejectWithValue }) => {
    try {
      await folderService.deleteFolder(folderId);
      return folderId;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "Failed to delete folder"
      );
    }
  }
);

const initialState = {
  rootFolders: [],
  currentFolder: null,
  loading: false,
  error: null,
};

const folderSlice = createSlice({
  name: "folders",
  initialState,
  reducers: {
    clearFolderError: (state) => {
      state.error = null;
    },
    clearCurrentFolder: (state) => {
      state.currentFolder = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Get Root Folders
      .addCase(getRootFolders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getRootFolders.fulfilled, (state, action) => {
        state.loading = false;
        state.rootFolders = action.payload;
      })
      .addCase(getRootFolders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Folder
      .addCase(getFolder.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getFolder.fulfilled, (state, action) => {
        state.loading = false;
        state.currentFolder = action.payload;
      })
      .addCase(getFolder.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Create Folder
      .addCase(createFolder.fulfilled, (state, action) => {
        if (action.payload.parentId === null) {
          state.rootFolders.push(action.payload);
        } else if (
          state.currentFolder &&
          state.currentFolder.id === action.payload.parentId
        ) {
          state.currentFolder.subfolders = state.currentFolder.subfolders || [];
          state.currentFolder.subfolders.push(action.payload);
        }
      })

      // Update Folder
      .addCase(updateFolder.fulfilled, (state, action) => {
        const updated = action.payload;

        if (state.currentFolder && state.currentFolder.id === updated.id) {
          state.currentFolder = updated;
        }

        if (updated.parentId === null) {
          const index = state.rootFolders.findIndex((f) => f.id === updated.id);
          if (index !== -1) {
            state.rootFolders[index] = updated;
          }
        }
      })

      // Delete Folder
      .addCase(deleteFolder.fulfilled, (state, action) => {
        const deletedId = action.payload;

        if (state.currentFolder && state.currentFolder.id === deletedId) {
          state.currentFolder = null;
        }

        state.rootFolders = state.rootFolders.filter((f) => f.id !== deletedId);

        if (state.currentFolder && state.currentFolder.subfolders) {
          state.currentFolder.subfolders =
            state.currentFolder.subfolders.filter((f) => f.id !== deletedId);
        }
      });
  },
});

export const { clearFolderError, clearCurrentFolder } = folderSlice.actions;
export default folderSlice.reducer;

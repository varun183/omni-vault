import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import folderReducer from "./slices/folderSlice";
import contentReducer from "./slices/contentSlice";
import tagReducer from "./slices/tagSlice";

const store = configureStore({
  reducer: {
    auth: authReducer,
    folders: folderReducer,
    content: contentReducer,
    tags: tagReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ["content/uploadFile/fulfilled"],
        ignoredActionPaths: ["meta.arg.file"],
      },
    }),
});

export default store;

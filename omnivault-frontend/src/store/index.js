import { configureStore, combineReducers } from "@reduxjs/toolkit";
import authReducer, { logout } from "./slices/authSlice";
import folderReducer from "./slices/folderSlice";
import contentReducer from "./slices/contentSlice";
import tagReducer from "./slices/tagSlice";

const rootReducer = combineReducers({
  auth: authReducer,
  folders: folderReducer,
  content: contentReducer,
  tags: tagReducer,
});

const appReducer = (state, action) => {
  if (action.type === logout.fulfilled.type) {
    return {
      ...state,
      auth: authReducer(undefined, { type: "RESET" }),
      folders: folderReducer(undefined, { type: "RESET" }),
      content: contentReducer(undefined, { type: "RESET" }),
      tags: tagReducer(undefined, { type: "RESET" }),
    };
  }

  return rootReducer(state, action);
};

const store = configureStore({
  reducer: appReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ["content/uploadFile/fulfilled"],
        ignoredActionPaths: ["meta.arg.file"],
      },
    }),
});

export default store;

import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { login, getCurrentUser } from "../../store/slices/authSlice";
import {
  getRootFolders,
  getFolder,
  createFolder,
} from "../../store/slices/folderSlice";
import {
  getAllContent,
  getFolderContent,
  searchContent,
} from "../../store/slices/contentSlice";
import { getAllTags, searchTags } from "../../store/slices/tagSlice";

const ReduxTester = () => {
  const dispatch = useDispatch();

  const auth = useSelector((state) => state.auth);
  const folders = useSelector((state) => state.folders);
  const content = useSelector((state) => state.content);
  const tags = useSelector((state) => state.tags);

  const mockLogin = () => {
    const usernameOrEmail = prompt("Enter username or email:");
    const password = prompt("Enter password:");

    if (usernameOrEmail && password) {
      dispatch(login({ usernameOrEmail, password }));
    }
  };

  const testAuth = () => {
    console.log("Testing Auth...");
    dispatch(getCurrentUser());
  };

  const testFolders = () => {
    console.log("Testing Folders...");
    dispatch(getRootFolders());
  };

  const testContent = () => {
    console.log("Testing Content...");
    dispatch(getAllContent({ page: 0, size: 10 }));
  };

  const testTags = () => {
    console.log("Testing Tags...");
    dispatch(getAllTags());
  };

  const testSpecificFolder = () => {
    const folderId = prompt("Enter folder ID to test:");
    if (folderId) {
      dispatch(getFolder(folderId));
    }
  };

  const testFolderContent = () => {
    const folderId = prompt("Enter folder ID to get content:");
    if (folderId) {
      dispatch(getFolderContent({ folderId, page: 0, size: 10 }));
    }
  };

  const testSearch = () => {
    const query = prompt("Enter search query:");
    if (query) {
      dispatch(searchContent({ query, page: 0, size: 10 }));
      dispatch(searchTags(query));
    }
  };

  const testCreateFolder = () => {
    const name = prompt("Enter folder name:");
    if (name) {
      dispatch(
        createFolder({
          name,
          parentId: null,
          description: "Test folder created via Redux test",
        })
      );
    }
  };

  return (
    <div style={{ padding: "20px", maxWidth: "1200px", margin: "0 auto" }}>
      <h1>Redux Tester</h1>

      <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
        <button onClick={mockLogin}>Mock Login</button>
        <button onClick={testAuth}>Test Auth</button>
        <button onClick={testFolders}>Test Folders</button>
        <button onClick={testContent}>Test Content</button>
        <button onClick={testTags}>Test Tags</button>
        <button onClick={testSpecificFolder}>Test Specific Folder</button>
        <button onClick={testFolderContent}>Test Folder Content</button>
        <button onClick={testSearch}>Test Search</button>
        <button onClick={testCreateFolder}>Test Create Folder</button>
      </div>

      <div
        style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "20px" }}
      >
        <div>
          <h2>Current State</h2>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "10px",
            }}
          >
            <div>
              <h3>Auth</h3>
              <pre
                style={{
                  background: "#f0f0f0",
                  padding: "10px",
                  overflow: "auto",
                  maxHeight: "200px",
                }}
              >
                {JSON.stringify(auth, null, 2)}
              </pre>
            </div>
            <div>
              <h3>Folders</h3>
              <pre
                style={{
                  background: "#f0f0f0",
                  padding: "10px",
                  overflow: "auto",
                  maxHeight: "200px",
                }}
              >
                {JSON.stringify(folders, null, 2)}
              </pre>
            </div>
            <div>
              <h3>Content</h3>
              <pre
                style={{
                  background: "#f0f0f0",
                  padding: "10px",
                  overflow: "auto",
                  maxHeight: "200px",
                }}
              >
                {JSON.stringify(content, null, 2)}
              </pre>
            </div>
            <div>
              <h3>Tags</h3>
              <pre
                style={{
                  background: "#f0f0f0",
                  padding: "10px",
                  overflow: "auto",
                  maxHeight: "200px",
                }}
              >
                {JSON.stringify(tags, null, 2)}
              </pre>
            </div>
          </div>
        </div>
        <div>
          <h2>Instructions</h2>
          <ol>
            <li>Click "Test Auth" to verify user authentication</li>
            <li>Click "Test Folders" to get root folders</li>
            <li>Click "Test Content" to get all content</li>
            <li>Click "Test Tags" to get all tags</li>
            <li>
              Use "Test Specific Folder" to fetch details for a specific folder
            </li>
            <li>
              Use "Test Folder Content" to get content within a specific folder
            </li>
            <li>Use "Test Search" to test search functionality</li>
            <li>Use "Test Create Folder" to create a new root folder</li>
          </ol>
          <p>Also check Redux DevTools for detailed action flow.</p>
        </div>
      </div>
    </div>
  );
};

export default ReduxTester;

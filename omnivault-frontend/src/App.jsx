import React, { useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { getCurrentUser } from "./store/slices/authSlice";

import ConnectivityStatus from "./components/common/ConnectivityStatus";
import connectivityService from "./services/connectivityService";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import HomePage from "./pages/HomePage";
import ContentDetailPage from "./pages/ContentDetailPage";
import FolderPage from "./pages/FolderPage";
import ContentTypePage from "./pages/ContentTypePage";
import TagPage from "./pages/TagPage";
import SearchPage from "./pages/SearchPage";
import FavoritesPage from "./pages/FavoritesPage";
import RecentPage from "./pages/RecentPage";
import NotFoundPage from "./pages/NotFoundPage";
import TagsPage from "./pages/TagsPage";
import EmailVerificationPage from "./pages/EmailVerificationPage";

import "./connectivity.css";

const App = () => {
  const dispatch = useDispatch();
  const { isAuthenticated } = useSelector((state) => state.auth);

  useEffect(() => {
    if (localStorage.getItem("access_token")) {
      dispatch(getCurrentUser());
    }
  }, [dispatch]);

  // Set up event-driven connectivity checks
  useEffect(() => {
    // Force a connectivity check when the page becomes visible again
    // This helps with scenarios where the user's device goes to sleep and wakes up
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        console.log("Page is now visible, checking connectivity status");
        connectivityService.forceCheck();
      }
    };

    // Set up the visibility change listener
    document.addEventListener("visibilitychange", handleVisibilityChange);

    // Clean up on component unmount
    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      connectivityService.destroy();
    };
  }, []);

  return (
    <BrowserRouter>
      <ConnectivityStatus />

      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route
          path="/"
          element={isAuthenticated ? <HomePage /> : <Navigate to="/login" />}
        />
        <Route path="/content/:contentId" element={<ContentDetailPage />} />
        <Route path="/folder/:folderId" element={<FolderPage />} />
        <Route
          path="/content-type/:contentType"
          element={<ContentTypePage />}
        />
        <Route path="/tags" element={<TagsPage />} />
        <Route path="/tag/:tagId" element={<TagPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/favorites" element={<FavoritesPage />} />
        <Route path="/recent" element={<RecentPage />} />
        <Route path="/verify-email" element={<EmailVerificationPage />} />

        <Route path="*" element={<NotFoundPage />} />
      </Routes>

      <ToastContainer
        position="bottom-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        limit={3}
      />
    </BrowserRouter>
  );
};

export default App;

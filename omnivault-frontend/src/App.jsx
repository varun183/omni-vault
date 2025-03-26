// src/App.jsx
import React, { useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { getCurrentUser } from "./store/slices/authSlice";

// Pages
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

const App = () => {
  const dispatch = useDispatch();
  const { isAuthenticated } = useSelector((state) => state.auth);

  useEffect(() => {
    if (localStorage.getItem("access_token")) {
      dispatch(getCurrentUser());
    }
  }, [dispatch]);

  return (
    <BrowserRouter>
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
        {/* {import.meta.env.DEV && <ReduxTester />} */}
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
      />
    </BrowserRouter>
  );
};

export default App;

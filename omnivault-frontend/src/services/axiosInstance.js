// src/services/axiosInstance.js
import axios from "axios";
import connectivityService from "./connectivityService";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30 second timeout
});

// Request interceptor to include auth token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("access_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token refresh and connectivity issues
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // ===== CONNECTIVITY ERROR HANDLING =====
    // If there's no response, it's likely a network issue or server down
    if (!error.response) {
      // Let the connectivity service know about this error and check server status
      connectivityService.handleApiError(error);

      // Add to retry queue if appropriate
      if (originalRequest && originalRequest.method) {
        const retryFn = () => axiosInstance(originalRequest);
        connectivityService.addToRetryQueue(retryFn);
      }

      // Add a flag to the error so logger can identify it
      error.isConnectivityError = true;
      return Promise.reject(error);
    }

    // ===== TOKEN REFRESH HANDLING =====
    // Handle token refresh for 401 errors
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem("refresh_token");
        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        const response = await axios.post(`${API_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = response.data;

        localStorage.setItem("access_token", accessToken);
        localStorage.setItem("refresh_token", newRefreshToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        return axiosInstance(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;

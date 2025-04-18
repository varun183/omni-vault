import axiosInstance from "./axiosInstance";
import logger from "./loggerService";

const systemService = {
  getCloudStorageStatus: async () => {
    try {
      const response = await axiosInstance.get("/system/cloud-status");
      return response.data.cloudStorageEnabled;
    } catch (error) {
      logger.error("Error checking cloud storage status:", error);
      return false;
    }
  },

  // Simple health check endpoint to verify server connectivity
  // Note: we avoid using cache headers that might be rejected by CORS policy
  checkHealth: async () => {
    try {
      // Add a timestamp to avoid caching issues
      const response = await fetch(
        `${
          import.meta.env.VITE_API_URL || "http://localhost:8080/api"
        }/system/health?_=${Date.now()}`,
        {
          method: "GET",
          mode: "cors",
          credentials: "include",
          timeout: 5000, // Short timeout for health checks
        }
      );
      return response.ok;
    } catch (error) {
      return false;
    }
  },

  // Get system information (version, uptime, etc.)
  getSystemInfo: async () => {
    try {
      const response = await axiosInstance.get("/system/info");
      return response.data;
    } catch (error) {
      logger.error("Error fetching system information:", error);
      throw error;
    }
  },
};

export default systemService;

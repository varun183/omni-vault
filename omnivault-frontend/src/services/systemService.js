import axiosInstance from "./axiosInstance";

const systemService = {
  getCloudStorageStatus: async () => {
    try {
      const response = await axiosInstance.get("/system/cloud-status");
      return response.data.cloudStorageEnabled;
    } catch (error) {
      console.error("Error checking cloud storage status:", error);
      return false;
    }
  },
};

export default systemService;

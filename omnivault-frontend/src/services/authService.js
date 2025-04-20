import axiosInstance from "./axiosInstance";

const authService = {
  login: async (usernameOrEmail, password) => {
    const response = await axiosInstance.post("/auth/login", {
      usernameOrEmail,
      password,
    });
    return response.data;
  },

  register: async (userData) => {
    const response = await axiosInstance.post("/auth/register", userData);
    return response.data;
  },

  logout: async () => {
    const refreshToken = localStorage.getItem("refresh_token");
    await axiosInstance.post("/auth/logout", { refreshToken });

    // Clear all authentication-related local storage
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("verification_email");
  },

  getCurrentUser: async () => {
    const response = await axiosInstance.get("/auth/user");
    return response.data;
  },

  // Profile Update Method
  updateProfile: async (userData) => {
    const response = await axiosInstance.put("/auth/profile", userData);
    return response.data;
  },

  changePassword: async (currentPassword, newPassword) => {
    const response = await axiosInstance.put("/auth/change-password", {
      currentPassword,
      newPassword,
    });
    return response.data;
  },
  // Delete Account Method
  deleteAccount: async (password) => {
    const response = await axiosInstance.post("/auth/delete-account", {
      password,
    });

    // Clear all local storage on successful deletion
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("verification_email");

    return response.data;
  },

  // Email Verification Methods
  verifyEmail: async (token) => {
    const response = await axiosInstance.post(
      `/auth/verify/token?token=${token}`
    );
    return response.data;
  },

  verifyEmailWithOTP: async (email, otpCode) => {
    const response = await axiosInstance.post(
      `/auth/verify/otp?email=${email}&otpCode=${otpCode}`
    );
    return response.data;
  },

  resendVerificationEmail: async (email) => {
    const response = await axiosInstance.post(
      `/auth/resend-verification?email=${email}`
    );
    return response.data;
  },

  // Token Refresh Method
  refreshToken: async (refreshToken) => {
    const response = await axiosInstance.post("/auth/refresh", {
      refreshToken,
    });

    // Update tokens in local storage
    if (response.data.accessToken) {
      localStorage.setItem("access_token", response.data.accessToken);
    }
    if (response.data.refreshToken) {
      localStorage.setItem("refresh_token", response.data.refreshToken);
    }

    return response.data;
  },

  // Password Reset Methods (if applicable)
  requestPasswordReset: async (email) => {
    const response = await axiosInstance.post("/auth/reset-password/request", {
      email,
    });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await axiosInstance.post("/auth/reset-password/confirm", {
      token,
      newPassword,
    });
    return response.data;
  },
};

export default authService;

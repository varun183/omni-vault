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
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
  },

  getCurrentUser: async () => {
    const response = await axiosInstance.get("/auth/user");
    return response.data;
  },

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
};

export default authService;

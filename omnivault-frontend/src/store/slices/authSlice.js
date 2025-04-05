import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import authService from "../../services/authService";
import { apiCache } from "../../utils/apiCache";
import logger from "../../services/loggerService";

const handleAsyncError = (
  error,
  rejectWithValue,
  customMessage,
  context = {}
) => {
  logger.error(customMessage, error, context);
  return rejectWithValue(error.response?.data?.message || customMessage);
};

export const login = createAsyncThunk(
  "auth/login",
  async ({ usernameOrEmail, password }, { rejectWithValue }) => {
    try {
      apiCache.clear();
      const data = await authService.login(usernameOrEmail, password);
      localStorage.setItem("access_token", data.accessToken);
      localStorage.setItem("refresh_token", data.refreshToken);
      return data;
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Login failed", {
        usernameOrEmail,
      });
    }
  }
);

export const register = createAsyncThunk(
  "auth/register",
  async (userData, { rejectWithValue }) => {
    try {
      apiCache.clear();
      const data = await authService.register(userData);
      localStorage.setItem("access_token", data.accessToken);
      localStorage.setItem("refresh_token", data.refreshToken);
      return data;
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Registration failed", {
        userData,
      });
    }
  }
);

export const logout = createAsyncThunk(
  "auth/logout",
  async (_, { rejectWithValue }) => {
    try {
      await authService.logout();
      apiCache.clear();
      return null;
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Logout failed");
    }
  }
);

export const getCurrentUser = createAsyncThunk(
  "auth/getCurrentUser",
  async (_, { rejectWithValue }) => {
    try {
      return await authService.getCurrentUser();
    } catch (error) {
      return handleAsyncError(error, rejectWithValue, "Failed to get user");
    }
  }
);

export const verifyEmail = createAsyncThunk(
  "auth/verifyEmail",
  async (token, { rejectWithValue }) => {
    try {
      return await authService.verifyEmail(token);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Email verification failed",
        { token }
      );
    }
  }
);

export const verifyEmailWithOTP = createAsyncThunk(
  "auth/verifyEmailWithOTP",
  async ({ email, otpCode }, { rejectWithValue }) => {
    try {
      return await authService.verifyEmailWithOTP(email, otpCode);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "OTP verification failed",
        { email }
      );
    }
  }
);

export const resendVerificationEmail = createAsyncThunk(
  "auth/resendVerificationEmail",
  async (email, { rejectWithValue }) => {
    try {
      return await authService.resendVerificationEmail(email);
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to resend verification email",
        { email }
      );
    }
  }
);

const initialState = {
  user: null,
  isAuthenticated: !!localStorage.getItem("access_token"),
  loading: false,
  error: null,
  verificationSuccess: false,
  verificationEmail: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setVerificationEmail: (state, action) => {
      state.verificationEmail = action.payload;
    },
    clearVerificationState: (state) => {
      state.verificationSuccess = false;
      state.verificationEmail = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Login
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.user = action.payload.user;
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Register
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        state.verificationEmail = action.payload.user.email;
        // Note: not authenticating as the user is not verified yet
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Logout
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
      })

      // Get Current User
      .addCase(getCurrentUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(getCurrentUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(getCurrentUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.isAuthenticated = false;
      })

      // Verify Email Token
      .addCase(verifyEmail.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyEmail.fulfilled, (state) => {
        state.loading = false;
        state.verificationSuccess = true;
      })
      .addCase(verifyEmail.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Verify Email with OTP
      .addCase(verifyEmailWithOTP.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyEmailWithOTP.fulfilled, (state) => {
        state.loading = false;
        state.verificationSuccess = true;
      })
      .addCase(verifyEmailWithOTP.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Resend Verification Email
      .addCase(resendVerificationEmail.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(resendVerificationEmail.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(resendVerificationEmail.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearError, setVerificationEmail, clearVerificationState } =
  authSlice.actions;
export default authSlice.reducer;

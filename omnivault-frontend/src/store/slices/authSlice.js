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
  async (userData, { rejectWithValue, dispatch }) => {
    try {
      apiCache.clear();
      const data = await authService.register(userData);

      // Store verification email explicitly
      dispatch(setVerificationEmail(userData.email));

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
  async ({ email, otpCode }, { rejectWithValue, dispatch }) => {
    try {
      const result = await authService.verifyEmailWithOTP(email, otpCode);

      // Clear verification state after successful verification
      dispatch(clearVerificationState());

      return result;
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

export const updateProfile = createAsyncThunk(
  "auth/updateProfile",
  async (userData, { rejectWithValue }) => {
    try {
      const response = await authService.updateProfile(userData);
      return {
        user: response,
        successMessage: "Profile updated successfully",
      };
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to update profile",
        { userData }
      );
    }
  }
);

export const changePassword = createAsyncThunk(
  "auth/changePassword",
  async ({ currentPassword, newPassword }, { rejectWithValue }) => {
    try {
      await authService.changePassword(currentPassword, newPassword);
      return {
        successMessage: "Password changed successfully",
      };
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to change password",
        { userIdentifier: true }
      );
    }
  }
);

export const deleteAccount = createAsyncThunk(
  "auth/deleteAccount",
  async ({ password }, { rejectWithValue, dispatch }) => {
    try {
      await authService.deleteAccount(password);
      // Dispatch logout after successful account deletion
      dispatch(logout());
      return null;
    } catch (error) {
      return handleAsyncError(
        error,
        rejectWithValue,
        "Failed to delete account",
        { userIdentifier: true }
      );
    }
  }
);

// Initial state with robust handling of authentication state
const initialState = {
  user: null,
  isAuthenticated: !!localStorage.getItem("access_token"),
  loading: false,
  error: null,
  verificationSuccess: false,
  // Prioritize Redux state, then localStorage, then null
  verificationEmail: localStorage.getItem("verification_email") || null,
};

// Create the auth slice
const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    // Clear any existing error state
    clearError: (state) => {
      state.error = null;
    },

    // Set verification email with localStorage persistence
    setVerificationEmail: (state, action) => {
      const email = action.payload;
      state.verificationEmail = email;

      // Persist in localStorage for page refresh resilience
      if (email) {
        localStorage.setItem("verification_email", email);
      } else {
        localStorage.removeItem("verification_email");
      }
    },

    // Reset verification-related state
    clearVerificationState: (state) => {
      state.verificationSuccess = false;
      state.verificationEmail = null;
      localStorage.removeItem("verification_email");
    },
  },

  // Handle async action states
  extraReducers: (builder) => {
    builder
      // Login handling
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.user = action.payload.user;
        // Clear any leftover verification state
        state.verificationEmail = null;
        localStorage.removeItem("verification_email");
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Registration handling
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.user;
        // Not authenticating as user is not verified yet
        state.isAuthenticated = false;
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Logout handling
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
        state.verificationEmail = null;
        localStorage.removeItem("verification_email");
      })

      // Get Current User handling
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

      // Email Verification Token handling
      .addCase(verifyEmail.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyEmail.fulfilled, (state) => {
        state.loading = false;
        state.verificationSuccess = true;
        state.verificationEmail = null;
        localStorage.removeItem("verification_email");
      })
      .addCase(verifyEmail.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // OTP Verification handling
      .addCase(verifyEmailWithOTP.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyEmailWithOTP.fulfilled, (state) => {
        state.loading = false;
        state.verificationSuccess = true;
        state.verificationEmail = null;
        localStorage.removeItem("verification_email");
      })
      .addCase(verifyEmailWithOTP.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Resend Verification Email handling
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
      })
      .addCase(updateProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.loading = false;
        // Update user details in state
        state.user = {
          ...state.user,
          firstName: action.payload.user.firstName,
          lastName: action.payload.user.lastName,
        };
        // Optional: You might want to clear any previous error
        state.error = null;
      })
      .addCase(updateProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(changePassword.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(changePassword.fulfilled, (state) => {
        state.loading = false;
        state.error = null;
      })
      .addCase(changePassword.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(deleteAccount.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteAccount.fulfilled, (state) => {
        state.loading = false;
        state.user = null;
        state.isAuthenticated = false;
      })
      .addCase(deleteAccount.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

// Export actions and reducer
export const { clearError, setVerificationEmail, clearVerificationState } =
  authSlice.actions;

export default authSlice.reducer;

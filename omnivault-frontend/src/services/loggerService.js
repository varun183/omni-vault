// src/services/loggerService.js
import { toast } from "react-toastify";
import connectivityService from "./connectivityService";

class LoggerService {
  constructor() {
    this.logLevel = this.getLogLevel();
    this.pendingErrors = new Map(); // Track pending errors to avoid duplicates
    this.connectivityErrorShown = false; // Track if connectivity error has been shown
  }

  getLogLevel() {
    const envLevel = import.meta.env.VITE_LOG_LEVEL;
    const levels = ["error", "warn", "info", "debug"];
    return levels.includes(envLevel) ? envLevel : "error";
  }

  _log(level, message, error = null, context = {}) {
    const levels = ["error", "warn", "info", "debug"];
    const currentLevelIndex = levels.indexOf(this.logLevel);
    const messageLevelIndex = levels.indexOf(level);

    // Check if this is a connectivity-related error, and if so, avoid duplicates
    if (level === "error" && error) {
      // Check if we've flagged this as a connectivity error in axiosInstance
      if (
        error.isConnectivityError ||
        (!error.response &&
          (!navigator.onLine || !connectivityService.hasServerConnection))
      ) {
        // Only log to console, don't show user-facing error
        console.error(`[Connectivity] ${message}`, error);
        return; // Skip the rest of the error handling
      }
    }

    if (messageLevelIndex <= currentLevelIndex) {
      // Prepare log details
      const logDetails = {
        timestamp: new Date().toISOString(),
        level,
        message,
        context,
        error: error ? this._serializeError(error) : null,
      };

      // Console logging
      switch (level) {
        case "error":
          console.error(JSON.stringify(logDetails, null, 2));
          break;
        case "warn":
          console.warn(JSON.stringify(logDetails, null, 2));
          break;
        case "info":
          console.info(JSON.stringify(logDetails, null, 2));
          break;
        case "debug":
          console.debug(JSON.stringify(logDetails, null, 2));
          break;
      }

      // User-facing error notification (only for errors and warnings)
      if (level === "error" || level === "warn") {
        this._notifyUser(level, message, error);
      }

      // Optional: Remote logging or error tracking
      this._trackError(logDetails);
    }
  }

  // Serialize error to prevent circular references
  _serializeError(error) {
    if (!(error instanceof Error)) return error;

    return {
      name: error.name,
      message: error.message,
      stack: error.stack,
      ...(error.response && {
        status: error.response.status,
        data: error.response.data,
      }),
    };
  }

  // User-facing notifications - integrated with connectivity service
  _notifyUser(level, message, error = null) {
    // First check if we're in a connectivity error state
    if (!navigator.onLine || !connectivityService.hasServerConnection) {
      // Don't show individual error toasts when we have connectivity issues
      return;
    }

    // Generate a unique key for this error to avoid duplicates
    const errorKey = `${level}:${message}`;

    // If this is a network error, let the connectivity service handle it
    if (error && !error.response) {
      // If this is a connectivity-related error, let the connectivity service handle it
      connectivityService.handleError(error, message);
      return;
    }

    // If we already have this error pending, don't show another toast
    if (this.pendingErrors.has(errorKey)) {
      return;
    }

    // For normal errors, show toast with proper configuration
    switch (level) {
      case "error": {
        const toastId = toast.error(message, {
          position: "bottom-right",
          autoClose: 5000,
          hideProgressBar: false,
          closeOnClick: true,
          pauseOnHover: true,
          onClose: () => this.pendingErrors.delete(errorKey),
        });
        this.pendingErrors.set(errorKey, toastId);
        break;
      }
      case "warn": {
        const toastId = toast.warn(message, {
          position: "bottom-right",
          autoClose: 3000,
          onClose: () => this.pendingErrors.delete(errorKey),
        });
        this.pendingErrors.set(errorKey, toastId);
        break;
      }
    }
  }

  _trackError(logDetails) {
    // Future integration with services like Sentry
    // For now, we'll just log to console in production
    if (import.meta.env.PROD && logDetails.level === "error") {
      // Example of where you might send error to a logging service
      // fetch('/api/log', {
      //   method: 'POST',
      //   body: JSON.stringify(logDetails)
      // });
    }
  }

  // Public logging methods
  error(message, error = null, context = {}) {
    this._log("error", message, error, context);
  }

  warn(message, context = {}) {
    this._log("warn", message, null, context);
  }

  info(message, context = {}) {
    this._log("info", message, null, context);
  }

  debug(message, context = {}) {
    this._log("debug", message, null, context);
  }

  // Async error handler - integrated with connectivity service
  async logAsyncError(
    promise,
    customMessage = "An unexpected error occurred",
    context = {}
  ) {
    try {
      return await promise;
    } catch (error) {
      // Check if this is a connectivity-related error first
      if (
        error.isConnectivityError ||
        (!error.response &&
          (!navigator.onLine || !connectivityService.hasServerConnection))
      ) {
        // Don't show individual API error messages during connectivity issues
        console.error(`[Connectivity] ${customMessage}`, error);
      } else {
        // For other errors, use the normal logging flow
        this.error(customMessage, error, context);
      }
      throw error;
    }
  }
}

export const logger = new LoggerService();
export default logger;

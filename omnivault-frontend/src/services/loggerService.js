import { toast } from "react-toastify";

class LoggerService {
  constructor() {
    this.logLevel = this.getLogLevel();
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

      // User-facing error notification
      this._notifyUser(level, message);

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

  // User-facing notifications
  _notifyUser(level, message) {
    switch (level) {
      case "error":
        toast.error(message, {
          position: "bottom-right",
          autoClose: 5000,
          hideProgressBar: false,
          closeOnClick: true,
          pauseOnHover: true,
        });
        break;
      case "warn":
        toast.warn(message, {
          position: "bottom-right",
          autoClose: 3000,
        });
        break;
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

  // Async error handler
  async logAsyncError(
    promise,
    customMessage = "An unexpected error occurred",
    context = {}
  ) {
    try {
      return await promise;
    } catch (error) {
      this.error(customMessage, error, context);
      throw error;
    }
  }
}

export const logger = new LoggerService();
export default logger;

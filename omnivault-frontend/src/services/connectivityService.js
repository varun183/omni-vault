import { toast } from "react-toastify";

// Singleton class to manage application connectivity status using an event-driven approach
class ConnectivityService {
  constructor() {
    this.isOnline = navigator.onLine;
    this.hasServerConnection = true;
    this.serverErrorToast = null;
    this.offlineToast = null;
    this.retryQueue = [];
    this.connectionListeners = [];
    this.checkingConnection = false;
    this.lastServerCheck = 0;
    this.minTimeBetweenChecks = 5000; // Minimum 5 seconds between server checks to prevent spam

    // Subscribe to online/offline events with proper binding
    this.handleOnline = this.handleOnline.bind(this);
    this.handleOffline = this.handleOffline.bind(this);

    window.addEventListener("online", this.handleOnline);
    window.addEventListener("offline", this.handleOffline);

    // Log initial state
    console.log(
      `Initial connectivity state: ${this.isOnline ? "online" : "offline"}`
    );

    // Initial check - with a slight delay to avoid initialization issues
    setTimeout(() => this.checkServerConnection(), 500);
  }

  // Handle browser online event
  handleOnline() {
    console.log("Browser reports online status");
    this.isOnline = true;

    // Dismiss offline toast if it exists
    if (this.offlineToast) {
      console.log("Dismissing offline toast");
      toast.dismiss(this.offlineToast);
      this.offlineToast = null;

      // Show a brief confirmation
      toast.success("You're back online!", { autoClose: 3000 });
    }

    // Check if server is also available - important to check server when network reconnects
    this.checkServerConnection();

    // Notify listeners
    this.notifyListeners();
  }

  // Handle browser offline event
  handleOffline() {
    console.log("Browser reports offline status");
    this.isOnline = false;
    this.hasServerConnection = false;

    // Show offline toast if not already showing
    if (!this.offlineToast) {
      console.log("Creating offline toast");
      this.offlineToast = toast.error(
        "You're currently offline. Please check your internet connection.",
        {
          autoClose: false,
          closeOnClick: false,
          draggable: false,
          closeButton: false,
          className: "connectivity-toast",
          toastId: "offline-toast",
        }
      );
    }

    // Notify listeners
    this.notifyListeners();
  }

  // Check if the server is reachable - only called in specific circumstances
  checkServerConnection = async (force = false) => {
    // Avoid checking if:
    // 1. We're offline
    // 2. Already checking
    // 3. Checked too recently (unless forced)
    const now = Date.now();
    if (
      !this.isOnline ||
      this.checkingConnection ||
      (!force && now - this.lastServerCheck < this.minTimeBetweenChecks)
    ) {
      return;
    }

    this.checkingConnection = true;
    this.lastServerCheck = now;

    try {
      // Simple health check endpoint with timestamp to prevent caching
      const response = await fetch(
        `${
          import.meta.env.VITE_API_URL || "http://localhost:8080/api"
        }/system/health?_=${Date.now()}`,
        {
          method: "GET",
          mode: "cors",
          credentials: "include",
        }
      );

      const wasDown = !this.hasServerConnection;
      this.hasServerConnection = response.ok;

      // If server was down but now it's up, dismiss error toast and process retry queue
      if (wasDown && this.hasServerConnection) {
        console.log("Server connection restored");

        if (this.serverErrorToast) {
          toast.dismiss(this.serverErrorToast);
          this.serverErrorToast = null;

          // Show success message
          toast.success("Connection to server restored!");
        }

        this.processRetryQueue();
      }
    } catch (error) {
      // If we were connected before, show an error
      if (this.hasServerConnection) {
        console.error("Server connection lost:", error);
        this.hasServerConnection = false;

        if (!this.serverErrorToast) {
          this.serverErrorToast = toast.error(
            "Unable to connect to the server. Please wait while we try to reconnect...",
            {
              autoClose: false,
              closeOnClick: false,
              draggable: false,
              closeButton: false,
              className: "connectivity-toast",
              toastId: "server-error-toast",
            }
          );
        }
      }
    } finally {
      this.checkingConnection = false;
    }

    // Notify listeners about changes
    this.notifyListeners();
  };

  // Handle API errors by checking server connectivity if needed
  handleApiError(error) {
    // Only check connectivity on network errors, not HTTP error responses
    if (!error.response) {
      this.checkServerConnection(true); // Force a check when an API error occurs
      return true; // Return true to indicate this was handled as a connectivity issue
    }
    return false; // Not a connectivity issue
  }

  // Force a check for online status and server connection
  forceCheck() {
    // First update the online status from the browser
    const currentOnline = navigator.onLine;
    if (currentOnline !== this.isOnline) {
      this.isOnline = currentOnline;
      if (currentOnline) {
        this.handleOnline();
      } else {
        this.handleOffline();
      }
    }

    // Then check server connection if we're online
    if (this.isOnline) {
      this.checkServerConnection(true); // Force check
    }
  }

  // Classify error types based on error responses
  classifyError(error) {
    // Network error or CORS error
    if (!error.response) {
      if (!this.isOnline) {
        return "OFFLINE";
      }
      return "SERVER_UNREACHABLE";
    }

    // Server responded with an error status
    const status = error.response.status;

    if (status === 401 || status === 403) {
      return "AUTHENTICATION";
    } else if (status === 404) {
      return "NOT_FOUND";
    } else if (status >= 500) {
      return "SERVER_ERROR";
    }

    return "APPLICATION_ERROR";
  }

  // Handle global error notifications consistently
  handleError(error, customMessage = null) {
    const errorType = this.classifyError(error);

    // Don't show new errors if we're offline or disconnected from server
    if (
      (errorType === "OFFLINE" || errorType === "SERVER_UNREACHABLE") &&
      (!this.isOnline || !this.hasServerConnection)
    ) {
      this.checkServerConnection(true); // Force check server connection
      return; // Don't show duplicate toasts
    }

    // Show appropriate error messages
    let message = customMessage;
    if (!message) {
      switch (errorType) {
        case "OFFLINE":
          message =
            "You're currently offline. Please check your internet connection.";
          break;
        case "SERVER_UNREACHABLE":
          message = "Unable to connect to the server. Please try again later.";
          break;
        case "AUTHENTICATION":
          message = "You need to sign in again to continue.";
          break;
        case "NOT_FOUND":
          message = "The requested resource was not found.";
          break;
        case "SERVER_ERROR":
          message = "An error occurred on the server. Please try again later.";
          break;
        default:
          message =
            error.response?.data?.message || "An unexpected error occurred.";
      }
    }

    toast.error(message, {
      autoClose:
        errorType === "OFFLINE" || errorType === "SERVER_UNREACHABLE"
          ? false
          : 5000,
      closeOnClick: true,
      pauseOnHover: true,
    });
  }

  // Add a failed request to retry later when connection is restored
  addToRetryQueue(retryFn) {
    this.retryQueue.push(retryFn);
  }

  // Process the retry queue when connection is restored
  processRetryQueue() {
    console.log(`Processing retry queue (${this.retryQueue.length} items)`);

    // Copy and clear the queue
    const queue = [...this.retryQueue];
    this.retryQueue = [];

    // Execute each retry function
    queue.forEach((retryFn) => {
      try {
        retryFn();
      } catch (error) {
        console.error("Error in retry queue execution:", error);
      }
    });
  }

  // Subscribe to connectivity changes
  addConnectionListener(listener) {
    this.connectionListeners.push(listener);
    return () => {
      this.connectionListeners = this.connectionListeners.filter(
        (l) => l !== listener
      );
    };
  }

  // Notify all listeners of state changes
  notifyListeners() {
    const status = {
      isOnline: this.isOnline,
      hasServerConnection: this.hasServerConnection,
    };

    this.connectionListeners.forEach((listener) => {
      try {
        listener(status);
      } catch (error) {
        console.error("Error in connectivity listener:", error);
      }
    });
  }

  // Clean up event listeners
  destroy() {
    window.removeEventListener("online", this.handleOnline);
    window.removeEventListener("offline", this.handleOffline);

    if (this.serverErrorToast) {
      toast.dismiss(this.serverErrorToast);
    }

    if (this.offlineToast) {
      toast.dismiss(this.offlineToast);
    }
  }
}

// Export a singleton instance
const connectivityService = new ConnectivityService();
export default connectivityService;

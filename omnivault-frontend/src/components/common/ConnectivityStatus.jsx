import React, { useState, useEffect } from "react";
import { FiWifi, FiWifiOff, FiServer } from "react-icons/fi";
import connectivityService from "../../services/connectivityService";

const ConnectivityStatus = () => {
  const [status, setStatus] = useState({
    isOnline: navigator.onLine,
    hasServerConnection: true,
  });

  useEffect(() => {
    // Subscribe to connectivity changes
    const unsubscribe = connectivityService.addConnectionListener(
      (newStatus) => {
        setStatus(newStatus);
      }
    );

    // Clean up subscription
    return () => {
      unsubscribe();
    };
  }, []);

  // If everything is fine, don't show anything
  if (status.isOnline && status.hasServerConnection) {
    return null;
  }

  return (
    <div className="fixed bottom-4 left-4 z-50">
      <div
        className={`flex items-center px-4 py-2 rounded-lg shadow-lg ${
          !status.isOnline
            ? "bg-red-100 text-red-800 border border-red-200"
            : "bg-yellow-100 text-yellow-800 border border-yellow-200"
        }`}
      >
        {!status.isOnline ? (
          <>
            <FiWifiOff className="mr-2" />
            <span>You're offline. Please check your internet connection.</span>
          </>
        ) : !status.hasServerConnection ? (
          <>
            <FiServer className="mr-2" />
            <span>Server connection lost. Reconnecting...</span>
          </>
        ) : (
          <>
            <FiWifi className="mr-2" />
            <span>Connected</span>
          </>
        )}
      </div>
    </div>
  );
};

export default ConnectivityStatus;

import React from "react";

const Alert = ({ message, type = "info", onClose, className = "" }) => {
  const typeClasses = {
    info: "bg-blue-50 text-blue-700 border-blue-200",
    success: "bg-green-50 text-green-700 border-green-200",
    warning: "bg-yellow-50 text-yellow-700 border-yellow-200",
    error: "bg-red-50 text-red-700 border-red-200",
  };

  return (
    <div
      className={`p-4 border rounded-md mb-4 ${typeClasses[type]} ${className}`}
    >
      <div className="flex justify-between items-center">
        <div>{message}</div>
        {onClose && (
          <button
            type="button"
            className="text-gray-500 hover:text-gray-700 focus:outline-none"
            onClick={onClose}
          >
            <span className="sr-only">Close</span>
            <span aria-hidden="true">&times;</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default Alert;

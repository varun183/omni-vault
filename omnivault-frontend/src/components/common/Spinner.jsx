import React from "react";

const Spinner = ({ size = "md", className = "" }) => {
  const sizeClasses = {
    sm: "w-4 h-4",
    md: "w-8 h-8",
    lg: "w-12 h-12",
  };

  return (
    <div className={`inline-block ${className}`}>
      <div
        className={`border-4 border-t-primary-500 border-r-primary-300 border-b-primary-200 border-l-primary-100 rounded-full animate-spin ${sizeClasses[size]}`}
      />
    </div>
  );
};

export default Spinner;

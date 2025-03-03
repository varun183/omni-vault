import React from "react";
import { FiFileText, FiLink, FiUpload } from "react-icons/fi";

const HomePage = () => {
  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">All Content</h1>
          <p className="text-gray-500">Browse and manage all your content</p>
        </div>
        <div className="flex gap-2">
          <button className="flex items-center bg-gray-200 px-4 py-2 rounded">
            <FiFileText className="mr-2" />
            Add Text
          </button>
          <button className="flex items-center bg-gray-200 px-4 py-2 rounded">
            <FiLink className="mr-2" />
            Add Link
          </button>
          <button className="flex items-center bg-gray-200 px-4 py-2 rounded">
            <FiUpload className="mr-2" />
            Upload File
          </button>
        </div>
      </div>
    </div>
  );
};

export default HomePage;

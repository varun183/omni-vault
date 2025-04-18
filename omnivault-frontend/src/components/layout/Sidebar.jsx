import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation } from "react-router-dom";
import {
  FiFolder,
  FiFile,
  FiImage,
  FiVideo,
  FiFileText,
  FiLink,
  FiStar,
  FiClock,
  FiTag,
  FiPlus,
} from "react-icons/fi";
import { getRootFolders } from "../../store/slices/folderSlice";
import { getAllTags } from "../../store/slices/tagSlice";
import Button from "../common/Button";
import FolderModal from "../features/folder/FolderModal";

const Sidebar = () => {
  const dispatch = useDispatch();
  const location = useLocation();
  const { rootFolders } = useSelector((state) => state.folders);
  const { tags } = useSelector((state) => state.tags);
  const [isFolderModalOpen, setIsFolderModalOpen] = useState(false);

  useEffect(() => {
    dispatch(getRootFolders());
    dispatch(getAllTags());
  }, [dispatch]);

  const isActiveRoute = (path) => {
    return location.pathname === path;
  };

  return (
    <aside className="w-64 bg-gray-50 border-r h-screen overflow-y-auto sticky top-0">
      <div className="p-4 space-y-6">
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-semibold uppercase text-gray-500">
              Content Types
            </h2>
          </div>
          <nav className="space-y-1">
            <Link
              to="/"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiFile className="mr-3 text-gray-400" />
              All Content
            </Link>
            <Link
              to="/content-type/text"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/content-type/text")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiFileText className="mr-3 text-gray-400" />
              Text
            </Link>
            <Link
              to="/content-type/link"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/content-type/link")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiLink className="mr-3 text-gray-400" />
              Links
            </Link>
            <Link
              to="/content-type/image"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/content-type/image")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiImage className="mr-3 text-gray-400" />
              Images
            </Link>
            <Link
              to="/content-type/video"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/content-type/video")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiVideo className="mr-3 text-gray-400" />
              Videos
            </Link>
            <Link
              to="/content-type/document"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/content-type/document")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiFileText className="mr-3 text-gray-400" />
              Documents
            </Link>
          </nav>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-semibold uppercase text-gray-500">
              Collections
            </h2>
          </div>
          <nav className="space-y-1">
            <Link
              to="/favorites"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/favorites")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiStar className="mr-3 text-gray-400" />
              Favorites
            </Link>
            <Link
              to="/recent"
              className={`flex items-center px-3 py-2 text-sm rounded-md ${
                isActiveRoute("/recent")
                  ? "bg-primary-100 text-primary-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              <FiClock className="mr-3 text-gray-400" />
              Recent
            </Link>
          </nav>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-semibold uppercase text-gray-500">
              Folders
            </h2>
            <Button
              onClick={() => setIsFolderModalOpen(true)}
              variant="ghost"
              size="sm"
              className="p-1"
            >
              <FiPlus />
            </Button>
          </div>
          <nav className="space-y-1">
            {rootFolders.map((folder) => (
              <Link
                key={folder.id}
                to={`/folder/${folder.id}`}
                className={`flex items-center px-3 py-2 text-sm rounded-md ${
                  isActiveRoute(`/folder/${folder.id}`)
                    ? "bg-primary-100 text-primary-700"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
              >
                <FiFolder className="mr-3 text-gray-400" />
                {folder.name}
              </Link>
            ))}
          </nav>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-semibold uppercase text-gray-500">
              Tags
            </h2>
            <Link
              to="/tags"
              className="text-primary-600 hover:text-primary-700 text-sm"
            >
              View All
            </Link>
          </div>
          <div className="flex flex-wrap gap-2">
            {tags.slice(0, 10).map((tag) => (
              <Link
                key={tag.id}
                to={`/tag/${tag.id}`}
                className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                style={{
                  backgroundColor: `${tag.color}25`,
                  color: tag.color,
                  borderColor: tag.color,
                }}
              >
                <FiTag className="mr-1" />
                {tag.name}
              </Link>
            ))}
          </div>
        </div>
      </div>

      <FolderModal
        isOpen={isFolderModalOpen}
        onClose={() => setIsFolderModalOpen(false)}
      />
    </aside>
  );
};

export default Sidebar;

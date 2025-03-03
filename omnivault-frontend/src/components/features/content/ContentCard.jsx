import React from "react";
import { Link } from "react-router-dom";
import { useDispatch } from "react-redux";
import {
  FiStar,
  FiFileText,
  FiImage,
  FiLink,
  FiVideo,
  FiFile,
  FiFolder,
  FiTrash,
  FiEdit,
} from "react-icons/fi";
import {
  toggleFavorite,
  deleteContent,
} from "../../../store/slices/contentSlice";
import contentService from "../../../services/contentService";
import Button from "../../common/Button";
import { format } from "date-fns";

const ContentCard = ({ content, onEdit }) => {
  const dispatch = useDispatch();

  const handleToggleFavorite = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dispatch(toggleFavorite(content.id));
  };

  const handleDelete = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (window.confirm("Are you sure you want to delete this content?")) {
      dispatch(deleteContent(content.id));
    }
  };

  const handleEdit = (e) => {
    e.preventDefault();
    e.stopPropagation();
    onEdit(content);
  };

  const getContentIcon = () => {
    switch (content.contentType) {
      case "TEXT":
        return <FiFileText className="h-6 w-6 text-blue-500" />;
      case "LINK":
        return <FiLink className="h-6 w-6 text-green-500" />;
      case "IMAGE":
        return <FiImage className="h-6 w-6 text-purple-500" />;
      case "VIDEO":
        return <FiVideo className="h-6 w-6 text-red-500" />;
      case "DOCUMENT":
        return <FiFile className="h-6 w-6 text-orange-500" />;
      default:
        return <FiFile className="h-6 w-6 text-gray-500" />;
    }
  };

  const getPreviewContent = () => {
    switch (content.contentType) {
      case "TEXT":
        return (
          <div className="text-sm text-gray-500 line-clamp-2">
            {content.textContent?.substring(0, 200)}
          </div>
        );
      case "LINK":
        return (
          <div className="text-sm text-gray-500 truncate">{content.url}</div>
        );
      case "IMAGE":
        return content.thumbnailPath ? (
          <img
            src={contentService.getThumbnailUrl(content.id)}
            alt={content.title}
            className="h-32 w-full object-cover rounded"
          />
        ) : null;
      case "VIDEO":
        return content.thumbnailPath ? (
          <div className="relative">
            <img
              src={contentService.getThumbnailUrl(content.id)}
              alt={content.title}
              className="h-32 w-full object-cover rounded"
            />
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="bg-black bg-opacity-50 rounded-full p-2">
                <FiPlay className="h-6 w-6 text-white" />
              </div>
            </div>
          </div>
        ) : null;
      default:
        return null;
    }
  };

  return (
    <Link to={`/content/${content.id}`} className="block">
      <div className="bg-white rounded-lg shadow-sm border hover:shadow-md transition-shadow p-4">
        <div className="flex justify-between items-start mb-2">
          <div className="flex items-center">
            {getContentIcon()}
            <h3 className="ml-2 font-medium line-clamp-1">{content.title}</h3>
          </div>
          <div className="flex items-center space-x-1">
            <Button
              variant="ghost"
              className="p-1 text-gray-400 hover:text-yellow-500"
              onClick={handleToggleFavorite}
            >
              <FiStar
                className={
                  content.favorite ? "text-yellow-500 fill-yellow-500" : ""
                }
              />
            </Button>
            <Button
              variant="ghost"
              className="p-1 text-gray-400 hover:text-primary-500"
              onClick={handleEdit}
            >
              <FiEdit />
            </Button>
            <Button
              variant="ghost"
              className="p-1 text-gray-400 hover:text-red-500"
              onClick={handleDelete}
            >
              <FiTrash />
            </Button>
          </div>
        </div>

        {getPreviewContent()}

        <div className="mt-3 flex items-center text-xs text-gray-500 justify-between">
          <div className="flex items-center">
            {content.folderId && (
              <div className="flex items-center mr-3">
                <FiFolder className="mr-1" />
                <span>{content.folderName}</span>
              </div>
            )}
            <div>{format(new Date(content.createdAt), "MMM d, yyyy")}</div>
          </div>

          {content.tags && content.tags.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {content.tags.slice(0, 2).map((tag) => (
                <span
                  key={tag.id}
                  className="inline-flex items-center px-2 py-0.5 rounded-full text-xs"
                  style={{
                    backgroundColor: `${tag.color}25`,
                    color: tag.color,
                  }}
                >
                  {tag.name}
                </span>
              ))}
              {content.tags.length > 2 && (
                <span className="text-gray-500">
                  +{content.tags.length - 2}
                </span>
              )}
            </div>
          )}
        </div>
      </div>
    </Link>
  );
};

export default ContentCard;

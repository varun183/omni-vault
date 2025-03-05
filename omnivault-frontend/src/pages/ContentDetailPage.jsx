import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { format } from "date-fns";
import {
  FiArrowLeft,
  FiFolder,
  FiStar,
  FiEdit,
  FiTrash,
  FiDownload,
  FiExternalLink,
} from "react-icons/fi";
import {
  getContent,
  toggleFavorite,
  deleteContent,
} from "../store/slices/contentSlice";
import contentService from "../services/contentService";
import Layout from "../components/layout/Layout";
import Button from "../components/common/Button";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";
import Spinner from "../components/common/Spinner";

const ContentDetailPage = () => {
  const { contentId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { currentContent, loading, error } = useSelector(
    (state) => state.content
  );
  const [isEditMode, setIsEditMode] = useState(false);

  useEffect(() => {
    if (contentId) {
      dispatch(getContent(contentId));
    }
  }, [dispatch, contentId]);

  const handleToggleFavorite = () => {
    dispatch(toggleFavorite(contentId));
  };

  const handleDelete = () => {
    if (window.confirm("Are you sure you want to delete this content?")) {
      dispatch(deleteContent(contentId))
        .unwrap()
        .then(() => {
          navigate("/");
        })
        .catch((error) => {
          console.error("Failed to delete content:", error);
        });
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      </Layout>
    );
  }

  if (error || !currentContent) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-gray-900">
            Content not found
          </h3>
          <p className="mt-1 text-gray-500">
            The content you're looking for doesn't exist or you don't have
            permission to view it.
          </p>
          <div className="mt-6">
            <Link to="/" className="text-primary-600 hover:text-primary-500">
              Go back to home
            </Link>
          </div>
        </div>
      </Layout>
    );
  }

  const renderContentBody = () => {
    switch (currentContent.contentType) {
      case "TEXT":
        return (
          <div className="bg-white rounded-lg shadow p-6 mt-4">
            <div className="prose prose-blue max-w-none">
              {currentContent.textContent.split("\n").map((paragraph, i) => (
                <p key={i}>{paragraph}</p>
              ))}
            </div>
          </div>
        );
      case "LINK":
        return (
          <div className="bg-white rounded-lg shadow p-6 mt-4">
            <a
              href={currentContent.url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center text-primary-600 hover:text-primary-700"
            >
              <FiExternalLink className="mr-1" />
              {currentContent.url}
            </a>
          </div>
        );
      case "IMAGE":
        return (
          <div className="bg-white rounded-lg shadow p-6 mt-4">
            <img
              src={contentService.getFileUrl(currentContent.id)}
              alt={currentContent.title}
              className="max-w-full rounded"
            />
          </div>
        );
      case "VIDEO":
        return (
          <div className="bg-white rounded-lg shadow p-6 mt-4">
            <video
              src={contentService.getFileUrl(currentContent.id)}
              controls
              className="max-w-full rounded"
            />
          </div>
        );
      case "DOCUMENT":
      case "OTHER":
        return (
          <div className="bg-white rounded-lg shadow p-6 mt-4 text-center">
            <p className="mb-4">
              This file type may not be viewable in the browser.
            </p>
            <Button
              as="a"
              href={contentService.getFileUrl(currentContent.id)}
              target="_blank"
              download={currentContent.originalFilename}
              className="flex items-center mx-auto"
            >
              <FiDownload className="mr-2" />
              Download File
            </Button>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <Layout>
      <div className="mb-4">
        <Button
          variant="ghost"
          className="flex items-center text-gray-600"
          onClick={() => navigate(-1)}
        >
          <FiArrowLeft className="mr-1" />
          Back
        </Button>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-between items-start">
          <h1 className="text-2xl font-bold">{currentContent.title}</h1>

          <div className="flex items-center space-x-2">
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-yellow-500"
              onClick={handleToggleFavorite}
            >
              <FiStar
                className={
                  currentContent.favorite
                    ? "text-yellow-500 fill-yellow-500"
                    : ""
                }
              />
            </Button>
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-primary-500"
              onClick={() => setIsEditMode(true)}
            >
              <FiEdit />
            </Button>
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-red-500"
              onClick={handleDelete}
            >
              <FiTrash />
            </Button>
          </div>
        </div>

        {currentContent.description && (
          <p className="text-gray-600 mt-2">{currentContent.description}</p>
        )}

        <div className="flex flex-wrap items-center mt-4 text-sm text-gray-500">
          <div className="mr-4">
            Added: {format(new Date(currentContent.createdAt), "MMM d, yyyy")}
          </div>

          {currentContent.folderName && (
            <div className="flex items-center mr-4">
              <FiFolder className="mr-1" />
              <Link
                to={`/folder/${currentContent.folderId}`}
                className="hover:text-primary-600"
              >
                {currentContent.folderName}
              </Link>
            </div>
          )}

          {currentContent.tags && currentContent.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2 sm:mt-0">
              {currentContent.tags.map((tag) => (
                <Link
                  key={tag.id}
                  to={`/tag/${tag.id}`}
                  className="inline-flex items-center px-2 py-0.5 rounded-full text-xs"
                  style={{
                    backgroundColor: `${tag.color}25`,
                    color: tag.color,
                  }}
                >
                  {tag.name}
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>

      {renderContentBody()}

      {isEditMode && currentContent.contentType === "TEXT" && (
        <TextContentForm
          isOpen={isEditMode}
          onClose={() => setIsEditMode(false)}
          content={currentContent}
        />
      )}

      {isEditMode && currentContent.contentType === "LINK" && (
        <LinkContentForm
          isOpen={isEditMode}
          onClose={() => setIsEditMode(false)}
          content={currentContent}
        />
      )}
    </Layout>
  );
};

export default ContentDetailPage;

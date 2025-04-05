import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { format } from "date-fns";
import { FiArrowLeft, FiFolder, FiStar, FiEdit, FiTrash } from "react-icons/fi";
import {
  getContent,
  toggleFavorite,
  deleteContent,
} from "../store/slices/contentSlice";
import logger from "../services/loggerService";
import Layout from "../components/layout/Layout";
import Button from "../components/common/Button";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";
import Spinner from "../components/common/Spinner";
import ContentRenderer from "../components/features/content/ContentRenderer";

const ContentDetailPage = () => {
  const { contentId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { currentContent, loading, error } = useSelector(
    (state) => state.content
  );
  const [isEditMode, setIsEditMode] = useState(false);

  useEffect(() => {
    // Log content detail page access
    logger.info("Content detail page accessed", { contentId });

    if (contentId) {
      // Track content retrieval
      dispatch(getContent(contentId))
        .unwrap()
        .then((content) => {
          logger.info("Content retrieved successfully", {
            contentId,
            contentType: content.contentType,
          });
        })
        .catch((error) => {
          logger.error("Failed to retrieve content", error, { contentId });
        });
    }
  }, [dispatch, contentId]);

  const handleToggleFavorite = () => {
    try {
      logger.info("Toggling favorite status", { contentId });

      dispatch(toggleFavorite(contentId))
        .unwrap()
        .then((updatedContent) => {
          logger.info("Favorite status updated", {
            contentId,
            isFavorite: updatedContent.favorite,
          });
        })
        .catch((error) => {
          logger.error("Failed to toggle favorite", error, { contentId });
        });
    } catch (error) {
      logger.error("Unexpected error toggling favorite", error, { contentId });
    }
  };

  const handleDelete = () => {
    try {
      const confirmDelete = window.confirm(
        "Are you sure you want to delete this content?"
      );

      if (confirmDelete) {
        logger.info("Attempting to delete content", { contentId });

        dispatch(deleteContent(contentId))
          .unwrap()
          .then(() => {
            logger.info("Content deleted successfully", { contentId });
            navigate("/");
          })
          .catch((error) => {
            logger.error("Failed to delete content", error, { contentId });
          });
      } else {
        logger.info("Content deletion cancelled", { contentId });
      }
    } catch (error) {
      logger.error("Unexpected error during content deletion", error, {
        contentId,
      });
    }
  };

  const handleEditStart = () => {
    try {
      logger.info("Entering edit mode", {
        contentId,
        contentType: currentContent?.contentType,
      });
      setIsEditMode(true);
    } catch (error) {
      logger.error("Error entering edit mode", error, { contentId });
    }
  };

  const handleEditClose = () => {
    try {
      logger.info("Exiting edit mode", {
        contentId,
        contentType: currentContent?.contentType,
      });
      setIsEditMode(false);
    } catch (error) {
      logger.error("Error exiting edit mode", error, { contentId });
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
    logger.warn("Content not found or access denied", {
      contentId,
      error,
    });

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
            <Link
              to="/"
              className="text-primary-600 hover:text-primary-500"
              onClick={() =>
                logger.info("Navigating to home from content not found")
              }
            >
              Go back to home
            </Link>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="mb-4">
        <Button
          variant="ghost"
          className="flex items-center text-gray-600"
          onClick={() => {
            logger.info("Navigating back from content detail");
            navigate(-1);
          }}
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
              onClick={handleEditStart}
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
                onClick={() =>
                  logger.info("Navigating to folder", {
                    folderId: currentContent.folderId,
                  })
                }
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
                  onClick={() =>
                    logger.info("Navigating to tag", {
                      tagId: tag.id,
                      tagName: tag.name,
                    })
                  }
                >
                  {tag.name}
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>

      <ContentRenderer content={currentContent} />

      {isEditMode && currentContent.contentType === "TEXT" && (
        <TextContentForm
          isOpen={isEditMode}
          onClose={handleEditClose}
          content={currentContent}
        />
      )}

      {isEditMode && currentContent.contentType === "LINK" && (
        <LinkContentForm
          isOpen={isEditMode}
          onClose={handleEditClose}
          content={currentContent}
        />
      )}
    </Layout>
  );
};

export default ContentDetailPage;

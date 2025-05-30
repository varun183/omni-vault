import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { FiEdit, FiTrash, FiPlus, FiArrowLeft, FiFolder } from "react-icons/fi";
import { getFolder, deleteFolder } from "../store/slices/folderSlice";
import { getFolderContent } from "../store/slices/contentSlice";
import logger from "../services/loggerService";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import FolderModal from "../components/features/folder/FolderModal";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";
import FileUploadForm from "../components/features/content/FileUploadForm";

const FolderPage = () => {
  const { folderId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { currentFolder, loading: folderLoading } = useSelector(
    (state) => state.folders
  );
  const {
    contents,
    loading: contentLoading,
    totalPages,
  } = useSelector((state) => state.content);

  const [currentPage, setCurrentPage] = useState(0);
  const [selectedContent, setSelectedContent] = useState(null);
  const [isFolderModalOpen, setIsFolderModalOpen] = useState(false);
  const [isSubfolderModalOpen, setIsSubfolderModalOpen] = useState(false);
  const [isTextFormOpen, setIsTextFormOpen] = useState(false);
  const [isLinkFormOpen, setIsLinkFormOpen] = useState(false);
  const [isFileUploadOpen, setIsFileUploadOpen] = useState(false);

  useEffect(() => {
    // Log folder page access
    logger.info("Folder page accessed", { folderId });

    if (folderId) {
      // Track folder and content retrieval
      Promise.all([
        dispatch(getFolder(folderId))
          .unwrap()
          .then((folder) => {
            logger.info("Folder retrieved successfully", {
              folderId,
              folderName: folder.name,
            });
          }),
        dispatch(
          getFolderContent({
            folderId,
            page: currentPage,
            size: 12,
          })
        )
          .unwrap()
          .then((content) => {
            logger.info("Folder content retrieved", {
              folderId,
              contentCount: content.content.length,
            });
          }),
      ]).catch((error) => {
        logger.error("Failed to retrieve folder or content", error, {
          folderId,
        });
      });
    }
  }, [dispatch, folderId, currentPage]);

  const handlePageChange = (newPage) => {
    try {
      logger.info("Changing folder content page", {
        folderId,
        currentPage: newPage,
      });
      setCurrentPage(newPage);
    } catch (error) {
      logger.error("Error changing page", error, {
        folderId,
        newPage,
      });
    }
  };

  const handleEditFolder = () => {
    try {
      logger.info("Opening folder edit modal", { folderId });
      setIsFolderModalOpen(true);
    } catch (error) {
      logger.error("Error opening folder edit modal", error, { folderId });
    }
  };

  const handleDeleteFolder = () => {
    try {
      const confirmDelete = window.confirm(
        "Are you sure you want to delete this folder? All contents will be preserved but moved to root."
      );

      if (confirmDelete) {
        logger.info("Attempting to delete folder", { folderId });

        dispatch(deleteFolder(folderId))
          .unwrap()
          .then(() => {
            logger.info("Folder deleted successfully", { folderId });
            navigate("/");
          })
          .catch((error) => {
            logger.error("Failed to delete folder", error, { folderId });
          });
      } else {
        logger.info("Folder deletion cancelled", { folderId });
      }
    } catch (error) {
      logger.error("Unexpected error deleting folder", error, { folderId });
    }
  };

  const handleEditContent = (content) => {
    try {
      logger.info("Preparing to edit content", {
        contentId: content.id,
        contentType: content.contentType,
      });

      setSelectedContent(content);

      if (content.contentType === "TEXT") {
        setIsTextFormOpen(true);
      } else if (content.contentType === "LINK") {
        setIsLinkFormOpen(true);
      }
    } catch (error) {
      logger.error("Error preparing content edit", error, {
        contentId: content.id,
      });
    }
  };

  // Render loading state
  if (folderLoading && !currentFolder) {
    return (
      <Layout>
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      </Layout>
    );
  }

  // Render not found state
  if (!currentFolder) {
    logger.warn("Folder not found", { folderId });

    return (
      <Layout>
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-gray-900">
            Folder not found
          </h3>
          <p className="mt-1 text-gray-500">
            The folder you're looking for doesn't exist or you don't have
            permission to view it.
          </p>
          <div className="mt-6">
            <Link
              to="/"
              className="text-primary-600 hover:text-primary-500"
              onClick={() =>
                logger.info("Navigating to home from folder not found")
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
      <div className="mb-2">
        <Button
          variant="ghost"
          className="flex items-center text-gray-600"
          onClick={() => {
            logger.info("Navigating back from folder page");
            navigate(-1);
          }}
        >
          <FiArrowLeft className="mr-1" />
          Back
        </Button>
      </div>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold flex items-center">
              <FiFolder className="mr-2 text-gray-400" />
              {currentFolder.name}
            </h1>

            {currentFolder.description && (
              <p className="text-gray-600 mt-2">{currentFolder.description}</p>
            )}

            <div className="text-sm text-gray-500 mt-2">
              {currentFolder.subfolderCount} subfolder(s),{" "}
              {currentFolder.contentCount} item(s)
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-primary-500"
              onClick={handleEditFolder}
            >
              <FiEdit />
            </Button>
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-red-500"
              onClick={handleDeleteFolder}
            >
              <FiTrash />
            </Button>
          </div>
        </div>
      </div>

      {currentFolder.subfolders && currentFolder.subfolders.length > 0 && (
        <div className="mb-6">
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-medium">Subfolders</h2>
            <Button
              variant="ghost"
              size="sm"
              className="flex items-center text-primary-600"
              onClick={() => {
                logger.info("Opening subfolder creation modal", {
                  parentFolderId: folderId,
                });
                setIsSubfolderModalOpen(true);
              }}
            >
              <FiPlus className="mr-1" />
              Add Subfolder
            </Button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {currentFolder.subfolders.map((subfolder) => (
              <Link
                key={subfolder.id}
                to={`/folder/${subfolder.id}`}
                className="flex items-center p-4 bg-white rounded-lg shadow hover:shadow-md transition-shadow"
                onClick={() =>
                  logger.info("Navigating to subfolder", {
                    subfolderId: subfolder.id,
                    subfolderName: subfolder.name,
                  })
                }
              >
                <FiFolder className="text-gray-400 mr-3" />
                <span className="font-medium">{subfolder.name}</span>
              </Link>
            ))}
          </div>
        </div>
      )}

      <div className="mb-6 flex justify-between items-center">
        <h2 className="text-lg font-medium">Contents</h2>

        <div className="flex gap-2">
          <Button
            onClick={() => {
              logger.info("Opening text content creation", { folderId });
              setIsTextFormOpen(true);
            }}
            className="flex items-center"
            size="sm"
          >
            Add Text
          </Button>
          <Button
            onClick={() => {
              logger.info("Opening link content creation", { folderId });
              setIsLinkFormOpen(true);
            }}
            className="flex items-center"
            size="sm"
          >
            Add Link
          </Button>
          <Button
            onClick={() => {
              logger.info("Opening file upload", { folderId });
              setIsFileUploadOpen(true);
            }}
            className="flex items-center"
            size="sm"
          >
            Upload File
          </Button>
        </div>
      </div>

      {contentLoading && contents.length === 0 ? (
        <div className="flex justify-center py-8">
          <Spinner size="lg" />
        </div>
      ) : contents.length === 0 ? (
        <div className="text-center py-8 bg-white rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">
            No content in this folder
          </h3>
          <p className="mt-1 text-gray-500">
            Start adding content to this folder
          </p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {contents.map((content) => (
              <ContentCard
                key={content.id}
                content={content}
                onEdit={handleEditContent}
              />
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center mt-8">
              <nav className="flex items-center">
                <Button
                  variant="ghost"
                  disabled={currentPage === 0}
                  onClick={() => handlePageChange(currentPage - 1)}
                >
                  Previous
                </Button>
                <span className="mx-4 text-sm text-gray-700">
                  Page {currentPage + 1} of {totalPages}
                </span>
                <Button
                  variant="ghost"
                  disabled={currentPage === totalPages - 1}
                  onClick={() => handlePageChange(currentPage + 1)}
                >
                  Next
                </Button>
              </nav>
            </div>
          )}
        </>
      )}

      <FolderModal
        isOpen={isFolderModalOpen}
        onClose={() => {
          logger.info("Closing folder edit modal", { folderId });
          setIsFolderModalOpen(false);
        }}
        folder={currentFolder}
      />

      <FolderModal
        isOpen={isSubfolderModalOpen}
        onClose={() => {
          logger.info("Closing subfolder creation modal", {
            parentFolderId: folderId,
          });
          setIsSubfolderModalOpen(false);
        }}
        parentId={folderId}
      />

      <TextContentForm
        isOpen={isTextFormOpen}
        onClose={() => {
          logger.info("Closing text content form", {
            folderId,
            editingContentId: selectedContent?.id,
          });
          setIsTextFormOpen(false);
          setSelectedContent(null);
        }}
        content={
          selectedContent && selectedContent.contentType === "TEXT"
            ? selectedContent
            : null
        }
      />

      <LinkContentForm
        isOpen={isLinkFormOpen}
        onClose={() => {
          logger.info("Closing link content form", {
            folderId,
            editingContentId: selectedContent?.id,
          });
          setIsLinkFormOpen(false);
          setSelectedContent(null);
        }}
        content={
          selectedContent && selectedContent.contentType === "LINK"
            ? selectedContent
            : null
        }
      />

      <FileUploadForm
        isOpen={isFileUploadOpen}
        onClose={() => {
          logger.info("Closing file upload form", { folderId });
          setIsFileUploadOpen(false);
        }}
      />
    </Layout>
  );
};

export default FolderPage;

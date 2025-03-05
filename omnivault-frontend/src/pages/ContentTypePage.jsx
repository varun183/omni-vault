import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import {
  FiPlus,
  FiFileText,
  FiImage,
  FiVideo,
  FiLink,
  FiFile,
} from "react-icons/fi";
import { getContentByType } from "../store/slices/contentSlice";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";
import FileUploadForm from "../components/features/content/FileUploadForm";

const ContentTypePage = () => {
  const { contentType } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { contents, loading, totalElements, totalPages } = useSelector(
    (state) => state.content
  );
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedContent, setSelectedContent] = useState(null);
  const [isTextFormOpen, setIsTextFormOpen] = useState(false);
  const [isLinkFormOpen, setIsLinkFormOpen] = useState(false);
  const [isFileUploadOpen, setIsFileUploadOpen] = useState(false);

  useEffect(() => {
    if (contentType) {
      dispatch(
        getContentByType({
          contentType: contentType.toUpperCase(),
          page: currentPage,
          size: 12,
        })
      );
    }
  }, [dispatch, contentType, currentPage]);

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleEditContent = (content) => {
    setSelectedContent(content);
    if (content.contentType === "TEXT") {
      setIsTextFormOpen(true);
    } else if (content.contentType === "LINK") {
      setIsLinkFormOpen(true);
    }
  };

  const getContentTypeIcon = () => {
    switch (contentType.toUpperCase()) {
      case "TEXT":
        return <FiFileText className="text-blue-500" />;
      case "LINK":
        return <FiLink className="text-green-500" />;
      case "IMAGE":
        return <FiImage className="text-purple-500" />;
      case "VIDEO":
        return <FiVideo className="text-red-500" />;
      case "DOCUMENT":
        return <FiFile className="text-orange-500" />;
      default:
        return <FiFile className="text-gray-500" />;
    }
  };

  const getContentTypeName = () => {
    switch (contentType.toUpperCase()) {
      case "TEXT":
        return "Text Notes";
      case "LINK":
        return "Links";
      case "IMAGE":
        return "Images";
      case "VIDEO":
        return "Videos";
      case "DOCUMENT":
        return "Documents";
      default:
        return "Files";
    }
  };

  const handleAddContent = () => {
    switch (contentType.toUpperCase()) {
      case "TEXT":
        setIsTextFormOpen(true);
        break;
      case "LINK":
        setIsLinkFormOpen(true);
        break;
      default:
        setIsFileUploadOpen(true);
        break;
    }
  };

  return (
    <Layout>
      <div className="mb-6 flex justify-between items-center">
        <div className="flex items-center">
          {getContentTypeIcon()}
          <h1 className="text-2xl font-bold ml-2">{getContentTypeName()}</h1>
          <p className="text-gray-500 ml-3">
            {totalElements} {totalElements === 1 ? "item" : "items"}
          </p>
        </div>

        <Button onClick={handleAddContent} className="flex items-center">
          <FiPlus className="mr-2" />
          Add{" "}
          {contentType.toLowerCase() === "document" ? "Document" : contentType}
        </Button>
      </div>

      {loading && contents.length === 0 ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : contents.length === 0 ? (
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-gray-900">
            No {contentType.toLowerCase()} content found
          </h3>
          <p className="mt-1 text-gray-500">
            Get started by adding some {contentType.toLowerCase()} content
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

      <TextContentForm
        isOpen={isTextFormOpen}
        onClose={() => {
          setIsTextFormOpen(false);
          setSelectedContent(null);
        }}
        content={selectedContent}
      />

      <LinkContentForm
        isOpen={isLinkFormOpen}
        onClose={() => {
          setIsLinkFormOpen(false);
          setSelectedContent(null);
        }}
        content={selectedContent}
      />

      <FileUploadForm
        isOpen={isFileUploadOpen}
        onClose={() => setIsFileUploadOpen(false)}
      />
    </Layout>
  );
};

export default ContentTypePage;

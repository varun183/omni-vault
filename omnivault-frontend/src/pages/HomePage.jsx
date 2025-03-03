// src/pages/HomePage.jsx
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { FiPlus, FiFileText, FiLink, FiUpload } from "react-icons/fi";
import { getAllContent } from "../store/slices/contentSlice";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";
import FileUploadForm from "../components/features/content/FileUploadForm";

const HomePage = () => {
  const dispatch = useDispatch();
  const { contents, loading, totalElements, totalPages } = useSelector(
    (state) => state.content
  );
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedContent, setSelectedContent] = useState(null);
  const [isTextFormOpen, setIsTextFormOpen] = useState(false);
  const [isLinkFormOpen, setIsLinkFormOpen] = useState(false);
  const [isFileUploadOpen, setIsFileUploadOpen] = useState(false);

  useEffect(() => {
    dispatch(getAllContent({ page: currentPage, size: 12 }));
  }, [dispatch, currentPage]);

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

  return (
    <Layout>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">All Content</h1>
          <p className="text-gray-500">Browse and manage all your content</p>
        </div>

        <div className="flex gap-2">
          <Button
            onClick={() => setIsTextFormOpen(true)}
            className="flex items-center"
          >
            <FiFileText className="mr-2" />
            Add Text
          </Button>
          <Button
            onClick={() => setIsLinkFormOpen(true)}
            className="flex items-center"
          >
            <FiLink className="mr-2" />
            Add Link
          </Button>
          <Button
            onClick={() => setIsFileUploadOpen(true)}
            className="flex items-center"
          >
            <FiUpload className="mr-2" />
            Upload File
          </Button>
        </div>
      </div>

      {loading && contents.length === 0 ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : contents.length === 0 ? (
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-gray-900">
            No content found
          </h3>
          <p className="mt-1 text-gray-500">
            Get started by creating some content
          </p>
          <div className="mt-6 flex justify-center gap-3">
            <Button
              onClick={() => setIsTextFormOpen(true)}
              className="flex items-center"
            >
              <FiPlus className="mr-2" />
              Add Content
            </Button>
          </div>
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
        content={
          selectedContent && selectedContent.contentType === "TEXT"
            ? selectedContent
            : null
        }
      />

      <LinkContentForm
        isOpen={isLinkFormOpen}
        onClose={() => {
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
        onClose={() => setIsFileUploadOpen(false)}
      />
    </Layout>
  );
};

export default HomePage;

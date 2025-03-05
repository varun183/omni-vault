import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { FiStar } from "react-icons/fi";
import { getFavoriteContent } from "../store/slices/contentSlice";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";

const FavoritesPage = () => {
  const dispatch = useDispatch();
  const { contents, loading, totalPages } = useSelector(
    (state) => state.content
  );
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedContent, setSelectedContent] = useState(null);
  const [isTextFormOpen, setIsTextFormOpen] = useState(false);
  const [isLinkFormOpen, setIsLinkFormOpen] = useState(false);

  useEffect(() => {
    dispatch(getFavoriteContent({ page: currentPage, size: 12 }));
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
      <div className="mb-6">
        <div className="flex items-center">
          <FiStar className="text-yellow-500 mr-2" />
          <h1 className="text-2xl font-bold">Favorites</h1>
        </div>
        <p className="text-gray-500">Your favorite content in one place</p>
      </div>

      {loading && contents.length === 0 ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : contents.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">
            No favorites yet
          </h3>
          <p className="mt-1 text-gray-500">
            Mark items as favorite to see them here
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
    </Layout>
  );
};

export default FavoritesPage;

import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { FiSearch } from "react-icons/fi";
import {
  searchContent,
  clearSearchResults,
} from "../store/slices/contentSlice";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import TextContentForm from "../components/features/content/TextContentForm";
import LinkContentForm from "../components/features/content/LinkContentForm";

const SearchPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { searchResults, loading } = useSelector((state) => state.content);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedContent, setSelectedContent] = useState(null);
  const [isTextFormOpen, setIsTextFormOpen] = useState(false);
  const [isLinkFormOpen, setIsLinkFormOpen] = useState(false);

  // Get search query from URL parameters
  const queryParams = new URLSearchParams(location.search);
  const searchQuery = queryParams.get("q") || "";

  useEffect(() => {
    if (searchQuery) {
      dispatch(
        searchContent({
          query: searchQuery,
          page: currentPage,
          size: 12,
        })
      );
    } else {
      navigate("/");
    }

    return () => {
      dispatch(clearSearchResults());
    };
  }, [dispatch, searchQuery, currentPage, navigate]);

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

  if (!searchQuery) {
    return null;
  }

  return (
    <Layout>
      <div className="mb-6">
        <div className="flex items-center">
          <FiSearch className="text-gray-500 mr-2" />
          <h1 className="text-2xl font-bold">Search Results</h1>
        </div>
        <p className="text-gray-500">
          Showing results for "{searchQuery}"
          {searchResults && ` (${searchResults.totalElements} results)`}
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : searchResults && searchResults.content.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">
            No results found
          </h3>
          <p className="mt-1 text-gray-500">Try using different keywords</p>
        </div>
      ) : (
        searchResults && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {searchResults.content.map((content) => (
                <ContentCard
                  key={content.id}
                  content={content}
                  onEdit={handleEditContent}
                />
              ))}
            </div>

            {searchResults.totalPages > 1 && (
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
                    Page {currentPage + 1} of {searchResults.totalPages}
                  </span>
                  <Button
                    variant="ghost"
                    disabled={currentPage === searchResults.totalPages - 1}
                    onClick={() => handlePageChange(currentPage + 1)}
                  >
                    Next
                  </Button>
                </nav>
              </div>
            )}
          </>
        )
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

export default SearchPage;

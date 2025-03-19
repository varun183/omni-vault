import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { FiTag, FiEdit, FiTrash } from "react-icons/fi";
import { getTag, deleteTag, updateTag } from "../store/slices/tagSlice";
import { getContentByTag } from "../store/slices/contentSlice";
import Layout from "../components/layout/Layout";
import ContentCard from "../components/features/content/ContentCard";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import Modal from "../components/common/Modal";
import Input from "../components/common/Input";

const TagPage = () => {
  const { tagId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { tags, loading: tagLoading } = useSelector((state) => state.tags);
  const {
    contents,
    loading: contentLoading,
    totalPages,
  } = useSelector((state) => state.content);
  const [currentPage, setCurrentPage] = useState(0);
  const [, setSelectedContent] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [tagName, setTagName] = useState("");
  const [tagColor, setTagColor] = useState("#808080");

  const currentTag = tags.find((tag) => tag.id === tagId);

  useEffect(() => {
    dispatch(getTag(tagId));
    dispatch(getContentByTag({ tagId, page: currentPage, size: 12 }));
  }, [dispatch, tagId, currentPage]);

  useEffect(() => {
    if (currentTag) {
      setTagName(currentTag.name);
      setTagColor(currentTag.color);
    }
  }, [currentTag]);

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleEditTag = () => {
    setIsEditModalOpen(true);
  };

  const handleSaveTag = () => {
    dispatch(
      updateTag({
        tagId,
        tagData: { name: tagName, color: tagColor },
      })
    )
      .unwrap()
      .then(() => {
        setIsEditModalOpen(false);
      })
      .catch((error) => {
        console.error("Failed to update tag:", error);
      });
  };

  const handleDeleteTag = () => {
    if (
      window.confirm(
        "Are you sure you want to delete this tag? It will be removed from all content."
      )
    ) {
      dispatch(deleteTag(tagId))
        .unwrap()
        .then(() => {
          navigate("/");
        })
        .catch((error) => {
          console.error("Failed to delete tag:", error);
        });
    }
  };

  const handleEditContent = (content) => {
    setSelectedContent(content);
  };

  if (tagLoading && !currentTag) {
    return (
      <Layout>
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      </Layout>
    );
  }

  if (!currentTag) {
    return (
      <Layout>
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-gray-900">Tag not found</h3>
          <p className="mt-1 text-gray-500">
            The tag you're looking for doesn't exist or you don't have
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

  return (
    <Layout>
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-start">
          <div className="flex items-center">
            <span
              className="w-6 h-6 rounded-full mr-3"
              style={{ backgroundColor: currentTag.color }}
            ></span>
            <div>
              <h1 className="text-2xl font-bold flex items-center">
                <FiTag className="mr-2" />
                {currentTag.name}
              </h1>
              <div className="text-sm text-gray-500 mt-1">
                {currentTag.contentCount}{" "}
                {currentTag.contentCount === 1 ? "item" : "items"}
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-primary-500"
              onClick={handleEditTag}
            >
              <FiEdit />
            </Button>
            <Button
              variant="ghost"
              className="text-gray-400 hover:text-red-500"
              onClick={handleDeleteTag}
            >
              <FiTrash />
            </Button>
          </div>
        </div>
      </div>

      <h2 className="text-lg font-medium mb-4">Tagged Content</h2>

      {contentLoading && contents.length === 0 ? (
        <div className="flex justify-center py-8">
          <Spinner size="lg" />
        </div>
      ) : contents.length === 0 ? (
        <div className="text-center py-8 bg-white rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">
            No content with this tag
          </h3>
          <p className="mt-1 text-gray-500">
            Add this tag to some content to see it here
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

      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Edit Tag"
      >
        <div className="space-y-4">
          <Input
            label="Tag Name"
            value={tagName}
            onChange={(e) => setTagName(e.target.value)}
            required
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Tag Color
            </label>
            <input
              type="color"
              value={tagColor}
              onChange={(e) => setTagColor(e.target.value)}
              className="w-full h-10 rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsEditModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleSaveTag}
              disabled={!tagName.trim() || !tagColor}
            >
              Save
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
};

export default TagPage;

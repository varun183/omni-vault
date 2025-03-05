// src/pages/TagsPage.jsx
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { FiTag, FiPlus, FiEdit, FiTrash } from "react-icons/fi";
import { getAllTags, deleteTag, updateTag } from "../store/slices/tagSlice";
import Layout from "../components/layout/Layout";
import Spinner from "../components/common/Spinner";
import Button from "../components/common/Button";
import Modal from "../components/common/Modal";
import Input from "../components/common/Input";

const TagsPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { tags, loading } = useSelector((state) => state.tags);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedTag, setSelectedTag] = useState(null);
  const [tagName, setTagName] = useState("");
  const [tagColor, setTagColor] = useState("#808080");

  useEffect(() => {
    dispatch(getAllTags());
  }, [dispatch]);

  const handleEditTag = (tag, e) => {
    e.preventDefault(); // Prevent navigating to tag page
    e.stopPropagation(); // Prevent event bubbling
    setSelectedTag(tag);
    setTagName(tag.name);
    setTagColor(tag.color);
    setIsEditModalOpen(true);
  };

  const handleSaveTag = () => {
    dispatch(
      updateTag({
        tagId: selectedTag.id,
        tagData: { name: tagName, color: tagColor },
      })
    )
      .unwrap()
      .then(() => {
        setIsEditModalOpen(false);
        setSelectedTag(null);
      })
      .catch((error) => {
        console.error("Failed to update tag:", error);
      });
  };

  const handleDeleteTag = (tag, e) => {
    e.preventDefault(); // Prevent navigating to tag page
    e.stopPropagation(); // Prevent event bubbling

    if (
      window.confirm(
        "Are you sure you want to delete this tag? It will be removed from all content."
      )
    ) {
      dispatch(deleteTag(tag.id))
        .unwrap()
        .catch((error) => {
          console.error("Failed to delete tag:", error);
        });
    }
  };

  if (loading && tags.length === 0) {
    return (
      <Layout>
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-2xl font-bold">All Tags</h1>
        <Button variant="primary" className="flex items-center">
          <FiPlus className="mr-1" />
          Create Tag
        </Button>
      </div>

      {tags.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <h3 className="text-lg font-medium text-gray-900">No tags found</h3>
          <p className="mt-1 text-gray-500">
            Create tags to organize your content
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {tags.map((tag) => (
            <Link
              key={tag.id}
              to={`/tag/${tag.id}`}
              className="bg-white rounded-lg shadow p-4 hover:shadow-md transition-shadow relative"
            >
              <div className="absolute top-2 right-2">
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-gray-400 hover:text-primary-500 p-1"
                  onClick={(e) => handleEditTag(tag, e)}
                >
                  <FiEdit size={16} />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-gray-400 hover:text-red-500 p-1"
                  onClick={(e) => handleDeleteTag(tag, e)}
                >
                  <FiTrash size={16} />
                </Button>
              </div>
              <div className="flex items-center">
                <span
                  className="w-5 h-5 rounded-full mr-2"
                  style={{ backgroundColor: tag.color }}
                ></span>
                <span className="font-medium">{tag.name}</span>
              </div>
              <div className="text-sm text-gray-500 mt-2">
                {tag.contentCount || 0}{" "}
                {tag.contentCount === 1 ? "item" : "items"}
              </div>
            </Link>
          ))}
        </div>
      )}

      <Modal
        isOpen={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false);
          setSelectedTag(null);
        }}
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
              onClick={() => {
                setIsEditModalOpen(false);
                setSelectedTag(null);
              }}
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

export default TagsPage;

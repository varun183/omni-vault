import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import {
  createTextContent,
  updateContent,
} from "../../../store/slices/contentSlice";
import { getRootFolders } from "../../../store/slices/folderSlice";
import { getAllTags } from "../../../store/slices/tagSlice";
import Modal from "../../common/Modal";
import Input from "../../common/Input";
import TextArea from "../../common/TextArea";
import Button from "../../common/Button";
import Spinner from "../../common/Spinner";

const TextContentSchema = Yup.object().shape({
  title: Yup.string()
    .required("Title is required")
    .max(255, "Title must be less than 255 characters"),
  textContent: Yup.string().required("Content is required"),
  description: Yup.string().max(
    500,
    "Description must be less than 500 characters"
  ),
});

const TextContentForm = ({ isOpen, onClose, content = null }) => {
  const dispatch = useDispatch();
  const { rootFolders } = useSelector((state) => state.folders);
  const { tags, loading: tagsLoading } = useSelector((state) => state.tags);
  const { loading: contentLoading } = useSelector((state) => state.content);
  const [selectedTags, setSelectedTags] = useState([]);
  const [newTagInput, setNewTagInput] = useState("");
  const [newTags, setNewTags] = useState([]);

  useEffect(() => {
    dispatch(getRootFolders());
    dispatch(getAllTags());
  }, [dispatch]);

  useEffect(() => {
    if (content && content.tags) {
      setSelectedTags(content.tags.map((tag) => tag.id));
    } else {
      setSelectedTags([]);
    }
    setNewTags([]);
  }, [content]);

  const isEditing = !!content;

  const initialValues = {
    title: content?.title || "",
    description: content?.description || "",
    textContent: content?.textContent || "",
    folderId: content?.folderId || "",
  };

  const handleSubmit = (values) => {
    const contentData = {
      ...values,
      tagIds: selectedTags,
      newTags,
    };

    if (isEditing) {
      dispatch(
        updateContent({
          contentId: content.id,
          contentData,
        })
      )
        .unwrap()
        .then(() => {
          onClose();
        })
        .catch((error) => {
          console.error("Failed to update content:", error);
        });
    } else {
      dispatch(createTextContent(contentData))
        .unwrap()
        .then(() => {
          onClose();
        })
        .catch((error) => {
          console.error("Failed to create content:", error);
        });
    }
  };

  const handleTagToggle = (tagId) => {
    setSelectedTags((prev) =>
      prev.includes(tagId)
        ? prev.filter((id) => id !== tagId)
        : [...prev, tagId]
    );
  };

  const handleAddNewTag = (e) => {
    e.preventDefault();
    if (newTagInput.trim() && !newTags.includes(newTagInput.trim())) {
      setNewTags([...newTags, newTagInput.trim()]);
      setNewTagInput("");
    }
  };

  const handleRemoveNewTag = (tag) => {
    setNewTags(newTags.filter((t) => t !== tag));
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Edit Text Content" : "Create Text Content"}
      size="lg"
    >
      <Formik
        initialValues={initialValues}
        validationSchema={TextContentSchema}
        onSubmit={handleSubmit}
      >
        {({
          values,
          errors,
          touched,
          handleChange,
          handleBlur,
          isSubmitting,
        }) => (
          <Form>
            <Input
              label="Title"
              id="title"
              name="title"
              value={values.title}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.title && errors.title}
              required
            />

            <div className="mb-4">
              <label
                htmlFor="folderId"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Folder
              </label>
              <select
                id="folderId"
                name="folderId"
                value={values.folderId}
                onChange={handleChange}
                onBlur={handleBlur}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">No folder</option>
                {rootFolders.map((folder) => (
                  <option key={folder.id} value={folder.id}>
                    {folder.name}
                  </option>
                ))}
              </select>
            </div>

            <TextArea
              label="Description"
              id="description"
              name="description"
              value={values.description}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.description && errors.description}
            />

            <TextArea
              label="Content"
              id="textContent"
              name="textContent"
              value={values.textContent}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.textContent && errors.textContent}
              rows={8}
              required
            />

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Tags
              </label>
              <div className="flex flex-wrap gap-2 mb-2">
                {tagsLoading ? (
                  <Spinner size="sm" />
                ) : (
                  tags.map((tag) => (
                    <button
                      key={tag.id}
                      type="button"
                      className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium transition-colors ${
                        selectedTags.includes(tag.id)
                          ? "bg-primary-100 text-primary-800 border-primary-300"
                          : "bg-gray-100 text-gray-800 border-gray-300"
                      }`}
                      onClick={() => handleTagToggle(tag.id)}
                    >
                      {tag.name}
                    </button>
                  ))
                )}
              </div>

              <div className="flex items-center mt-2">
                <Input
                  placeholder="Add new tag"
                  value={newTagInput}
                  onChange={(e) => setNewTagInput(e.target.value)}
                  className="flex-1"
                />
                <Button
                  type="button"
                  onClick={handleAddNewTag}
                  className="ml-2"
                  disabled={!newTagInput.trim()}
                >
                  Add
                </Button>
              </div>

              {newTags.length > 0 && (
                <div className="mt-2">
                  <p className="text-sm text-gray-500 mb-1">New tags:</p>
                  <div className="flex flex-wrap gap-2">
                    {newTags.map((tag) => (
                      <div
                        key={tag}
                        className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800"
                      >
                        {tag}
                        <button
                          type="button"
                          className="ml-1 text-green-600 hover:text-green-800"
                          onClick={() => handleRemoveNewTag(tag)}
                        >
                          &times;
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="mt-6 flex justify-end space-x-2">
              <Button type="button" variant="secondary" onClick={onClose}>
                Cancel
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={isSubmitting || contentLoading}
              >
                {contentLoading && <Spinner size="sm" className="mr-2" />}
                {isEditing ? "Update" : "Create"}
              </Button>
            </div>
          </Form>
        )}
      </Formik>
    </Modal>
  );
};

export default TextContentForm;

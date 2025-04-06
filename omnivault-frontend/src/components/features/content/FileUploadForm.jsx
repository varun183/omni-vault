import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useDropzone } from "react-dropzone";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { FiUpload, FiFile, FiCloud, FiServer } from "react-icons/fi";
import { uploadFile } from "../../../store/slices/contentSlice";
import { getRootFolders } from "../../../store/slices/folderSlice";
import { getAllTags } from "../../../store/slices/tagSlice";
import logger from "../../../services/loggerService";
import systemService from "../../../services/systemService";
import Modal from "../../common/Modal";
import Input from "../../common/Input";
import TextArea from "../../common/TextArea";
import Button from "../../common/Button";
import Spinner from "../../common/Spinner";

const FileUploadSchema = Yup.object().shape({
  title: Yup.string()
    .max(255, "Title must be less than 255 characters")
    .optional(),
  description: Yup.string()
    .max(500, "Description must be less than 500 characters")
    .optional(),
});

const FileUploadForm = ({ isOpen, onClose }) => {
  const dispatch = useDispatch();
  const { rootFolders } = useSelector((state) => state.folders);
  const { tags, loading: tagsLoading } = useSelector((state) => state.tags);
  const { loading: contentLoading } = useSelector((state) => state.content);

  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedTags, setSelectedTags] = useState([]);
  const [newTagInput, setNewTagInput] = useState("");
  const [newTags, setNewTags] = useState([]);
  const [storageLocation, setStorageLocation] = useState("CLOUD");
  const [isCloudEnabled, setIsCloudEnabled] = useState(false);

  // Initial data loading
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        logger.info("Loading initial upload form data");
        await Promise.all([
          dispatch(getRootFolders()),
          dispatch(getAllTags()),
          checkCloudStorageStatus(),
        ]);
      } catch (error) {
        logger.error("Failed to load initial upload form data", error);
      }
    };

    if (isOpen) {
      loadInitialData();
    }
  }, [dispatch, isOpen]);

  // Cloud storage status check
  const checkCloudStorageStatus = async () => {
    try {
      const cloudStatus = await systemService.getCloudStorageStatus();
      logger.info("Cloud storage status checked", { isEnabled: cloudStatus });
      setIsCloudEnabled(cloudStatus);
    } catch (error) {
      logger.warn("Could not check cloud storage status", error);
      setIsCloudEnabled(false);
    }
  };

  // Dropzone file selection
  const onDrop = useCallback((acceptedFiles) => {
    if (acceptedFiles.length > 0) {
      const file = acceptedFiles[0];
      logger.info("File selected for upload", {
        fileName: file.name,
        fileSize: file.size,
      });
      setSelectedFile(file);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    maxSize: 100 * 1024 * 1024, // 100MB max file size
  });

  // Tag handling methods
  const handleTagToggle = (tagId) => {
    setSelectedTags((prev) =>
      prev.includes(tagId)
        ? prev.filter((id) => id !== tagId)
        : [...prev, tagId]
    );
  };

  const handleAddNewTag = (e) => {
    e.preventDefault();
    const trimmedTag = newTagInput.trim();
    if (trimmedTag && !newTags.includes(trimmedTag)) {
      logger.info("New tag added", { tagName: trimmedTag });
      setNewTags([...newTags, trimmedTag]);
      setNewTagInput("");
    }
  };

  const handleRemoveNewTag = (tag) => {
    logger.info("Removing new tag", { tagName: tag });
    setNewTags(newTags.filter((t) => t !== tag));
  };

  // Form submission handler
  const handleSubmit = async (values, { setSubmitting }) => {
    if (!selectedFile) {
      logger.warn("No file selected for upload");
      return;
    }

    const formData = {
      file: selectedFile,
      title: values.title || selectedFile.name,
      description: values.description,
      folderId: values.folderId || null,
      tagIds: selectedTags,
      newTags,
      storageLocation,
    };

    try {
      logger.info("Attempting file upload", {
        fileName: selectedFile.name,
        storageLocation,
      });

      await logger.logAsyncError(
        dispatch(uploadFile(formData)).unwrap(),
        "File upload failed"
      );

      // Refresh tags if new tags were added
      if (newTags.length > 0) {
        await dispatch(getAllTags());
      }

      // Reset form state
      logger.info("File upload successful", { fileName: selectedFile.name });
      onClose();
      resetForm();
    } catch (error) {
      // Error is already logged by logAsyncError
      setSubmitting(false);
    }
  };

  // Form reset method
  const resetForm = () => {
    setSelectedFile(null);
    setSelectedTags([]);
    setNewTags([]);
    setStorageLocation("CLOUD");
    setNewTagInput("");
  };

  // Close handler with logging
  const handleClose = () => {
    logger.info("File upload form closed", {
      fileSelected: !!selectedFile,
      tagCount: selectedTags.length,
      newTagCount: newTags.length,
    });
    resetForm();
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Upload File" size="lg">
      <Formik
        initialValues={{
          title: "",
          description: "",
          folderId: "",
        }}
        validationSchema={FileUploadSchema}
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
            {/* File Dropzone */}
            <div className="mb-4">
              <div
                {...getRootProps()}
                className={`border-2 border-dashed rounded-md p-6 flex flex-col items-center justify-center cursor-pointer ${
                  isDragActive
                    ? "border-primary-500 bg-primary-50"
                    : "border-gray-300 hover:border-primary-500"
                }`}
              >
                <input {...getInputProps()} />

                {selectedFile ? (
                  <div className="text-center">
                    <FiFile className="mx-auto h-12 w-12 text-gray-400" />
                    <p className="mt-1 text-sm text-gray-500">
                      {selectedFile.name}
                    </p>
                    <p className="text-xs text-gray-400">
                      {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                    </p>
                  </div>
                ) : (
                  <div className="text-center">
                    <FiUpload className="mx-auto h-12 w-12 text-gray-400" />
                    <p className="mt-2 text-sm text-gray-500">
                      {isDragActive
                        ? "Drop the file here"
                        : "Drag and drop a file here, or click to select a file"}
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Title Input */}
            <Input
              label="Title (optional)"
              id="title"
              name="title"
              placeholder={selectedFile ? selectedFile.name : "Enter title"}
              value={values.title}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.title && errors.title}
            />

            {/* Folder Selection */}
            <div className="mb-4">
              <label
                htmlFor="folderId"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Folder (optional)
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

            {/* Description Input */}
            <TextArea
              label="Description (optional)"
              id="description"
              name="description"
              value={values.description}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.description && errors.description}
            />

            {/* Tag Selection */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Tags (optional)
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

              {/* New Tag Input */}
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

              {/* New Tags Display */}
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

            {/* Submit Buttons */}
            <div className="mt-6 flex justify-end space-x-2">
              <Button type="button" variant="secondary" onClick={handleClose}>
                Cancel
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={!selectedFile || isSubmitting || contentLoading}
              >
                {contentLoading && <Spinner size="sm" className="mr-2" />}
                Upload
              </Button>
            </div>
          </Form>
        )}
      </Formik>
    </Modal>
  );
};

export default FileUploadForm;

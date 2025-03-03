import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useDropzone } from "react-dropzone";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { FiUpload, FiFile } from "react-icons/fi";
import { uploadFile } from "../../../store/slices/contentSlice";
import { getRootFolders } from "../../../store/slices/folderSlice";
import { getAllTags } from "../../../store/slices/tagSlice";
import Modal from "../../common/Modal";
import Input from "../../common/Input";
import TextArea from "../../common/TextArea";
import Button from "../../common/Button";
import Spinner from "../../common/Spinner";

const FileUploadSchema = Yup.object().shape({
  title: Yup.string().max(255, "Title must be less than 255 characters"),
  description: Yup.string().max(
    500,
    "Description must be less than 500 characters"
  ),
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

  useEffect(() => {
    dispatch(getRootFolders());
    dispatch(getAllTags());
  }, [dispatch]);

  const onDrop = useCallback((acceptedFiles) => {
    if (acceptedFiles.length > 0) {
      setSelectedFile(acceptedFiles[0]);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  const initialValues = {
    title: "",
    description: "",
    folderId: "",
  };

  const handleSubmit = (values) => {
    if (!selectedFile) {
      return;
    }

    const formData = {
      file: selectedFile,
      title: values.title || selectedFile.name,
      description: values.description,
      folderId: values.folderId || null,
      tagIds: selectedTags,
      newTags,
    };

    dispatch(uploadFile(formData))
      .unwrap()
      .then(() => {
        onClose();
        setSelectedFile(null);
        setSelectedTags([]);
        setNewTags([]);
      })
      .catch((error) => {
        console.error("Failed to upload file:", error);
      });
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

  const handleClose = () => {
    setSelectedFile(null);
    setSelectedTags([]);
    setNewTags([]);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Upload File" size="lg">
      <Formik
        initialValues={initialValues}
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

            <TextArea
              label="Description (optional)"
              id="description"
              name="description"
              value={values.description}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.description && errors.description}
            />

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

import React from "react";
import { useDispatch } from "react-redux";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { createFolder, updateFolder } from "../../../store/slices/folderSlice";
import Modal from "../../common/Modal";
import Input from "../../common/Input";
import TextArea from "../../common/TextArea";
import Button from "../../common/Button";

const FolderSchema = Yup.object().shape({
  name: Yup.string()
    .min(1, "Name must be at least 1 character")
    .max(100, "Name must be less than 100 characters")
    .required("Name is required"),
  description: Yup.string().max(
    500,
    "Description must be less than 500 characters"
  ),
});

const FolderModal = ({ isOpen, onClose, folder = null, parentId = null }) => {
  const dispatch = useDispatch();
  const isEditing = !!folder;

  const initialValues = {
    name: folder?.name || "",
    description: folder?.description || "",
    parentId: folder?.parentId || parentId,
  };

  const handleSubmit = (values) => {
    if (isEditing) {
      dispatch(updateFolder({ folderId: folder.id, folderData: values }))
        .unwrap()
        .then(() => {
          onClose();
        })
        .catch((error) => {
          console.error("Failed to update folder:", error);
        });
    } else {
      dispatch(createFolder(values))
        .unwrap()
        .then(() => {
          onClose();
        })
        .catch((error) => {
          console.error("Failed to create folder:", error);
        });
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Edit Folder" : "Create Folder"}
    >
      <Formik
        initialValues={initialValues}
        validationSchema={FolderSchema}
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
              label="Name"
              id="name"
              name="name"
              value={values.name}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.name && errors.name}
              required
            />

            <TextArea
              label="Description"
              id="description"
              name="description"
              value={values.description}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.description && errors.description}
            />

            <div className="mt-4 flex justify-end space-x-2">
              <Button type="button" variant="secondary" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" disabled={isSubmitting}>
                {isEditing ? "Update" : "Create"}
              </Button>
            </div>
          </Form>
        )}
      </Formik>
    </Modal>
  );
};

export default FolderModal;

import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { toast } from "react-toastify";
import Layout from "../components/layout/Layout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Modal from "../components/common/Modal";
import Alert from "../components/common/Alert";
import logger from "../services/loggerService";
import {
  updateProfile,
  changePassword,
  deleteAccount,
  clearError,
} from "../store/slices/authSlice";
import { Formik, Form } from "formik";
import * as Yup from "yup";

const ProfileUpdateSchema = Yup.object().shape({
  firstName: Yup.string().max(50, "First name must be less than 50 characters"),
  lastName: Yup.string().max(50, "Last name must be less than 50 characters"),
});

const ChangePasswordSchema = Yup.object().shape({
  currentPassword: Yup.string().required("Current password is required"),
  newPassword: Yup.string()
    .min(8, "Password must be at least 8 characters")
    .notOneOf(
      [Yup.ref("currentPassword")],
      "New password must be different from current password"
    )
    .required("New password is required"),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref("newPassword")], "Passwords must match")
    .required("Confirm password is required"),
});

const DeleteAccountSchema = Yup.object().shape({
  confirmPassword: Yup.string().required("Password confirmation is required"),
  confirmDeletion: Yup.boolean().oneOf(
    [true],
    "You must confirm account deletion"
  ),
});

const SettingsPage = () => {
  const dispatch = useDispatch();
  const { user, error, loading } = useSelector((state) => state.auth);
  const [activeTab, setActiveTab] = useState("profile");
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  useEffect(() => {
    if (!loading) {
      dispatch(clearError());
    }
  }, [activeTab, loading, dispatch]);

  const handleProfileUpdate = async (values, { setSubmitting }) => {
    try {
      const resultAction = dispatch(updateProfile(values));

      if (updateProfile.fulfilled.match(resultAction)) {
        toast.success(
          resultAction.payload.successMessage || "Profile updated successfully"
        );
      }

      setSubmitting(false);
    } catch (error) {
      logger.error("Profile update failed", error);
      setSubmitting(false);
    }
  };

  const handlePasswordChange = async (values, { setSubmitting, resetForm }) => {
    try {
      const resultAction = dispatch(
        changePassword({
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
        })
      );

      if (changePassword.fulfilled.match(resultAction)) {
        toast.success(
          resultAction.payload.successMessage || "Password changed successfully"
        );

        resetForm();
      }

      setSubmitting(false);
    } catch (error) {
      logger.error("Password change failed", error);
      setSubmitting(false);
    }
  };

  const handleAccountDeletion = async (values, { setSubmitting }) => {
    try {
      await dispatch(
        deleteAccount({ password: values.confirmPassword })
      ).unwrap();

      logger.info("Account deleted successfully");
    } catch (error) {
      logger.error("Account deletion failed", error);
      setSubmitting(false);
    }
  };

  const renderProfileUpdateForm = () => (
    <Formik
      initialValues={{
        firstName: user?.firstName || "",
        lastName: user?.lastName || "",
      }}
      validationSchema={ProfileUpdateSchema}
      onSubmit={handleProfileUpdate}
    >
      {({
        values,
        errors,
        touched,
        handleChange,
        handleBlur,
        isSubmitting,
        handleSubmit,
      }) => (
        <Form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="First Name"
              id="firstName"
              name="firstName"
              value={values.firstName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.firstName && errors.firstName}
            />
            <Input
              label="Last Name"
              id="lastName"
              name="lastName"
              value={values.lastName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.lastName && errors.lastName}
            />
          </div>
          <div className="mt-6">
            <Button type="submit" variant="primary" disabled={isSubmitting}>
              Update Profile
            </Button>
          </div>
        </Form>
      )}
    </Formik>
  );

  const renderChangePasswordForm = () => (
    <Formik
      initialValues={{
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      }}
      validationSchema={ChangePasswordSchema}
      onSubmit={handlePasswordChange}
    >
      {({
        values,
        errors,
        touched,
        handleChange,
        handleBlur,
        isSubmitting,
        handleSubmit,
      }) => (
        <Form onSubmit={handleSubmit}>
          <Input
            label="Current Password"
            id="currentPassword"
            name="currentPassword"
            type="password"
            value={values.currentPassword}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.currentPassword && errors.currentPassword}
            required
          />
          <Input
            label="New Password"
            id="newPassword"
            name="newPassword"
            type="password"
            value={values.newPassword}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.newPassword && errors.newPassword}
            required
          />
          <Input
            label="Confirm New Password"
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            value={values.confirmPassword}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.confirmPassword && errors.confirmPassword}
            required
          />
          <div className="mt-6">
            <Button type="submit" variant="primary" disabled={isSubmitting}>
              Change Password
            </Button>
          </div>
        </Form>
      )}
    </Formik>
  );

  const renderDeleteAccountModal = () => (
    <Modal
      isOpen={isDeleteModalOpen}
      onClose={() => setIsDeleteModalOpen(false)}
      title="Delete Account"
      size="md"
    >
      <Formik
        initialValues={{
          confirmPassword: "",
          confirmDeletion: false,
        }}
        validationSchema={DeleteAccountSchema}
        onSubmit={handleAccountDeletion}
      >
        {({
          values,
          errors,
          touched,
          handleChange,
          handleBlur,
          isSubmitting,
          handleSubmit,
        }) => (
          <Form onSubmit={handleSubmit}>
            <div className="text-red-600 mb-4">
              <p className="font-bold">
                Warning: This action cannot be undone!
              </p>
              <p>
                All your data, including contents, folders, and tags will be
                permanently deleted.
              </p>
            </div>
            <Input
              label="Confirm Password"
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              value={values.confirmPassword}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.confirmPassword && errors.confirmPassword}
              required
            />
            <div className="mt-4">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  name="confirmDeletion"
                  checked={values.confirmDeletion}
                  onChange={handleChange}
                  className="mr-2"
                />
                <span>
                  I understand this will permanently delete my account
                </span>
              </label>
              {touched.confirmDeletion && errors.confirmDeletion && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.confirmDeletion}
                </p>
              )}
            </div>
            <div className="mt-6 flex space-x-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => setIsDeleteModalOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit" variant="danger" disabled={isSubmitting}>
                Permanently Delete Account
              </Button>
            </div>
          </Form>
        )}
      </Formik>
    </Modal>
  );

  return (
    <Layout>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold mb-6">Account Settings</h1>

        {error && (
          <Alert
            type="error"
            message={error}
            className="mb-4"
            onClose={() => dispatch(clearError())}
          />
        )}

        <div className="flex border-b mb-6">
          {[
            { key: "profile", label: "Profile" },
            { key: "password", label: "Change Password" },
            { key: "delete", label: "Delete Account" },
          ].map((tab) => (
            <button
              key={tab.key}
              className={`px-4 py-2 ${
                activeTab === tab.key
                  ? "border-b-2 border-primary-500 text-primary-500"
                  : "text-gray-500"
              }`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="max-w-xl">
          {activeTab === "profile" && renderProfileUpdateForm()}
          {activeTab === "password" && renderChangePasswordForm()}
          {activeTab === "delete" && (
            <div>
              <p className="text-red-600 mb-4">
                Deleting your account is a permanent action and cannot be
                undone.
              </p>
              <Button
                variant="danger"
                onClick={() => setIsDeleteModalOpen(true)}
              >
                Delete Account
              </Button>
            </div>
          )}
        </div>

        {renderDeleteAccountModal()}
      </div>
    </Layout>
  );
};

export default SettingsPage;

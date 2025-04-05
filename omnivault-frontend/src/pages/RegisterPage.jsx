import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import {
  register,
  clearError,
  setVerificationEmail,
} from "../store/slices/authSlice";
import logger from "../services/loggerService";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import Spinner from "../components/common/Spinner";

const RegisterSchema = Yup.object().shape({
  username: Yup.string()
    .min(3, "Username must be at least 3 characters")
    .max(50, "Username must be less than 50 characters")
    .required("Username is required"),
  email: Yup.string()
    .email("Invalid email address")
    .required("Email is required"),
  password: Yup.string()
    .min(8, "Password must be at least 8 characters")
    .required("Password is required"),
  firstName: Yup.string().max(50, "First name must be less than 50 characters"),
  lastName: Yup.string().max(50, "Last name must be less than 50 characters"),
});

const RegisterPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { error } = useSelector((state) => state.auth);
  const [showError, setShowError] = useState(false);

  useEffect(() => {
    // Log registration page access
    logger.info("Registration page accessed");
  }, []);

  useEffect(() => {
    if (error) {
      setShowError(true);

      // Log registration error
      logger.error("Registration attempt failed", error, {
        errorType: "RegistrationError",
      });
    }
  }, [error]);

  const handleCloseError = () => {
    setShowError(false);
    dispatch(clearError());

    // Log error dismissal
    logger.info("Registration error alert dismissed");
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      // Log registration attempt
      logger.info("Registration attempt initiated", {
        username: values.username,
        email: values.email,
      });

      // Use logAsyncError to handle potential registration failures
      await logger.logAsyncError(
        dispatch(register(values)).unwrap(),
        "Registration failed",
        { username: values.username, email: values.email }
      );

      // Log successful registration
      logger.info("User registered successfully", {
        username: values.username,
        email: values.email,
      });

      // Store verification email
      dispatch(setVerificationEmail(values.email));

      // Navigate to verification page
      navigate("/verify-email");
    } catch (error) {
      // Error is already logged by logAsyncError
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">
        Create a new account
      </h2>

      {showError && (
        <Alert
          type="error"
          message={error}
          onClose={handleCloseError}
          className="mb-4"
        />
      )}

      <Formik
        initialValues={{
          username: "",
          email: "",
          password: "",
          firstName: "",
          lastName: "",
        }}
        validationSchema={RegisterSchema}
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
              label="Username"
              id="username"
              name="username"
              type="text"
              value={values.username}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.username && errors.username}
              required
            />

            <Input
              label="Email"
              id="email"
              name="email"
              type="email"
              value={values.email}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.email && errors.email}
              required
            />

            <Input
              label="Password"
              id="password"
              name="password"
              type="password"
              value={values.password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.password && errors.password}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="First Name"
                id="firstName"
                name="firstName"
                type="text"
                value={values.firstName}
                onChange={handleChange}
                onBlur={handleBlur}
                error={touched.firstName && errors.firstName}
              />

              <Input
                label="Last Name"
                id="lastName"
                name="lastName"
                type="text"
                value={values.lastName}
                onChange={handleChange}
                onBlur={handleBlur}
                error={touched.lastName && errors.lastName}
              />
            </div>

            <div className="mt-6">
              <Button
                type="submit"
                variant="primary"
                fullWidth
                disabled={isSubmitting}
              >
                {isSubmitting ? <Spinner size="sm" className="mr-2" /> : null}
                Create Account
              </Button>
            </div>
          </Form>
        )}
      </Formik>

      <div className="mt-4 text-center">
        <p className="text-sm text-gray-600">
          Already have an account?{" "}
          <Link
            to="/login"
            className="text-primary-600 hover:text-primary-500"
            onClick={() => logger.info("Navigating to login page")}
          >
            Sign in
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
};

export default RegisterPage;

import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { login, clearError } from "../store/slices/authSlice";
import logger from "../services/loggerService";
import { apiCache } from "../utils/apiCache";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import Spinner from "../components/common/Spinner";

const LoginSchema = Yup.object().shape({
  usernameOrEmail: Yup.string().required("Username or email is required"),
  password: Yup.string().required("Password is required"),
});

const LoginPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { error } = useSelector((state) => state.auth);
  const [showError, setShowError] = useState(false);

  useEffect(() => {
    // Clear any existing API cache
    apiCache.clearAll();

    // Log page visit
    logger.info("Login page accessed");
  }, []);

  useEffect(() => {
    if (error) {
      setShowError(true);

      // Log login error with context
      logger.error("Login attempt failed", error, {
        errorType: "AuthenticationError",
      });
    }
  }, [error]);

  const handleCloseError = () => {
    setShowError(false);
    dispatch(clearError());

    // Log error dismissal
    logger.info("Login error alert dismissed");
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      // Log login attempt
      logger.info("Login attempt initiated", {
        usernameOrEmail: values.usernameOrEmail,
      });

      // Clear any existing cache before login
      apiCache.clearAll();

      // Use logAsyncError to handle potential login failures
      await logger.logAsyncError(
        dispatch(login(values)).unwrap(),
        "Login failed",
        { usernameOrEmail: values.usernameOrEmail }
      );

      // Log successful login
      logger.info("User logged in successfully", {
        usernameOrEmail: values.usernameOrEmail,
      });

      // Navigate to home page after successful login
      navigate("/");
    } catch (error) {
      // Error is already logged by logAsyncError
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">
        Sign in to your account
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
        initialValues={{ usernameOrEmail: "", password: "" }}
        validationSchema={LoginSchema}
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
              label="Username or Email"
              id="usernameOrEmail"
              name="usernameOrEmail"
              type="text"
              value={values.usernameOrEmail}
              onChange={handleChange}
              onBlur={handleBlur}
              error={touched.usernameOrEmail && errors.usernameOrEmail}
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

            <div className="mt-6">
              <Button
                type="submit"
                variant="primary"
                fullWidth
                disabled={isSubmitting}
              >
                {isSubmitting ? <Spinner size="sm" className="mr-2" /> : null}
                Sign in
              </Button>
            </div>
          </Form>
        )}
      </Formik>

      <div className="mt-4 text-center">
        <p className="text-sm text-gray-600">
          Don't have an account?{" "}
          <Link
            to="/register"
            className="text-primary-600 hover:text-primary-500"
            onClick={() => logger.info("Navigating to registration page")}
          >
            Sign up
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
};

export default LoginPage;

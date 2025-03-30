import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useSearchParams } from "react-router-dom";
import { Formik, Form } from "formik";
import * as Yup from "yup";
import { login, clearError } from "../store/slices/authSlice";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import Spinner from "../components/common/Spinner";
import { apiCache } from "../utils/apiCache";

const LoginSchema = Yup.object().shape({
  usernameOrEmail: Yup.string().required("Username or email is required"),
  password: Yup.string().required("Password is required"),
});

const LoginPage = () => {
  const dispatch = useDispatch();
  const { loading, error } = useSelector((state) => state.auth);
  const [showError, setShowError] = useState(false);
  const [searchParams] = useSearchParams();
  const [showVerifiedMessage, setShowVerifiedMessage] = useState(false);

  useEffect(() => {
    apiCache.clearAll(); // Clear ALL cache, regardless of user
  }, []);

  useEffect(() => {
    if (error) {
      setShowError(true);
    }
  }, [error]);

  useEffect(() => {
    if (searchParams.get("verified") === "true") {
      setShowVerifiedMessage(true);
    }
  }, [searchParams]);

  const handleCloseError = () => {
    setShowError(false);
    dispatch(clearError());
  };

  const handleSubmit = (values) => {
    apiCache.clearAll();
    dispatch(login(values));
  };

  return (
    <AuthLayout>
      <h2 className="text-2xl font-bold text-center mb-6">
        Sign in to your account
      </h2>

      {showVerifiedMessage && (
        <Alert
          type="success"
          message="Your email has been verified! You can now log in."
          className="mb-4"
          onClose={() => setShowVerifiedMessage(false)}
        />
      )}

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
        {({ values, errors, touched, handleChange, handleBlur }) => (
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
                disabled={loading}
              >
                {loading ? <Spinner size="sm" className="mr-2" /> : null}
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
          >
            Sign up
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
};

export default LoginPage;

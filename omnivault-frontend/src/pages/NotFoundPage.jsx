import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { FiArrowLeft } from "react-icons/fi";
import Button from "../components/common/Button";
import Layout from "../components/layout/Layout";

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <Layout>
      <div className="flex flex-col items-center justify-center py-12">
        <h1 className="text-6xl font-bold text-gray-900">404</h1>
        <h2 className="mt-4 text-xl font-medium text-gray-600">
          Page Not Found
        </h2>
        <p className="mt-2 text-gray-500">
          The page you are looking for does not exist.
        </p>
        <div className="mt-8 flex space-x-4">
          <Button
            variant="ghost"
            className="flex items-center"
            onClick={() => navigate(-1)}
          >
            <FiArrowLeft className="mr-2" />
            Go Back
          </Button>
          <Button as={Link} to="/">
            Go Home
          </Button>
        </div>
      </div>
    </Layout>
  );
};

export default NotFoundPage;

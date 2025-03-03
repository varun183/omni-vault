import React from "react";
import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

const Layout = ({ children }) => {
  const { isAuthenticated, loading } = useSelector((state) => state.auth);

  if (!isAuthenticated && !loading) {
    return <Navigate to="/login" />;
  }

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
};

export default Layout;

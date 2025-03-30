import React, { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { Navigate, useLocation } from "react-router-dom";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";
import { clearCurrentContent } from "../../store/slices/contentSlice";
import { clearCurrentFolder } from "../../store/slices/folderSlice";

const Layout = ({ children }) => {
  const { isAuthenticated, loading } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const location = useLocation();

  useEffect(() => {
    return () => {
      dispatch(clearCurrentContent());
      dispatch(clearCurrentFolder());
    };
  }, [location.pathname, dispatch]);

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

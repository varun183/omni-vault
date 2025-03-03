import React, { useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useDispatch } from "react-redux";
import "react-toastify/dist/ReactToastify.css";
import { getCurrentUser } from "./store/slices/authSlice";
import LoginPage from "./pages/LoginPage";
import Navbar from "./components/layout/Navbar";

function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    if (localStorage.getItem("access_token")) {
      dispatch(getCurrentUser());
    }
  }, [dispatch]);

  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        {/* {import.meta.env.DEV && <ReduxTester />} */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;

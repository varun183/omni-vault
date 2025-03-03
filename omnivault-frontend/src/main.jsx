import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import store from "../src/store/index.js";
import "./index.css";
import App from "./App.jsx";
import { Provider } from "react-redux";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </StrictMode>
);

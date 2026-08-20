import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import { App } from "./App";

// The console is a single state-driven page (tabs, not routes), so no router is needed.
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

import React from "react";
import ReactDOM from "react-dom/client";
import { App } from "./presentation/App";
import { createHttpApi } from "./infrastructure/httpApi";
import { createBrowserStorage } from "./infrastructure/storage";
import "./presentation/ui/styles.css";

const storage = createBrowserStorage();
const api = createHttpApi(() => storage.loadSession()?.token ?? "");

const root = ReactDOM.createRoot(document.getElementById("root")!);

root.render(
  <React.StrictMode>
    <App api={api} storage={storage} />
  </React.StrictMode>
);

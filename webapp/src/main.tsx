import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import "maplibre-gl/dist/maplibre-gl.css";
import "@fortawesome/fontawesome-free/css/fontawesome.min.css";
import "@fortawesome/fontawesome-free/css/solid.min.css";
import "./index.css";
import { App } from "./App";
import { I18nProvider } from "./i18n";
import { CountryProvider } from "./state";

// HashRouter: the app is served as a static SPA under /app with no server-side route fallback, so hash
// URLs (/app/#/guia) are always resolved by index.html — deep links + shareable section URLs, no extra
// Ktor config. (Can move to BrowserRouter later if we add an SPA fallback route.)
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <I18nProvider>
      <CountryProvider>
        <HashRouter>
          <App />
        </HashRouter>
      </CountryProvider>
    </I18nProvider>
  </React.StrictMode>,
);

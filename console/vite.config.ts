/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Served by the Ktor server under /console (staticResources("/console", "console")), so the built
// asset URLs must be prefixed with /console/.
export default defineConfig({
  base: "/console/",
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
  },
  server: {
    // Dev proxy so `npm run dev` can call the local API without CORS.
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});

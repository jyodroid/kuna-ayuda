/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Served by the Ktor server under /app (staticResources("/app", "app")), so the built asset URLs
// must be prefixed with /app/. Same-origin in production, so /api needs no CORS.
export default defineConfig({
  base: "/app/",
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

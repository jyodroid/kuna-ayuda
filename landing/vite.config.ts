import { defineConfig } from "vite";
import { resolve } from "node:path";

// Multi-page static build. Output goes to dist/ and is copied into the Ktor server's
// src/main/resources/web (see server/build.gradle.kts buildLanding task) so the fat jar serves it.
export default defineConfig({
  base: "/",
  // Dev-only: the console lives on the Ktor server. Proxy /console (and /api) to :8080 so the footer
  // "Moderadores" link works while running the landing dev server. In production both are same-origin.
  server: {
    proxy: {
      "/console": "http://localhost:8080",
      "/api": "http://localhost:8080",
    },
  },
  build: {
    outDir: "dist",
    emptyOutDir: true,
    rollupOptions: {
      input: {
        index: resolve(__dirname, "index.html"),
        privacy: resolve(__dirname, "privacy.html"),
        terms: resolve(__dirname, "terms.html"),
      },
    },
  },
});

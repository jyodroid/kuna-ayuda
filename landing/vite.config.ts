import { defineConfig } from "vite";
import { resolve } from "node:path";

// Multi-page static build. Output goes to dist/ and is copied into the Ktor server's
// src/main/resources/web (see server/build.gradle.kts buildLanding task) so the fat jar serves it.
export default defineConfig({
  base: "/",
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

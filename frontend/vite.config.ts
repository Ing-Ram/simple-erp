import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

/** Vite config: React plugin and a dev proxy so /api hits the Spring backend. */
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});

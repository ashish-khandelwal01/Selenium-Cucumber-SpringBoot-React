import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";
import path from "path";

export default defineConfig({
  plugins: [
    react(),
    tsconfigPaths(),
  ],
  optimizeDeps: {
    include: ["react-data-grid"],
  },
  resolve: {
    dedupe: ["react", "react-dom"],
    alias: {
      "react-data-grid": path.resolve(
        __dirname,
        "node_modules/react-data-grid/lib/index.js"
      ),
    },
  },
  server: {
    host: "0.0.0.0",
    port: 3000,
    watch: {
      usePolling: true,
    },
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});

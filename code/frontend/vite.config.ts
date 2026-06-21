import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    // Собираем прямо в Spring static/, чтобы backend отдавал готовые ассеты.
    outDir: path.resolve(__dirname, "../backend/src/main/resources/static"),
    emptyOutDir: false,
    assetsDir: "assets",
  },
});


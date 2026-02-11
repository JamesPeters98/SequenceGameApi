import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

const configuredApiUrl = process.env.VITE_API_URL?.trim()
const apiProxyTarget = process.env.VITE_API_PROXY_TARGET ?? "http://localhost:8080"
const useApiProxy = !configuredApiUrl

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  ...(useApiProxy
    ? {
        server: {
          proxy: {
            "/api": {
              target: apiProxyTarget,
              changeOrigin: true,
              rewrite: (path) => path.replace(/^\/api/, ""),
            },
          },
        },
      }
    : {}),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})

import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

const configuredApiUrl = process.env.VITE_API_URL?.trim()
const apiProxyTarget = process.env.VITE_API_PROXY_TARGET ?? "http://localhost:8080"
const useApiProxy = !configuredApiUrl
const configuredAllowedHosts = process.env.VITE_ALLOWED_HOSTS?.trim()

const allowedHosts =
  configuredAllowedHosts === "true" || configuredAllowedHosts === "*"
    ? true
    : configuredAllowedHosts
      ? configuredAllowedHosts
          .split(",")
          .map((host) => host.trim())
          .filter(Boolean)
      : undefined

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    ...(allowedHosts !== undefined ? { allowedHosts } : {}),
    ...(useApiProxy
      ? {
          proxy: {
            "/api": {
              target: apiProxyTarget,
              changeOrigin: true,
              rewrite: (path) => path.replace(/^\/api/, ""),
            },
          },
        }
      : {}),
  },
  preview: {
    ...(allowedHosts !== undefined ? { allowedHosts } : {}),
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})

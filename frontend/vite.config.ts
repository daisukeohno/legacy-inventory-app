import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// /api へのリクエストは Spring Boot バックエンド(8080)にプロキシする
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.API_BASE_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})

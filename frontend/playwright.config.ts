import { defineConfig } from '@playwright/test'

/**
 * E2E は起動済みのバックエンド(http://localhost:8080)を前提とし、
 * フロントエンドの dev サーバーは Playwright が起動する。
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'npm run dev -- --port 5173',
    url: 'http://localhost:5173',
    reuseExistingServer: true,
    timeout: 60_000,
  },
})

import { defineConfig, devices } from '@playwright/test';

/** 배포된 FE 대상 E2E. 기본 타깃은 홈서버(내부 IP):13000. */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: { timeout: 10_000 },
  retries: 0,
  reporter: [['list'], ['json', { outputFile: 'e2e-results.json' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://172.30.1.79:13000',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});

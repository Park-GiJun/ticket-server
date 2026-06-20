import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 13000,
    proxy: {
      '/api': {
        // 게이트웨이(단일 진입점). 개발도 실제 서버 인프라를 쓰지만 게이트웨이는 로컬 구동.
        target: 'http://localhost:18080',
        changeOrigin: true,
      },
    },
  },
});

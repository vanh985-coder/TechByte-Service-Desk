import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Mọi request bắt đầu bằng /api sẽ được chuyển hướng sang backend
      '/api': {
        target: 'http://localhost:8080', // <-- Sửa cổng này nếu Backend chạy cổng khác
        changeOrigin: true,
        // Nếu backend của bạn KHÔNG có chữ /api ở đầu URL, hãy mở comment dòng dưới:
        // rewrite: (path) => path.replace(/^\/api/, ''), 
      },
    },
  },
});
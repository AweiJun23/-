import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发阶段将 /api 代理到网关（Spring Cloud Gateway，端口 9000）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true
      }
    }
  }
})

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: './', // 让静态资源使用相对路径
  rollupOptions: {
    output: {
      inlineDynamicImports: true
    }
  },
  build: {
    assetsDir: 'assets', // 静态资源存放路径
    sourcemap: true // 生成 source map 以便调试
  },
  server: {
    host: '0.0.0.0', // 允许局域网访问
    port: 5173
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
})

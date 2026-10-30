import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    server: {
        port: 5173,
        proxy: {
            // 将前端 /api 开头的请求 → 转发到后端 8080
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            }
        }
    }
})
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5889,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/mqtt': {
        target: 'ws://localhost:8083',
        ws: true,
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/mqtt/, '')
      }
    }
  }
})
import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:18080'

  return {
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
          target: proxyTarget,
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
  }
})
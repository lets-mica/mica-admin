import { defineConfig, loadEnv } from 'vite'
import Vue from '@vitejs/plugin-vue'
import VueJsx from '@vitejs/plugin-vue-jsx'
import VueDevtools from 'vite-plugin-vue-devtools'
import Tailwindcss from '@tailwindcss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'
import { existsSync, statSync } from 'fs'

const root = resolve(__dirname)

// Vben package resolver plugin
function vbenResolver(): import('vite').Plugin {
  return {
    name: 'vben-resolver',
    resolveId(id) {
      function tryResolve(base) {
        if (existsSync(base + '.ts')) return base + '.ts'
        if (existsSync(base) && !statSync(base).isDirectory()) return base
        const index = base + '/index.ts'
        if (existsSync(index)) return index
        return null
      }

      // @vben/styles/naive → css file
      if (id === '@vben/styles/naive') {
        return resolve(root, 'vben/packages/styles/src/naive/index.css')
      }

      // @vben/common-ui/es/* → components subdir
      const esMatch = id.match(/^@vben\/common-ui\/es\/(.+)$/)
      if (esMatch) {
        const p = resolve(root, `vben/packages/common-ui/src/components/${esMatch[1]}`)
        const resolved = tryResolve(p)
        if (resolved) return resolved
      }

      // @vben/plugins/* → plugin subdir
      const pluginMatch = id.match(/^@vben\/plugins\/(.+)$/)
      if (pluginMatch && pluginMatch[1] !== 'plugins') {
        const p = resolve(root, `vben/packages/plugins/src/${pluginMatch[1]}`)
        const resolved = tryResolve(p)
        if (resolved) return resolved
      }

      // @vben/xxx → package
      const vbenMatch = id.match(/^@vben\/([^/]+)(?:\/(.+))?$/)
      if (vbenMatch) {
        // tailwind-config is in vben/tailwind-config, not vben/packages
        if (vbenMatch[1] === 'tailwind-config') {
          const pkgDir = resolve(root, `vben/tailwind-config`)
          if (vbenMatch[2]) {
            const resolved = tryResolve(resolve(pkgDir, `src/${vbenMatch[2]}`))
            if (resolved) return resolved
          }
          const resolved = tryResolve(resolve(pkgDir, 'src/index'))
          if (resolved) return resolved
        }
        const pkgDir = resolve(root, `vben/packages/${vbenMatch[1]}`)
        if (vbenMatch[2]) {
          const resolved = tryResolve(resolve(pkgDir, `src/${vbenMatch[2]}`))
          if (resolved) return resolved
        }
        const resolved = tryResolve(resolve(pkgDir, 'src/index'))
        if (resolved) return resolved
      }

      // @vben-core/xxx/subpath → package
      const coreMatch = id.match(/^@vben-core\/([^/]+)(?:\/(.+))?$/)
      if (coreMatch) {
        const pkgDir = resolve(root, `vben/core/${coreMatch[1]}`)
        if (coreMatch[2]) {
          const resolved = tryResolve(resolve(pkgDir, `src/${coreMatch[2]}`))
          if (resolved) return resolved
        }
        const resolved = tryResolve(resolve(pkgDir, 'src/index'))
        if (resolved) return resolved
      }
    }
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, root, '')
  return {
    plugins: [
      vbenResolver(),
      Vue({
        include: [/\.vue$/],
        template: {
          compilerOptions: {
            preserveWhitespace: true,
          },
        },
      }),
      VueJsx(),
      VueDevtools(),
      Tailwindcss(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          {
            'naive-ui': [
              'useDialog',
              'useMessage',
              'useNotification',
              'useLoadingBar',
            ],
          },
        ],
        dts: 'src/auto-imports.d.ts',
        dirs: [],
      }),
      Components({
        resolvers: [NaiveUiResolver()],
        dts: 'src/components.d.ts',
      }),
    ],
    resolve: {
      alias: {
        '#': resolve(root, 'src'),
        '@': resolve(root, 'src'),
      },
    },
    server: {
      host: true,
      port: 5888,
      proxy: {
        '/api': {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          ws: true,
        },
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
      rollupOptions: {
        output: {
          manualChunks: {
            'naive-ui': ['naive-ui'],
          },
        },
      },
    },
  }
})

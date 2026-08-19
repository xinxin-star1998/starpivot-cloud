import {defineConfig} from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'node:path'

export default defineConfig(({command}) => ({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    minify: command === 'build' ? 'esbuild' : false
  }
}))

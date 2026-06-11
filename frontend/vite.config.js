import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    // Increase warning limit to avoid false positives for large but split chunks;
    // still prefer to split by route (see router dynamic imports).
    chunkSizeWarningLimit: 1200, // in kB
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          // split core frameworks and large libs into separate chunks
          if (id.includes('node_modules/vuetify') || id.includes('node_modules/vue')) return 'vue'
          if (id.includes('node_modules/element-plus')) return 'element'
          if (id.includes('node_modules/axios')) return 'axios'
          if (id.includes('node_modules/dompurify')) return 'dompurify'
          // fallback vendor chunk
          return 'vendor'
        }
      }
    }
  },
  server: {
    port: 5173
  }
})

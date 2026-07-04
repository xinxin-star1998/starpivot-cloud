import App from './App.vue'
import {initStore} from './store' // Store
import {initRouter} from './router' // Router
import language from './locales' // 国际化
import '@styles/core/tailwind.css' // tailwind
import '@styles/index.scss' // 样式
import '@utils/sys/console.ts' // 控制台输出内容
import {setupGlobDirectives} from './directives'
import {setupErrorHandle} from './utils/sys/error-handle'
import {setupOfflineIconify} from './utils/ui/iconify-offline'
import {setupPerformanceMonitor} from './utils/performance'
// 添加缺失的导入
import {createApp} from 'vue'

void setupOfflineIconify()

// 初始化前端性能监控（采集 Core Web Vitals：LCP/CLS/INP/FCP/TTFB）
setupPerformanceMonitor({
  enableConsoleLog: import.meta.env.DEV,
  enableReport: import.meta.env.VITE_PERF_REPORT === 'true',
  reportUrl: (import.meta.env.VITE_PERF_REPORT_URL as string) || '',
  onlyAbnormal: import.meta.env.VITE_PERF_ONLY_ABNORMAL === 'true'
})

document.addEventListener(
  'touchstart',
  function () {},
  { passive: false }
)

const app = createApp(App)
initStore(app)
initRouter(app)
setupGlobDirectives(app)
setupErrorHandle(app)

app.use(language)
app.mount('#app')
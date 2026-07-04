/**
 * 前端性能监控模块
 *
 * ## 功能说明
 * - 采集 Google 核心 Web 指标（Core Web Vitals）：LCP / CLS / INP / FCP / TTFB
 * - 采集自定义运行时指标：页面首屏时间、路由切换耗时、API 请求耗时 P75
 * - 支持指标上报（可配置上报地址 / 自定义回调），便于接入后端或第三方监控平台
 *
 * ## 使用方式
 * 在 `main.ts` 中调用 `setupPerformanceMonitor()` 即可开启；开发环境默认打印到控制台。
 *
 * @module utils/performance
 */

import {type Metric, onCLS, onFCP, onINP, onLCP, onTTFB} from 'web-vitals'

/** 单条性能指标数据结构 */
export interface PerformanceMetric {
  /** 指标名称，如 LCP / CLS / INP 等 */
  name: string
  /** 指标数值 */
  value: number
  /** 指标评级：good / needs-improvement / poor（web-vitals 提供） */
  rating: 'good' | 'needs-improvement' | 'poor'
  /** 指标单位（由评级推断，便于展示） */
  unit: string
  /** 导航类型：navigate / reload / back-forward / prerender */
  navigationType: string
  /** 指标采集时间戳（ISO 格式） */
  timestamp: string
  /** 当前页面 URL */
  url: string
  /** 页面路径（不含 query，便于聚合） */
  path: string
  /** 用户代理 */
  userAgent: string
  /** 应用版本号（来自构建期注入的 __APP_VERSION__） */
  appVersion?: string
}

/** 性能监控配置 */
export interface PerformanceMonitorOptions {
  /** 是否启用控制台打印（默认仅在开发环境打印） */
  enableConsoleLog?: boolean
  /** 是否启用指标上报（默认 false，避免无后端时产生错误） */
  enableReport?: boolean
  /** 指标上报地址（POST JSON） */
  reportUrl?: string
  /** 上报批处理间隔（毫秒），默认 10000ms */
  reportIntervalMs?: number
  /** 自定义指标处理回调（可接入任意第三方监控 SDK，如 Sentry / 自研平台） */
  onMetric?: (metric: PerformanceMetric) => void
  /** 只上报评级为 poor / needs-improvement 的指标（默认 false，全部上报） */
  onlyAbnormal?: boolean
}

/** 单位映射 */
const UNIT_MAP: Record<string, string> = {
  CLS: 'score',
  LCP: 'ms',
  FCP: 'ms',
  INP: 'ms',
  TTFB: 'ms',
  FID: 'ms',
  ROUTE_CHANGE: 'ms',
  API_DURATION: 'ms'
}

/** 指标缓冲区 */
let metricBuffer: PerformanceMetric[] = []

/** 定时器句柄 */
let reportTimer: ReturnType<typeof setInterval> | null = null

/** 当前生效配置 */
let currentOptions: Required<PerformanceMonitorOptions> = {
  enableConsoleLog: import.meta.env.DEV,
  enableReport: false,
  reportUrl: '',
  reportIntervalMs: 10000,
  onMetric: () => {},
  onlyAbnormal: false
}

/** 是否已初始化 */
let initialized = false

/**
 * 将 web-vitals 的 Metric 转换为统一的 PerformanceMetric
 */
function normalizeMetric(metric: Metric): PerformanceMetric {
  return {
    name: metric.name,
    value: metric.value,
    rating: metric.rating,
    unit: UNIT_MAP[metric.name] || 'ms',
    navigationType: metric.navigationType,
    timestamp: new Date().toISOString(),
    url: window.location.href,
    path: window.location.pathname,
    userAgent: navigator.userAgent,
    appVersion: typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : undefined
  }
}

/**
 * 控制台输出指标（带颜色区分评级）
 */
function logMetric(metric: PerformanceMetric): void {
  const colorMap = {
    good: '#0cce6b',
    'needs-improvement': '#ffa400',
    poor: '#ff4e42'
  }
  const color = colorMap[metric.rating] || '#999'
  // eslint-disable-next-line no-console
  console.log(
    `%c[Perf] ${metric.name} ${metric.value.toFixed(2)} ${metric.unit} (${metric.rating})%c ${metric.path}`,
    `color: ${color}; font-weight: bold;`,
    'color: #888;'
  )
}

/**
 * 处理单条指标：入缓冲区 + 触发自定义回调 + 控制台打印
 */
function handleMetric(metric: Metric): void {
  const normalized = normalizeMetric(metric)

  // 异常过滤
  if (currentOptions.onlyAbnormal && normalized.rating === 'good') {
    return
  }

  metricBuffer.push(normalized)

  // 触发自定义回调（可用于接入 Sentry 等）
  try {
    currentOptions.onMetric(normalized)
  } catch (err) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error('[Perf] onMetric callback error:', err)
    }
  }

  // 控制台打印
  if (currentOptions.enableConsoleLog) {
    logMetric(normalized)
  }
}

/**
 * 上报缓冲区中的指标
 */
async function flushMetrics(): Promise<void> {
  if (metricBuffer.length === 0) return
  if (!currentOptions.enableReport || !currentOptions.reportUrl) {
    // 未配置上报地址，仅清空缓冲区
    metricBuffer = []
    return
  }

  const payload = metricBuffer.slice()
  metricBuffer = []

  try {
    // 使用 sendBeacon 保证在页面卸载时也能完成上报；失败时回退到 fetch
    const body = JSON.stringify({ metrics: payload, sentAt: new Date().toISOString() })
    const sent = navigator.sendBeacon?.(currentOptions.reportUrl, body) ?? false
    if (!sent) {
      await fetch(currentOptions.reportUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
        keepalive: true
      })
    }
  } catch (err) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error('[Perf] report failed:', err)
    }
  }
}

/**
 * 初始化性能监控
 * - 注册 Core Web Vitals 监听
 * - 启动批处理上报定时器
 * - 注册页面卸载时的强制 flush
 */
export function setupPerformanceMonitor(options: PerformanceMonitorOptions = {}): void {
  if (initialized || typeof window === 'undefined') return

  currentOptions = { ...currentOptions, ...options }

  // 注册 Core Web Vitals
  onLCP(handleMetric)
  onCLS(handleMetric)
  onINP(handleMetric)
  onFCP(handleMetric)
  onTTFB(handleMetric)

  // 启动批处理定时器
  if (currentOptions.reportIntervalMs > 0) {
    reportTimer = setInterval(() => {
      void flushMetrics()
    }, currentOptions.reportIntervalMs)
  }

  // 页面卸载时强制 flush
  window.addEventListener('pagehide', () => {
    void flushMetrics()
  })
  window.addEventListener('beforeunload', () => {
    void flushMetrics()
  })

  initialized = true

  if (currentOptions.enableConsoleLog) {
    // eslint-disable-next-line no-console
    console.log('[Perf] 性能监控已启动', {
      report: currentOptions.enableReport,
      reportUrl: currentOptions.reportUrl,
      onlyAbnormal: currentOptions.onlyAbnormal
    })
  }
}

/**
 * 手动记录自定义指标（如路由切换耗时、API 请求耗时等）
 */
export function reportCustomMetric(
  name: string,
  value: number,
  rating: PerformanceMetric['rating'] = 'good'
): void {
  if (!initialized) return
  const navigation = performance.getEntriesByType('navigation')[0] as
    | PerformanceNavigationTiming
    | undefined
  handleMetric({
    id: `${name}-${Date.now()}`,
    name,
    value,
    rating,
    delta: value,
    entries: [],
    navigationType: navigation?.type || 'custom'
  } as unknown as Metric)
}

/**
 * 获取并清空当前缓冲区（供外部调试或手动上报）
 */
export function takeMetrics(): PerformanceMetric[] {
  const data = metricBuffer.slice()
  metricBuffer = []
  return data
}

/**
 * 停止性能监控（清除定时器，flush 残余数据）
 */
export function teardownPerformanceMonitor(): void {
  if (!initialized) return
  if (reportTimer) {
    clearInterval(reportTimer)
    reportTimer = null
  }
  void flushMetrics()
  initialized = false
}

// 声明构建期注入的版本号变量
declare const __APP_VERSION__: string

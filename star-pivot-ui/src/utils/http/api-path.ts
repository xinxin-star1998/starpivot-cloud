/**
 * API 路径前缀约定（与后端 context-path=/api/v1 对齐）
 *
 * - 业务层 @/api 模块 url 统一写 /api/xxx（不含版本号）
 * - axios baseURL 统一为 /api/v1（开发走 Vite 代理，生产走 Nginx）
 * - normalizeRequestUrl 负责剥离 url 中与 baseURL 重复的前缀，避免 /api/v1/v1 等重复
 */

/** 后端网关 / 微服务 context-path 前缀 */
export const API_VERSION_PREFIX = '/api/v1'

/** 业务 API 模块内 url 字段前缀 */
export const API_MODULE_PREFIX = '/api'

/**
 * baseURL 是否已包含带版本的 API 前缀（/api/v1、/api/v2 …）
 */
export function baseHasVersionPrefix(base: string): boolean {
  const normalized = base.replace(/\/$/, '')
  if (!normalized || normalized === '/') return false
  return /\/api\/v\d+$/.test(normalized)
}

/**
 * baseURL 是否已包含 API 前缀（含旧版 /api 无版本号配置）
 */
export function baseHasApiPrefix(base: string): boolean {
  const normalized = base.replace(/\/$/, '')
  if (!normalized || normalized === '/') return false
  if (baseHasVersionPrefix(normalized)) return true
  return normalized === '/api' || normalized.endsWith('/api')
}

/**
 * 从业务 url 中剥离与 baseURL 重复的 /api 或 /api/vN 前缀
 *
 * @example base=/api/v1, url=/api/sys/user → /sys/user
 * @example base=/api/v1, url=/api/v1/sys/user → /sys/user（避免 v1 重复）
 * @example base=/, url=/api/sys/user → /api/sys/user（由 Vite 代理补 v1，兼容旧开发配置）
 */
export function normalizeRequestUrl(url: string, baseUrl: string): string {
  if (!url) return url
  if (!url.startsWith('/api')) return url

  const base = (baseUrl || '').trim()
  if (!baseHasApiPrefix(base)) return url

  if (url.startsWith('/api/v') && /^\/api\/v\d+(\/|$)/.test(url)) {
    const versionPrefix = url.match(/^\/api\/v\d+/)?.[0]
    if (versionPrefix && (base.endsWith(versionPrefix) || base.includes(`${versionPrefix}/`))) {
      const stripped = url.slice(versionPrefix.length)
      return stripped.startsWith('/') ? stripped : `/${stripped}`
    }
  }

  if (url.startsWith('/api/') || url === '/api') {
    const stripped = url.slice('/api'.length)
    return stripped.startsWith('/') ? stripped : `/${stripped}`
  }

  return url
}

/**
 * 拼接完整 API 地址（供富文本上传、iframe 等非 axios 场景使用）
 *
 * @example base=/api/v1, path=/common/upload → /api/v1/common/upload
 * @example base=/, path=/common/upload → /api/common/upload（走 Vite 代理 rewrite）
 */
export function buildAbsoluteApiUrl(path: string, baseUrl: string): string {
  const base = (baseUrl || '').trim().replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (!base || base === '/') {
    if (normalizedPath.startsWith('/api')) return normalizedPath
    return `${API_MODULE_PREFIX}${normalizedPath}`
  }

  if (normalizedPath.startsWith('/api/v') && /^\/api\/v\d+(\/|$)/.test(normalizedPath)) {
    const versionPrefix = normalizedPath.match(/^\/api\/v\d+/)?.[0]!
    if (base.endsWith(versionPrefix)) {
      return `${base}${normalizedPath.slice(versionPrefix.length)}`
    }
  }

  if (normalizedPath.startsWith('/api/') || normalizedPath === '/api') {
    return `${base}${normalizedPath.slice('/api'.length)}`
  }

  return `${base}${normalizedPath}`
}

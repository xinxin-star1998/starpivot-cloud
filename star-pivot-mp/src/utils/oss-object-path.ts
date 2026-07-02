/** OSS 对象路径前缀（与后端 StoragePathValidator 保持一致） */
export const OSS_OBJECT_PREFIXES = ['avatar/', 'goods/', 'editor/', 'brand/', 'file/'] as const

export function isHttpUrl(value?: string | null): value is string {
  return !!value && (value.startsWith('http://') || value.startsWith('https://'))
}

export function isOssHostUrl(url: string): boolean {
  if (!isHttpUrl(url)) return false
  try {
    const hostname = new URL(url).hostname
    return hostname.includes('.oss-') || hostname.includes('.aliyuncs.com')
  } catch {
    return false
  }
}

export function isStorageObjectName(value?: string | null): boolean {
  if (!value) return false
  if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('blob:')) {
    return false
  }
  return OSS_OBJECT_PREFIXES.some((prefix) => value.startsWith(prefix))
}

function sanitizeObjectPath(path: string): string {
  return path.replace(/\\/g, '/').replace(/\/{2,}/g, '/').replace(/^\//, '')
}

/** 从 OSS 完整 URL 或对象路径解析 objectName */
export function extractOssObjectPath(url?: string | null): string | null {
  const trimmed = url?.trim()
  if (!trimmed) return null
  if (isStorageObjectName(trimmed)) return sanitizeObjectPath(trimmed)
  if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
    return trimmed.includes('/') ? sanitizeObjectPath(trimmed) : null
  }
  try {
    const pathname = new URL(trimmed).pathname.replace(/^\//, '')
    const localIdx = pathname.indexOf('local-storage/')
    if (localIdx >= 0) {
      return sanitizeObjectPath(pathname.substring(localIdx + 'local-storage/'.length))
    }
    if (isOssHostUrl(trimmed)) return sanitizeObjectPath(pathname) || null
    const prefixes = [...OSS_OBJECT_PREFIXES].sort((a, b) => b.length - a.length)
    for (const prefix of prefixes) {
      const idx = pathname.indexOf(prefix)
      if (idx >= 0) return sanitizeObjectPath(pathname.substring(idx))
    }
    const parts = pathname.split('/')
    if (parts.length > 1) return sanitizeObjectPath(parts.slice(1).join('/'))
    return sanitizeObjectPath(pathname) || null
  } catch {
    return trimmed.includes('/') ? sanitizeObjectPath(trimmed) : null
  }
}

export function normalizeToObjectName(value?: string | null): string {
  return extractOssObjectPath(value) ?? ''
}

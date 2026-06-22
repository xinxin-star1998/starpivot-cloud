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

/** 私有 OSS 永久 URL 或对象路径，浏览器展示需换 presigned URL */
export function needsOssPresignedDisplay(url?: string | null): boolean {
  if (!url) return false
  if (isStorageObjectName(url)) return true
  return isOssHostUrl(url)
}

/** 从 OSS 完整 URL 或对象路径解析 objectName */
export function extractOssObjectPath(url?: string | null): string | null {
  const trimmed = url?.trim()
  if (!trimmed) return null
  if (isStorageObjectName(trimmed)) return trimmed
  if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
    return trimmed.includes('/') ? trimmed : null
  }
  try {
    const pathname = new URL(trimmed).pathname.replace(/^\//, '')
    if (isOssHostUrl(trimmed)) return pathname || null
    for (const prefix of OSS_OBJECT_PREFIXES) {
      const idx = pathname.indexOf(prefix)
      if (idx >= 0) return pathname.substring(idx)
    }
    const parts = pathname.split('/')
    if (parts.length > 1) return parts.slice(1).join('/')
    return pathname || null
  } catch {
    return trimmed.includes('/') ? trimmed : null
  }
}

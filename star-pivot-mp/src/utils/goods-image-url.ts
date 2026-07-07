import {fetchImagePresignedUrls} from '@/api/image'
import {isHttpUrl, isStorageObjectName, normalizeToObjectName} from '@/utils/oss-object-path'

const displayUrlCache = new Map<string, string>()
/** 非响应式版本号，供 imageSrc 读取；页面在 prefetch 后自增本地 tick 触发重渲染 */
let cacheVersion = 0

/** 微信小程序 image 组件加载外部 http 资源易超时，OSS 域名统一走 https */
function normalizeDisplayUrl(url: string): string {
  if (
    url.startsWith('http://') &&
    (url.includes('.oss-') || url.includes('.aliyuncs.com'))
  ) {
    return url.replace(/^http:\/\//i, 'https://')
  }
  return url
}

function rememberDisplayUrl(key: string, url: string) {
  const normalized = normalizeDisplayUrl(url)
  displayUrlCache.set(key, normalized)
  return normalized
}

export function getImageCacheVersion() {
  return cacheVersion
}

/** 同步获取已解析的展示 URL（仅使用预签名缓存，不做本地磁盘兜底） */
export function getCoverDisplayUrl(coverImg?: string | null): string {
  if (!coverImg) return ''
  if (isHttpUrl(coverImg)) return normalizeDisplayUrl(coverImg)

  const cachedRaw = displayUrlCache.get(coverImg)
  if (cachedRaw) return cachedRaw

  const objectName = normalizeToObjectName(coverImg)
  if (!objectName) return ''
  if (isHttpUrl(objectName)) return objectName
  return displayUrlCache.get(objectName) || ''
}

/** 批量解析展示 URL（带缓存） */
export async function resolveGoodsImageDisplayUrls(
  rawValues: Array<string | undefined | null>
): Promise<Map<string, string>> {
  const result = new Map<string, string>()
  const needFetch: string[] = []

  for (const raw of rawValues) {
    if (!raw) continue
    const objectName = normalizeToObjectName(raw)
    if (!objectName) continue
    if (isHttpUrl(objectName)) {
      const url = normalizeDisplayUrl(objectName)
      result.set(objectName, url)
      if (raw !== objectName) result.set(raw, url)
      continue
    }
    if (!isStorageObjectName(objectName)) continue

    const cached = displayUrlCache.get(objectName) ?? displayUrlCache.get(raw)
    if (cached) {
      result.set(objectName, cached)
      if (raw !== objectName) result.set(raw, cached)
    } else if (!needFetch.includes(objectName)) {
      needFetch.push(objectName)
    }
  }

  if (needFetch.length > 0) {
    try {
      const remote = await fetchImagePresignedUrls(needFetch)
      for (const objectName of needFetch) {
        const url = remote[objectName]
        if (isHttpUrl(url)) {
          const normalized = rememberDisplayUrl(objectName, url)
          result.set(objectName, normalized)
        }
      }
    } catch (error) {
      console.warn('批量获取图片 URL 失败:', error)
    }
  }

  return result
}

/** 便捷：从若干字段收集并预解析图片，返回是否解析到新 URL */
export async function prefetchImages(...groups: Array<Array<string | undefined | null>>) {
  const merged: string[] = []
  for (const group of groups) {
    for (const raw of group) {
      if (raw) merged.push(raw)
    }
  }
  const before = cacheVersion
  await resolveGoodsImageDisplayUrls(merged)
  cacheVersion++
  return cacheVersion !== before || merged.length > 0
}

/** 模板用：解析后的 src；tick 为页面本地 ref，prefetch 后自增以触发重渲染 */
export function imageSrc(raw?: string | null, tick?: number): string {
  void tick
  void cacheVersion
  return getCoverDisplayUrl(raw)
}

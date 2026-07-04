import {fetchGoodsImagePresignedUrls} from '@/api/mall/goods-image'
import {extractOssObjectPath, isHttpUrl, isStorageObjectName} from '@/utils/storage/oss-object-path'

const displayUrlCache = new Map<string, string>()

export { isHttpUrl, isStorageObjectName }

/**
 * 将完整 OSS URL（含过期预签名）规范化为 objectName
 */
export function normalizeToObjectName(value?: string | null): string {
  return extractOssObjectPath(value) ?? ''
}

/** 解析单张图片展示 URL（带缓存） */
export async function resolveGoodsImageDisplayUrl(
  rawValue: string,
  fallbackDisplayUrl?: string
): Promise<string> {
  const objectName = normalizeToObjectName(rawValue)
  if (!objectName) return ''
  if (isHttpUrl(objectName)) return objectName
  if (isHttpUrl(fallbackDisplayUrl)) {
    displayUrlCache.set(objectName, fallbackDisplayUrl)
    return fallbackDisplayUrl
  }
  const cached = displayUrlCache.get(objectName)
  if (cached) return cached

  const map = await resolveGoodsImageDisplayUrls([objectName])
  return map.get(objectName) || ''
}

/** 批量解析展示 URL（带缓存） */
export async function resolveGoodsImageDisplayUrls(
  rawValues: string[]
): Promise<Map<string, string>> {
  const result = new Map<string, string>()
  const needFetch: string[] = []

  for (const raw of rawValues) {
    const objectName = normalizeToObjectName(raw)
    if (!objectName) continue
    if (isHttpUrl(objectName)) {
      result.set(objectName, objectName)
      if (raw !== objectName) {
        result.set(raw, objectName)
      }
      continue
    }
    if (!isStorageObjectName(objectName)) continue

    const cached = displayUrlCache.get(objectName)
    if (cached) {
      result.set(objectName, cached)
      if (raw !== objectName) {
        result.set(raw, cached)
      }
    } else {
      needFetch.push(objectName)
    }
  }

  if (needFetch.length > 0) {
    try {
      const remote = await fetchGoodsImagePresignedUrls(needFetch)
      for (const objectName of needFetch) {
        const url = remote[objectName]
        if (isHttpUrl(url)) {
          displayUrlCache.set(objectName, url)
          result.set(objectName, url)
        }
      }
    } catch (error) {
      console.error('批量获取预签名 URL 失败:', error)
    }
  }

  return result
}

/** 同步获取封面展示 URL */
export function getCoverDisplayUrl(
  coverImg: string,
  localCache: Map<string, string> = displayUrlCache
): string {
  const objectName = normalizeToObjectName(coverImg)
  if (!objectName) return ''
  if (isHttpUrl(objectName)) return objectName
  return localCache.get(objectName) || displayUrlCache.get(objectName) || ''
}

export function clearGoodsImageDisplayUrlCache() {
  displayUrlCache.clear()
}

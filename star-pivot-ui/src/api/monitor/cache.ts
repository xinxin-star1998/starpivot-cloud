import request from '@/utils/http'
import type { RedisCacheInfo, CacheKeyInfo, CacheContentInfo } from '@/types/api/monitor'

/**
 * 获取缓存列表
 */
export function fetchGetCacheList() {
  return request.get<RedisCacheInfo[]>({
    url: '/api/monitor/cache/list'
  })
}

/**
 * 根据缓存名称获取键名列表
 */
export function fetchGetCacheKeys(cacheName: string) {
  return request.get<CacheKeyInfo[]>({
    url: '/api/monitor/cache/keys',
    params: { cacheName }
  })
}

/**
 * 获取缓存内容
 */
export function fetchGetCacheContent(cacheName: string, key: string) {
  return request.get<CacheContentInfo>({
    url: '/api/monitor/cache/content',
    params: { cacheName, key }
  })
}

/**
 * 删除缓存（根据缓存名称删除所有匹配的键）
 */
export function fetchDeleteCache(cacheName: string) {
  return request.del<number>({
    url: `/api/monitor/cache/group/${encodeURIComponent(cacheName)}`
  })
}

/**
 * 删除单个缓存键
 */
export function fetchDeleteCacheKey(cacheName: string, key: string) {
  return request.del({
    url: '/api/monitor/cache/key',
    params: { cacheName, key }
  })
}

/**
 * 清空所有缓存
 */
export function fetchClearAllCache() {
  return request.del({
    url: '/api/monitor/cache/clear'
  })
}

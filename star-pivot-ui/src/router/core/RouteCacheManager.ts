/**
 * 路由缓存管理器
 *
 * 负责缓存路由相关数据，减少重复计算，提高性能
 *
 * @module router/core/RouteCacheManager
 * @author Art Design Pro Team
 */

import type { AppRouteRecord } from '@/types/router'
import type { RouteRecordRaw } from 'vue-router'
import { safeLog } from '@/utils'

/**
 * 路由缓存项
 */
interface RouteCacheItem {
  route: AppRouteRecord
  convertedRoute: RouteRecordRaw
  timestamp: number
}

/**
 * 路由缓存管理器
 */
export class RouteCacheManager {
  private static instance: RouteCacheManager
  private routeCache = new Map<string, RouteCacheItem>()
  private menuCache: AppRouteRecord[] | null = null
  private menuCacheTimestamp: number = 0
  private readonly cacheExpiryTime = 5 * 60 * 1000 // 5分钟缓存过期时间

  private constructor() {}

  /**
   * 获取单例实例
   */
  static getInstance(): RouteCacheManager {
    if (!RouteCacheManager.instance) {
      RouteCacheManager.instance = new RouteCacheManager()
    }
    return RouteCacheManager.instance
  }

  /**
   * 缓存路由转换结果
   * @param route 原始路由
   * @param convertedRoute 转换后的路由
   */
  cacheRoute(route: AppRouteRecord, convertedRoute: RouteRecordRaw): void {
    const cacheKey = this.generateCacheKey(route)
    if (cacheKey) {
      this.routeCache.set(cacheKey, {
        route,
        convertedRoute,
        timestamp: Date.now()
      })
      safeLog(`[RouteCacheManager] 缓存路由: ${cacheKey}`)
    }
  }

  /**
   * 获取缓存的路由转换结果
   * @param route 原始路由
   * @returns 转换后的路由或 null
   */
  getCachedRoute(route: AppRouteRecord): RouteRecordRaw | null {
    const cacheKey = this.generateCacheKey(route)
    if (!cacheKey) return null

    const cacheItem = this.routeCache.get(cacheKey)
    if (cacheItem) {
      // 检查缓存是否过期
      if (Date.now() - cacheItem.timestamp < this.cacheExpiryTime) {
        safeLog(`[RouteCacheManager] 命中缓存: ${cacheKey}`)
        return cacheItem.convertedRoute
      } else {
        // 缓存过期，移除
        this.routeCache.delete(cacheKey)
        safeLog(`[RouteCacheManager] 缓存过期: ${cacheKey}`)
      }
    }
    return null
  }

  /**
   * 缓存菜单列表
   * @param menuList 菜单列表
   */
  cacheMenuList(menuList: AppRouteRecord[]): void {
    this.menuCache = menuList
    this.menuCacheTimestamp = Date.now()
    safeLog(`[RouteCacheManager] 缓存菜单列表，共 ${menuList.length} 个菜单`)
  }

  /**
   * 获取缓存的菜单列表
   * @returns 菜单列表或 null
   */
  getCachedMenuList(): AppRouteRecord[] | null {
    // 检查缓存是否过期
    if (this.menuCache && Date.now() - this.menuCacheTimestamp < this.cacheExpiryTime) {
      safeLog(`[RouteCacheManager] 命中菜单缓存`)
      return this.menuCache
    }
    return null
  }

  /**
   * 清除路由缓存
   */
  clearRouteCache(): void {
    this.routeCache.clear()
    safeLog(`[RouteCacheManager] 清除路由缓存`)
  }

  /**
   * 清除菜单缓存
   */
  clearMenuCache(): void {
    this.menuCache = null
    this.menuCacheTimestamp = 0
    safeLog(`[RouteCacheManager] 清除菜单缓存`)
  }

  /**
   * 清除所有缓存
   */
  clearAllCache(): void {
    this.clearRouteCache()
    this.clearMenuCache()
    safeLog(`[RouteCacheManager] 清除所有缓存`)
  }

  /**
   * 生成缓存键
   * @param route 路由对象
   * @returns 缓存键
   */
  private generateCacheKey(route: AppRouteRecord): string {
    if (route.name) {
      return `route_${String(route.name)}`
    } else if (route.path) {
      return `route_${(route.path || '').replace(/\//g, '_')}`
    }
    return ''
  }

  /**
   * 获取缓存大小
   */
  getCacheSize(): {
    routeCacheSize: number
    hasMenuCache: boolean
  } {
    return {
      routeCacheSize: this.routeCache.size,
      hasMenuCache: this.menuCache !== null
    }
  }
}

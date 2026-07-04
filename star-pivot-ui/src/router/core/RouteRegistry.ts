/**
 * 路由注册核心类
 *
 * 负责动态路由的注册、验证和管理
 *
 * @module router/core/RouteRegistry
 * @author Art Design Pro Team
 */

import type {Router, RouteRecordRaw} from 'vue-router'
import type {AppRouteRecord} from '@/types/router'
import {ComponentLoader} from './ComponentLoader'
import {RouteValidator} from './RouteValidator'
import {RouteTransformer} from './RouteTransformer'
import {safeLog, safeWarn} from '@/utils'

export class RouteRegistry {
  private router: Router
  private componentLoader: ComponentLoader
  private validator: RouteValidator
  private transformer: RouteTransformer
  private removeRouteFns: (() => void)[] = []
  private registered = false
  private routeNames = new Set<string>()
  private routePaths = new Set<string>()

  private static toRouteKey(name: string | symbol | undefined): string | undefined {
    return name === undefined ? undefined : String(name)
  }

  constructor(router: Router) {
    this.router = router
    this.componentLoader = new ComponentLoader()
    this.validator = new RouteValidator()
    this.transformer = new RouteTransformer(this.componentLoader)
  }

  /**
   * 注册动态路由
   */
  register(menuList: AppRouteRecord[]): void {
    if (this.registered) {
      safeWarn('[RouteRegistry] 路由已注册，跳过重复注册')
      return
    }

    // 验证路由配置
    const validationResult = this.validator.validate(menuList)
    if (!validationResult.valid) {
      throw new Error(`路由配置验证失败: ${validationResult.errors.join(', ')}`)
    }

    // 转换并注册路由
    const removeRouteFns: (() => void)[] = []
    const processedRoutes = new Set<string>()

    menuList.forEach((route) => {
      try {
        // 生成唯一标识，避免重复处理
        const routeKey =
          RouteRegistry.toRouteKey(route.name) ||
          route.path ||
          `route_${Date.now()}_${Math.random()}`
        if (processedRoutes.has(routeKey)) {
          safeWarn(`[RouteRegistry] 跳过重复路由: ${routeKey}`)
          return
        }
        processedRoutes.add(routeKey)

        // 检查路由名称是否已存在
        const routeName =
          RouteRegistry.toRouteKey(route.name) || this.generateRouteKey(route.path || '')
        if (this.routeNames.has(routeName) || this.router.hasRoute(routeName)) {
          safeWarn(`[RouteRegistry] 路由名称已存在: ${routeName}`)
          return
        }

        // 检查路由路径是否已存在
        if (route.path && (this.routePaths.has(route.path) || this.router.hasRoute(route.path))) {
          safeWarn(`[RouteRegistry] 路由路径已存在: ${route.path}`)
          return
        }

        // 转换路由配置
        const routeConfig = this.transformer.transform(route)

        // 注册路由
        const removeRouteFn = this.router.addRoute(routeConfig as RouteRecordRaw)
        removeRouteFns.push(removeRouteFn)

        // 记录已注册的路由
        if (routeName) this.routeNames.add(routeName)
        if (route.path) this.routePaths.add(route.path)

        safeLog(`[RouteRegistry] 成功注册路由: ${routeName || route.path}`)
      } catch (error) {
        console.error(
          `[RouteRegistry] 注册路由失败: ${RouteRegistry.toRouteKey(route.name) || route.path}`,
          error
        )
        // 继续处理其他路由，不中断整个注册过程
      }
    })

    this.removeRouteFns = removeRouteFns
    this.registered = true
    safeLog(`[RouteRegistry] 动态路由注册完成，共注册 ${removeRouteFns.length} 个路由`)
  }

  /**
   * 移除所有动态路由
   */
  unregister(): void {
    try {
      this.removeRouteFns.forEach((fn) => {
        try {
          fn()
        } catch (error) {
          console.error('[RouteRegistry] 移除路由失败', error)
        }
      })
      this.removeRouteFns = []
      this.routeNames.clear()
      this.routePaths.clear()
      this.registered = false
      safeLog('[RouteRegistry] 动态路由已全部移除')
    } catch (error) {
      console.error('[RouteRegistry] 移除路由失败', error)
    }
  }

  /**
   * 检查是否已注册
   */
  isRegistered(): boolean {
    return this.registered
  }

  /**
   * 获取移除函数列表（用于 store 管理）
   */
  getRemoveRouteFns(): (() => void)[] {
    return this.removeRouteFns
  }

  /**
   * 标记为已注册（用于错误处理场景，避免重复请求）
   */
  markAsRegistered(): void {
    this.registered = true
  }

  /**
   * 生成路由键值
   */
  private generateRouteKey(path?: string): string {
    if (!path) {
      return `Route_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    return path.replace(/[^a-zA-Z0-9]/g, '_')
  }
}

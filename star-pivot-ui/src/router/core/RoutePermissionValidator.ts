/**
 * 路由权限验证模块
 *
 * 提供路由权限验证和路径检查功能
 *
 * ## 主要功能
 *
 * - 验证路径是否在用户菜单权限中
 * - 构建菜单路径集合（扁平化处理）
 * - 支持动态路由参数匹配
 * - 路径前缀匹配
 *
 * ## 使用场景
 *
 * - 路由守卫中验证用户权限
 * - 动态路由注册后的权限检查
 * - 防止用户访问无权限的页面
 *
 * @module router/core/RoutePermissionValidator
 * @author Art Design Pro Team
 */

import type {AppRouteRecord} from '@/types/router'

/**
 * 路由权限验证器
 */
export class RoutePermissionValidator {
  /**
   * 验证路径是否在用户菜单权限中
   * @param targetPath 目标路径
   * @param menuList 菜单列表
   * @returns 是否有权限访问
   */
  static hasPermission(targetPath: string, menuList: AppRouteRecord[]): boolean {
    // 根路径始终允许访问
    if (targetPath === '/') {
      return true
    }

    // 构建路径集合（包含隐藏路由，用于权限验证）
    const pathSet = this.buildMenuPathSet(menuList, true)

    // 检查路径是否在集合中（精确匹配或前缀匹配）
    if (pathSet.has(targetPath) || this.checkPathPrefix(targetPath, pathSet)) {
      return true
    }

    // 检查动态路由模式匹配（如 /system/dict/data/:dictType 匹配 /system/dict/data/sys_user_sex）
    return this.checkDynamicRouteMatch(targetPath, menuList)
  }

  /**
   * 构建菜单路径集合（扁平化处理）
   * @param menuList 菜单列表
   * @param includeHidden 是否包含隐藏路由（用于权限验证）
   * @param pathSet 路径集合
   * @returns 路径集合
   */
  static buildMenuPathSet(
    menuList: AppRouteRecord[],
    includeHidden: boolean = false,
    pathSet: Set<string> = new Set()
  ): Set<string> {
    if (!Array.isArray(menuList) || menuList.length === 0) {
      return pathSet
    }

    for (const menuItem of menuList) {
      // 如果 includeHidden 为 false，跳过隐藏的菜单项
      if (!includeHidden && (menuItem.meta?.isHide || !menuItem.path)) {
        continue
      }

      // 如果 includeHidden 为 true，只跳过没有 path 的项
      if (includeHidden && !menuItem.path) {
        continue
      }

      // 标准化路径并添加到集合
      const menuPath = menuItem.path.startsWith('/') ? menuItem.path : `/${menuItem.path}`
      pathSet.add(menuPath)

      // 递归处理子菜单
      if (menuItem.children?.length) {
        this.buildMenuPathSet(menuItem.children, includeHidden, pathSet)
      }
    }

    return pathSet
  }

  /**
   * 检查目标路径是否匹配集合中的某个路径前缀
   * 用于支持动态路由参数匹配，如 /user/123 匹配 /user
   * @param targetPath 目标路径
   * @param pathSet 路径集合
   * @returns 是否匹配
   */
  static checkPathPrefix(targetPath: string, pathSet: Set<string>): boolean {
    // 遍历路径集合，检查是否有前缀匹配
    for (const menuPath of pathSet) {
      if (targetPath.startsWith(`${menuPath}/`)) {
        return true
      }
    }
    return false
  }

  /**
   * 检查动态路由模式匹配
   * 如 /system/dict/data/:dictType 匹配 /system/dict/data/sys_user_sex
   * @param targetPath 目标路径
   * @param menuList 菜单列表
   * @returns 是否匹配
   */
  static checkDynamicRouteMatch(targetPath: string, menuList: AppRouteRecord[]): boolean {
    const checkRoute = (routes: AppRouteRecord[]): boolean => {
      for (const route of routes) {
        if (!route.path) {
          continue
        }

        // 如果路径包含动态参数（:param），进行模式匹配
        if (route.path.includes(':')) {
          // 将动态路由模式转换为正则表达式
          // 例如：/system/dict/data/:dictType -> /system/dict/data/[^/]+
          const pattern = route.path.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
          const regex = new RegExp(`^${pattern}$`)

          if (regex.test(targetPath)) {
            return true
          }
        }

        // 递归检查子路由
        if (route.children?.length && checkRoute(route.children)) {
          return true
        }
      }
      return false
    }

    return checkRoute(menuList)
  }

  /**
   * 验证并返回有效的路径
   * 如果目标路径无权限，返回首页路径
   * @param targetPath 目标路径
   * @param menuList 菜单列表
   * @param homePath 首页路径
   * @returns 验证后的路径
   */
  static validatePath(
    targetPath: string,
    menuList: AppRouteRecord[],
    homePath: string = '/'
  ): { path: string; hasPermission: boolean } {
    const hasPermission = this.hasPermission(targetPath, menuList)

    if (hasPermission) {
      return { path: targetPath, hasPermission: true }
    }

    return { path: homePath, hasPermission: false }
  }
}

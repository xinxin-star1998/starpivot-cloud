/**
 * 菜单处理器
 *
 * 负责菜单数据的获取、过滤和处理
 *
 * @module router/core/MenuProcessor
 * @author Art Design Pro Team
 */

import type {AppRouteRecord} from '@/types/router'
import {type DynamicRouteVo, fetchGetDynamicRoutes, type SysMenu} from '@/api/menu/menu'
import {RoutesAlias} from '../routesAlias'
import {formatMenuTitle} from '@/utils'

export class MenuProcessor {
  /**
   * 获取菜单数据（同时返回原始菜单与转换后的路由菜单）
   */
  async getMenuListWithRaw(): Promise<{ rawMenuList: SysMenu[]; menuList: AppRouteRecord[] }> {
    try {
      const dynamicRoutes = await fetchGetDynamicRoutes()

      if (!dynamicRoutes || !Array.isArray(dynamicRoutes)) {
        console.error('[MenuProcessor] 后端返回的动态路由格式错误:', dynamicRoutes)
        throw new Error('后端返回的菜单数据格式错误，期望数组类型')
      }

      if (import.meta.env.DEV) {
        console.log('[MenuProcessor] 获取到动态路由:', dynamicRoutes)
      }

      const rawMenuList = this.dynamicRoutesToSysMenuShim(dynamicRoutes)
      const convertedList = this.convertDynamicRouteToRouteRecord(dynamicRoutes)
      const menuList = this.filterEmptyMenus(convertedList)
      this.validateMenuPaths(menuList)

      return {
        rawMenuList,
        menuList: this.normalizeMenuPaths(menuList)
      }
    } catch (error) {
      console.error('[MenuProcessor] 获取菜单数据失败:', error)
      throw error
    }
  }

  /**
   * 获取菜单数据（从后端获取）
   */
  async getMenuList(): Promise<AppRouteRecord[]> {
    const { menuList } = await this.getMenuListWithRaw()
    return menuList
  }

  /**
   * 将 SysMenu 数组转换为 AppRouteRecord 数组（公共方法）
   * 用于菜单管理页面等需要处理所有菜单的场景
   * @param sysMenus 后端菜单列表
   * @returns 转换后的路由记录数组
   */
  convertSysMenuToRouteRecordPublic(sysMenus: SysMenu[]): AppRouteRecord[] {
    const convertedList = this.convertSysMenuToRouteRecord(sysMenus)
    const filteredList = this.filterEmptyMenus(convertedList)
    this.validateMenuPaths(filteredList)
    return this.normalizeMenuPaths(filteredList)
  }

  /**
   * 将动态路由树转为精简 SysMenu 树，仅用于权限标识收集（getPermsByPrefix 等）
   */
  private dynamicRoutesToSysMenuShim(nodes: DynamicRouteVo[]): SysMenu[] {
    if (!Array.isArray(nodes) || nodes.length === 0) return []
    return nodes.map((node) => {
      const title = node.meta?.title ?? node.name ?? ''
      const id = node.menuId ?? 0
      return {
        menuName: title,
        path: (node.path || '').replace(/#/g, '').trim(),
        menuId: node.menuId,
        perms: node.perms,
        menuType: node.menuType,
        label: title,
        value: id,
        children:
          node.children && node.children.length > 0
            ? this.dynamicRoutesToSysMenuShim(node.children)
            : undefined
      }
    })
  }

  /**
   * 将后端 RouterVo（dynamic-routes）转为前端 AppRouteRecord
   */
  private convertDynamicRouteToRouteRecord(routes: DynamicRouteVo[], level = 0): AppRouteRecord[] {
    if (!Array.isArray(routes)) {
      console.error('[MenuProcessor] convertDynamicRouteToRouteRecord 接收到的不是数组:', routes)
      return []
    }

    return routes
      .map((route, index) => {
        try {
          if (!route) {
            console.warn(`[MenuProcessor] 动态路由项 ${index} 为空，跳过`)
            return null
          }

          const path = (route.path || '').replace(/#/g, '').trim()
          const isExternalLink = path.startsWith('http://') || path.startsWith('https://')
          const isFrame = route.isFrame
          const isIframe = isFrame === 0 && !isExternalLink
          const menuType = route.menuType
          const isDirectory = menuType === 'M'

          let component = route.component
          if (component) {
            component = component.replace(/#/g, '').trim()
          }

          const hasChildren = route.children && route.children.length > 0

          if (isDirectory && (!component || component === '')) {
            if (hasChildren) {
              component = RoutesAlias.Layout
              if (import.meta.env.DEV) {
                console.log(
                  `[MenuProcessor] 目录 "${route.meta?.title || route.name}" (level: ${level}) 有子菜单，设置为 Layout`
                )
              }
            } else {
              component = undefined
              if (import.meta.env.DEV) {
                console.log(
                  `[MenuProcessor] 目录 "${route.meta?.title || route.name}" (level: ${level}) 没有子菜单，不设置 component`
                )
              }
            }
          }

          let authList: Array<{ title: string; authMark: string }> | undefined
          if (route.perms) {
            const permsArray = route.perms
              .split(',')
              .map((p) => p.trim())
              .filter(Boolean)
            authList = permsArray.map((perm) => ({
              title: perm,
              authMark: perm
            }))
          }

          const metaNoCache = route.meta?.noCache === true

          const routeRecord: AppRouteRecord = {
            id: route.menuId,
            name: route.name || undefined,
            path,
            component: component || undefined,
            meta: {
              title: route.meta?.title || '',
              icon: route.meta?.icon,
              isIframe,
              keepAlive: !metaNoCache,
              isHide: route.hidden === true,
              link: route.meta?.link || (isExternalLink ? path : undefined),
              authList,
              isLayout: component === RoutesAlias.Layout
            },
            menuType: menuType as 'M' | 'C' | 'F' | undefined,
            children:
              route.children && route.children.length > 0
                ? this.convertDynamicRouteToRouteRecord(route.children, level + 1)
                : undefined
          }

          return routeRecord
        } catch (error) {
          console.error(`[MenuProcessor] 转换动态路由项 ${index} 失败:`, route, error)
          return null
        }
      })
      .filter((item): item is AppRouteRecord => item !== null)
  }

  /**
   * 将后端 SysMenu 转换为前端 AppRouteRecord
   * @param sysMenus 后端菜单列表
   * @param level 当前层级（从0开始，0表示一级菜单）
   */
  private convertSysMenuToRouteRecord(sysMenus: SysMenu[], level = 0): AppRouteRecord[] {
    if (!Array.isArray(sysMenus)) {
      console.error('[MenuProcessor] convertSysMenuToRouteRecord 接收到的不是数组:', sysMenus)
      return []
    }

    return sysMenus
      .map((menu, index) => {
        try {
          if (!menu) {
            console.warn(`[MenuProcessor] 菜单项 ${index} 为空，跳过`)
            return null
          }

          // 清理路径中的 # 符号
          const path = (menu.path || '').replace(/#/g, '').trim()
          const isExternalLink = path.startsWith('http://') || path.startsWith('https://')
          // isFrame: 0是外链/iframe, 1否
          const isIframe = menu.isFrame === 0 && !isExternalLink
          const isDirectory = menu.menuType === 'M' // M目录 C菜单 F按钮

          // 处理 component
          let component = menu.component
          if (component) {
            // 清理 component 中的 # 符号
            component = component.replace(/#/g, '').trim()
          }

          // 检查是否有子菜单
          // 注意：后端返回的应该是树形结构，menu.children 应该已经包含子菜单
          const hasChildren = menu.children && menu.children.length > 0

          // 只有目录类型（M）才能使用 Layout
          // 菜单类型（C）必须有自己的 component，不能使用 Layout
          // 支持多级路由：任何目录类型如果有子菜单，都应该使用 Layout 来承载子路由
          if (isDirectory && (!component || component === '')) {
            if (hasChildren) {
              // 任何层级的目录，只要有子菜单，都使用 Layout 来承载子路由
              component = RoutesAlias.Layout
              if (import.meta.env.DEV) {
                console.log(
                  `[MenuProcessor] 目录 "${menu.menuName}" (level: ${level}) 有子菜单，设置为 Layout`
                )
              }
            } else {
              // 目录类型但没有子菜单，不设置 component
              component = undefined
              if (import.meta.env.DEV) {
                console.log(
                  `[MenuProcessor] 目录 "${menu.menuName}" (level: ${level}) 没有子菜单，不设置 component`
                )
              }
            }
          }

          // 将 perms 字符串转换为 authList 数组格式
          // perms 可能是单个权限标识，也可能是逗号分隔的多个权限标识
          let authList: Array<{ title: string; authMark: string }> | undefined
          if (menu.perms) {
            // 如果 perms 是逗号分隔的字符串，拆分成数组
            const permsArray = menu.perms
              .split(',')
              .map((p) => p.trim())
              .filter(Boolean)
            authList = permsArray.map((perm) => ({
              title: perm, // 使用权限标识作为标题
              authMark: perm // 权限标识
            }))
          }

          const routeRecord: AppRouteRecord = {
            id: menu.menuId,
            name: menu.routeName || undefined,
            path: path,
            component: component || undefined,
            meta: {
              title: menu.menuName || '',
              icon: menu.icon,
              // isFrame: 0是外链/iframe, 1否
              isIframe: isIframe,
              // isCache: 0缓存, 1不缓存 -> keepAlive: true缓存, false不缓存
              keepAlive: menu.isCache === 0,
              // visible: 0显示, 1隐藏 -> isHide: true隐藏, false显示
              isHide: menu.visible === '1',
              // 如果是外链，设置 link
              link: isExternalLink ? path : undefined,
              // 将 perms 转换为 authList 格式
              authList: authList,
              // 标记是否是 Layout 组件（用于多级路由判断）
              isLayout: component === RoutesAlias.Layout
            },
            // 保留菜单的额外信息
            createTime: menu.createTime,
            updateTime: menu.updateTime,
            status: menu.status,
            orderNum: menu.orderNum,
            remark: menu.remark,
            menuType: menu.menuType as 'M' | 'C' | 'F' | undefined,
            children:
              menu.children && menu.children.length > 0
                ? this.convertSysMenuToRouteRecord(menu.children, level + 1)
                : undefined
          }

          return routeRecord
        } catch (error) {
          console.error(`[MenuProcessor] 转换菜单项 ${index} 失败:`, menu, error)
          return null
        }
      })
      .filter((item): item is AppRouteRecord => item !== null)
  }

  /**
   * 递归过滤空菜单项
   */
  private filterEmptyMenus(menuList: AppRouteRecord[]): AppRouteRecord[] {
    return menuList
      .map((item) => {
        // 如果有子菜单，先递归过滤子菜单
        if (item.children && item.children.length > 0) {
          const filteredChildren = this.filterEmptyMenus(item.children)
          // 保留菜单项，即使过滤后的子菜单为空数组
          // 因为目录菜单本身应该被保留，不管是否有子菜单
          return {
            ...item,
            children: filteredChildren
          }
        }
        return item
      })
      .filter((item): item is AppRouteRecord => {
        if (!item) return false

        // 如果有子菜单数组（即使为空），说明这是一个目录菜单，应该保留
        if ('children' in item && Array.isArray(item.children)) {
          return true
        }

        // 如果是按钮类型，保留
        if (item.menuType === 'F') {
          return true
        }

        // 如果有外链或 iframe，保留
        if (item.meta?.isIframe === true || item.meta?.link) {
          return true
        }

        // 如果有有效的 component，保留
        if (item.component && item.component !== '' && item.component !== RoutesAlias.Layout) {
          return true
        }

        // 如果是一级菜单且使用 Layout，保留
        if (item.component === RoutesAlias.Layout) {
          return true
        }

        // 其他情况过滤掉
        return false
      })
  }

  /**
   * 验证菜单列表是否有效
   */
  validateMenuList(menuList: AppRouteRecord[]): boolean {
    if (!Array.isArray(menuList)) {
      console.error('[MenuProcessor] 菜单列表不是数组类型:', menuList)
      return false
    }
    if (menuList.length === 0) {
      console.warn('[MenuProcessor] 菜单列表为空，用户可能没有菜单权限')
      return false
    }
    return true
  }

  /**
   * 规范化菜单路径
   * 将相对路径转换为完整路径，确保菜单跳转正确
   */
  private normalizeMenuPaths(menuList: AppRouteRecord[], parentPath = ''): AppRouteRecord[] {
    return menuList.map((item) => {
      // 清理父路径中的 # 符号
      const cleanParentPath = parentPath.replace(/#/g, '').trim()

      // 构建完整路径
      const fullPath = this.buildFullPath(item.path || '', cleanParentPath)

      // 递归处理子菜单
      const children = item.children?.length
        ? this.normalizeMenuPaths(item.children, fullPath)
        : item.children

      return {
        ...item,
        path: fullPath,
        children
      }
    })
  }

  /**
   * 验证菜单路径配置
   * 检测非一级菜单是否错误使用了 / 开头的路径
   */
  /**
   * 验证菜单路径配置
   * 检测非一级菜单是否错误使用了 / 开头的路径
   */
  private validateMenuPaths(menuList: AppRouteRecord[], level = 1): void {
    menuList.forEach((route) => {
      if (!route.children?.length) return

      const parentName = String(route.name || route.path || '未知路由')

      route.children.forEach((child) => {
        const childPath = child.path || ''

        // 跳过合法的绝对路径：外部链接和 iframe 路由
        if (this.isValidAbsolutePath(childPath)) return

        // 检测非法的绝对路径
        if (childPath.startsWith('/')) {
          this.logPathError(child, childPath, parentName, level)
        }
      })

      // 递归检查更深层级的子路由
      this.validateMenuPaths(route.children, level + 1)
    })
  }

  /**
   * 判断是否为合法的绝对路径
   */
  private isValidAbsolutePath(path: string): boolean {
    return (
      path.startsWith('http://') ||
      path.startsWith('https://') ||
      path.startsWith('/outside/iframe/')
    )
  }

  /**
   * 输出路径配置错误日志
   */
  private logPathError(
    route: AppRouteRecord,
    path: string,
    parentName: string,
    level: number
  ): void {
    const routeName = String(route.name || path || '未知路由')
    const menuTitle = route.meta?.title || routeName
    const suggestedPath = path.split('/').pop() || path.slice(1)

    console.error(
      `[路由配置错误] 菜单 "${formatMenuTitle(menuTitle)}" (name: ${routeName}, path: ${path}) 配置错误\n` +
        `  位置: ${parentName} > ${routeName}\n` +
        `  问题: ${level + 1}级菜单的 path 不能以 / 开头\n` +
        `  当前配置: path: '${path}'\n` +
        `  应该改为: path: '${suggestedPath}'`
    )
  }

  /**
   * 构建完整路径
   */
  private buildFullPath(path: string, parentPath: string): string {
    if (!path) return ''

    // 清理路径中的 # 符号和多余的空格
    path = path.replace(/#/g, '').trim()

    // 外部链接直接返回
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path
    }

    // 如果已经是绝对路径，直接返回（清理后）
    if (path.startsWith('/')) {
      // 清理路径中的重复斜杠
      return path.replace(/\/+/g, '/')
    }

    // 拼接父路径和当前路径
    if (parentPath) {
      // 清理父路径中的 # 符号和重复斜杠
      const cleanParent = parentPath.replace(/#/g, '').replace(/\/+/g, '/').replace(/\/$/, '')
      const cleanChild = path.replace(/^\//, '')

      // 检查子路径是否已经包含父路径的一部分，避免重复拼接
      // 例如：父路径是 /monitor，子路径是 monitor/druid，应该拼接成 /monitor/druid
      // 但如果子路径已经包含完整的父路径，则直接使用子路径
      const parentSegments = cleanParent.split('/').filter(Boolean)
      const childSegments = cleanChild.split('/').filter(Boolean)

      // 如果子路径的第一个段与父路径的最后一个段相同，跳过第一个段
      if (parentSegments.length > 0 && childSegments.length > 0) {
        const parentLastSegment = parentSegments[parentSegments.length - 1]
        const childFirstSegment = childSegments[0]

        if (parentLastSegment === childFirstSegment) {
          // 跳过重复的段
          childSegments.shift()
          const remainingPath = childSegments.join('/')
          const fullPath = `${cleanParent}/${remainingPath}`
          return fullPath.replace(/\/+/g, '/')
        }
      }

      const fullPath = `${cleanParent}/${cleanChild}`
      // 清理最终路径中的重复斜杠
      return fullPath.replace(/\/+/g, '/')
    }

    // 没有父路径，添加前导斜杠
    return `/${path}`
  }
}

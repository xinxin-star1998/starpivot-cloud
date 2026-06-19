/**
 * 前端动态路由追加模块
 *
 * 提供前端动态追加隐藏路由的功能
 *
 * ## 主要功能
 *
 * - 追加首页（工作台）路由（数据库不存菜单）
 * - 追加个人中心路由（数据库不存菜单）
 * - 追加 Druid 监控 iframe 路由（数据库不存菜单）
 * - 支持扩展其他前端动态路由
 *
 * ## 使用场景
 *
 * - 路由守卫中动态添加前端路由
 * - 不需要在数据库中配置的隐藏路由
 * - 明细页面路由
 *
 * @module router/core/DynamicRouteAppender
 * @author Art Design Pro Team
 */

import type { AppRouteRecord } from '@/types/router'
import { safeLog, safeWarn } from '@/utils'

interface RouteIndex {
  names: Set<string>
  paths: Set<string>
  nestedPaths: Map<string, AppRouteRecord[]>
}

/**
 * 前端动态路由追加器
 */
export class DynamicRouteAppender {
  /**
   * 追加所有前端动态路由到菜单列表
   * @param menuList 菜单列表
   * @returns 更新后的菜单列表
   */
  static appendDynamicRoutes(menuList: AppRouteRecord[]): AppRouteRecord[] {
    // 构建路由索引，提高查找效率
    const routeIndex = this.buildRouteIndex(menuList)

    // 追加首页（工作台）路由，置于最前
    this.appendDashboardConsoleRoute(menuList, routeIndex)

    // 追加个人中心路由
    this.appendUserCenterRoute(menuList, routeIndex)

    // 追加分配用户路由
    this.appendAssignUserRoute(menuList, routeIndex)

    // 追加代码生成编辑页路由
    this.appendGenEditRoute(menuList, routeIndex)

    // 追加商城 SPU 发布/编辑向导页
    this.appendMallProductSpuRoutes(menuList, routeIndex)

    // 追加 Druid 监控 iframe 路由
    this.appendDruidIframeRoute(menuList, routeIndex)

    // 可以在这里继续追加其他前端动态路由

    return menuList
  }

  /**
   * 构建路由索引，提高查找效率
   * @param menuList 菜单列表
   * @returns 路由索引对象
   */
  private static buildRouteIndex(menuList: AppRouteRecord[]): RouteIndex {
    const names = new Set<string>()
    const paths = new Set<string>()
    const nestedPaths = new Map<string, AppRouteRecord[]>()

    const traverseRoutes = (routes: AppRouteRecord[]) => {
      routes.forEach((route) => {
        if (route.name) names.add(String(route.name))
        if (route.path) paths.add(route.path)

        if (route.children && route.children.length > 0) {
          nestedPaths.set(route.path || '', route.children)
          traverseRoutes(route.children)
        }
      })
    }

    traverseRoutes(menuList)
    return { names, paths, nestedPaths }
  }

  /**
   * 追加「首页（工作台）」路由（数据库不存菜单）
   * 对应视图：@/views/dashboard/console/index.vue，与 HOME_PAGE_PATH 一致
   * 结构：仪表盘（父级目录）-> 工作台（子菜单）
   * @param menuList 菜单列表
   * @param routeIndex 路由索引
   */
  static appendDashboardConsoleRoute(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    // 检查是否已存在工作台路由
    if (routeIndex.names.has('Console') || routeIndex.paths.has('/dashboard/console')) {
      safeWarn('[DynamicRouteAppender] 工作台路由已存在，跳过追加')
      return
    }

    // 检查子路由中是否已存在工作台
    const dashboardChildren = routeIndex.nestedPaths.get('/dashboard')
    if (
      dashboardChildren &&
      dashboardChildren.some((child) => child.name === 'Console' || child.path === 'console')
    ) {
      safeWarn('[DynamicRouteAppender] 工作台子路由已存在于仪表盘目录下，跳过追加')
      return
    }

    // 查找是否已存在仪表盘父级目录
    let dashboardParent = menuList.find((route: AppRouteRecord) => route.path === '/dashboard')

    if (!dashboardParent) {
      // 创建仪表盘父级目录
      dashboardParent = {
        path: '/dashboard',
        name: 'Dashboard',
        component: '/index/index', // 使用 Layout
        meta: {
          title: 'menus.dashboard.title',
          icon: 'ri:dashboard-3-line',
          keepAlive: true
        },
        menuType: 'M',
        status: '0',
        orderNum: 0,
        children: []
      }
      menuList.unshift(dashboardParent)
      safeLog('[DynamicRouteAppender] 已创建仪表盘父级目录')
    } else {
      // 如果已存在 dashboard 路由，确保它的配置正确
      // 确保 menuType 是 'M'（目录类型），这样才能承载子路由
      if (dashboardParent.menuType !== 'M') {
        safeWarn(
          `[DynamicRouteAppender] 检测到 /dashboard 路由的 menuType 不是 'M'，将更新为 'M' 以支持子路由`
        )
        dashboardParent.menuType = 'M'
      }
      // 确保有 Layout 组件，这样才能正确渲染子路由
      if (!dashboardParent.component || dashboardParent.component === '') {
        dashboardParent.component = '/index/index'
        safeLog('[DynamicRouteAppender] 已为仪表盘父级目录设置 Layout 组件')
      }
      // 确保有 name，这样路由注册时才能正确识别
      if (!dashboardParent.name) {
        dashboardParent.name = 'Dashboard'
        safeLog('[DynamicRouteAppender] 已为仪表盘父级目录设置路由名称')
      }
    }

    // 确保 children 数组存在
    if (!dashboardParent.children) {
      dashboardParent.children = []
    }

    // 创建工作台子路由
    const consoleRoute: AppRouteRecord = {
      path: 'console', // 相对路径
      name: 'Console',
      component: '/dashboard/console/index',
      meta: {
        title: 'menus.dashboard.console',
        icon: 'ri:dashboard-3-line',
        keepAlive: true
      },
      menuType: 'C',
      status: '0',
      orderNum: 0
    }

    dashboardParent.children.push(consoleRoute)
    safeLog('[DynamicRouteAppender] 已动态追加首页（工作台）路由到仪表盘目录下')
  }

  /**
   * 追加「个人中心」路由（数据库不存菜单）
   * @param menuList 菜单列表
   * @param routeIndex 路由索引
   */
  static appendUserCenterRoute(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    if (routeIndex.names.has('UserCenter') || routeIndex.paths.has('/system/user-center')) {
      safeWarn('[DynamicRouteAppender] 个人中心路由已存在，跳过追加')
      return
    }

    const userCenterRoute: AppRouteRecord = {
      path: '/system/user-center',
      name: 'UserCenter',
      // 注意：这里以 `/` 开头，避免出现 `viewssystem` 拼接问题
      component: '/system/user-center/index',
      meta: {
        title: '个人中心',
        icon: 'ri:user-3-line',
        isHide: true,
        keepAlive: true
      },
      menuType: 'C',
      status: '0',
      orderNum: 999
    }

    menuList.push(userCenterRoute)
    safeLog('[DynamicRouteAppender] 已动态追加个人中心路由')
  }

  /**
   * 追加「分配用户」路由（数据库不存菜单）
   * @param menuList 菜单列表
   * @param routeIndex 路由索引
   */
  static appendAssignUserRoute(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    if (
      routeIndex.names.has('AssignUser') ||
      Array.from(routeIndex.paths).some((path) => String(path).includes('/system/role/assign-user'))
    ) {
      safeWarn('[DynamicRouteAppender] 分配用户路由已存在，跳过追加')
      return
    }

    const assignUserRoute: AppRouteRecord = {
      // 分配用户页面路径，带上动态参数 roleId
      path: '/system/role/assign-user/:roleId',
      name: 'AssignUser',
      // 注意：这里以 `/` 开头，对应视图文件 `src/views/system/role/assign-user.vue`
      component: '/system/role/assign-user',
      meta: {
        title: '分配用户',
        // 不在菜单树中显示，只通过点击"分配用户"按钮进入
        isHide: true,
        // 指定父级菜单路径，用于面包屑、高亮等
        parentPath: '/system/role',
        keepAlive: true,
        isHideTab: true
      },
      menuType: 'C',
      status: '0',
      orderNum: 1001
    }

    menuList.push(assignUserRoute)
    safeLog('[DynamicRouteAppender] 已动态追加分配用户路由')
  }

  /**
   * 追加「代码生成编辑页」路由（数据库不存菜单）
   * @param menuList 菜单列表
   * @param routeIndex 路由索引
   */
  static appendGenEditRoute(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    if (
      routeIndex.names.has('GenEdit') ||
      Array.from(routeIndex.paths).some((path) => String(path).includes('/tool/gen/edit'))
    ) {
      safeWarn('[DynamicRouteAppender] 代码生成编辑页路由已存在，跳过追加')
      return
    }

    const parentGen = this.findRouteNode(
      menuList,
      (r) =>
        r.component === '/tools/generator/index' ||
        r.name === 'GenerateTools' ||
        String(r.path).includes('/tools/generator')
    )
    const parentPath = parentGen?.path || '/tools/generator'

    const genEditRoute: AppRouteRecord = {
      // 编辑页路径，带上动态参数 tableId
      path: '/tool/gen/edit/:tableId',
      name: 'GenEdit',
      // 对应视图文件 src/views/tools/generator/modules/gen-edit.vue
      component: '/tools/generator/modules/gen-edit',
      meta: {
        title: '修改生成配置',
        // 不在菜单树中显示，只通过代码生成列表页跳转进入
        isHide: true,
        // 指定父级菜单路径，用于面包屑、高亮等
        parentPath,
        keepAlive: true,
        isHideTab: true,
        authList: this.resolveGeneratorAuthList(menuList)
      },
      menuType: 'C',
      status: '0',
      orderNum: 1002
    }

    menuList.push(genEditRoute)
    safeLog('[DynamicRouteAppender] 已动态追加代码生成编辑页路由')
  }

  /**
   * 从代码生成菜单继承按钮权限，供 v-auth / useAuth 使用
   */
  private static resolveGeneratorAuthList(
    menuList: AppRouteRecord[]
  ): AppRouteRecord['meta']['authList'] {
    const parent = this.findRouteNode(
      menuList,
      (r) =>
        r.component === '/tools/generator/index' ||
        r.name === 'GenerateTools' ||
        String(r.path).includes('/tools/generator')
    )
    const authList = parent?.meta?.authList
    return authList?.length ? [...authList] : undefined
  }

  /**
   * 追加商城 SPU 发布向导（新增 / 编辑，数据库不存菜单）
   * 必须挂到已有 /mall 目录下，避免与商城根路由重复注册导致 addRoute 被跳过、访问 500。
   */
  static appendMallProductSpuRoutes(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    const mallRoot = this.findRouteNode(
      menuList,
      (r) => r.path === '/mall' || r.name === 'MallSystem'
    )
    if (!mallRoot) {
      safeWarn('[DynamicRouteAppender] 未找到商城根菜单 /mall，跳过 SPU 向导路由')
      return
    }

    if (!mallRoot.children) {
      mallRoot.children = []
    }

    const hasAdd =
      routeIndex.names.has('MallProductAdd') ||
      Array.from(routeIndex.paths).some((p) => String(p).includes('product/add'))
    const hasEdit =
      routeIndex.names.has('MallProductEdit') ||
      Array.from(routeIndex.paths).some((p) => String(p).includes('product/edit'))

    if (!hasAdd) {
      mallRoot.children.push({
        path: 'product/add',
        name: 'MallProductAdd',
        component: '/mall/product/modules/addSpu',
        meta: {
          title: '发布商品',
          isHide: true,
          parentPath: '/mall/product/index',
          keepAlive: false
        },
        menuType: 'C',
        status: '0',
        orderNum: 1104
      })
      safeLog('[DynamicRouteAppender] 已在 /mall 下追加 SPU 新增向导路由')
    }

    if (!hasEdit) {
      mallRoot.children.push({
        path: 'product/edit/:id',
        name: 'MallProductEdit',
        component: '/mall/product/modules/addSpu',
        meta: {
          title: '编辑商品',
          isHide: true,
          parentPath: '/mall/product/index',
          keepAlive: false
        },
        menuType: 'C',
        status: '0',
        orderNum: 1105
      })
      safeLog('[DynamicRouteAppender] 已在 /mall 下追加 SPU 编辑向导路由')
    }
  }

  /** 在菜单树中查找节点 */
  private static findRouteNode(
    routes: AppRouteRecord[],
    predicate: (route: AppRouteRecord) => boolean
  ): AppRouteRecord | null {
    for (const route of routes) {
      if (predicate(route)) return route
      if (route.children?.length) {
        const found = this.findRouteNode(route.children, predicate)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 追加「Druid 监控」iframe 路由（数据库不存菜单）
   * 将 Druid 原生监控页面通过 iframe 方式集成到动态路由中
   * <p>
   * 注意：项目中存在两种 Druid 监控页面：
   * 1. 自定义页面：/monitor/druid（Vue 组件，通过 API 展示监控数据）
   * 2. 内置页面：/monitor/druid-iframe（Druid 原生页面，通过 iframe 嵌入）
   * <p>
   * 此方法添加的是内置页面路由，提供完整的 Druid 原生监控功能
   * @param menuList 菜单列表
   * @param routeIndex 路由索引
   */
  static appendDruidIframeRoute(menuList: AppRouteRecord[], routeIndex: RouteIndex): void {
    if (routeIndex.names.has('DruidIframe') || routeIndex.paths.has('/monitor/druid-iframe')) {
      safeWarn('[DynamicRouteAppender] Druid 监控路由已存在，跳过追加')
      return
    }

    // 使用相对路径，让 Vite 代理处理
    // 这样前端和后端就是同源（都是 localhost:3000），可以满足 SAMEORIGIN 的要求
    // 开发环境：通过 Vite 代理转发到后端
    // 生产环境：如果前后端部署在同一域名下，也是同源
    const druidUrl = '/api/druid/index.html'

    const druidIframeRoute: AppRouteRecord = {
      path: '/monitor/druid-iframe',
      name: 'DruidIframe',
      meta: {
        title: 'Druid 监控（内置页面）',
        icon: 'ri:database-2-line',
        // 设置为 iframe 类型，通过 link 指定外部链接
        isIframe: true,
        // Druid 监控页面地址（使用相对路径，通过 Vite 代理，确保同源）
        link: druidUrl,
        // 不在菜单树中显示，可通过编程式导航访问
        // 如果需要显示，可以设置为 false，但建议通过自定义页面入口访问
        isHide: true,
        // 指定父级菜单路径，用于面包屑、高亮等
        parentPath: '/monitor',
        keepAlive: true
      },
      menuType: 'C',
      status: '0',
      orderNum: 1003
    }

    menuList.push(druidIframeRoute)
    safeLog('[DynamicRouteAppender] 已动态追加 Druid 监控内置页面 iframe 路由, URL:', druidUrl)
  }
}

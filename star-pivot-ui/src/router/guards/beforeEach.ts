/**
 * 路由全局前置守卫模块
 *
 * 提供完整的路由导航守卫功能
 *
 * ## 主要功能
 *
 * - 登录状态验证和重定向
 * - 动态路由注册和权限控制
 * - 菜单数据获取和处理（后端模式）
 * - 用户信息获取和缓存
 * - 页面标题设置
 * - 工作标签页管理
 * - 进度条和加载动画控制
 * - 静态路由识别和处理
 * - 错误处理和异常跳转
 *
 * ## 使用场景
 *
 * - 路由跳转前的权限验证
 * - 动态菜单加载和路由注册
 * - 用户登录状态管理
 * - 页面访问控制
 * - 路由级别的加载状态管理
 *
 * ## 工作流程
 *
 * 1. 检查登录状态，未登录跳转到登录页
 * 2. 首次访问时获取用户信息和菜单数据
 * 3. 根据权限动态注册路由
 * 4. 设置页面标题和工作标签页
 * 5. 处理根路径重定向到首页
 * 6. 未匹配路由跳转到 404 页面
 *
 * @module router/guards/beforeEach
 * @author Art Design Pro Team
 */
import type { NavigationGuardNext, RouteLocationNormalized, Router } from 'vue-router'
import NProgress from 'nprogress'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { setWorktab } from '@/utils/navigation'
import { setPageTitle } from '@/utils/router'
import {
  handleLoginStatus,
  handleRegisterRouteGuard,
  handleRootPathRedirect,
  isStaticRoute
} from './authGuard'
import {
  closeLoading,
  getRouteLoadingState,
  handleDynamicRoutes,
  resetRouteLoadingState,
  resetRouterState,
  setupRouteRegistry,
  tryRestoreRoutesFromCache
} from './dynamicRouteGuard'

export { getRouteLoadingState, resetRouteLoadingState, resetRouterState }

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  // 初始化路由注册器
  setupRouteRegistry(router)

  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) => {
      try {
        await handleRouteGuard(to, from, next, router)
      } catch (error) {
        console.error('[RouteGuard] 路由守卫处理失败:', error)
        closeLoading()
        next({ name: 'Exception500' })
      }
    }
  )
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  const settingStore = useSettingStore()
  const userStore = useUserStore()

  // 启动进度条
  if (settingStore.showNprogress) {
    NProgress.start()
  }

  // 0. 注册页开关校验（需在登录态检查之前，未登录也可拦截）
  if (!(await handleRegisterRouteGuard(to, next))) {
    return
  }

  // 1. 检查登录状态
  if (!handleLoginStatus(to, next)) {
    return
  }

  // 2. 检查路由初始化是否已失败（防止死循环）
  if (getRouteLoadingState().routeInitFailed) {
    // 已经失败过，直接放行到错误页面，不再重试
    if (to.matched.length > 0) {
      next()
    } else {
      // 未匹配到路由，跳转到 500 页面
      next({ name: 'Exception500', replace: true })
    }
    return
  }

  // 3. 处理动态路由注册
  // 如果用户已登录，但菜单列表为空或路由未注册，需要重新加载菜单
  // 这种情况可能发生在：页面回退、刷新、或菜单数据被意外清空时
  // 注意：若目标为静态路由（如 /403、/500），则不触发菜单加载，避免菜单为空时跳转 403 后再次进入本逻辑导致一直转圈
  const menuStore = useMenuStore()
  const currentUserId = userStore.info?.user?.userId
  const menuCacheValid = menuStore.isMenuCacheValid(currentUserId)
  const needReloadMenu =
    userStore.isLogin &&
    !isStaticRoute(to.path) &&
    (!menuStore.menuList.length || !menuStore.getHomePath() || !menuCacheValid)

  // 3.1 刷新场景：菜单已持久化但内存路由丢失时，优先从缓存快速恢复
  if (userStore.isLogin && !isStaticRoute(to.path) && tryRestoreRoutesFromCache(to, next)) {
    return
  }

  if (needReloadMenu) {
    await handleDynamicRoutes(to, next, router)
    return
  }

  // 4. 处理根路径重定向
  if (handleRootPathRedirect(to, next)) {
    return
  }

  // 5. 处理已匹配的路由
  if (to.matched.length > 0) {
    setWorktab(to)
    setPageTitle(to)
    next()
    return
  }

  // 6. 未匹配到路由，跳转到 404
  next({ name: 'Exception404' })
}

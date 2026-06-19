import type { Router, RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useMenuStore } from '@/store/modules/menu'
import { useWorktabStore } from '@/store/modules/worktab'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/hooks/core/useCommon'
import { fetchGetUserInfo } from '@/api/auth'
import { ApiStatus } from '@/utils/http/status'
import { isHttpError } from '@/utils/http/error'
import { safeLog, safeWarn } from '@/utils'
import {
  RouteRegistry,
  MenuProcessor,
  IframeRouteManager,
  RoutePermissionValidator,
  DynamicRouteAppender
} from '../core'
import { useUserStore } from '@/store/modules/user'

// 路由注册器实例
let routeRegistry: RouteRegistry | null = null

// 菜单处理器实例
const menuProcessor = new MenuProcessor()

// 路由加载状态管理
const routeLoadingState = {
  pendingLoading: false,
  routeInitFailed: false,
  isLoading: false
}

/**
 * 初始化路由注册器
 */
export function setupRouteRegistry(router: Router): void {
  routeRegistry = new RouteRegistry(router)
}

/**
 * 获取路由加载状态
 */
export function getRouteLoadingState(): typeof routeLoadingState {
  return routeLoadingState
}

/**
 * 重置路由加载状态
 */
export function resetRouteLoadingState(): void {
  routeLoadingState.pendingLoading = false
  routeLoadingState.routeInitFailed = false
  routeLoadingState.isLoading = false
}

/**
 * 关闭 loading 效果
 */
export function closeLoading(): void {
  if (routeLoadingState.pendingLoading) {
    loadingService.hideLoading()
    routeLoadingState.pendingLoading = false
    routeLoadingState.isLoading = false
  }
}

/**
 * 从缓存菜单快速恢复动态路由（刷新后的首次进入）
 * 返回 true 表示已接管导航流程并调用 next
 */
export function tryRestoreRoutesFromCache(
  to: RouteLocationNormalized,
  next: NavigationGuardNext
): boolean {
  const menuStore = useMenuStore()
  const cachedMenuList = menuStore.menuList
  const userStore = useUserStore()
  const currentUserId = userStore.info?.user?.userId

  if (!cachedMenuList.length || !menuStore.isMenuCacheValid(currentUserId)) {
    return false
  }

  // 已注册时无需恢复
  if (routeRegistry?.isRegistered()) {
    return false
  }

  try {
    safeLog('[RouteGuard] 检测到缓存菜单，开始快速恢复动态路由...')
    routeRegistry?.register(cachedMenuList)
    menuStore.clearRemoveRouteFns()
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

    const { homePath } = useCommon()
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      cachedMenuList,
      homePath.value || menuStore.getHomePath() || '/'
    )

    next({
      path: hasPermission ? to.path : validatedPath,
      query: hasPermission ? to.query : undefined,
      hash: hasPermission ? to.hash : undefined,
      replace: true
    })
    return true
  } catch (error) {
    console.error('[RouteGuard] 从缓存恢复动态路由失败:', error)
    return false
  }
}

/**
 * 处理动态路由注册与菜单初始化
 */
export async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  // 显示 loading
  routeLoadingState.pendingLoading = true
  routeLoadingState.isLoading = true
  loadingService.showLoading()

  try {
    // 1. 并发获取用户信息与菜单数据，缩短首次路由初始化耗时
    safeLog('[RouteGuard] 开始并发获取用户信息与菜单数据...')
    const [{ rawMenuList, menuList }] = await Promise.all([
      menuProcessor.getMenuListWithRaw(),
      fetchUserInfo()
    ])
    safeLog('[RouteGuard] 用户信息与菜单数据获取成功，菜单数量:', menuList.length)

    // 2.1 保存原始菜单数据到 store（在追加动态路由之前，以便权限获取）
    const menuStore = useMenuStore()
    menuStore.setRawMenuList(rawMenuList || [])
    const currentUserId = useUserStore().info?.user?.userId
    menuStore.markMenuCache(currentUserId)

    // 2.2 仪表盘和工作台无论动态菜单有无都要加载：若后端菜单为空，先追加前端固定路由
    if (menuList.length === 0) {
      safeLog('[RouteGuard] 动态菜单为空，追加仪表盘/工作台等固定路由')
      DynamicRouteAppender.appendDynamicRoutes(menuList)
    }

    // 3. 验证菜单数据
    if (!menuProcessor.validateMenuList(menuList)) {
      const errorMsg = '获取菜单列表失败，菜单数据格式错误'
      safeWarn('[RouteGuard] 菜单数据验证失败，菜单列表格式错误')
      throw new Error(errorMsg)
    }

    // 3.1 前端动态追加路由（数据库不存菜单，如个人中心、字典明细等）
    DynamicRouteAppender.appendDynamicRoutes(menuList)

    // 4. 注册动态路由
    safeLog('[RouteGuard] 开始注册动态路由...')
    routeRegistry?.register(menuList)
    safeLog('[RouteGuard] 动态路由注册成功')

    // 5. 保存菜单数据到 store（必须在重新导航之前）
    menuStore.setMenuList(menuList)
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

    // 确保 homePath 已被正确设置
    if (!menuStore.getHomePath() || menuStore.getHomePath() === '/') {
      safeWarn('[RouteGuard] 首页路径为空，使用默认路径 /dashboard/console')
      menuStore.setHomePath('/dashboard/console')
    }

    // 6. 保存 iframe 路由
    IframeRouteManager.getInstance().save()

    // 7. 验证工作标签页
    useWorktabStore().validateWorktabs(router)

    // 8. 验证目标路径权限
    const { homePath } = useCommon()
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      menuList,
      homePath.value || '/'
    )

    // 初始化成功

    // 9. 重新导航到目标路由
    if (!hasPermission) {
      // 无权限访问，跳转到首页
      closeLoading()

      // 输出警告信息
      safeWarn(`[RouteGuard] 用户无权限访问路径: ${to.path}，已跳转到首页`)

      // 直接跳转到首页
      next({
        path: validatedPath,
        replace: true
      })
    } else {
      // 有权限，正常导航
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      })
    }
  } catch (error) {
    handleRouteError(error, next)
  }
}

/**
 * 处理路由错误
 */
function handleRouteError(error: unknown, next: NavigationGuardNext): void {
  console.error('[RouteGuard] 动态路由注册失败:', error)
  console.error('[RouteGuard] 错误详情:', {
    message: error instanceof Error ? error.message : String(error),
    stack: error instanceof Error ? error.stack : undefined,
    error
  })

  // 关闭 loading
  closeLoading()

  // 401 错误：axios 拦截器已处理退出登录，取消当前导航
  if (isUnauthorizedError(error)) {
    safeWarn('[RouteGuard] 401未授权错误，已取消导航')
    next(false)
    return
  }

  // 标记初始化失败，防止死循环
  routeLoadingState.routeInitFailed = true

  // 输出详细错误信息，便于排查
  if (isHttpError(error)) {
    console.error(`[RouteGuard] HTTP错误 - 错误码: ${error.code}, 消息: ${error.message}`)
  } else if (error instanceof Error) {
    console.error(`[RouteGuard] 错误类型: ${error.constructor.name}, 消息: ${error.message}`)
  }

  // 跳转到 500 页面，使用 replace 避免产生历史记录
  console.error('[RouteGuard] 跳转到500错误页面')
  next({ name: 'Exception500', replace: true })
}

/**
 * 获取用户信息
 */
export async function fetchUserInfo(): Promise<void> {
  const userStore = useUserStore()
  try {
    const data = await fetchGetUserInfo()
    userStore.setUserInfo(data)
    // 检查并清理工作台标签页（如果是不同用户登录）
    userStore.checkAndClearWorktabs()
  } catch (error) {
    console.error('[RouteGuard] 获取用户信息失败:', error)
    throw error
  }
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(delay: number): void {
  setTimeout(() => {
    routeRegistry?.unregister()
    IframeRouteManager.getInstance().clear()

    const menuStore = useMenuStore()
    // removeRouteFn 已由 routeRegistry.unregister 执行，这里只清空引用，避免重复执行
    menuStore.clearRemoveRouteFns()
    menuStore.setMenuList([])
    menuStore.setRawMenuList([])
    menuStore.setHomePath('')
    menuStore.clearMenuCacheMeta()

    // 重置路由初始化状态，允许重新登录后再次初始化
    resetRouteLoadingState()
  }, delay)
}

/**
 * 判断是否为未授权错误（401）
 */
export function isUnauthorizedError(error: unknown): boolean {
  return isHttpError(error) && error.code === ApiStatus.unauthorized
}

/**
 * 重新注册动态路由（用于菜单变更后立即更新路由）
 */
export async function reloadDynamicRoutes(): Promise<void> {
  try {
    safeLog('[RouteGuard] 开始重新注册动态路由...')

    // 1. 获取最新的菜单数据
    const { rawMenuList, menuList } = await menuProcessor.getMenuListWithRaw()
    safeLog('[RouteGuard] 获取到最新菜单数据，菜单数量:', menuList.length)

    // 2. 清除旧的动态路由
    const menuStore = useMenuStore()
    // 这里统一由 RouteRegistry 执行 removeRouteFn，避免重复执行导致抛错中断后续流程
    routeRegistry?.unregister()
    menuStore.clearRemoveRouteFns()
    safeLog('[RouteGuard] 清除旧路由成功')

    // 3. 仪表盘和工作台无论动态菜单有无都要加载：若后端菜单为空，先追加前端固定路由
    if (menuList.length === 0) {
      safeLog('[RouteGuard] 动态菜单为空，追加仪表盘/工作台等固定路由')
      DynamicRouteAppender.appendDynamicRoutes(menuList)
    }

    // 4. 验证菜单数据
    if (!menuProcessor.validateMenuList(menuList)) {
      const errorMsg = '获取菜单列表失败，菜单数据格式错误'
      safeWarn('[RouteGuard] 菜单数据验证失败，菜单列表格式错误')
      throw new Error(errorMsg)
    }

    // 5. 前端动态追加路由（数据库不存菜单，如个人中心、字典明细等）
    DynamicRouteAppender.appendDynamicRoutes(menuList)

    // 6. 注册新的动态路由
    safeLog('[RouteGuard] 开始注册新路由...')
    routeRegistry?.register(menuList)
    safeLog('[RouteGuard] 新路由注册成功')

    // 7. 更新 store 中的菜单数据
    menuStore.setRawMenuList(rawMenuList || [])
    const currentUserId = useUserStore().info?.user?.userId
    menuStore.markMenuCache(currentUserId)
    menuStore.setMenuList(menuList)
    menuStore.clearRemoveRouteFns()
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

    // 8. 确保 homePath 已被正确设置
    if (!menuStore.getHomePath() || menuStore.getHomePath() === '/') {
      safeWarn('[RouteGuard] 首页路径为空，使用默认路径 /dashboard/console')
      menuStore.setHomePath('/dashboard/console')
    }

    // 9. 保存 iframe 路由
    IframeRouteManager.getInstance().save()

    safeLog('[RouteGuard] 动态路由重新注册完成')
  } catch (error) {
    console.error('[RouteGuard] 重新注册动态路由失败:', error)
    throw error
  }
}

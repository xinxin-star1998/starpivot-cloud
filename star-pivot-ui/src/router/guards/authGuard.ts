import type {NavigationGuardNext, RouteLocationNormalized} from 'vue-router'
import {useUserStore} from '@/store/modules/user'
import {RoutesAlias} from '../routesAlias'
import {staticRoutes} from '../routes/staticRoutes'
import {useCommon} from '@/hooks/core/useCommon'
import {isRegisterEnabled} from '@/utils/auth/register-config'

/**
 * 注册页访问控制：配置关闭时重定向到登录页
 * @returns true 表示可以继续，false 表示已处理跳转
 */
export async function handleRegisterRouteGuard(
  to: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<boolean> {
  if (to.name !== 'Register') {
    return true
  }

  const enabled = await isRegisterEnabled()
  if (!enabled) {
    next({ name: 'Login', replace: true })
    return false
  }

  return true
}

/**
 * 处理登录状态
 * @returns true 表示可以继续，false 表示已处理跳转
 */
export function handleLoginStatus(to: RouteLocationNormalized, next: NavigationGuardNext): boolean {
  const userStore = useUserStore()

  // 已登录或访问登录页或静态路由，直接放行
  if (userStore.isLogin || to.path === RoutesAlias.Login || isStaticRoute(to.path)) {
    return true
  }

  // 未登录且访问需要权限的页面，跳转到登录页并携带 redirect 参数
  userStore.logOut()
  next({
    name: 'Login',
    query: { redirect: to.fullPath }
  })
  return false
}

/**
 * 检查路由是否为静态路由
 */
export function isStaticRoute(path: string): boolean {
  const checkRoute = (routes: any[], targetPath: string): boolean => {
    return routes.some((route) => {
      // 处理动态路由参数匹配
      const routePath = route.path
      const pattern = routePath.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
      const regex = new RegExp(`^${pattern}$`)

      if (regex.test(targetPath)) {
        return true
      }
      if (route.children && route.children.length > 0) {
        return checkRoute(route.children, targetPath)
      }
      return false
    })
  }

  return checkRoute(staticRoutes, path)
}

/**
 * 处理根路径重定向到首页
 * 未登录访问 / 时重定向到登录页，避免停留在根路径或落入 404
 * @returns true 表示已处理跳转，false 表示无需跳转
 */
export function handleRootPathRedirect(
  to: RouteLocationNormalized,
  next: NavigationGuardNext
): boolean {
  if (to.path !== '/') {
    return false
  }

  const userStore = useUserStore()
  const { homePath } = useCommon()

  // 未登录时根路径直接重定向到登录页
  if (!userStore.isLogin) {
    next({ name: 'Login', query: { redirect: '/' }, replace: true })
    return true
  }

  // 已登录时，如果 homePath 存在且不等于根路径，则重定向到首页
  if (homePath.value && homePath.value !== '/') {
    next({ path: homePath.value, replace: true })
    return true
  }

  // 如果 homePath 为空或等于根路径，使用默认首页路径
  if (!homePath.value || homePath.value === '/') {
    const defaultHomePath = '/dashboard/console'
    next({ path: defaultHomePath, replace: true })
    return true
  }

  return false
}

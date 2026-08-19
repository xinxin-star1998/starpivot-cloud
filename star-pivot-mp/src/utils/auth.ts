import {isLogin} from '@/stores/member'

const TAB_BAR_PAGES = new Set(['pages/index/index', 'pages/cart/index', 'pages/account/index'])

let redirectingToLogin = false

/** 当前页路径（含 query），形如 /pkg-mall/orders/index?status=0 */
export function getCurrentPageUrl(): string {
  const pages = getCurrentPages()
  if (!pages.length) return '/pages/index/index'
  const page = pages[pages.length - 1] as {
    route?: string
    options?: Record<string, string | undefined>
  }
  const route = page.route || 'pages/index/index'
  const options = page.options || {}
  const query = Object.entries(options)
    .filter(([, v]) => v != null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')
  return query ? `/${route}?${query}` : `/${route}`
}

function isLoginPage(url?: string): boolean {
  const path = (url || '').split('?')[0].replace(/^\//, '')
  return path === 'pages/login/index'
}

export function isTabBarPage(url: string): boolean {
  const path = url.split('?')[0].replace(/^\//, '')
  return TAB_BAR_PAGES.has(path)
}

/** 跳转登录，登录成功后回到 redirect（默认当前页） */
export function goLogin(redirect?: string) {
  if (redirectingToLogin) return
  const pages = getCurrentPages()
  const current = pages[pages.length - 1] as { route?: string } | undefined
  if (current?.route === 'pages/login/index') return

  // @click 直接绑定会传入事件对象，需忽略
  let target = typeof redirect === 'string' && redirect ? redirect : getCurrentPageUrl()
  if (isLoginPage(target)) {
    target = '/pages/account/index'
  }

  redirectingToLogin = true
  uni.navigateTo({
    url: `/pages/login/index?redirect=${encodeURIComponent(target)}`,
    complete: () => {
      setTimeout(() => {
        redirectingToLogin = false
      }, 800)
    }
  })
}

/** 未登录则跳转登录并返回 false */
export function requireLogin(redirect?: string): boolean {
  if (isLogin()) return true
  goLogin(redirect)
  return false
}

/** 登录成功后的页面跳转 */
export function navigateAfterLogin(redirect?: string) {
  if (!redirect) {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack()
      return
    }
    uni.switchTab({ url: '/pages/index/index' })
    return
  }

  const raw = redirect.startsWith('/') ? redirect : `/${redirect}`
  if (isTabBarPage(raw)) {
    const path = raw.split('?')[0]
    uni.switchTab({ url: path })
    return
  }

  uni.redirectTo({
    url: raw,
    fail: () => {
      uni.reLaunch({ url: raw })
    }
  })
}

import type {PortalCartItem} from '@/api/types'

/** tabBar cart tab index: home 0, cart 1, account 2 */
export const CART_TAB_INDEX = 1

const TAB_BAR_PAGES = new Set(['pages/index/index', 'pages/cart/index', 'pages/account/index'])

export function cartItemCount(items: PortalCartItem[]): number {
  return items.reduce((sum, item) => sum + (item.quantity || 1), 0)
}

function isCurrentTabBarPage(): boolean {
  const pages = getCurrentPages()
  if (!pages.length) return false
  const route = (pages[pages.length - 1] as { route?: string }).route || ''
  return TAB_BAR_PAGES.has(route)
}

export function applyCartBadge(count: number) {
  if (!isCurrentTabBarPage()) return
  const fail = () => undefined
  if (count <= 0) {
    uni.removeTabBarBadge({ index: CART_TAB_INDEX, fail })
    return
  }
  uni.setTabBarBadge({
    index: CART_TAB_INDEX,
    text: count > 99 ? '99+' : String(count),
    fail
  })
}

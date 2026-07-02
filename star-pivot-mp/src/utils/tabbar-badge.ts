import type {PortalCartItem} from '@/api/types'

/** tabBar cart tab index: home 0, cart 1, account 2 */
export const CART_TAB_INDEX = 1

export function cartItemCount(items: PortalCartItem[]): number {
  return items.reduce((sum, item) => sum + (item.quantity || 1), 0)
}

export function applyCartBadge(count: number) {
  try {
    if (count <= 0) {
      uni.removeTabBarBadge({ index: CART_TAB_INDEX })
      return
    }
    uni.setTabBarBadge({
      index: CART_TAB_INDEX,
      text: count > 99 ? '99+' : String(count)
    })
  } catch {
    // tabBar may not be ready yet
  }
}
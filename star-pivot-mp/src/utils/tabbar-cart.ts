import {fetchCart} from '@/api/cart'
import type {PortalCartItem} from '@/api/types'
import {isLogin} from '@/stores/member'
import {applyCartBadge, cartItemCount} from '@/utils/tabbar-badge'

export {applyCartBadge, cartItemCount, CART_TAB_INDEX} from '@/utils/tabbar-badge'

export function syncCartBadgeFromItems(items: PortalCartItem[]) {
  applyCartBadge(cartItemCount(items))
}

export async function refreshCartBadge() {
  if (!isLogin()) {
    applyCartBadge(0)
    return
  }
  try {
    const data = await fetchCart()
    syncCartBadgeFromItems(data.items || [])
  } catch {
    // silent
  }
}
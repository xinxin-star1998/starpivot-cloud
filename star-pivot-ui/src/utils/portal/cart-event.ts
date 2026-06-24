/** 通知 header 刷新购物车数量 */
export function notifyPortalCartChanged() {
  window.dispatchEvent(new Event('portal-cart-changed'))
}

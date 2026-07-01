/** 评价/待评价数量变化时通知各入口刷新 */
export function notifyPortalReviewChanged() {
  window.dispatchEvent(new Event('portal-review-changed'))
}

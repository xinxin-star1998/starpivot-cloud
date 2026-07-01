/** 浏览足迹变化时通知首页等入口刷新 */
export function notifyPortalBrowseChanged() {
  window.dispatchEvent(new Event('portal-browse-changed'))
}

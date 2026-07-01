/** 搜索历史变化时通知各入口刷新 */
export function notifyPortalSearchHistoryChanged() {
  window.dispatchEvent(new Event('portal-search-history-changed'))
}

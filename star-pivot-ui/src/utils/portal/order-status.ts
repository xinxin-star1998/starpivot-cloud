/** C 端订单状态文案 */
export const PORTAL_ORDER_STATUS: Record<number, { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '待发货', type: 'info' },
  2: { label: '已发货', type: 'info' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已关闭', type: 'info' },
  5: { label: '无效订单', type: 'danger' }
}

export function getPortalOrderStatusLabel(status?: number) {
  if (status === undefined || status === null) return '未知'
  return PORTAL_ORDER_STATUS[status]?.label ?? `状态${status}`
}

export function getPortalOrderStatusType(status?: number) {
  if (status === undefined || status === null) return 'info' as const
  return PORTAL_ORDER_STATUS[status]?.type ?? 'info'
}

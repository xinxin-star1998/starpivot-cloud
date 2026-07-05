/**
 * 站内消息 → 业务详情页导航
 */
export interface MessageBizNav {
  path: string
  query?: Record<string, string>
  label: string
}

export function resolveMessageBizNav(
  bizModule?: string,
  bizType?: string,
  bizKey?: string,
  linkPath?: string
): MessageBizNav | null {
  if (linkPath) {
    return { path: linkPath, label: '查看详情' }
  }
  if (!bizModule || !bizType || !bizKey) return null
  const parts = bizKey.split(':')
  const id = parts[parts.length - 1]
  if (!id || !/^\d+$/.test(id)) return null

  if (bizModule === 'mall') {
    switch (bizType) {
      case 'purchase':
        return { path: '/mall/wms/purchaseorder', query: { openId: id }, label: '查看采购单' }
      case 'return':
        return { path: '/mall/oms/order/return', query: { openId: id }, label: '查看退货单' }
      case 'coupon':
        return { path: '/mall/sms/coupon/coupon', query: { openId: id }, label: '查看优惠券' }
      case 'spu':
        return { path: '/mall/pms/product/spu', query: { openId: id }, label: '查看商品' }
      default:
        return null
    }
  }

  if (bizModule === 'approval') {
    if (bizType === 'task') {
      return { path: '/approval/todo', label: '查看待办' }
    }
    return { path: '/approval/mine', label: '查看我发起的' }
  }

  return null
}

export const MSG_TYPE_LABEL: Record<string, string> = {
  APPROVAL_TASK: '审批待办',
  APPROVAL_RESULT: '审批结果',
  SYSTEM: '系统通知',
  ORDER: '订单消息',
  REFUND_ALERT: '退款告警',
  BROADCAST: '系统群发'
}

/**
 * 审批待办 → 业务详情页导航
 */
export interface ApprovalBizNav {
  path: string
  query?: Record<string, string>
  label: string
}

export function resolveApprovalBizNav(
  bizModule?: string,
  bizType?: string,
  bizKey?: string
): ApprovalBizNav | null {
  if (!bizModule || !bizType || !bizKey) return null
  const parts = bizKey.split(':')
  const id = parts[parts.length - 1]
  if (!id || !/^\d+$/.test(id)) return null

  if (bizModule !== 'mall') return null

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

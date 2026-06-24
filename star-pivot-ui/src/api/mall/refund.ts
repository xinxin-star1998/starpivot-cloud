import request from '@/utils/http'

export interface RefundVo {
  id?: number
  orderReturnId?: number
  refund?: number
  refundSn?: string
  refundStatus?: number
  refundChannel?: number
  refundContent?: string
}

export interface RefundListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
}

export function fetchRefundList(params: RefundListParams) {
  return request.post<Api.Common.PaginatedResponse<RefundVo>>({
    url: '/api/mall/refund/list',
    data: params
  })
}

/** 退款状态 */
export const REFUND_STATUS_MAP: Record<number, string> = {
  0: '待退款',
  1: '退款中',
  2: '已退款',
  3: '退款失败'
}

/** 退款渠道 */
export const REFUND_CHANNEL_MAP: Record<number, string> = {
  1: '支付宝',
  2: '微信'
}

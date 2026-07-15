import request from '@/utils/http'

export interface RefundVo {
  id?: number
  orderReturnId?: number
  orderSn?: string
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
  return request.post<Api.Common.PageResponse<RefundVo>>({
    url: '/api/mall/refund/refundPageList',
    data: params
  })
}

export function fetchRefundById(id: number) {
  return request.get<RefundVo>({
    url: `/api/mall/refund/${id}`
  })
}

export function fetchRefundSync(id: number) {
  return request.put<RefundVo>({
    url: `/api/mall/refund/${id}/sync`
  })
}

export function fetchRefundRetry(id: number) {
  return request.put<RefundVo>({
    url: `/api/mall/refund/${id}/retry`
  })
}

export interface RefundAlertItem {
  id?: number
  orderSn?: string
  refundSn?: string
  refund?: number
  refundChannel?: number
}

export interface RefundAlertSummary {
  unreadCount?: number
  recentItems?: RefundAlertItem[]
}

export function fetchRefundAlertSummary() {
  return request.get<RefundAlertSummary>({
    url: '/api/mall/refund/alert/summary'
  })
}

export function fetchRefundAckAlert(id: number) {
  return request.post<void>({
    url: `/api/mall/refund/${id}/ack-alert`
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

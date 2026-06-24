import request from '@/utils/http'

export interface PaymentVo {
  id?: number
  orderSn?: string
  orderId?: number
  alipayTradeNo?: string
  totalAmount?: number
  subject?: string
  paymentStatus?: string
  createTime?: string
  confirmTime?: string
  callbackContent?: string
  callbackTime?: string
}

export interface PaymentListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
}

export function fetchPaymentList(params: PaymentListParams) {
  return request.post<Api.Common.PaginatedResponse<PaymentVo>>({
    url: '/api/mall/payment/list',
    data: params
  })
}

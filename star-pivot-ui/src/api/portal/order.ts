import request from '@/utils/http'
import type {
    PortalOrder,
    PortalOrderPriceTrial,
    PortalOrderPriceTrialPayload,
    PortalOrderQueryParams,
    PortalOrderSubmitPayload,
    PortalOrderSubmitResult,
    PortalOrderSubmitToken
} from './types'

export function fetchPortalOrderSubmitToken() {
  return request.get<PortalOrderSubmitToken>({
    url: '/api/portal/order/submit-token'
  })
}

export function fetchPortalOrderPriceTrial(data: PortalOrderPriceTrialPayload) {
  return request.post<PortalOrderPriceTrial>({
    url: '/api/portal/order/price-trial',
    data
  })
}

export function fetchPortalOrderSubmit(data: PortalOrderSubmitPayload) {
  return request.post<PortalOrderSubmitResult>({
    url: '/api/portal/order/submit',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalOrderList(params: PortalOrderQueryParams) {
  return request.post<Api.Common.PaginatedResponse<PortalOrder>>({
    url: '/api/portal/order/portalOrderPageList',
    data: params
  })
}

export function fetchPortalOrderStatusCounts() {
  return request.get<Record<string, number>>({
    url: '/api/portal/order/status-counts'
  })
}

export function fetchPortalOrderDetail(id: number) {
  return request.get<PortalOrder>({
    url: `/api/portal/order/${id}`
  })
}

export function fetchPortalOrderCancel(id: number) {
  return request.put<void>({
    url: `/api/portal/order/${id}/cancel`,
    showSuccessMessage: true
  })
}

/** 开发联调：Mock 支付，待付款 → 待发货 */
export function fetchPortalOrderMockPay(id: number) {
  return request.put<void>({
    url: `/api/portal/order/${id}/pay`,
    showSuccessMessage: true
  })
}

export function fetchPortalOrderConfirmReceive(id: number) {
  return request.put<void>({
    url: `/api/portal/order/${id}/receive`,
    showSuccessMessage: true
  })
}

export interface PortalOrderReturnApplyPayload {
  orderId: number
  skuId?: number
  quantity?: number
  items?: Array<{ skuId: number; quantity: number }>
  reason: string
  description?: string
}

export function fetchPortalOrderReturnApply(data: PortalOrderReturnApplyPayload) {
  return request.post<number[]>({
    url: '/api/portal/order/return',
    data,
    showSuccessMessage: true
  })
}

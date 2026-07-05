import {request} from '@/utils/request'
import type {
    PageResult,
    PortalOrder,
    PortalOrderPriceTrial,
    PortalOrderSubmitPayload,
    PortalOrderSubmitResult,
    PortalOrderSubmitToken
} from './types'

export function fetchOrderSubmitToken() {
  return request<PortalOrderSubmitToken>({ url: '/portal/order/submit-token' })
}

export function fetchOrderPriceTrial(data: {
  useCart?: boolean
  items?: Array<{ skuId: number; quantity: number }>
  couponHistoryId?: number
  useIntegration?: number
}) {
  return request<PortalOrderPriceTrial>({
    url: '/portal/order/price-trial',
    method: 'POST',
    data
  })
}

export function fetchOrderSubmit(data: PortalOrderSubmitPayload) {
  return request<PortalOrderSubmitResult>({
    url: '/portal/order/submit',
    method: 'POST',
    data
  })
}

export function fetchOrders(pageNum = 1, pageSize = 10, status?: number) {
  return request<PageResult<PortalOrder>>({
    url: '/portal/order/portalOrderPageList',
    method: 'POST',
    data: { pageNum, pageSize, status }
  })
}

export function fetchOrderDetail(id: number) {
  return request<PortalOrder>({ url: `/portal/order/${id}` })
}

export function fetchOrderLogistics(id: number) {
  return request<import('./types').PortalShipmentTracking>({ url: `/portal/order/${id}/logistics` })
}

export function cancelOrder(id: number) {
  return request<void>({ url: `/portal/order/${id}/cancel`, method: 'PUT' })
}

export function confirmReceive(id: number) {
  return request<void>({ url: `/portal/order/${id}/receive`, method: 'PUT' })
}

export interface PortalOrderReturnApplyPayload {
  orderId: number
  skuId?: number
  quantity?: number
  items?: Array<{ skuId: number; quantity: number }>
  reason: string
  description?: string
}

export function applyOrderReturn(data: PortalOrderReturnApplyPayload) {
  return request<number[]>({
    url: '/portal/order/return',
    method: 'POST',
    data
  })
}

export function fetchOrderStatusCounts() {
  return request<Record<string, number>>({ url: '/portal/order/status-counts' })
}

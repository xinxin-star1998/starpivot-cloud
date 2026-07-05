import request from '@/utils/http'
import type { TrackEventVo } from '@/api/tms/types'

export interface TmsShipment {
  id?: number
  shipmentSn?: string
  orderSn?: string
  bizId?: number
  carrierName?: string
  kuaidi100Com?: string
  trackingNo?: string
  status?: string
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  shipTime?: string
  deliverTime?: string
  createTime?: string
  events?: TrackEventVo[]
}

export interface TmsShipmentListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
  trackingNo?: string
  status?: string
  carrierName?: string
}

export interface TmsShipmentShipPayload {
  orderId: number
  carrierId: number
  trackingNo: string
}

export function fetchTmsShipmentList(params: TmsShipmentListParams) {
  return request.post<Api.Common.PageResponse<TmsShipment>>({
    url: '/tms/shipment/pageList',
    data: params
  })
}

export function fetchTmsShipmentDetail(id: number) {
  return request.get<TmsShipment>({
    url: `/tms/shipment/${id}`
  })
}

export function fetchTmsShipmentShip(data: TmsShipmentShipPayload) {
  return request.post<number>({
    url: '/tms/shipment/ship',
    data
  })
}

export function fetchTmsShipmentRefreshTrack(id: number) {
  return request.post<TmsShipment>({
    url: `/tms/shipment/${id}/refresh-track`
  })
}

export function fetchPortalOrderLogistics(orderId: number) {
  return request.get<import('@/api/tms/types').ShipmentTracking>({
    url: `/portal/order/${orderId}/logistics`
  })
}

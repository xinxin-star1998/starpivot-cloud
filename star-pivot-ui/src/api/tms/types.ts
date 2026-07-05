export interface TrackEventVo {
  eventTime?: string
  eventStatus?: string
  eventDesc?: string
  location?: string
  source?: string
}

export interface ShipmentTracking {
  shipmentId?: number
  shipmentSn?: string
  orderSn?: string
  carrierName?: string
  kuaidi100Com?: string
  trackingNo?: string
  status?: string
  shipTime?: string
  deliverTime?: string
  events?: TrackEventVo[]
}

export const shipmentStatusLabel: Record<string, string> = {
  SHIPPED: '已发货',
  IN_TRANSIT: '运输中',
  DELIVERED: '已签收',
  EXCEPTION: '异常'
}

export const shipmentStatusTagType: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
  SHIPPED: 'info',
  IN_TRANSIT: 'warning',
  DELIVERED: 'success',
  EXCEPTION: 'danger'
}

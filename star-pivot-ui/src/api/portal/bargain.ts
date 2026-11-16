import request from '@/utils/http'

export interface PortalBargainActivity {
  id?: number
  title?: string
  skuId?: number
  skuName?: string
  skuImg?: string
  originalPrice?: number
  floorPrice?: number
  bargainTimes?: number
  minAmount?: number
  maxAmount?: number
  remainStock?: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  state?: 'ongoing' | 'upcoming' | 'ended'
}

export interface PortalBargainHelper {
  helperMemberId?: number
  cutAmount?: number
}

export interface PortalBargainRecord {
  id?: number
  activityId?: number
  title?: string
  skuId?: number
  skuName?: string
  skuImg?: string
  originalPrice?: number
  currentPrice?: number
  floorPrice?: number
  bargainTimes?: number
  helpCount?: number
  remainHelps?: number
  status?: number
  statusText?: string
  expireTime?: string
  orderId?: number
  orderSn?: string
  createTime?: string
  helpers?: PortalBargainHelper[]
}

export interface PortalBargainDetail {
  activity?: PortalBargainActivity
  record?: PortalBargainRecord
}

export function fetchPortalBargainList() {
  return request.get<PortalBargainActivity[]>({ url: '/api/portal/bargain' })
}

export function fetchPortalBargainDetail(activityId: number) {
  return request.get<PortalBargainDetail>({ url: `/api/portal/bargain/${activityId}` })
}

export function fetchPortalBargainRecord(recordId: number) {
  return request.get<PortalBargainRecord>({ url: `/api/portal/bargain/record/${recordId}` })
}

export function fetchPortalBargainStart(activityId: number) {
  return request.post<PortalBargainRecord>({
    url: '/api/portal/bargain/start',
    data: { activityId }
  })
}

export function fetchPortalBargainHelp(recordId: number) {
  return request.post<PortalBargainRecord>({
    url: '/api/portal/bargain/help',
    data: { recordId }
  })
}

export function fetchPortalBargainOrder(payload: {
  recordId: number
  quantity: number
  addressId: number
  orderToken: string
}) {
  return request.post<PortalBargainRecord>({
    url: '/api/portal/bargain/order',
    data: payload
  })
}

export function fetchPortalBargainMy() {
  return request.get<PortalBargainRecord[]>({ url: '/api/portal/bargain/my' })
}

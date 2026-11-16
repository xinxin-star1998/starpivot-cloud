import request from '@/utils/http'
import type { PortalOrderSubmitResult } from './types'

export interface PortalGroupBuyActivity {
  id?: number
  title?: string
  skuId?: number
  skuName?: string
  skuImg?: string
  originalPrice?: number
  groupPrice?: number
  groupSize?: number
  remainStock?: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  state?: 'ongoing' | 'upcoming' | 'ended'
}

export interface PortalGroupBuyGroup {
  groupNo?: string
  leaderMemberId?: number
  joinedCount?: number
  groupSize?: number
  remainCount?: number
  expireTime?: string
  remainSeconds?: number
}

export interface PortalGroupBuyDetail {
  activity?: PortalGroupBuyActivity
  groups?: PortalGroupBuyGroup[]
}

export interface PortalGroupBuyRecord {
  id?: number
  activityId?: number
  title?: string
  skuId?: number
  skuName?: string
  skuImg?: string
  groupNo?: string
  groupPrice?: number
  quantity?: number
  status?: number
  statusText?: string
  leader?: boolean
  joinedCount?: number
  groupSize?: number
  expireTime?: string
  orderId?: number
  orderSn?: string
  createTime?: string
}

export interface PortalGroupBuyOrderPayload {
  activityId?: number
  groupNo?: string
  quantity: number
  addressId: number
  orderToken: string
}

export function fetchPortalGroupBuyList() {
  return request.get<PortalGroupBuyActivity[]>({ url: '/api/portal/group-buy' })
}

export function fetchPortalGroupBuyDetail(activityId: number) {
  return request.get<PortalGroupBuyDetail>({ url: `/api/portal/group-buy/${activityId}` })
}

export function fetchPortalGroupBuyStart(payload: PortalGroupBuyOrderPayload) {
  return request.post<PortalGroupBuyRecord>({
    url: '/api/portal/group-buy/start',
    data: payload
  })
}

export function fetchPortalGroupBuyJoin(payload: PortalGroupBuyOrderPayload) {
  return request.post<PortalGroupBuyRecord>({
    url: '/api/portal/group-buy/join',
    data: payload
  })
}

export function fetchPortalGroupBuyMy() {
  return request.get<PortalGroupBuyRecord[]>({ url: '/api/portal/group-buy/my' })
}

export type { PortalOrderSubmitResult }

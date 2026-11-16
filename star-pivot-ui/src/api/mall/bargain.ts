import request from '@/utils/http'

export interface BargainActivityVo {
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
  stockCount?: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  status?: number
  sort?: number
  createTime?: string
}

export interface BargainActivityListParams extends Api.Common.CommonSearchParams {
  title?: string
  status?: number
  skuId?: number
}

export interface BargainActivitySavePayload {
  id?: number
  title: string
  skuId: number
  originalPrice: number
  floorPrice: number
  bargainTimes: number
  minAmount: number
  maxAmount: number
  stockCount: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  status?: number
  sort?: number
}

export function fetchBargainActivityList(params: BargainActivityListParams) {
  return request.post<Api.Common.PageResponse<BargainActivityVo>>({
    url: '/api/mall/bargain-activity/pageList',
    data: params
  })
}

export function fetchBargainActivityById(id: number) {
  return request.get<BargainActivityVo>({
    url: `/api/mall/bargain-activity/${id}`
  })
}

export function fetchBargainActivityAdd(data: BargainActivitySavePayload) {
  return request.post<void>({
    url: '/api/mall/bargain-activity',
    data,
    showSuccessMessage: true
  })
}

export function fetchBargainActivityUpdate(data: BargainActivitySavePayload) {
  return request.put<void>({
    url: '/api/mall/bargain-activity',
    data,
    showSuccessMessage: true
  })
}

export function fetchBargainActivityRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/bargain-activity/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

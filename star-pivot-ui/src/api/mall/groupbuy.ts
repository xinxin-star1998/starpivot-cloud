import request from '@/utils/http'

export interface GroupBuyActivityVo {
  id?: number
  title?: string
  skuId?: number
  skuName?: string
  skuImg?: string
  originalPrice?: number
  groupPrice?: number
  groupSize?: number
  stockCount?: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  status?: number
  sort?: number
  createTime?: string
}

export interface GroupBuyActivityListParams extends Api.Common.CommonSearchParams {
  title?: string
  status?: number
  skuId?: number
}

export interface GroupBuyActivitySavePayload {
  id?: number
  title: string
  skuId: number
  groupPrice: number
  groupSize: number
  stockCount: number
  limitBuy?: number
  startTime?: string
  endTime?: string
  validHours?: number
  status?: number
  sort?: number
}

export function fetchGroupBuyActivityList(params: GroupBuyActivityListParams) {
  return request.post<Api.Common.PageResponse<GroupBuyActivityVo>>({
    url: '/api/mall/group-buy-activity/pageList',
    data: params
  })
}

export function fetchGroupBuyActivityById(id: number) {
  return request.get<GroupBuyActivityVo>({
    url: `/api/mall/group-buy-activity/${id}`
  })
}

export function fetchGroupBuyActivityAdd(data: GroupBuyActivitySavePayload) {
  return request.post<void>({
    url: '/api/mall/group-buy-activity',
    data,
    showSuccessMessage: true
  })
}

export function fetchGroupBuyActivityUpdate(data: GroupBuyActivitySavePayload) {
  return request.put<void>({
    url: '/api/mall/group-buy-activity',
    data,
    showSuccessMessage: true
  })
}

export function fetchGroupBuyActivityRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/group-buy-activity/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

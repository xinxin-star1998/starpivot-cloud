import request from '@/utils/http'

export interface MemberPriceVo {
  id?: number
  skuId?: number
  skuName?: string
  memberLevelId?: number
  memberLevelName?: string
  memberPrice?: number
  addOther?: number
}

export interface MemberPriceListParams extends Api.Common.CommonSearchParams {
  skuId?: number
  memberLevelId?: number
}

export interface MemberPriceSavePayload {
  id?: number
  skuId: number
  memberLevelId: number
  memberLevelName?: string
  memberPrice?: number
  addOther?: number
}

export function fetchMemberPriceList(params: MemberPriceListParams) {
  return request.post<Api.Common.PaginatedResponse<MemberPriceVo>>({
    url: '/api/mall/member-price/memberPricePageList',
    data: params
  })
}

export function fetchMemberPriceById(id: number) {
  return request.get<MemberPriceVo>({
    url: `/api/mall/member-price/${id}`
  })
}

export function fetchMemberPriceAdd(data: MemberPriceSavePayload) {
  return request.post<void>({
    url: '/api/mall/member-price',
    data,
    showSuccessMessage: true
  })
}

export function fetchMemberPriceUpdate(data: MemberPriceSavePayload) {
  return request.put<void>({
    url: '/api/mall/member-price',
    data,
    showSuccessMessage: true
  })
}

export function fetchMemberPriceRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/member-price/removeMemberPrice',
    data: { ids },
    showSuccessMessage: true
  })
}

export interface MemberLevelVo {
  id?: number
  name?: string
}

export function fetchMemberLevelList() {
  return request.post<MemberLevelVo[]>({
    url: '/api/mall/member-level/memberLevelPageList'
  })
}

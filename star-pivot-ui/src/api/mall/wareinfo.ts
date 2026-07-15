import request from '@/utils/http'

/** 仓库 VO（wms_ware_info） */
export interface WmsWareInfoVo {
  id?: number
  name?: string
  address?: string
  areacode?: string
}

export interface WmsWareInfoListParams extends Api.Common.CommonSearchParams {
  name?: string
  address?: string
  areacode?: string
}

export interface WmsWareInfoSavePayload {
  id?: number
  name: string
  address?: string
  areacode?: string
}

export function fetchWmsWareInfoList(params: WmsWareInfoListParams) {
  return request.post<Api.Common.PageResponse<WmsWareInfoVo>>({
    url: '/api/mall/wareinfo/wareInfoPageList',
    data: params
  })
}

export function fetchWmsWareInfoById(id: number) {
  return request.get<WmsWareInfoVo>({
    url: `/api/mall/wareinfo/${id}`
  })
}

export function fetchWmsWareInfoAdd(data: WmsWareInfoSavePayload) {
  return request.post<void>({
    url: '/api/mall/wareinfo',
    data,
    showSuccessMessage: true
  })
}

export function fetchWmsWareInfoUpdate(data: WmsWareInfoSavePayload) {
  return request.put<void>({
    url: '/api/mall/wareinfo',
    data,
    showSuccessMessage: true
  })
}

export function fetchWmsWareInfoRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/wareinfo/removeWareInfo',
    data: { ids },
    showSuccessMessage: true
  })
}

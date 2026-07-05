import request from '@/utils/http'

export interface TmsCarrier {
  id?: number
  carrierCode?: string
  carrierName?: string
  kuaidi100Com?: string
  sortOrder?: number
  status?: string
  remark?: string
}

export interface TmsCarrierListParams extends Api.Common.CommonSearchParams {
  carrierCode?: string
  carrierName?: string
  status?: string
}

export interface TmsCarrierSavePayload {
  id?: number
  carrierCode: string
  carrierName: string
  kuaidi100Com: string
  sortOrder?: number
  status?: string
  remark?: string
}

export function fetchTmsCarrierList(params: TmsCarrierListParams) {
  return request.post<Api.Common.PageResponse<TmsCarrier>>({
    url: '/tms/carrier/pageList',
    data: params
  })
}

export function fetchTmsCarrierEnabled() {
  return request.get<TmsCarrier[]>({
    url: '/tms/carrier/enabled'
  })
}

export function fetchTmsCarrierSave(data: TmsCarrierSavePayload) {
  return request.post<number>({
    url: '/tms/carrier/save',
    data
  })
}

export function fetchTmsCarrierRemove(id: number) {
  return request.delete<void>({
    url: `/tms/carrier/${id}`
  })
}

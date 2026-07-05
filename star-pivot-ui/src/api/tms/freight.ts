import request from '@/utils/http'

export interface TmsFreightRule {
  id?: number
  ruleName?: string
  ruleType?: string
  defaultFlag?: string
  baseFee?: number
  firstWeightKg?: number
  firstFee?: number
  continueFeePerKg?: number
  status?: string
  sortOrder?: number
  remark?: string
}

export interface TmsFreightListParams extends Api.Common.CommonSearchParams {
  ruleName?: string
  ruleType?: string
  status?: string
}

export interface TmsFreightSavePayload {
  id?: number
  ruleName: string
  ruleType: string
  defaultFlag?: string
  baseFee?: number
  firstWeightKg?: number
  firstFee?: number
  continueFeePerKg?: number
  status?: string
  sortOrder?: number
  remark?: string
}

export function fetchTmsFreightList(params: TmsFreightListParams) {
  return request.post<Api.Common.PageResponse<TmsFreightRule>>({
    url: '/tms/freight/pageList',
    data: params
  })
}

export function fetchTmsFreightSave(data: TmsFreightSavePayload) {
  return request.post<number>({
    url: '/tms/freight/save',
    data
  })
}

export function fetchTmsFreightRemove(id: number) {
  return request.delete<void>({
    url: `/tms/freight/${id}`
  })
}

export const freightRuleTypeLabel: Record<string, string> = {
  FIXED: '固定运费',
  WEIGHT: '按重量'
}

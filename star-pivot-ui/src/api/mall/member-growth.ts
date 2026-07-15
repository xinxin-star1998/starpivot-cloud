import request from '@/utils/http'

export interface IntegrationChangeHistoryVo {
  id?: number
  memberId?: number
  createTime?: string
  changeCount?: number
  note?: string
  sourceType?: number
}

export interface GrowthChangeHistoryVo {
  id?: number
  memberId?: number
  createTime?: string
  changeCount?: number
  note?: string
  sourceType?: number
}

export interface MemberGrowthListParams extends Api.Common.CommonSearchParams {
  memberId?: number
}

export function fetchIntegrationHistoryList(params: MemberGrowthListParams) {
  return request.post<Api.Common.PageResponse<IntegrationChangeHistoryVo>>({
    url: '/api/mall/member-growth/integrationPageList',
    data: params
  })
}

export function fetchGrowthHistoryList(params: MemberGrowthListParams) {
  return request.post<Api.Common.PageResponse<GrowthChangeHistoryVo>>({
    url: '/api/mall/member-growth/growthPageList',
    data: params
  })
}

/** 变动来源类型 */
export const CHANGE_SOURCE_TYPE_MAP: Record<number, string> = {
  0: '购物',
  1: '管理员修改',
  2: '活动赠送'
}

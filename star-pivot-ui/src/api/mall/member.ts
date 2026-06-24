import request from '@/utils/http'

export interface MemberVo {
  id?: number
  levelId?: number
  username?: string
  nickname?: string
  mobile?: string
  email?: string
  header?: string
  gender?: number
  birth?: string
  city?: string
  job?: string
  sign?: string
  sourceType?: number
  integration?: number
  growth?: number
  status?: number
  createTime?: string
}

export interface MemberListParams extends Api.Common.CommonSearchParams {
  username?: string
  mobile?: string
  status?: number
}

export interface MemberSavePayload {
  id: number
  nickname?: string
  status?: number
  integration?: number
  growth?: number
}

export function fetchMemberList(params: MemberListParams) {
  return request.post<Api.Common.PaginatedResponse<MemberVo>>({
    url: '/api/mall/member/list',
    data: params
  })
}

export function fetchMemberById(id: number) {
  return request.get<MemberVo>({
    url: `/api/mall/member/${id}`
  })
}

export function fetchMemberUpdate(data: MemberSavePayload) {
  return request.put<void>({
    url: '/api/mall/member',
    data,
    showSuccessMessage: true
  })
}

/** 会员状态 */
export const MEMBER_STATUS_MAP: Record<number, string> = {
  0: '禁用',
  1: '启用'
}

export const MEMBER_GENDER_MAP: Record<number, string> = {
  0: '未知',
  1: '男',
  2: '女'
}

import request from '@/utils/http'

export interface MemberLoginLogVo {
  id?: number
  memberId?: number
  memberUsername?: string
  memberNickname?: string
  createTime?: string
  ip?: string
  city?: string
  loginType?: number
  loginTypeLabel?: string
}

export interface MemberLoginLogListParams extends Api.Common.CommonSearchParams {
  memberId?: number
  loginType?: number
  ip?: string
}

export function fetchMemberLoginLogList(params: MemberLoginLogListParams) {
  return request.post<Api.Common.PageResponse<MemberLoginLogVo>>({
    url: '/api/mall/member-login-log/loginLogPageList',
    data: params
  })
}

export const MEMBER_LOGIN_TYPE_MAP: Record<number, string> = {
  1: '密码',
  2: '手机号',
  3: '微信',
  4: 'QQ',
  5: '支付宝',
  6: 'Apple',
  7: '邮箱'
}

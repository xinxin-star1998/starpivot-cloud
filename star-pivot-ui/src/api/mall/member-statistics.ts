import request from '@/utils/http'

export interface MemberStatisticsVo {
  id?: number
  memberId?: number
  username?: string
  nickname?: string
  mobile?: string
  consumeAmount?: number
  couponAmount?: number
  orderCount?: number
  couponCount?: number
  commentCount?: number
  returnOrderCount?: number
  loginCount?: number
  attendCount?: number
  fansCount?: number
  collectProductCount?: number
  collectSubjectCount?: number
  collectCommentCount?: number
  inviteFriendCount?: number
}

export interface MemberStatisticsListParams extends Api.Common.CommonSearchParams {
  memberId?: number
  username?: string
  mobile?: string
}

export function fetchMemberStatisticsList(params: MemberStatisticsListParams) {
  return request.post<Api.Common.PageResponse<MemberStatisticsVo>>({
    url: '/api/mall/member-statistics/memberStatisticsPageList',
    data: params
  })
}

export function fetchMemberStatisticsByMemberId(memberId: number) {
  return request.get<MemberStatisticsVo>({
    url: `/api/mall/member-statistics/member/${memberId}`
  })
}

export function fetchMemberStatisticsRefresh(memberId: number) {
  return request.put({
    url: `/api/mall/member-statistics/refresh/${memberId}`
  })
}

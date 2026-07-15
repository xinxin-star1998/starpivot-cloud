import request from '@/utils/http'

export interface MemberCollectSpuVo {
  id?: number
  memberId?: number
  memberUsername?: string
  spuId?: number
  spuName?: string
  spuImg?: string
  createTime?: string
}

export interface MemberCollectSubjectVo {
  id?: number
  memberId?: number
  memberUsername?: string
  subjectId?: number
  subjectName?: string
  subjectImg?: string
  subjectUrl?: string
}

export interface MemberCollectListParams extends Api.Common.CommonSearchParams {
  memberId?: number
  spuName?: string
  subjectName?: string
}

export function fetchMemberCollectSpuList(params: MemberCollectListParams) {
  return request.post<Api.Common.PageResponse<MemberCollectSpuVo>>({
    url: '/api/mall/member-collect/spuPageList',
    data: params
  })
}

export function fetchMemberCollectSubjectList(params: MemberCollectListParams) {
  return request.post<Api.Common.PageResponse<MemberCollectSubjectVo>>({
    url: '/api/mall/member-collect/subjectPageList',
    data: params
  })
}

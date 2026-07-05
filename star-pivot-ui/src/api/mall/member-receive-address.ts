import request from '@/utils/http'

export interface MemberReceiveAddressVo {
  id?: number
  memberId?: number
  memberUsername?: string
  memberNickname?: string
  name?: string
  phone?: string
  postCode?: string
  province?: string
  city?: string
  region?: string
  detailAddress?: string
  areacode?: string
  defaultStatus?: number
  defaultStatusLabel?: string
}

export interface MemberReceiveAddressListParams extends Api.Common.CommonSearchParams {
  memberId?: number
  name?: string
  phone?: string
  province?: string
  defaultStatus?: number
}

export function fetchMemberReceiveAddressList(params: MemberReceiveAddressListParams) {
  return request.post<Api.Common.PageResponse<MemberReceiveAddressVo>>({
    url: '/api/mall/member-receive-address/receiveAddressPageList',
    data: params
  })
}

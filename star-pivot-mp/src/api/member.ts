import {request} from '@/utils/request'
import type {PortalMember, PortalMemberCenter, PortalMemberProfilePayload} from './types'

export function fetchMemberCenter() {
  return request<PortalMemberCenter>({ url: '/portal/member/center' })
}

export function fetchMemberInfo() {
  return request<PortalMember>({ url: '/portal/member/info' })
}

export function updateMemberProfile(data: PortalMemberProfilePayload) {
  return request<PortalMember>({ url: '/portal/member/profile', method: 'PUT', data })
}

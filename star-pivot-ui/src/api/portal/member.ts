import request from '@/utils/http'
import type {
  PortalLoginPayload,
  PortalLoginResult,
  PortalMember,
  PortalRegisterPayload
} from './types'

export function fetchPortalRegister(data: PortalRegisterPayload) {
  return request.post<void>({
    url: '/api/portal/member/register',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalLogin(data: PortalLoginPayload) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/member/login',
    data
  })
}

export function fetchPortalMemberInfo() {
  return request.get<PortalMember>({
    url: '/api/portal/member/info'
  })
}

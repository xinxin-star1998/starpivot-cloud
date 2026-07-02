import {request} from '@/utils/request'
import type {PortalAuthConfig, PortalLoginResult} from './types'

export function fetchAuthConfig() {
  return request<PortalAuthConfig>({ url: '/portal/auth/config', auth: false })
}

export function fetchMiniProgramLogin(code: string) {
  return request<PortalLoginResult>({
    url: '/portal/auth/wechat/mini/login',
    method: 'POST',
    data: { code },
    auth: false
  })
}

export function fetchSmsLogin(mobile: string, code: string) {
  return request<PortalLoginResult>({
    url: '/portal/auth/sms/login',
    method: 'POST',
    data: { mobile, code },
    auth: false
  })
}

export function fetchSmsSend(mobile: string, scene: 'login' | 'register' | 'bind' | 'set_password' | 'unbind' = 'login') {
  return request<{ expireSeconds: number }>({
    url: '/portal/auth/sms/send',
    method: 'POST',
    data: { mobile, scene },
    auth: scene === 'login' || scene === 'register' ? false : true
  })
}

export function fetchAuthBindings() {
  return request<import('./types').PortalMemberAuthBinding[]>({
    url: '/portal/member/auth/bindings'
  })
}

export function bindMobile(mobile: string, code: string) {
  return request<void>({
    url: '/portal/member/auth/bind/mobile',
    method: 'POST',
    data: { mobile, code }
  })
}

export function setPassword(password: string, code: string) {
  return request<void>({
    url: '/portal/member/auth/set-password',
    method: 'POST',
    data: { password, code }
  })
}

export function unbindMobile(code: string) {
  return request<void>({
    url: '/portal/member/auth/unbind/2',
    method: 'DELETE',
    data: { code }
  })
}

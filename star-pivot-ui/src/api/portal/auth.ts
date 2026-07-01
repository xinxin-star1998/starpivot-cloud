import request from '@/utils/http'
import type {
    PortalAuthConfig,
    PortalLoginResult,
    PortalMemberAuthBinding,
    PortalSmsLoginPayload,
    PortalSmsSendPayload
} from './types'

export function fetchPortalAuthConfig() {
  return request.get<PortalAuthConfig>({
    url: '/api/portal/auth/config'
  })
}

export function fetchPortalSmsSend(data: PortalSmsSendPayload) {
  return request.post<{ expireSeconds: number }>({
    url: '/api/portal/auth/sms/send',
    data
  })
}

export function fetchPortalSmsLogin(data: PortalSmsLoginPayload) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/auth/sms/login',
    data
  })
}

export function fetchPortalSmsRegister(data: PortalSmsLoginPayload) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/auth/sms/register',
    data
  })
}

export function fetchPortalPasswordLogin(data: { account: string; password: string }) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/auth/login/password',
    data
  })
}

export function fetchPortalAuthBindings() {
  return request.get<PortalMemberAuthBinding[]>({
    url: '/api/portal/member/auth/bindings'
  })
}

export function fetchPortalBindMobile(data: { mobile: string; code: string }) {
  return request.post<void>({
    url: '/api/portal/member/auth/bind/mobile',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalSetPassword(data: { password: string; code: string }) {
  return request.post<void>({
    url: '/api/portal/member/auth/set-password',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalUnbindMobile(code: string) {
  return request.del<void>({
    url: '/api/portal/member/auth/unbind/2',
    data: { code },
    showSuccessMessage: true
  })
}

const WECHAT_MODE_KEY = 'portal_wechat_mode'
const WECHAT_REDIRECT_KEY = 'portal_wechat_redirect'

/** 微信 OAuth 回调页（需与后端 redirect-uri 路径一致） */
export function getPortalWechatCallbackUri() {
  return `${window.location.origin}/portal/login/wechat/callback`
}

export function fetchPortalWechatAuthorize(params: {
  redirect?: string
  mode?: 'login' | 'register' | 'bind'
  callbackUri?: string
}) {
  const callbackUri = params.callbackUri || getPortalWechatCallbackUri()
  const url =
    params.mode === 'bind'
      ? '/api/portal/member/auth/bind/wechat/authorize'
      : '/api/portal/auth/wechat/authorize'
  return request.get<{ authorizeUrl: string; state: string }>({
    url,
    params: {
      redirect: params.redirect,
      mode: params.mode === 'bind' ? undefined : params.mode,
      callbackUri
    }
  })
}

export function fetchPortalWechatLogin(data: { code: string; state: string }) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/auth/wechat/login',
    data
  })
}

export function fetchPortalWechatRegister(data: { code: string; state: string }) {
  return request.post<PortalLoginResult>({
    url: '/api/portal/auth/wechat/register',
    data
  })
}

export function fetchPortalBindWechat(data: { code: string; state: string }) {
  return request.post<void>({
    url: '/api/portal/member/auth/bind/wechat',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalUnbindWechat() {
  return request.del<void>({
    url: '/api/portal/member/auth/unbind/3',
    showSuccessMessage: true
  })
}

/** 跳转微信授权（Mock 模式下会直接回到回调页） */
export async function startPortalWechatOAuth(options: {
  mode: 'login' | 'register' | 'bind'
  redirect?: string
}) {
  const redirect = options.redirect || (options.mode === 'bind' ? '/portal/account/security' : '/portal')
  sessionStorage.setItem(WECHAT_MODE_KEY, options.mode)
  sessionStorage.setItem(WECHAT_REDIRECT_KEY, redirect)
  const { authorizeUrl } = await fetchPortalWechatAuthorize({ redirect, mode: options.mode })
  window.location.href = authorizeUrl
}

export function consumePortalWechatOAuthContext() {
  const mode = (sessionStorage.getItem(WECHAT_MODE_KEY) || 'login') as 'login' | 'register' | 'bind'
  const redirect = sessionStorage.getItem(WECHAT_REDIRECT_KEY) || '/portal'
  sessionStorage.removeItem(WECHAT_MODE_KEY)
  sessionStorage.removeItem(WECHAT_REDIRECT_KEY)
  return { mode, redirect }
}

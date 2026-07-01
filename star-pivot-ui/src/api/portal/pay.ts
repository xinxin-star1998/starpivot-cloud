import request from '@/utils/http'

export interface PortalAlipayPagePayResult {
  orderSn: string
  payForm: string
}

/** 是否已配置并启用支付宝 */
export function fetchPortalAlipayEnabled() {
  return request.get<boolean>({
    url: '/api/portal/pay/alipay/enabled'
  })
}

/** 发起支付宝电脑网站支付，返回自动提交表单 HTML */
export function fetchPortalAlipayPay(orderId: number) {
  return request.post<PortalAlipayPagePayResult>({
    url: `/api/portal/pay/alipay/order/${orderId}`
  })
}

export interface PortalWxPayStatus {
  enabled: boolean
  mock: boolean
}

export interface PortalWxNativePayResult {
  orderSn: string
  codeUrl: string
  mock?: boolean
}

/** 是否已配置并启用微信支付 */
export function fetchPortalWxPayEnabled() {
  return request.get<PortalWxPayStatus>({
    url: '/api/portal/pay/wx/enabled'
  })
}

/** 发起微信 Native 支付，返回 code_url */
export function fetchPortalWxNativePay(orderId: number) {
  return request.post<PortalWxNativePayResult>({
    url: `/api/portal/pay/wx/order/${orderId}`
  })
}

/** Mock 微信确认支付 */
export function fetchPortalWxMockPay(orderId: number) {
  return request.post<void>({
    url: `/api/portal/pay/wx/mock/${orderId}`,
    showSuccessMessage: true
  })
}

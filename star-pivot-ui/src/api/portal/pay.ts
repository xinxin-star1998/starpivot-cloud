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

import {request} from '@/utils/request'
import type {WxJsapiPayParams} from './types'

export function fetchWxPayEnabled() {
  return request<{ enabled: boolean; mock: boolean; jsapi: boolean }>({
    url: '/portal/pay/wx/enabled',
    auth: false
  })
}

export function fetchWxJsapiPay(orderId: number) {
  return request<WxJsapiPayParams>({
    url: `/portal/pay/wx/jsapi/${orderId}`,
    method: 'POST'
  })
}

export function mockWxPay(orderId: number) {
  return request<void>({ url: `/portal/pay/wx/mock/${orderId}`, method: 'POST' })
}

import {request} from '@/utils/request'
import type {PortalCheckoutCoupon, PortalClaimableCoupon, PortalMyCoupon} from './types'

export function fetchClaimableCoupons() {
  return request<PortalClaimableCoupon[]>({ url: '/portal/coupon/claimable' })
}

export function fetchMyCoupons(status?: number) {
  return request<PortalMyCoupon[]>({
    url: '/portal/coupon/mine',
    auth: true,
    method: 'GET',
    data: status != null ? { status } : undefined
  })
}

export function receiveCoupon(couponId: number) {
  return request<number>({ url: `/portal/coupon/receive/${couponId}`, method: 'POST' })
}

export function fetchCheckoutCoupons(data: {
  useCart?: boolean
  items?: Array<{ skuId: number; quantity: number }>
}) {
  return request<PortalCheckoutCoupon[]>({
    url: '/portal/coupon/checkout',
    method: 'POST',
    data
  })
}

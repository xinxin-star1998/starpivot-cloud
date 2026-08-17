import request from '@/utils/http'
import type {
  PortalCheckoutCoupon,
  PortalClaimableCoupon,
  PortalMemberCoupon,
  PortalMyCoupon
} from './types'

export interface PortalCouponTrialPayload {
  useCart?: boolean
  items?: Array<{ skuId: number; quantity: number }>
  cartSkuIds?: number[]
}

export function fetchPortalCouponUsable(data: PortalCouponTrialPayload) {
  return request.post<PortalMemberCoupon[]>({
    url: '/api/portal/coupon/usable',
    data
  })
}

export function fetchPortalCouponCheckout(data: PortalCouponTrialPayload) {
  return request.post<PortalCheckoutCoupon[]>({
    url: '/api/portal/coupon/checkout',
    data
  })
}

export function fetchPortalCouponClaimable() {
  return request.get<PortalClaimableCoupon[]>({
    url: '/api/portal/coupon/claimable'
  })
}

export function fetchPortalCouponMine(status?: number) {
  return request.get<PortalMyCoupon[]>({
    url: '/api/portal/coupon/mine',
    params: status != null ? { status } : undefined
  })
}

export function fetchPortalCouponReceive(couponId: number) {
  return request.post<number>({
    url: `/api/portal/coupon/receive/${couponId}`,
    showSuccessMessage: true
  })
}

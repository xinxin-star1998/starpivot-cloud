import request from '@/utils/http'
import type {PortalCart, PortalCartAddPayload, PortalCartUpdatePayload} from './types'

export function fetchPortalCart() {
  return request.get<PortalCart>({
    url: '/api/portal/cart'
  })
}

export function fetchPortalCartAdd(data: PortalCartAddPayload) {
  return request.post<void>({
    url: '/api/portal/cart',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalCartUpdate(data: PortalCartUpdatePayload) {
  return request.put<void>({
    url: '/api/portal/cart',
    data
  })
}

export function fetchPortalCartRemove(skuIds: number[]) {
  return request.del<void>({
    url: '/api/portal/cart/removeCart',
    data: { ids: skuIds },
    showSuccessMessage: true
  })
}

import {request} from '@/utils/request'
import type {PortalCartData} from './types'

export function fetchCart() {
  return request<PortalCartData>({ url: '/portal/cart' })
}

export function addToCart(skuId: number, quantity = 1) {
  return request<void>({ url: '/portal/cart', method: 'POST', data: { skuId, quantity } })
}

export function updateCartItem(skuId: number, quantity: number, checked?: boolean) {
  return request<void>({
    url: '/portal/cart',
    method: 'PUT',
    data: { skuId, quantity, checked }
  })
}

export function removeFromCart(skuIds: number[]) {
  return request<void>({
    url: '/portal/cart/removeCart',
    method: 'DELETE',
    data: { ids: skuIds }
  })
}

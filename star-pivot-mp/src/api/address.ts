import {request} from '@/utils/request'
import type {PortalAddress, PortalAddressSavePayload} from './types'

export function fetchAddressList() {
  return request<PortalAddress[]>({ url: '/portal/address' })
}

export function fetchAddressSave(data: PortalAddressSavePayload) {
  return request<void>({ url: '/portal/address', method: 'POST', data })
}

export function fetchAddressUpdate(data: PortalAddressSavePayload) {
  return request<void>({ url: '/portal/address', method: 'PUT', data })
}

export function fetchAddressRemove(id: number) {
  return request<void>({ url: `/portal/address/${id}`, method: 'DELETE' })
}

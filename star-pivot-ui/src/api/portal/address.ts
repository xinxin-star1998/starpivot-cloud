import request from '@/utils/http'
import type {PortalAddress, PortalAddressSavePayload} from './types'

export function fetchPortalAddressList() {
  return request.get<PortalAddress[]>({
    url: '/api/portal/address'
  })
}

export function fetchPortalAddressById(id: number) {
  return request.get<PortalAddress>({
    url: `/api/portal/address/${id}`
  })
}

export function fetchPortalAddressSave(data: PortalAddressSavePayload) {
  return request.post<void>({
    url: '/api/portal/address',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalAddressUpdate(data: PortalAddressSavePayload) {
  return request.put<void>({
    url: '/api/portal/address',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalAddressRemove(id: number) {
  return request.del<void>({
    url: `/api/portal/address/${id}`,
    showSuccessMessage: true
  })
}

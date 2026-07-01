import request from '@/utils/http'

/** 省市区地址 VO */
export interface Address {
  id?: number
  code?: string
  parentCode?: string
  name?: string
  level?: number
  createTime?: string
  updateTime?: string
  hasChildren?: boolean
  children?: Address[]
}

export interface AddressListParams {
  code?: string
  parentCode?: string
  name?: string
  level?: number
}

export interface AddressSavePayload {
  id?: number
  code: string
  parentCode?: string
  name: string
  level: number
}

/** 懒加载：查询直接子级（parentCode 默认 0 为省） */
export function fetchGetAddressChildren(parentCode = '0') {
  return request.get<Address[]>({
    url: '/api/mall/address/children',
    params: { parentCode }
  })
}

/** 搜索（扁平，最多 200 条） */
export function fetchSearchAddress(params: AddressListParams) {
  return request.get<Address[]>({
    url: '/api/mall/address/search',
    params
  })
}

export function fetchGetAddressById(id: number) {
  return request.get<Address>({
    url: `/api/mall/address/${id}`
  })
}

export function fetchAddAddress(data: AddressSavePayload) {
  return request.post<void>({
    url: '/api/mall/address',
    data,
    showSuccessMessage: true
  })
}

export function fetchUpdateAddress(data: AddressSavePayload) {
  return request.put<void>({
    url: '/api/mall/address',
    data,
    showSuccessMessage: true
  })
}

export function fetchDeleteAddress(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/address/removeAddress',
    data: { ids },
    showSuccessMessage: true
  })
}

import {request} from '@/utils/request'
import type {PageResult, PortalProductDetail, PortalProductListItem, PortalProductSearchParams} from './types'

export function fetchProductDetail(id: number) {
  return request<PortalProductDetail>({ url: `/portal/product/${id}`, auth: false })
}

export function searchProducts(params: PortalProductSearchParams) {
  return request<PageResult<PortalProductListItem>>({
    url: '/portal/product/search',
    method: 'POST',
    data: params,
    auth: false
  })
}

export function productImageRaw(item: { coverImg?: string; pic?: string; spuImg?: string; skuDefaultImg?: string }) {
  return item.coverImg || item.pic || item.spuImg || item.skuDefaultImg || ''
}

export function productCover(item: { coverImg?: string; pic?: string; spuImg?: string; skuDefaultImg?: string }) {
  return productImageRaw(item)
}

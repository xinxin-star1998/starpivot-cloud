import request from '@/utils/http'
import type {PortalProductDetail, PortalProductListItem, PortalProductSearchParams} from './types'

export function fetchPortalProductSearch(params: PortalProductSearchParams) {
  return request.post<Api.Common.PageResponse<PortalProductListItem>>({
    url: '/api/portal/product/search',
    data: params
  })
}

export function fetchPortalProductDetail(id: number) {
  return request.get<PortalProductDetail>({
    url: `/api/portal/product/${id}`
  })
}

export function fetchPortalProductRelated(id: number, limit = 8) {
  return request.get<PortalProductListItem[]>({
    url: `/api/portal/product/${id}/related`,
    params: { limit }
  })
}

import request from '@/utils/http'
import type { PortalProductDetail, PortalProductListItem, PortalProductSearchParams } from './types'

export function fetchPortalProductSearch(params: PortalProductSearchParams) {
  return request.post<Api.Common.PaginatedResponse<PortalProductListItem>>({
    url: '/api/portal/product/search',
    data: params
  })
}

export function fetchPortalProductDetail(id: number) {
  return request.get<PortalProductDetail>({
    url: `/api/portal/product/${id}`
  })
}

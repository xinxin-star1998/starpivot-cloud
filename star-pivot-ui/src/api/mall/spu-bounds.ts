import request from '@/utils/http'

export interface SpuBoundsVo {
  id?: number
  spuId?: number
  spuName?: string
  growBounds?: number
  buyBounds?: number
  work?: number
}

export interface SpuBoundsListParams extends Api.Common.CommonSearchParams {
  spuId?: number
}

export interface SpuBoundsSavePayload {
  id?: number
  spuId: number
  growBounds?: number
  buyBounds?: number
  work?: number
}

export function fetchSpuBoundsList(params: SpuBoundsListParams) {
  return request.post<Api.Common.PaginatedResponse<SpuBoundsVo>>({
    url: '/api/mall/spu-bounds/spuBoundsPageList',
    data: params
  })
}

export function fetchSpuBoundsById(id: number) {
  return request.get<SpuBoundsVo>({
    url: `/api/mall/spu-bounds/${id}`
  })
}

export function fetchSpuBoundsAdd(data: SpuBoundsSavePayload) {
  return request.post<void>({
    url: '/api/mall/spu-bounds',
    data,
    showSuccessMessage: true
  })
}

export function fetchSpuBoundsUpdate(data: SpuBoundsSavePayload) {
  return request.put<void>({
    url: '/api/mall/spu-bounds',
    data,
    showSuccessMessage: true
  })
}

export function fetchSpuBoundsRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/spu-bounds/removeSpuBounds',
    data: { ids },
    showSuccessMessage: true
  })
}

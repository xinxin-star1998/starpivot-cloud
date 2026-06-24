import request from '@/utils/http'

export interface SkuFullReductionVo {
  id?: number
  skuId?: number
  skuName?: string
  fullPrice?: number
  reducePrice?: number
  addOther?: number
}

export interface SkuFullReductionListParams extends Api.Common.CommonSearchParams {
  skuId?: number
}

export interface SkuFullReductionSavePayload {
  id?: number
  skuId: number
  fullPrice?: number
  reducePrice?: number
  addOther?: number
}

export function fetchSkuFullReductionList(params: SkuFullReductionListParams) {
  return request.post<Api.Common.PaginatedResponse<SkuFullReductionVo>>({
    url: '/api/mall/sku-full-reduction/list',
    data: params
  })
}

export function fetchSkuFullReductionById(id: number) {
  return request.get<SkuFullReductionVo>({
    url: `/api/mall/sku-full-reduction/${id}`
  })
}

export function fetchSkuFullReductionAdd(data: SkuFullReductionSavePayload) {
  return request.post<void>({
    url: '/api/mall/sku-full-reduction',
    data,
    showSuccessMessage: true
  })
}

export function fetchSkuFullReductionUpdate(data: SkuFullReductionSavePayload) {
  return request.put<void>({
    url: '/api/mall/sku-full-reduction',
    data,
    showSuccessMessage: true
  })
}

export function fetchSkuFullReductionRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/sku-full-reduction/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

export interface SkuLadderVo {
  id?: number
  skuId?: number
  skuName?: string
  fullCount?: number
  discount?: number
  price?: number
  addOther?: number
}

export interface SkuLadderListParams extends Api.Common.CommonSearchParams {
  skuId?: number
}

export interface SkuLadderSavePayload {
  id?: number
  skuId: number
  fullCount?: number
  discount?: number
  price?: number
  addOther?: number
}

export function fetchSkuLadderList(params: SkuLadderListParams) {
  return request.post<Api.Common.PaginatedResponse<SkuLadderVo>>({
    url: '/api/mall/sku-ladder/list',
    data: params
  })
}

export function fetchSkuLadderById(id: number) {
  return request.get<SkuLadderVo>({
    url: `/api/mall/sku-ladder/${id}`
  })
}

export function fetchSkuLadderAdd(data: SkuLadderSavePayload) {
  return request.post<void>({
    url: '/api/mall/sku-ladder',
    data,
    showSuccessMessage: true
  })
}

export function fetchSkuLadderUpdate(data: SkuLadderSavePayload) {
  return request.put<void>({
    url: '/api/mall/sku-ladder',
    data,
    showSuccessMessage: true
  })
}

export function fetchSkuLadderRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/sku-ladder/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

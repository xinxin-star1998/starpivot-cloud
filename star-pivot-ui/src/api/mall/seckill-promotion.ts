import request from '@/utils/http'

export interface SeckillSkuRelationVo {
  id?: number
  promotionId?: number
  promotionSessionId?: number
  sessionName?: string
  skuId?: number
  skuName?: string
  seckillPrice?: number
  seckillCount?: number
  seckillLimit?: number
  seckillSort?: number
}

export interface SeckillPromotionVo {
  id?: number
  title?: string
  startTime?: string
  endTime?: string
  status?: number
  createTime?: string
  userId?: number
  skuList?: SeckillSkuRelationVo[]
}

export interface SeckillPromotionListParams extends Api.Common.CommonSearchParams {
  title?: string
  status?: number
}

export interface SeckillPromotionSavePayload {
  id?: number
  title: string
  startTime?: string
  endTime?: string
  status?: number
  skuList?: {
    promotionSessionId?: number
    skuId?: number
    seckillPrice?: number
    seckillCount?: number
    seckillLimit?: number
    seckillSort?: number
  }[]
}

export function fetchSeckillPromotionList(params: SeckillPromotionListParams) {
  return request.post<Api.Common.PaginatedResponse<SeckillPromotionVo>>({
    url: '/api/mall/seckill-promotion/seckillPromotionPageList',
    data: params
  })
}

export function fetchSeckillPromotionById(id: number) {
  return request.get<SeckillPromotionVo>({
    url: `/api/mall/seckill-promotion/${id}`
  })
}

export function fetchSeckillPromotionAdd(data: SeckillPromotionSavePayload) {
  return request.post<void>({
    url: '/api/mall/seckill-promotion',
    data,
    showSuccessMessage: true
  })
}

export function fetchSeckillPromotionUpdate(data: SeckillPromotionSavePayload) {
  return request.put<void>({
    url: '/api/mall/seckill-promotion',
    data,
    showSuccessMessage: true
  })
}

export function fetchSeckillPromotionRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/seckill-promotion/removeSeckillPromotion',
    data: { ids },
    showSuccessMessage: true
  })
}

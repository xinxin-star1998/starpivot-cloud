import request from '@/utils/http'

export interface MallSkuVo {
  skuId?: number
  spuId?: number
  spuName?: string
  skuName?: string
  skuDesc?: string
  catalogId?: number
  brandId?: number
  skuDefaultImg?: string
  skuTitle?: string
  skuSubtitle?: string
  price?: number | string
  saleCount?: number
  saleAttrText?: string
  /** 所属 SPU 上架状态：0-下架 1-上架 */
  spuPublishStatus?: number
}

export interface MallSkuListParams extends Api.Common.CommonSearchParams {
  skuName?: string
  spuName?: string
  spuId?: number
  catalogId?: number
  brandId?: number
  minPrice?: number
  maxPrice?: number
  spuPublishStatus?: number
}

export interface MallSkuCreatePayload {
  spuId: number
  skuName: string
  price: number
  skuTitle?: string
  skuSubtitle?: string
  skuDefaultImg?: string
}

export interface MallSkuSavePayload {
  skuId: number
  price: number
  skuTitle?: string
  skuSubtitle?: string
  skuDefaultImg?: string
}

/** SKU 分页列表 */
export function fetchMallSkuList(params: MallSkuListParams) {
  return request.post<Api.Common.PageResponse<MallSkuVo>>({
    url: '/api/mall/sku/skuPageList',
    data: params
  })
}

export function fetchMallSkuById(skuId: number) {
  return request.get<MallSkuVo>({
    url: `/api/mall/sku/${skuId}`
  })
}

/** 新增 SKU（挂载已有 SPU） */
export function fetchMallSkuCreate(data: MallSkuCreatePayload) {
  return request.post<number>({
    url: '/api/mall/sku',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallSkuUpdate(data: MallSkuSavePayload) {
  return request.put<void>({
    url: '/api/mall/sku',
    data,
    showSuccessMessage: true
  })
}

/** SKU 改价 */
export function fetchMallSkuUpdatePrice(skuId: number, price: number) {
  return request.put<void>({
    url: '/api/mall/sku/price',
    data: { skuId, price },
    showSuccessMessage: true
  })
}

/** SKU 上下架（更新所属 SPU publishStatus） */
export function fetchMallSkuPublishStatus(skuId: number, publishStatus: 0 | 1) {
  return request.put<void>({
    url: '/api/mall/sku/publish-status',
    data: { skuId, publishStatus },
    showSuccessMessage: true
  })
}

export function fetchMallSkuRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/sku/removeSku',
    data: { ids },
    showSuccessMessage: true
  })
}

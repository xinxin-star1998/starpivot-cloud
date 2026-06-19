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
}

export interface MallSkuListParams extends Api.Common.CommonSearchParams {
  skuName?: string
  spuName?: string
  spuId?: number
  catalogId?: number
  brandId?: number
}

/** SKU 只读分页列表（数据由 SPU 向导生成） */
export function fetchMallSkuList(params: MallSkuListParams) {
  return request.post<Api.Common.PaginatedResponse<MallSkuVo>>({
    url: '/api/mall/sku/list',
    data: params
  })
}

import request from '@utils/http'

/**
 * 商品库存实体类型
 */
export interface WareSku {
  id?: number
  skuId?: number
  wareId?: number
  stock?: number
  skuName?: string
  stockLocked?: number
}

/**
 * 商品库存搜索参数
 */
export interface SkuSearchParams {
  pageNum?: number
  pageSize?: number
  skuId?: number
  wareId?: number
  stock?: number
  skuName?: string
  stockLocked?: number
}

/**
 * 获取商品库存列表（分页）
 */
export function fetchGetSkuList(params: SkuSearchParams) {
  return request.post<Api.Common.PaginatedResponse<WareSku>>({
    url: '/api/mall/ware-sku/list',
    data: params
  })
}

/**
 * 根据ID获取商品库存详情
 */
export function fetchGetSkuById(id: number) {
  return request.get<WareSku>({
    url: `/api/mall/ware-sku/${id}`
  })
}

/**
 * 新增商品库存
 */
export function fetchAddSku(data: WareSku) {
  return request.post({
    url: '/api/mall/ware-sku',
    data
  })
}

/**
 * 修改商品库存
 */
export function fetchUpdateSku(data: WareSku) {
  return request.put({
    url: '/api/mall/ware-sku',
    data
  })
}

/**
 * 删除商品库存（支持单删和批量删除，请求体 ids）
 */
export function fetchDeleteSku(ids: number[]) {
  return request.del({
    url: '/api/mall/ware-sku/delete',
    params: { ids: ids }
  })
}

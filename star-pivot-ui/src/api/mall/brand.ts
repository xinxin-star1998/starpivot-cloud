import request from '@/utils/http'

/** 品牌 VO（pms_brand） */
export interface MallBrandVo {
  brandId?: number
  name?: string
  logo?: string
  descript?: string
  sort?: number
  showStatus?: number /** 0-不显示；1-显示 */
  firstLetter?: string
}

export interface MallBrandListParams extends Api.Common.CommonSearchParams {
  name?: string
  showStatus?: number /** 0-不显示；1-显示 */
  firstLetter?: string
}

export interface MallBrandSavePayload {
  brandId?: number
  name: string
  logo?: string
  descript?: string
  sort?: number
  showStatus: number
  firstLetter?: string
}

export function fetchMallBrandList(params: MallBrandListParams) {
  return request.post<Api.Common.PaginatedResponse<MallBrandVo>>({
    url: '/api/mall/brand/list',
    data: params
  })
}

export function fetchMallBrandById(id: number) {
  return request.get<MallBrandVo>({
    url: `/api/mall/brand/${id}`
  })
}

export function fetchMallBrandAdd(data: MallBrandSavePayload) {
  return request.post<void>({
    url: '/api/mall/brand',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallBrandUpdate(data: MallBrandSavePayload) {
  return request.put<void>({
    url: '/api/mall/brand',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallBrandRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/brand/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

/** 品牌已关联的三级分类 */
export interface MallBrandCategoryRelation {
  id?: number
  catelogId?: number
  catelogName?: string
}

export function fetchMallBrandBoundCategories(brandId: number) {
  return request.get<MallBrandCategoryRelation[]>({
    url: `/api/mall/brand/${brandId}/categories`
  })
}

/** 品牌绑定三级分类（全量覆盖） */
export function fetchMallBrandBindCategories(brandId: number, catIds: number[]) {
  return request.put<void>({
    url: '/api/mall/brand/categories',
    data: { brandId, catIds },
    showSuccessMessage: true
  })
}

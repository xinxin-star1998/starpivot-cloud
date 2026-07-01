import request from '@/utils/http'

export interface HomeCategoryHotVo {
  id?: number
  catId?: number
  catName?: string
  title?: string
  icon?: string
  url?: string
  status?: number
  sort?: number
  note?: string
}

export interface HomeCategoryHotListParams extends Api.Common.CommonSearchParams {
  title?: string
  status?: number
  catId?: number
}

export interface HomeCategoryHotSavePayload {
  id?: number
  catId?: number
  title?: string
  icon?: string
  url?: string
  status?: number
  sort?: number
  note?: string
}

export function fetchCategoryHotList(params: HomeCategoryHotListParams) {
  return request.post<Api.Common.PaginatedResponse<HomeCategoryHotVo>>({
    url: '/api/mall/category-hot/categoryHotPageList',
    data: params
  })
}

export function fetchCategoryHotById(id: number) {
  return request.get<HomeCategoryHotVo>({
    url: `/api/mall/category-hot/${id}`
  })
}

export function fetchCategoryHotAdd(data: HomeCategoryHotSavePayload) {
  return request.post<void>({
    url: '/api/mall/category-hot',
    data,
    showSuccessMessage: true
  })
}

export function fetchCategoryHotUpdate(data: HomeCategoryHotSavePayload) {
  return request.put<void>({
    url: '/api/mall/category-hot',
    data,
    showSuccessMessage: true
  })
}

export function fetchCategoryHotRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/category-hot/removeCategoryHot',
    data: { ids },
    showSuccessMessage: true
  })
}

export const CATEGORY_HOT_STATUS_MAP: Record<number, string> = {
  0: '下架',
  1: '上架'
}

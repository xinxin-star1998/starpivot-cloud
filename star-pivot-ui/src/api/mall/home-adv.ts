import request from '@/utils/http'

export interface HomeAdvVo {
  id?: number
  name?: string
  pic?: string
  startTime?: string
  endTime?: string
  status?: number
  clickCount?: number
  url?: string
  note?: string
  sort?: number
  publisherId?: number
  authId?: number
}

export interface HomeAdvListParams extends Api.Common.CommonSearchParams {
  name?: string
  status?: number
}

export interface HomeAdvSavePayload {
  id?: number
  name: string
  pic?: string
  startTime?: string
  endTime?: string
  status?: number
  clickCount?: number
  url?: string
  note?: string
  sort?: number
  publisherId?: number
  authId?: number
}

export function fetchHomeAdvList(params: HomeAdvListParams) {
  return request.post<Api.Common.PaginatedResponse<HomeAdvVo>>({
    url: '/api/mall/home-adv/homeAdvPageList',
    data: params
  })
}

export function fetchHomeAdvById(id: number) {
  return request.get<HomeAdvVo>({
    url: `/api/mall/home-adv/${id}`
  })
}

export function fetchHomeAdvAdd(data: HomeAdvSavePayload) {
  return request.post<void>({
    url: '/api/mall/home-adv',
    data,
    showSuccessMessage: true
  })
}

export function fetchHomeAdvUpdate(data: HomeAdvSavePayload) {
  return request.put<void>({
    url: '/api/mall/home-adv',
    data,
    showSuccessMessage: true
  })
}

export function fetchHomeAdvRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/home-adv/removeHomeAdv',
    data: { ids },
    showSuccessMessage: true
  })
}

export const HOME_ADV_STATUS_MAP: Record<number, string> = {
  0: '下架',
  1: '上架'
}

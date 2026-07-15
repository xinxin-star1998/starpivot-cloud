import request from '@/utils/http'

export interface HomeSubjectSpuVo {
  id?: number
  subjectId?: number
  spuId?: number
  name?: string
  spuName?: string
  sort?: number
}

export interface HomeSubjectVo {
  id?: number
  name?: string
  title?: string
  subTitle?: string
  status?: number
  url?: string
  sort?: number
  img?: string
  spuList?: HomeSubjectSpuVo[]
}

export interface HomeSubjectListParams extends Api.Common.CommonSearchParams {
  name?: string
  status?: number
}

export interface HomeSubjectSavePayload {
  id?: number
  name: string
  title?: string
  subTitle?: string
  status?: number
  url?: string
  sort?: number
  img?: string
  spuList?: { spuId?: number; name?: string; sort?: number }[]
}

export function fetchHomeSubjectList(params: HomeSubjectListParams) {
  return request.post<Api.Common.PageResponse<HomeSubjectVo>>({
    url: '/api/mall/subject/subjectPageList',
    data: params
  })
}

export function fetchHomeSubjectById(id: number) {
  return request.get<HomeSubjectVo>({
    url: `/api/mall/subject/${id}`
  })
}

export function fetchHomeSubjectAdd(data: HomeSubjectSavePayload) {
  return request.post<void>({
    url: '/api/mall/subject',
    data,
    showSuccessMessage: true
  })
}

export function fetchHomeSubjectUpdate(data: HomeSubjectSavePayload) {
  return request.put<void>({
    url: '/api/mall/subject',
    data,
    showSuccessMessage: true
  })
}

export function fetchHomeSubjectRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/subject/removeSubject',
    data: { ids },
    showSuccessMessage: true
  })
}

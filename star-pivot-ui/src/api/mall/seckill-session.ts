import request from '@/utils/http'

export interface SeckillSessionVo {
  id?: number
  name?: string
  startTime?: string
  endTime?: string
  status?: number
  createTime?: string
}

export interface SeckillSessionListParams extends Api.Common.CommonSearchParams {
  name?: string
  status?: number
}

export interface SeckillSessionSavePayload {
  id?: number
  name: string
  startTime?: string
  endTime?: string
  status?: number
}

export function fetchSeckillSessionList(params: SeckillSessionListParams) {
  return request.post<Api.Common.PageResponse<SeckillSessionVo>>({
    url: '/api/mall/seckill-session/seckillSessionPageList',
    data: params
  })
}

export function fetchSeckillSessionAll() {
  return request.get<SeckillSessionVo[]>({
    url: '/api/mall/seckill-session/all'
  })
}

export function fetchSeckillSessionById(id: number) {
  return request.get<SeckillSessionVo>({
    url: `/api/mall/seckill-session/${id}`
  })
}

export function fetchSeckillSessionAdd(data: SeckillSessionSavePayload) {
  return request.post<void>({
    url: '/api/mall/seckill-session',
    data,
    showSuccessMessage: true
  })
}

export function fetchSeckillSessionUpdate(data: SeckillSessionSavePayload) {
  return request.put<void>({
    url: '/api/mall/seckill-session',
    data,
    showSuccessMessage: true
  })
}

export function fetchSeckillSessionRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/seckill-session/removeSeckillSession',
    data: { ids },
    showSuccessMessage: true
  })
}

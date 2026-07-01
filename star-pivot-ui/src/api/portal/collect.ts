import request from '@/utils/http'
import type {PortalCollectItem} from './types'

export function fetchPortalCollectPageList(params: Api.Common.CommonSearchParams) {
  return request.post<Api.Common.PaginatedResponse<PortalCollectItem>>({
    url: '/api/portal/collect/collectPageList',
    data: params
  })
}

export function fetchPortalCollectAdd(spuId: number, options?: { silent?: boolean }) {
  return request.post<void>({
    url: '/api/portal/collect',
    data: { spuId },
    showSuccessMessage: options?.silent ? false : true
  })
}

export function fetchPortalCollectRemove(spuId: number) {
  return request.del<void>({
    url: `/api/portal/collect/${spuId}`,
    showSuccessMessage: true
  })
}

export function fetchPortalCollectStatus(spuId: number) {
  return request.get<{ collected: boolean }>({
    url: `/api/portal/collect/status/${spuId}`
  })
}

export function fetchPortalCollectSubjectAdd(subjectId: number, options?: { silent?: boolean }) {
  return request.post<void>({
    url: '/api/portal/collect/subject',
    data: { subjectId },
    showSuccessMessage: options?.silent ? false : true
  })
}

export function fetchPortalCollectSubjectRemove(subjectId: number) {
  return request.del<void>({
    url: `/api/portal/collect/subject/${subjectId}`,
    showSuccessMessage: true
  })
}

export function fetchPortalCollectSubjectStatus(subjectId: number) {
  return request.get<{ collected: boolean }>({
    url: `/api/portal/collect/subject/status/${subjectId}`
  })
}

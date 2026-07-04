import request from '@/utils/http'
import type {PortalSubjectDetail} from './types'

export function fetchPortalSubjectDetail(
  id: number,
  params?: { pageNum?: number; pageSize?: number }
) {
  return request.get<PortalSubjectDetail>({
    url: `/api/portal/subject/${id}`,
    params
  })
}

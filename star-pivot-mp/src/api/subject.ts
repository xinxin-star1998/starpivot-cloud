import {request} from '@/utils/request'
import type {PortalSubjectDetail} from './types'

export function fetchSubjectDetail(id: number, pageNum = 1, pageSize = 16) {
  return request<PortalSubjectDetail>({
    url: `/portal/subject/${id}`,
    auth: false,
    method: 'GET',
    data: { pageNum, pageSize }
  })
}

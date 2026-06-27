import request from '@/utils/http'
import type { ApTaskActionDto, ApTaskVo } from './types'

export interface ApTaskQuery {
  title?: string
  bizModule?: string
  bizType?: string
  pageNum?: number
  pageSize?: number
}

export function fetchApprovalTodoList(data: ApTaskQuery) {
  return request.post<{ total: number; rows: ApTaskVo[] }>({
    url: '/api/approval/task/todo/list',
    data
  })
}

export function fetchApprovalDoneList(data: ApTaskQuery) {
  return request.post<{ total: number; rows: ApTaskVo[] }>({
    url: '/api/approval/task/done/list',
    data
  })
}

export function fetchApprovalApprove(data: ApTaskActionDto) {
  return request.post({
    url: '/api/approval/task/approve',
    data
  })
}

export function fetchApprovalReject(data: ApTaskActionDto) {
  return request.post({
    url: '/api/approval/task/reject',
    data
  })
}

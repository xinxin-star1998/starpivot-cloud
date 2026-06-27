import request from '@/utils/http'
import type { ApInstanceVo, ApprovalSubmitRequest, ApprovalTimelineVo } from './types'

export interface ApInstanceQuery {
  title?: string
  status?: string
  bizModule?: string
  bizType?: string
  pageNum?: number
  pageSize?: number
}

export function fetchApprovalMineList(data: ApInstanceQuery) {
  return request.post<{ total: number; rows: ApInstanceVo[] }>({
    url: '/api/approval/instance/mine/list',
    data
  })
}

export function fetchApprovalSubmit(data: ApprovalSubmitRequest) {
  return request.post<number>({
    url: '/api/approval/instance/submit',
    data
  })
}

export function fetchApprovalWithdraw(instanceId: number) {
  return request.post({
    url: `/api/approval/instance/${instanceId}/withdraw`
  })
}

export function fetchApprovalTimeline(instanceId: number) {
  return request.get<ApprovalTimelineVo>({
    url: `/api/approval/instance/${instanceId}/timeline`
  })
}

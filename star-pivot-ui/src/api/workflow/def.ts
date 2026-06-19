import request from '@/utils/http'

export interface ProcessDefVo {
  defId?: number
  processCode?: string
  processName?: string
  bizModule?: string
  version?: number
  defJson?: string
  status?: string
  remark?: string
  createBy?: string
  createTime?: string
  updateTime?: string
}

export interface ProcessDefQuery {
  processCode?: string
  processName?: string
  bizModule?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface ProcessDefSaveBo {
  defId?: number
  processCode: string
  processName: string
  bizModule: string
  defJson: string
  remark?: string
}

export function fetchWorkflowDefList(data: ProcessDefQuery) {
  return request.post<{ total: number; rows: ProcessDefVo[] }>({
    url: '/api/workflow/def/list',
    data
  })
}

export function fetchWorkflowDefDetail(defId: number) {
  return request.get<ProcessDefVo>({
    url: `/api/workflow/def/${defId}`
  })
}

export function fetchWorkflowDefSave(data: ProcessDefSaveBo) {
  return request.post<number>({
    url: '/api/workflow/def/save',
    data
  })
}

export function fetchWorkflowDefPublish(defId: number) {
  return request.post({
    url: `/api/workflow/def/${defId}/publish`
  })
}

export function fetchWorkflowDefDisable(defId: number) {
  return request.post({
    url: `/api/workflow/def/${defId}/disable`
  })
}

export function fetchWorkflowDefRemove(ids: number[]) {
  return request.del({
    url: '/api/workflow/def/remove',
    data: { ids }
  })
}

import request from '@/utils/http'

export interface TaskVo {
  taskId?: number
  instanceId?: number
  processCode?: string
  processName?: string
  businessKey?: string
  title?: string
  nodeId?: string
  nodeName?: string
  assigneeId?: number
  assigneeName?: string
  starterId?: number
  starterName?: string
  status?: string
  createTime?: string
}

export interface TaskQuery {
  title?: string
  processCode?: string
  pageNum?: number
  pageSize?: number
}

export interface TaskActionBo {
  taskId: number
  comment?: string
}

export interface StartWorkflowBo {
  processCode: string
  businessKey: string
  title: string
  variables?: Record<string, unknown>
}

export interface InstanceHistoryVo {
  historyId?: number
  nodeId?: string
  nodeName?: string
  action?: string
  comment?: string
  operatorId?: number
  operatorName?: string
  createTime?: string
}

export interface InstanceNodeStatusVo {
  nodeId?: string
  status?: string
}

export interface InstanceProgressVo {
  instanceId?: number
  title?: string
  processCode?: string
  processName?: string
  status?: string
  currentNodeId?: string
  defJson?: string
  nodeStatuses?: InstanceNodeStatusVo[]
  histories?: InstanceHistoryVo[]
  createTime?: string
  finishTime?: string
}

export function fetchWorkflowTodoList(data: TaskQuery) {
  return request.post<{ total: number; rows: TaskVo[] }>({
    url: '/api/workflow/task/todo/list',
    data
  })
}

export function fetchWorkflowDoneList(data: TaskQuery) {
  return request.post<{ total: number; rows: TaskVo[] }>({
    url: '/api/workflow/task/done/list',
    data
  })
}

export function fetchWorkflowMineList(data: TaskQuery) {
  return request.post<{ total: number; rows: TaskVo[] }>({
    url: '/api/workflow/task/mine/list',
    data
  })
}

export function fetchWorkflowApprove(data: TaskActionBo) {
  return request.post({
    url: '/api/workflow/task/approve',
    data
  })
}

export function fetchWorkflowReject(data: TaskActionBo) {
  return request.post({
    url: '/api/workflow/task/reject',
    data
  })
}

export function fetchWorkflowCancel(instanceId: number) {
  return request.post({
    url: `/api/workflow/task/cancel/${instanceId}`
  })
}

export function fetchWorkflowStart(data: StartWorkflowBo) {
  return request.post<number>({
    url: '/api/workflow/task/start',
    data
  })
}

export function fetchWorkflowInstanceProgress(instanceId: number) {
  return request.get<InstanceProgressVo>({
    url: `/api/workflow/task/instance/${instanceId}/progress`
  })
}

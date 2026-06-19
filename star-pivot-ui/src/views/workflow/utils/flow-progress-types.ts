export type FlowNodeProgressStatus = 'COMPLETED' | 'CURRENT' | 'REJECTED' | 'PENDING'

export const PROGRESS_STATUS_LABEL: Record<FlowNodeProgressStatus, string> = {
  COMPLETED: '已完成',
  CURRENT: '进行中',
  REJECTED: '已驳回',
  PENDING: '未到达'
}

export const HISTORY_ACTION_LABEL: Record<string, string> = {
  START: '发起',
  APPROVE: '通过',
  REJECT: '驳回',
  CANCEL: '撤销'
}

export function toNodeStatusMap(
  list?: Array<{ nodeId?: string; status?: string }>
): Record<string, FlowNodeProgressStatus> {
  const map: Record<string, FlowNodeProgressStatus> = {}
  list?.forEach((item) => {
    if (item.nodeId && item.status) {
      map[item.nodeId] = item.status as FlowNodeProgressStatus
    }
  })
  return map
}

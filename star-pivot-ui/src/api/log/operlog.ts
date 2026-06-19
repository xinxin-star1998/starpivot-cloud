import request from '@/utils/http'
import type { OperLogListItem, OperLogSearchParams, OperLogList } from '@/types/api/operlog'

/**
 * 获取操作日志列表（分页）
 */
export function fetchGetOperLogList(params: OperLogSearchParams) {
  return request.post<OperLogList>({
    url: '/api/sys/operlog/pageList',
    params
  })
}

/**
 * 根据ID获取操作日志详情
 */
export function fetchGetOperLogById(operId: number) {
  return request.get<OperLogListItem>({
    url: `/api/sys/operlog/${operId}`
  })
}

/**
 * 删除操作日志（支持单删和批量删除）
 */
export function fetchDeleteOperLog(operIds: number[]) {
  return request.del({
    url: '/api/sys/operlog/delete',
    data: { ids: operIds }
  })
}

/**
 * 清空操作日志
 */
export function fetchCleanOperLog() {
  return request.del({
    url: '/api/sys/operlog/clean'
  })
}

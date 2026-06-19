import request from '@/utils/http'
import type {
  LogininforListItem,
  LogininforSearchParams,
  LogininforList
} from '@/types/api/logininfor'

/**
 * 获取登录日志列表（分页）
 */
export function fetchGetLogininforList(params: LogininforSearchParams) {
  return request.post<LogininforList>({
    url: '/api/sys/logininfor/pageList',
    params
  })
}

/**
 * 根据ID获取登录日志详情
 */
export function fetchGetLogininforById(infoId: number) {
  return request.get<LogininforListItem>({
    url: `/api/sys/logininfor/${infoId}`
  })
}

/**
 * 删除登录日志（支持单删和批量删除）
 */
export function fetchDeleteLogininfor(infoIds: number[]) {
  return request.del({
    url: '/api/sys/logininfor/delete',
    data: { ids: infoIds }
  })
}

/**
 * 清空登录日志
 */
export function fetchCleanLogininfor() {
  return request.del({
    url: '/api/sys/logininfor/clean'
  })
}

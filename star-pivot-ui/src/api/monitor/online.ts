import request from '@/utils/http'
import type {OnlineUser, OnlineUserQueryParams} from '@/types/api/monitor'

/**
 * 获取在线用户列表
 */
export function fetchGetOnlineUserList(params?: OnlineUserQueryParams) {
  return request.get<OnlineUser[]>({
    url: '/api/monitor/online',
    params
  })
}

/**
 * 强制用户下线
 */
export function fetchForceLogout(sessionId: string) {
  return request.del({
    url: `/api/monitor/online/${sessionId}`
  })
}

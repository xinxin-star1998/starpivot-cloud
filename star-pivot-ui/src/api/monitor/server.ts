import request from '@/utils/http'
import type {ServerInfo} from '@/types/api/monitor'

/**
 * 获取服务器信息
 */
export function fetchGetServerInfo() {
  return request.get<ServerInfo>({
    url: '/api/monitor/server'
  })
}

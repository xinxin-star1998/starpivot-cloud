import request from '@/utils/http'
import type {ConsoleDashboardData} from '@/types/api/dashboard'

/**
 * 获取工作台首页数据
 */
export function fetchGetConsoleDashboardData() {
  return request.get<ConsoleDashboardData>({
    url: '/api/dashboard/console'
  })
}

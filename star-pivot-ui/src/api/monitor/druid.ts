import request from '@/utils/http'
import type {DruidMonitorInfo} from '@/types/api/monitor'

/**
 * 获取 Druid 监控信息
 * @param includeSlowSqlList 是否包含慢SQL列表（可选，默认false）
 * @param slowSqlThreshold 慢SQL阈值（毫秒，可选，默认5000，仅在 includeSlowSqlList=true 时有效）
 */
export function fetchGetDruidMonitorInfo(includeSlowSqlList?: boolean, slowSqlThreshold?: number) {
  return request.get<DruidMonitorInfo>({
    url: '/api/monitor/druid',
    params: {
      includeSlowSqlList,
      slowSqlThreshold
    }
  })
}

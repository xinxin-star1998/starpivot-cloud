import request from '@/utils/http'

export interface ApStatisticsVo {
  totalInstances?: number
  runningCount?: number
  approvedCount?: number
  rejectedCount?: number
  withdrawnCount?: number
  pendingTaskCount?: number
  overdueTaskCount?: number
  avgFinishHours?: number
  bizTypeStats?: BizTypeStatItem[]
  dailyFinished?: DailyFinishedItem[]
}

export interface BizTypeStatItem {
  bizType?: string
  total?: number
  approved?: number
  rejected?: number
}

export interface DailyFinishedItem {
  day?: string
  count?: number
}

export function fetchApprovalStatistics(bizModule?: string) {
  return request.get<ApStatisticsVo>({
    url: '/api/approval/statistics/dashboard',
    params: bizModule ? { bizModule } : undefined
  })
}

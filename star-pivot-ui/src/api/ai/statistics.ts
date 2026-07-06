import request from '@/utils/http'

export interface AiUsageSummary {
  totalRequests?: number
  totalTokens?: number
  successRequests?: number
  failedRequests?: number
  avgLatencyMs?: number
  promptTokens?: number
  completionTokens?: number
}

export interface AiUsageLogItem {
  logId?: number
  userId?: number
  conversationId?: string
  model?: string
  requestType?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  latencyMs?: number
  success?: string
  errorMessage?: string
  createTime?: string
}

export interface AiUsageLogListParams extends Api.Common.CommonSearchParams {
  userId?: number
  model?: string
  requestType?: string
  beginTime?: string
  endTime?: string
}

export function fetchAiUsageSummary(params?: { beginTime?: string; endTime?: string }) {
  return request.get<AiUsageSummary>({
    url: '/ai/statistics/summary',
    params
  })
}

export function fetchAiUsageLogList(params: AiUsageLogListParams) {
  return request.post<Api.Common.PageResponse<AiUsageLogItem>>({
    url: '/ai/statistics/pageList',
    data: params
  })
}

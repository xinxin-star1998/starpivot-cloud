import request from '@/utils/http'
import type { ChatHistoryMessage } from '@/api/ai/chat'

export interface AiChatSessionAdminItem {
  sessionId?: number
  conversationId?: string
  userId?: number
  title?: string
  messageCount?: number
  createTime?: string
  updateTime?: string
}

export interface AiChatSessionListParams extends Api.Common.CommonSearchParams {
  userId?: number
  conversationId?: string
  title?: string
}

export function fetchAiSessionAdminList(params: AiChatSessionListParams) {
  return request.post<Api.Common.PageResponse<AiChatSessionAdminItem>>({
    url: '/ai/session/admin/pageList',
    data: params
  })
}

export function fetchAiSessionAdminMessages(conversationId: string) {
  return request.get<ChatHistoryMessage[]>({
    url: '/ai/session/admin/messages',
    params: { conversationId }
  })
}

export function fetchAiSessionAdminRemove(sessionId: number) {
  return request.delete<void>({
    url: `/ai/session/admin/${sessionId}`
  })
}

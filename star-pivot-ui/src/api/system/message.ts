import request from '@/utils/http'

export interface UserMessageVo {
  messageId?: number
  msgType?: string
  title?: string
  content?: string
  bizModule?: string
  bizType?: string
  bizKey?: string
  bizId?: number
  linkPath?: string
  readFlag?: string
  createTime?: string
}

export interface UserMessageQuery {
  pageNum?: number
  pageSize?: number
  readFlag?: string
  msgType?: string
}

export function fetchUserMessageList(params: UserMessageQuery) {
  return request.post<Api.Common.PageResponse<UserMessageVo>>({
    url: '/api/message/messagePageList',
    data: params
  })
}

export function fetchUserMessageUnreadCount() {
  return request.get<number>({
    url: '/api/message/unread-count'
  })
}

export function fetchUserMessageRead(id: number) {
  return request.post<void>({
    url: `/api/message/${id}/read`
  })
}

export function fetchUserMessageReadAll() {
  return request.post<void>({
    url: '/api/message/read-all',
    showSuccessMessage: true
  })
}

export interface MessageBroadcastRequest {
  title: string
  content?: string
  targetType: 'ALL' | 'ROLE' | 'USER'
  roleIds?: number[]
  userIds?: number[]
}

export interface MessagePushPayload {
  userId?: number
  unreadCount?: number
  message?: UserMessageVo
}

export function fetchMessageBroadcast(data: MessageBroadcastRequest) {
  return request.post<number>({
    url: '/api/message/broadcast',
    data,
    showSuccessMessage: true
  })
}

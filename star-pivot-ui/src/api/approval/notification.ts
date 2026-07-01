import request from '@/utils/http'

export interface ApNotificationVo {
  notifyId?: number
  notifyType?: string
  title?: string
  content?: string
  instanceId?: number
  taskId?: number
  readFlag?: string
  createTime?: string
}

export interface ApNotificationQuery {
  pageNum?: number
  pageSize?: number
  readFlag?: string
}

export function fetchApprovalNotificationList(params: ApNotificationQuery) {
  return request.post<Api.Common.PaginatedResponse<ApNotificationVo>>({
    url: '/api/approval/notification/notificationPageList',
    data: params
  })
}

export function fetchApprovalUnreadCount() {
  return request.get<number>({
    url: '/api/approval/notification/unread-count'
  })
}

export function fetchApprovalNotificationRead(id: number) {
  return request.post<void>({
    url: `/api/approval/notification/${id}/read`
  })
}

export function fetchApprovalNotificationReadAll() {
  return request.post<void>({
    url: '/api/approval/notification/read-all',
    showSuccessMessage: true
  })
}

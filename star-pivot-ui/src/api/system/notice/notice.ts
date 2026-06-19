import request from '@/utils/http'

/**
 * 通知公告实体类型
 */
export interface Notice {
  noticeId?: number
  noticeTitle?: string
  noticeType?: string
  noticeContent?: string
  status?: string
  createTime?: string
  updateTime?: string
}

/**
 * 通知公告搜索参数
 */
export interface NoticeSearchParams {
  pageNum?: number
  pageSize?: number
  noticeTitle?: string
  noticeType?: string
  noticeContent?: string
  status?: string
}

/**
 * 获取通知公告列表（分页）
 */
export function fetchGetNoticeList(params: NoticeSearchParams) {
  return request.post<Api.Common.PaginatedResponse<Notice>>({
    url: '/api/system/notice/list',
    data: params
  })
}

/**
 * 根据ID获取通知公告详情
 */
export function fetchGetNoticeById(noticeId: number) {
  return request.get<Notice>({
    url: `/api/system/notice/getNoticeInfo/${noticeId}`
  })
}

/**
 * 新增通知公告
 */
export function fetchAddNotice(data: Notice) {
  return request.post({
    url: '/api/system/notice',
    data
  })
}

/**
 * 修改通知公告
 */
export function fetchUpdateNotice(data: Notice) {
  return request.put({
    url: '/api/system/notice',
    data
  })
}

/**
 * 删除通知公告（支持单删和批量删除）
 * 对应后端 DELETE /system/notice/removeNotice，请求体 { ids: number[] }
 */
export function fetchDeleteNotice(noticeIds: number[]) {
  return request.del({
    url: '/api/system/notice/removeNotice',
    data: { ids: noticeIds }
  })
}

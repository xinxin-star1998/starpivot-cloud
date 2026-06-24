import request from '@/utils/http'

export interface CommentVo {
  id?: number
  skuId?: number
  spuId?: number
  spuName?: string
  memberNickName?: string
  star?: number
  memberIp?: string
  createTime?: string
  showStatus?: number
  spuAttributes?: string
  likesCount?: number
  replyCount?: number
  resources?: string
  content?: string
  memberIcon?: string
  commentType?: number
}

export interface CommentListParams extends Api.Common.CommonSearchParams {
  spuId?: number
  skuId?: number
  memberNickName?: string
  showStatus?: number
  star?: number
}

export interface CommentShowStatusPayload {
  id: number
  showStatus: number
}

export function fetchCommentList(params: CommentListParams) {
  return request.post<Api.Common.PaginatedResponse<CommentVo>>({
    url: '/api/mall/comment/list',
    data: params
  })
}

export function fetchCommentById(id: number) {
  return request.get<CommentVo>({
    url: `/api/mall/comment/${id}`
  })
}

export function fetchCommentUpdateShowStatus(data: CommentShowStatusPayload) {
  return request.put<void>({
    url: '/api/mall/comment/show-status',
    data,
    showSuccessMessage: true
  })
}

export function fetchCommentRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/comment/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

export const COMMENT_SHOW_STATUS_MAP: Record<number, string> = {
  0: '隐藏',
  1: '显示'
}

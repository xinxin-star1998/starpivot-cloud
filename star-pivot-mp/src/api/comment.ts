import {request} from '@/utils/request'
import type {PageResult, PortalComment, PortalCommentSummary, PortalPendingReview} from './types'

export function fetchCommentList(spuId: number, pageNum = 1, pageSize = 10) {
  return request<PageResult<PortalComment>>({
    url: '/portal/comment/commentPageList',
    method: 'POST',
    data: { spuId, pageNum, pageSize },
    auth: false
  })
}

export function fetchCommentSummary(spuId: number) {
  return request<PortalCommentSummary>({
    url: `/portal/comment/summary/${spuId}`,
    auth: false
  })
}

export function fetchPendingReviews() {
  return request<PortalPendingReview[]>({ url: '/portal/comment/pending' })
}

export function fetchCanComment(spuId: number) {
  return request<{ canComment: boolean }>({
    url: `/portal/comment/can-comment/${spuId}`
  })
}

export function fetchReviewableSpuIds(spuIds: number[]) {
  return request<{ reviewableSpuIds: number[] }>({
    url: '/portal/comment/reviewable-spu-ids',
    method: 'POST',
    data: { spuIds }
  })
}

export function submitComment(data: {
  spuId: number
  skuId: number
  star: number
  content: string
  resources?: string
}) {
  return request<void>({ url: '/portal/comment/submit', method: 'POST', data })
}

export function fetchMyComments(pageNum = 1, pageSize = 20) {
  return request<PageResult<PortalComment>>({
    url: '/portal/comment/mineCommentPageList',
    method: 'POST',
    data: { pageNum, pageSize }
  })
}

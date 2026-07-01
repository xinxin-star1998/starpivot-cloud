import request from '@/utils/http'
import type {
    PortalComment,
    PortalCommentQueryParams,
    PortalCommentSubmitPayload,
    PortalCommentSummary,
    PortalPendingReview
} from './types'

export function fetchPortalCommentPageList(params: PortalCommentQueryParams) {
  return request.post<Api.Common.PaginatedResponse<PortalComment>>({
    url: '/api/portal/comment/commentPageList',
    data: params
  })
}

export function fetchPortalCommentSubmit(data: PortalCommentSubmitPayload) {
  return request.post<void>({
    url: '/api/portal/comment/submit',
    data,
    showSuccessMessage: true
  })
}

export function fetchPortalCanComment(spuId: number) {
  return request.get<{ canComment: boolean }>({
    url: `/api/portal/comment/can-comment/${spuId}`
  })
}

export function fetchPortalMyCommentPageList(params: Api.Common.CommonSearchParams) {
  return request.post<Api.Common.PaginatedResponse<PortalComment>>({
    url: '/api/portal/comment/mineCommentPageList',
    data: params
  })
}

export function fetchPortalReviewableSpuIds(spuIds: number[]) {
  return request.post<{ reviewableSpuIds: number[] }>({
    url: '/api/portal/comment/reviewable-spu-ids',
    data: { spuIds }
  })
}

export function fetchPortalCommentSummary(spuId: number) {
  return request.get<PortalCommentSummary>({
    url: `/api/portal/comment/summary/${spuId}`
  })
}

export function fetchPortalPendingReviews() {
  return request.get<PortalPendingReview[]>({
    url: '/api/portal/comment/pending'
  })
}

export function fetchPortalPendingReviewCount() {
  return request.get<{ count: number }>({
    url: '/api/portal/comment/pending-count'
  })
}

import request from '@/utils/http'

export interface CouponHistoryVo {
  id?: number
  couponId?: number
  memberId?: number
  memberNickName?: string
  getType?: number
  createTime?: string
  useType?: number
  useTime?: string
  orderId?: number
  orderSn?: string
}

export interface CouponHistoryListParams extends Api.Common.CommonSearchParams {
  memberId?: number
  couponId?: number
}

export function fetchCouponHistoryList(params: CouponHistoryListParams) {
  return request.post<Api.Common.PaginatedResponse<CouponHistoryVo>>({
    url: '/api/mall/coupon-history/couponHistoryPageList',
    data: params
  })
}

/** 领取方式 */
export const COUPON_GET_TYPE_MAP: Record<number, string> = {
  0: '后台赠送',
  1: '主动领取'
}

/** 使用状态 */
export const COUPON_HISTORY_USE_TYPE_MAP: Record<number, string> = {
  0: '未使用',
  1: '已使用',
  2: '已过期'
}

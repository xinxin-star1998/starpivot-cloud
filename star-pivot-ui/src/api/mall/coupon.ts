import request from '@/utils/http'

export interface CouponSpuVo {
  id?: number
  spuId?: number
  spuName?: string
}

export interface CouponCategoryVo {
  id?: number
  categoryId?: number
  categoryName?: string
}

export interface CouponVo {
  id?: number
  couponType?: number
  couponImg?: string
  couponName?: string
  num?: number
  amount?: number
  perLimit?: number
  minPoint?: number
  startTime?: string
  endTime?: string
  useType?: number
  note?: string
  publishCount?: number
  useCount?: number
  receiveCount?: number
  enableStartTime?: string
  enableEndTime?: string
  code?: string
  memberLevel?: number
  publish?: number
  spuList?: CouponSpuVo[]
  categoryList?: CouponCategoryVo[]
}

export interface CouponListParams extends Api.Common.CommonSearchParams {
  couponName?: string
}

export interface CouponSavePayload {
  id?: number
  couponType?: number
  couponImg?: string
  couponName: string
  num?: number
  amount?: number
  perLimit?: number
  minPoint?: number
  startTime?: string
  endTime?: string
  useType?: number
  note?: string
  publishCount?: number
  enableStartTime?: string
  enableEndTime?: string
  code?: string
  memberLevel?: number
  publish?: number
  spuList?: { spuId?: number; spuName?: string }[]
  categoryList?: { categoryId?: number; categoryName?: string }[]
}

export function fetchCouponList(params: CouponListParams) {
  return request.post<Api.Common.PaginatedResponse<CouponVo>>({
    url: '/api/mall/coupon/list',
    data: params
  })
}

export function fetchCouponById(id: number) {
  return request.get<CouponVo>({
    url: `/api/mall/coupon/${id}`
  })
}

export function fetchCouponAdd(data: CouponSavePayload) {
  return request.post<void>({
    url: '/api/mall/coupon',
    data,
    showSuccessMessage: true
  })
}

export function fetchCouponUpdate(data: CouponSavePayload) {
  return request.put<void>({
    url: '/api/mall/coupon',
    data,
    showSuccessMessage: true
  })
}

export function fetchCouponRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/coupon/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

export const COUPON_USE_TYPE_OPTIONS = [
  { label: '全场通用', value: 0 },
  { label: '指定分类', value: 1 },
  { label: '指定商品', value: 2 }
]

export const COUPON_TYPE_OPTIONS = [
  { label: '全场赠券', value: 0 },
  { label: '会员赠券', value: 1 },
  { label: '购物赠券', value: 2 },
  { label: '注册赠券', value: 3 }
]

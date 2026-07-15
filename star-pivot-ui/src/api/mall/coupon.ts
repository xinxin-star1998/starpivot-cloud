import request from '@/utils/http'
import {canSubmitMallAudit} from '@/utils/mall/audit-status'

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
  approvalInstanceId?: number
  auditStatus?: string
  spuList?: CouponSpuVo[]
  categoryList?: CouponCategoryVo[]
}

export interface CouponListParams extends Api.Common.CommonSearchParams {
  couponName?: string
  publish?: number
  useType?: number
  validityStart?: string
  validityEnd?: string
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
  return request.post<Api.Common.PageResponse<CouponVo>>({
    url: '/api/mall/coupon/couponPageList',
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
    url: '/api/mall/coupon/removeCoupon',
    data: { ids },
    showSuccessMessage: true
  })
}

export function fetchCouponPublishStatus(id: number, publish: 0 | 1) {
  return request.put<void>({
    url: '/api/mall/coupon/publish-status',
    data: { id, publish },
    showSuccessMessage: true
  })
}

export function fetchCouponSubmitApproval(id: number) {
  return request.post<void>({
    url: `/api/mall/coupon/${id}/submit-approval`,
    showSuccessMessage: true
  })
}

export function canSubmitCouponAudit(row: Pick<CouponVo, 'publish' | 'auditStatus'>) {
  return row.publish !== 1 && canSubmitMallAudit(row.auditStatus)
}

export const COUPON_PUBLISH_OPTIONS = [
  { label: '未发布', value: 0 },
  { label: '已发布', value: 1 }
]

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

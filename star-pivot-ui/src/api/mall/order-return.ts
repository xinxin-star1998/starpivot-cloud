import request from '@/utils/http'

export interface ReturnVo {
  id?: number
  orderId?: number
  skuId?: number
  orderSn?: string
  createTime?: string
  memberUsername?: string
  returnAmount?: number
  returnName?: string
  returnPhone?: string
  status?: number
  handleTime?: string
  skuImg?: string
  skuName?: string
  skuBrand?: string
  skuAttrsVals?: string
  skuCount?: number
  skuPrice?: number
  skuRealPrice?: number
  reason?: string
  description?: string
  descPics?: string
  handleNote?: string
  handleMan?: string
  receiveMan?: string
  receiveTime?: string
  receiveNote?: string
  receivePhone?: string
  companyAddress?: string
}

export interface ReturnListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
  memberUsername?: string
  status?: number
  startTime?: string
  endTime?: string
}

export interface ReturnAuditPayload {
  id: number
  status: number
  handleNote?: string
  handleMan?: string
}

export function fetchReturnList(params: ReturnListParams) {
  return request.post<Api.Common.PaginatedResponse<ReturnVo>>({
    url: '/api/mall/order-return/list',
    data: params
  })
}

export function fetchReturnById(id: number) {
  return request.get<ReturnVo>({
    url: `/api/mall/order-return/${id}`
  })
}

export function fetchReturnAudit(data: ReturnAuditPayload) {
  return request.put<void>({
    url: '/api/mall/order-return/audit',
    data,
    showSuccessMessage: true
  })
}

export function fetchReturnComplete(id: number) {
  return request.put<void>({
    url: `/api/mall/order-return/${id}/complete`,
    showSuccessMessage: true
  })
}

/** 退货状态：0待处理 1退货中 2已完成 3已拒绝 */
export const RETURN_STATUS_MAP: Record<number, string> = {
  0: '待处理',
  1: '退货中',
  2: '已完成',
  3: '已拒绝'
}

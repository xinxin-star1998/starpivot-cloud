import request from '@/utils/http'

/** 采购单 */
export interface PurchaseVo {
  id?: number
  assigneeId?: number
  assigneeName?: string
  phone?: string
  priority?: number
  status?: number
  wareId?: number
  wareName?: string
  amount?: number
  approvalInstanceId?: number
  auditStatus?: string
  createTime?: string
  updateTime?: string
  details?: PurchaseDetailVo[]
}

/** 采购明细 / 采购需求 */
export interface PurchaseDetailVo {
  id?: number
  purchaseId?: number
  skuId?: number
  skuName?: string
  skuNum?: number
  skuPrice?: number
  wareId?: number
  status?: number
}

export interface PurchaseListParams extends Api.Common.CommonSearchParams {
  status?: number
  assigneeName?: string
  wareId?: number
}

export interface PurchaseDetailListParams extends Api.Common.CommonSearchParams {
  status?: number
  skuId?: number
  purchaseId?: number
  onlyDemand?: boolean
}

export interface PurchaseDetailSavePayload {
  skuId: number
  skuNum: number
  skuPrice?: number
  wareId?: number
}

export interface PurchaseMergePayload {
  purchaseId?: number
  items: number[]
}

export interface PurchaseItemDonePayload {
  itemId: number
  status: number
}

export interface PurchaseDonePayload {
  id: number
  items: PurchaseItemDonePayload[]
}

export function fetchPurchaseList(params: PurchaseListParams) {
  return request.post<Api.Common.PaginatedResponse<PurchaseVo>>({
    url: '/api/mall/purchase/list',
    data: params
  })
}

export function fetchPurchaseUnreceiveList(params: PurchaseListParams) {
  return request.post<Api.Common.PaginatedResponse<PurchaseVo>>({
    url: '/api/mall/purchase/unreceive/list',
    data: params
  })
}

export function fetchPurchaseById(id: number) {
  return request.get<PurchaseVo>({
    url: `/api/mall/purchase/${id}`
  })
}

export function fetchPurchaseMerge(data: PurchaseMergePayload) {
  return request.post<void>({
    url: '/api/mall/purchase/merge',
    data,
    showSuccessMessage: true
  })
}

export function fetchPurchaseReceived(ids: number[]) {
  return request.post<void>({
    url: '/api/mall/purchase/received',
    data: ids,
    showSuccessMessage: true
  })
}

export function fetchPurchaseDone(data: PurchaseDonePayload) {
  return request.post<void>({
    url: '/api/mall/purchase/done',
    data,
    showSuccessMessage: true
  })
}

export function fetchPurchaseSubmitApproval(id: number) {
  return request.post<void>({
    url: `/api/mall/purchase/${id}/submit-approval`,
    showSuccessMessage: true
  })
}

export function fetchPurchaseDetailList(params: PurchaseDetailListParams) {
  return request.post<Api.Common.PaginatedResponse<PurchaseDetailVo>>({
    url: '/api/mall/purchase/detail/list',
    data: params
  })
}

export function fetchPurchaseDetailAdd(data: PurchaseDetailSavePayload) {
  return request.post<void>({
    url: '/api/mall/purchase/detail',
    data,
    showSuccessMessage: true
  })
}

export function fetchPurchaseDetailRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/purchase/detail/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

/** 采购单状态 */
export const PURCHASE_STATUS_MAP: Record<number, string> = {
  0: '新建',
  1: '已分配',
  2: '已领取',
  3: '已完成',
  4: '有异常'
}

/** 采购明细状态 */
export const PURCHASE_DETAIL_STATUS_MAP: Record<number, string> = {
  0: '新建',
  1: '已分配',
  2: '采购中',
  3: '完成',
  4: '失败'
}

/** 采购单审批状态 */
export const PURCHASE_AUDIT_STATUS_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
}

export function canSubmitPurchaseAudit(status?: string) {
  return !status || status === 'DRAFT' || status === 'REJECTED' || status === 'WITHDRAWN'
}

export function canReceivePurchase(row: Pick<PurchaseVo, 'status' | 'auditStatus'>) {
  return (row.status === 0 || row.status === 1) && row.auditStatus === 'APPROVED'
}

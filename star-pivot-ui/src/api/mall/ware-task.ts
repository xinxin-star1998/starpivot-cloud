import request from '@/utils/http'

export interface WareOrderTaskDetailVo {
  id?: number
  skuId?: number
  skuName?: string
  skuNum?: number
  taskId?: number
  wareId?: number
  lockStatus?: number
}

export interface WareOrderTaskVo {
  id?: number
  orderId?: number
  orderSn?: string
  consignee?: string
  consigneeTel?: string
  deliveryAddress?: string
  orderComment?: string
  paymentWay?: number
  taskStatus?: number
  orderBody?: string
  trackingNo?: string
  createTime?: string
  wareId?: number
  taskComment?: string
  details?: WareOrderTaskDetailVo[]
}

export interface WareOrderTaskListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
  taskStatus?: number
  wareId?: number
}

export function fetchWareTaskList(params: WareOrderTaskListParams) {
  return request.post<Api.Common.PaginatedResponse<WareOrderTaskVo>>({
    url: '/api/mall/ware-task/wareTaskPageList',
    data: params
  })
}

export function fetchWareTaskById(id: number) {
  return request.get<WareOrderTaskVo>({
    url: `/api/mall/ware-task/${id}`
  })
}

export function fetchWareTaskCreateFromOrder(orderId: number) {
  return request.post<number>({
    url: `/api/mall/ware-task/from-order/${orderId}`,
    showSuccessMessage: true
  })
}

export function fetchWareTaskLock(id: number) {
  return request.put<void>({
    url: `/api/mall/ware-task/${id}/lock`,
    showSuccessMessage: true
  })
}

export function fetchWareTaskDeduct(id: number) {
  return request.put<void>({
    url: `/api/mall/ware-task/${id}/deduct`,
    showSuccessMessage: true
  })
}

export function fetchWareTaskUnlock(id: number) {
  return request.put<void>({
    url: `/api/mall/ware-task/${id}/unlock`,
    showSuccessMessage: true
  })
}

export const TASK_STATUS_MAP: Record<number, string> = {
  0: '待处理',
  1: '处理中',
  2: '已完成',
  3: '无效'
}

export const LOCK_STATUS_MAP: Record<number, string> = {
  1: '锁定',
  2: '解锁',
  3: '扣减'
}

import request from '@/utils/http'

export interface OmsOrderItemVo {
  id?: number
  orderId?: number
  orderSn?: string
  spuId?: number
  spuName?: string
  spuPic?: string
  spuBrand?: string
  categoryId?: number
  skuId?: number
  skuName?: string
  skuPic?: string
  skuPrice?: number
  skuQuantity?: number
  skuAttrsVals?: string
  promotionAmount?: number
  couponAmount?: number
  integrationAmount?: number
  realAmount?: number
  giftIntegration?: number
  giftGrowth?: number
}

export interface OmsOrderVo {
  id?: number
  memberId?: number
  orderSn?: string
  couponId?: number
  createTime?: string
  memberUsername?: string
  totalAmount?: number
  payAmount?: number
  freightAmount?: number
  promotionAmount?: number
  integrationAmount?: number
  couponAmount?: number
  discountAmount?: number
  payType?: number
  sourceType?: number
  status?: number
  deliveryCompany?: string
  deliverySn?: string
  autoConfirmDay?: number
  integration?: number
  growth?: number
  receiverName?: string
  receiverPhone?: string
  receiverPostCode?: string
  receiverProvince?: string
  receiverCity?: string
  receiverRegion?: string
  receiverDetailAddress?: string
  note?: string
  paymentTime?: string
  deliveryTime?: string
  receiveTime?: string
  commentTime?: string
  modifyTime?: string
  orderItemList?: OmsOrderItemVo[]
}

export interface OmsOrderListParams extends Api.Common.CommonSearchParams {
  orderSn?: string
  memberUsername?: string
  status?: number
  startTime?: string
  endTime?: string
}

export interface OmsDeliverPayload {
  orderId: number
  deliveryCompany: string
  deliverySn: string
}

export interface OmsOrderClosePayload {
  orderId: number
}

export function fetchOmsOrderList(params: OmsOrderListParams) {
  return request.post<Api.Common.PaginatedResponse<OmsOrderVo>>({
    url: '/api/mall/order/orderPageList',
    data: params
  })
}

export function fetchOmsOrderById(id: number) {
  return request.get<OmsOrderVo>({
    url: `/api/mall/order/${id}`
  })
}

export function fetchOmsOrderDeliver(data: OmsDeliverPayload) {
  return request.put<void>({
    url: '/api/mall/order/deliver',
    data,
    showSuccessMessage: true
  })
}

export function fetchOmsOrderClose(data: OmsOrderClosePayload) {
  return request.put<void>({
    url: '/api/mall/order/close',
    data,
    showSuccessMessage: true
  })
}

/** 订单状态：0待付款 1待发货 2已发货 3已完成 4已关闭 5无效 */
export const OMS_ORDER_STATUS_MAP: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已关闭',
  5: '无效订单'
}

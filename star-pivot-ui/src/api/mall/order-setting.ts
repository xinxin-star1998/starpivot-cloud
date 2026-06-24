import request from '@/utils/http'

export interface OmsOrderSetting {
  id?: number
  flashOrderOvertime?: number
  normalOrderOvertime?: number
  confirmOvertime?: number
  finishOvertime?: number
  commentOvertime?: number
  memberLevel?: number
}

export function fetchOrderSetting() {
  return request.get<OmsOrderSetting>({
    url: '/api/mall/order-setting'
  })
}

export function fetchOrderSettingUpdate(data: OmsOrderSetting) {
  return request.put<void>({
    url: '/api/mall/order-setting',
    data,
    showSuccessMessage: true
  })
}

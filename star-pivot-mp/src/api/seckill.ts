import {request} from '@/utils/request'
import type {PortalOrderSubmitResult, PortalSeckillOrderPayload, PortalSeckillPage} from './types'

export function fetchSeckillPage(sessionId?: number) {
  return request<PortalSeckillPage>({
    url: '/portal/seckill',
    auth: false,
    method: 'GET',
    data: sessionId != null ? { sessionId } : undefined
  })
}

export function submitSeckillOrder(data: PortalSeckillOrderPayload) {
  return request<PortalOrderSubmitResult>({
    url: '/portal/seckill/order',
    method: 'POST',
    data
  })
}

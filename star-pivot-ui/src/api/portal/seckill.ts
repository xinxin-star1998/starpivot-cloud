import request from '@/utils/http'
import type { PortalOrderSubmitResult, PortalSeckillOrderPayload, PortalSeckillPage } from './types'

export function fetchPortalSeckillPage(sessionId?: number) {
  return request.get<PortalSeckillPage>({
    url: '/api/portal/seckill',
    params: sessionId != null ? { sessionId } : undefined
  })
}

export function fetchPortalSeckillOrder(payload: PortalSeckillOrderPayload) {
  return request.post<PortalOrderSubmitResult>({
    url: '/api/portal/seckill/order',
    data: payload
  })
}

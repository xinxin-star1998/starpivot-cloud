import request from '@/utils/http'
import type { PortalHomeData } from './types'

export function fetchPortalHome() {
  return request.get<PortalHomeData>({
    url: '/api/portal/home'
  })
}

import {request} from '@/utils/request'
import type {PortalHomeData} from './types'

export function fetchHome() {
  return request<PortalHomeData>({ url: '/portal/home', auth: false })
}

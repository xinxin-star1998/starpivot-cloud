import {request} from '@/utils/request'
import type {PortalRegion} from './types'

export function fetchRegionChildren(parentCode = '0') {
  return request<PortalRegion[]>({
    url: '/portal/region/children',
    auth: false,
    method: 'GET',
    data: { parentCode }
  })
}

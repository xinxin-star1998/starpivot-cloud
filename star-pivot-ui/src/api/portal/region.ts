import request from '@/utils/http'
import type { PortalRegion } from './types'

/** 懒加载省市区（parentCode 默认 0 为省） */
export function fetchPortalRegionChildren(parentCode = '0') {
  return request.get<PortalRegion[]>({
    url: '/api/portal/region/children',
    params: { parentCode }
  })
}

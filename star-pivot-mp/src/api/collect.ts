import {request} from '@/utils/request'
import type {PageResult, PortalCollectItem} from './types'

export function fetchCollectList(pageNum = 1, pageSize = 20) {
  return request<PageResult<PortalCollectItem>>({
    url: '/portal/collect/collectPageList',
    method: 'POST',
    data: { pageNum, pageSize }
  })
}

export function addCollect(spuId: number) {
  return request<void>({ url: '/portal/collect', method: 'POST', data: { spuId } })
}

export function removeCollect(spuId: number) {
  return request<void>({ url: `/portal/collect/${spuId}`, method: 'DELETE' })
}

export function fetchCollectStatus(spuId: number) {
  return request<{ collected: boolean }>({ url: `/portal/collect/status/${spuId}` })
}

export function addSubjectCollect(subjectId: number) {
  return request<void>({ url: '/portal/collect/subject', method: 'POST', data: { subjectId } })
}

export function removeSubjectCollect(subjectId: number) {
  return request<void>({ url: `/portal/collect/subject/${subjectId}`, method: 'DELETE' })
}

export function fetchSubjectCollectStatus(subjectId: number) {
  return request<{ collected: boolean }>({ url: `/portal/collect/subject/status/${subjectId}` })
}

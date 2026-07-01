import request from '@/utils/http'
import type { ApTemplate, ApTemplateBind, ApTemplateBindSaveDto, ApTemplateRoute, ApTemplateSaveDto } from './types'

export interface ApTemplateQuery {
  templateCode?: string
  templateName?: string
  bizModule?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface ApTemplateBindQuery {
  bizModule?: string
  bizType?: string
  pageNum?: number
  pageSize?: number
}

export interface ApTemplateDetailVo {
  template?: ApTemplate
  steps?: ApTemplateSaveDto['steps']
  routes?: ApTemplateRoute[]
}

export function fetchApprovalTemplateList(data: ApTemplateQuery) {
  return request.post<{ total: number; rows: ApTemplate[] }>({
    url: '/api/approval/template/templatePageList',
    data
  })
}

export function fetchApprovalTemplateDetail(id: number) {
  return request.get<ApTemplateDetailVo>({
    url: `/api/approval/template/${id}`
  })
}

export function fetchApprovalTemplateSave(data: ApTemplateSaveDto) {
  return request.post<number>({
    url: '/api/approval/template/save',
    data
  })
}

export function fetchApprovalTemplatePublish(id: number) {
  return request.post({
    url: `/api/approval/template/${id}/publish`
  })
}

export function fetchApprovalTemplateDisable(id: number) {
  return request.post({
    url: `/api/approval/template/${id}/disable`
  })
}

export function fetchApprovalBindList(data: ApTemplateBindQuery) {
  return request.post<{ total: number; rows: ApTemplateBind[] }>({
    url: '/api/approval/template/templateBindPageList',
    data
  })
}

export function fetchApprovalBindSave(data: ApTemplateBindSaveDto) {
  return request.post({
    url: '/api/approval/template/bind/save',
    data
  })
}

export function fetchApprovalBindRemove(ids: number[]) {
  return request.del({
    url: '/api/approval/template/removeTemplateBind',
    data: { ids }
  })
}

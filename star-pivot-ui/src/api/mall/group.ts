import request from '@/utils/http'
import { fetchExcelExport, fetchExcelImport, fetchExcelTemplate } from '@/api/common/excel'

/**
 * 属性分组实体类型
 */
export interface Group {
  attrGroupId?: number
  attrGroupName?: string
  sort?: number
  descript?: string
  icon?: string
  catelogId?: number
}

/**
 * 属性分组搜索参数
 */
export interface GroupSearchParams {
  pageNum?: number
  pageSize?: number
  attrGroupName?: string
  sort?: number
  descript?: string
  icon?: string
  catelogId?: number
}

/**
 * 获取属性分组列表（分页）
 */
export function fetchGetGroupList(params: GroupSearchParams) {
  return request.post<Api.Common.PaginatedResponse<Group>>({
    url: '/api/mall/group/list',
    data: params
  })
}

/**
 * 根据ID获取属性分组详情
 */
export function fetchGetGroupById(attrGroupId: number) {
  return request.get<Group>({
    url: `/api/mall/group/${attrGroupId}`
  })
}

/**
 * 新增属性分组
 */
export function fetchAddGroup(data: Group) {
  return request.post({
    url: '/api/mall/group',
    data
  })
}

/**
 * 修改属性分组
 */
export function fetchUpdateGroup(data: Group) {
  return request.put({
    url: '/api/mall/group',
    data
  })
}

/**
 * 删除属性分组（支持单删和批量删除，请求体 ids）
 */
export function fetchDeleteGroup(attrGroupIds: number[]) {
  return request.del({
    url: '/api/mall/group/delete',
    data: { ids: attrGroupIds }
  })
}

/** 分组下可关联的基本属性（含 linked、attrSort） */
export interface GroupAttrRelation {
  attrId?: number
  attrName?: string
  icon?: string
  valueSelect?: string
  linked?: boolean
  attrSort?: number
}

export function fetchGroupAttrRelations(attrGroupId: number) {
  return request.get<GroupAttrRelation[]>({
    url: `/api/mall/group/${attrGroupId}/attrs`
  })
}

export function fetchSaveGroupAttrRelations(
  attrGroupId: number,
  items: { attrId: number; attrSort?: number }[]
) {
  return request.put({
    url: `/api/mall/group/${attrGroupId}/attrs`,
    data: { items }
  })
}

/** EasyExcel 导出属性分组 */
export function fetchExportGroup(params: GroupSearchParams) {
  return fetchExcelExport({
    url: '/api/mall/group/export',
    data: params as Record<string, unknown>,
    filenameFallback: `pms_attr_group_${Date.now()}.xlsx`,
    successMessage: false
  })
}

/** EasyExcel 导入属性分组 */
export function fetchImportGroup(file: File, updateSupport = false) {
  return fetchExcelImport({
    url: '/api/mall/group/import',
    file,
    updateSupport
  })
}

/** EasyExcel 下载属性分组导入模板 */
export function fetchDownloadGroupImportTemplate() {
  return fetchExcelTemplate({
    url: '/api/mall/group/importTemplate',
    filenameFallback: 'pms_attr_group_import_template.xlsx',
    successMessage: false
  })
}

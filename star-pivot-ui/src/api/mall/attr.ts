import request from '@/utils/http'
import {fetchExcelExport, fetchExcelImport, fetchExcelTemplate} from '@/api/common/excel'

/** 商品属性 */
export interface MallAttr {
  attrId?: number
  attrName?: string
  searchType?: number
  valueType?: number
  icon?: string
  valueSelect?: string
  /** 0-销售属性 1-基本属性 */
  attrType?: number
  enable?: number
  catelogId?: number
  /** 所属分组，对应表 pms_attr_attrgroup_relation.attr_group_id */
  attrGroupId?: number
  /** 组内排序，对应表 pms_attr_attrgroup_relation.attr_sort */
  attrSort?: number
  showDesc?: number
}

export interface MallAttrSearchParams {
  pageNum?: number
  pageSize?: number
  attrName?: string
  searchType?: number
  valueType?: number
  attrType?: number
  enable?: number
  catelogId?: number
  showDesc?: number
}

export function fetchGetAttrList(params: MallAttrSearchParams) {
  return request.post<Api.Common.PaginatedResponse<MallAttr>>({
    url: '/api/mall/attr/attrPageList',
    data: params
  })
}

export function fetchGetAttrById(attrId: number) {
  return request.get<MallAttr>({
    url: `/api/mall/attr/${attrId}`
  })
}

export function fetchAddAttr(data: MallAttr) {
  return request.post({
    url: '/api/mall/attr',
    data
  })
}

export function fetchUpdateAttr(data: MallAttr) {
  return request.put({
    url: '/api/mall/attr',
    data
  })
}

export function fetchDeleteAttr(attrIds: number[]) {
  return request.del({
    url: '/api/mall/attr/removeAttr',
    data: { ids: attrIds }
  })
}

/** EasyExcel 导出商品属性 */
export function fetchExportAttr(params: MallAttrSearchParams) {
  const prefix = params.attrType === 1 ? 'pms_base_attr_' : 'pms_sale_attr_'
  return fetchExcelExport({
    url: '/api/mall/attr/export',
    data: params as Record<string, unknown>,
    filenameFallback: `${prefix}${Date.now()}.xlsx`,
    successMessage: false
  })
}

/** EasyExcel 导入商品属性 */
export function fetchImportAttr(file: File, attrType: 0 | 1, updateSupport = false) {
  return fetchExcelImport({
    url: '/api/mall/attr/import',
    file,
    updateSupport,
    extraFormData: { attrType }
  })
}

/** EasyExcel 下载商品属性导入模板 */
export function fetchDownloadAttrImportTemplate(attrType: 0 | 1) {
  return fetchExcelTemplate({
    url: '/api/mall/attr/importTemplate',
    params: { attrType },
    filenameFallback:
      attrType === 1 ? 'pms_base_attr_import_template.xlsx' : 'pms_sale_attr_import_template.xlsx',
    successMessage: false
  })
}

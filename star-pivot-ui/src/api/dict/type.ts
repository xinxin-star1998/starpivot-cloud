import request from '@/utils/http'

// 字典类型实体
export interface SysDictType {
  dictId?: number
  dictName: string
  dictType: string
  status?: string // 0正常 1停用
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

// 字典类型表单数据
export interface DictTypeFormData {
  dictId?: number
  dictName: string
  dictType: string
  status?: string
  remark?: string
}

// 字典类型搜索参数（分页）
export interface DictTypeSearchParams {
  pageNum?: number
  pageSize?: number
  dictName?: string
  dictType?: string
  status?: string
}

// PageResponse 分页结果结构
export interface PageResponseResult<T> {
  rows: T[]
  total: number
  pageNum: number
  pageSize: number
  pageCount: number
}

/**
 * 获取字典类型列表（分页）
 * 后端返回 Result<PageResponse<DictTypeVO>>
 */
export function fetchGetDictTypeList(params?: DictTypeSearchParams) {
  return request.post<PageResponseResult<SysDictType>>({
    url: '/api/sys/dict/type/dictTypePageList',
    data: params || {}
  })
}
/**
 * 获取字典类型下拉列表
 */
export function fetchGetDictTypeSelectList() {
  return request.get<SysDictType[]>({
    url: '/api/sys/dict/type/selectList'
  })
}

/**
 * 根据ID获取字典类型详情
 */
export function fetchGetDictTypeById(dictId: number) {
  return request.get<SysDictType>({
    url: `/api/sys/dict/type/${dictId}`
  })
}

/**
 * 新增字典类型
 */
export function fetchAddDictType(data: DictTypeFormData) {
  return request.post({
    url: '/api/sys/dict/type',
    data
  })
}

/**
 * 修改字典类型
 */
export function fetchUpdateDictType(data: DictTypeFormData) {
  return request.put({
    url: '/api/sys/dict/type',
    data
  })
}

/**
 * 删除字典类型（支持单删和批量删除）
 */
export function fetchDeleteDictType(dictIds: number[]) {
  return request.del({
    url: '/api/sys/dict/type/removeDictType',
    data: { ids: dictIds }
  })
}

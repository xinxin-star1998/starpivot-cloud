import request from '@/utils/http'

// 字典数据实体
export interface SysDictData {
  dictCode?: number
  dictSort?: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string
  isDefault?: string // Y是 N否
  status?: string // 0正常 1停用
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

// 字典数据表单数据
export interface DictDataFormData {
  dictCode?: number
  dictSort?: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  status?: string
  remark?: string
}

// 字典数据搜索参数（分页）
export interface DictDataSearchParams {
  pageNum?: number
  pageSize?: number
  dictLabel?: string
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
 * 根据字典类型获取字典数据列表
 */
export function fetchGetDictDataByType(dictType: string) {
  return request.get<SysDictData[]>({
    url: `/api/sys/dict/data/type/${dictType}`
  })
}

/**
 * 获取字典数据列表（分页）
 * 后端返回 Result<PageResponse<DictDataVO>>
 */
export function fetchGetDictDataList(params?: DictDataSearchParams) {
  return request.post<PageResponseResult<SysDictData>>({
    url: '/api/sys/dict/data/list',
    data: params || {}
  })
}

/**
 * 根据ID获取字典数据详情
 */
export function fetchGetDictDataById(dictCode: number) {
  return request.get<SysDictData>({
    url: `/api/sys/dict/data/${dictCode}`
  })
}

/**
 * 新增字典数据
 */
export function fetchAddDictData(data: DictDataFormData) {
  return request.post({
    url: '/api/sys/dict/data',
    data
  })
}

/**
 * 修改字典数据
 */
export function fetchUpdateDictData(data: DictDataFormData) {
  return request.put({
    url: '/api/sys/dict/data',
    data
  })
}

/**
 * 删除字典数据（支持单删和批量删除）
 */
export function fetchDeleteDictData(dictCodes: number[]) {
  return request.del({
    url: '/api/sys/dict/data/delete',
    data: { ids: dictCodes }
  })
}

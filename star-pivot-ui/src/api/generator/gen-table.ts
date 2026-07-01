import request from '@/utils/http'

/**
 * 通用接口返回结果
 */
interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
}

/**
 * 获取代码生成表列表（分页）
 */
export function fetchGetGenTableList(params: Api.Generator.GenTableSearchParams) {
  return request.post<Api.Generator.GenTableList>({
    url: '/api/tool/gen/genTablePageList',
    data: params
  })
}
// 获取数据库列表
export function fetchGetDbList(params: Api.Generator.GenTableSearchParams) {
  return request.post<Api.Generator.GenTableList>({
    url: '/api/tool/gen/genDbTablePageList',
    data: params
  })
}
// 创建表
export function fetchCreateTable(tableSql: string) {
  return request.post<Api.Generator.GenTableList>({
    url: '/api/tool/gen/createTable',
    data: { tableSql }
  })
}
//导入表 importTable
export function fetchImportTable(tables: string) {
  return request.post<ApiResult<void>>({
    url: '/api/tool/gen/importTable',
    data: { tables }
  })
}
/**
 * 根据tableId获取代码生成信息
 * 返回数据结构：{ info: GenTable, rows: GenTableColumn[], tables: GenTable[] }
 */
export function fetchGetGenTableInfo(tableId: number) {
  return request.get<Map<string, object>>({
    url: `/api/tool/gen/${tableId}`
  })
}

/**
 * 查询数据表字段列表
 * @param tableId 表ID
 */
export function fetchGetColumnList(tableId: number) {
  return request.get<ApiResult<any[]>>({
    url: `/api/tool/gen/column/${tableId}`
  })
}
// 修改保存 编辑功能
type GenTableEditPayload = Api.Generator.GenTableListItem & {
  // 字段信息和生成配置等附加数据
  columns?: any[]
  options?: any
}
export function fetchEditSave(genTable: GenTableEditPayload) {
  return request.post<ApiResult<void>>({
    url: '/api/tool/gen/editSave',
    data: genTable
  })
}
/**
 * 删除表数据（支持单删和批量删除）
 * @param tableIds 表ID数组
 */
export function fetchDeleteTable(tableIds: number[]) {
  return request.del<ApiResult<void>>({
    url: '/api/tool/gen/removeGenTable',
    data: { ids: tableIds },
    showSuccessMessage: true
  })
}
//预览代码
export function fetchPreviewCode(tableId: number) {
  return request.get<Map<string, string>>({
    url: `/api/tool/gen/preview/${tableId}`
  })
}
//同步数据库
export function fetchSyncDatabase(tableName: string) {
  return request.get<ApiResult<void>>({
    url: `/api/tool/gen/syncDb/${tableName}`
  })
}
//生成代码（下载方式）
export function fetchGenerateCode(tableName: string) {
  return request.get<Blob>({
    url: `/api/tool/gen/download/${tableName}`,
    responseType: 'blob'
  })
}
//批量生成代码（下载方式）
export function fetchBatchGenerateCode(tableNames: string[]) {
  const tables = tableNames.join(',')
  return request.get<Blob>({
    url: `/api/tool/gen/batchGenCode`,
    params: { tables },
    responseType: 'blob'
  })
}

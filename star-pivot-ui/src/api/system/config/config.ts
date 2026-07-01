import request from '@/utils/http'
import {fetchExcelExport} from '@/api/common/excel'

/**
 * 参数配置实体类型
 */
export interface Config {
  configId?: number
  configName?: string
  configKey?: string
  configValue?: string
  configType?: string
  remark?: string
}

/**
 * 参数配置搜索参数
 */
export interface ConfigSearchParams {
  pageNum?: number
  pageSize?: number
  configName?: string
  configKey?: string
  configValue?: string
  configType?: string
}

/**
 * 获取参数配置列表（分页）
 */
export function fetchGetConfigList(params: ConfigSearchParams) {
  return request.post<Api.Common.PaginatedResponse<Config>>({
    url: '/api/config/configPageList',
    data: params
  })
}

/**
 * 根据ID获取参数配置详情
 */
export function fetchGetConfigById(configId: number) {
  return request.get<Config>({
    url: `/api/config/${configId}`
  })
}

/**
 * 新增参数配置
 */
export function fetchAddConfig(data: Config) {
  return request.post({
    url: '/api/config',
    data
  })
}

/**
 * 修改参数配置
 */
export function fetchUpdateConfig(data: Config) {
  return request.put({
    url: '/api/config',
    data
  })
}

/**
 * 删除参数配置（支持单删和批量删除，请求体 ids）
 */
export function fetchDeleteConfig(configIds: number[]) {
  return request.del({
    url: '/api/config/removeConfig',
    data: { ids: configIds }
  })
}

/** 导出参数配置 */
export function fetchExportConfig(params: ConfigSearchParams) {
  return fetchExcelExport({
    url: '/api/config/export',
    data: params as Record<string, unknown>,
    filenameFallback: `sys_config_export_${Date.now()}.xlsx`,
    successMessage: false
  })
}

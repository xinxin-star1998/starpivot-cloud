import request, {type BlobFullResponse} from '@/utils/http'
import {downloadBlob, getContentDisposition, getFilenameFromContentDisposition} from '@/utils/common/file'
import {ElMessage} from 'element-plus'

/**
 * EasyExcel 模块导入导出 API（multipart 上传 + blob 下载）
 * 各业务模块在 Controller 暴露 `/export`、`/import`、`/importTemplate`，由 {@link ExcelBizHandler} 对接 Service。
 */

export interface ExcelExportOptions {
  url: string
  data?: Record<string, unknown>
  method?: 'post' | 'get'
  /** 无法从响应头解析文件名时的回退名 */
  filenameFallback?: string
  /** 设为 false 则不弹出成功提示 */
  successMessage?: string | false
}

export interface ExcelImportOptions {
  url: string
  file: File
  updateSupport?: boolean
  /** 除 file、updateSupport 外的表单字段 */
  extraFormData?: Record<string, string | number | boolean>
}

export interface ExcelTemplateOptions {
  url: string
  params?: Record<string, unknown>
  filenameFallback?: string
  successMessage?: string | false
}

/** 导出 Excel 并触发浏览器下载 */
export async function fetchExcelExport(options: ExcelExportOptions) {
  const { url, data, method = 'post', filenameFallback, successMessage } = options
  const response = await request[method]<BlobFullResponse>({
    url,
    ...(method === 'get' ? { params: data } : { data: data ?? {} }),
    responseType: 'blob',
    returnFullResponse: true
  })
  const disposition = getContentDisposition(response.headers)
  const filename =
    getFilenameFromContentDisposition(disposition) || filenameFallback || 'export.xlsx'
  downloadBlob(response.data, filename)
  if (successMessage !== false) {
    ElMessage.success(successMessage ?? '导出成功')
  }
}

/** 上传 Excel 文件导入（返回业务 data，含 successCount / failCount） */
export function fetchExcelImport<T = ExcelImportResultVo>(options: ExcelImportOptions) {
  const { url, file, updateSupport = false, extraFormData } = options
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', String(updateSupport))
  if (extraFormData) {
    for (const [key, value] of Object.entries(extraFormData)) {
      formData.append(key, String(value))
    }
  }
  return request.post<T>({
    url,
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 与后端 ExcelImportResult 字段一致 */
export interface ExcelImportResultVo {
  successCount: number
  failCount: number
  errorMessages?: string[]
}

/** 下载导入模板 */
export async function fetchExcelTemplate(options: ExcelTemplateOptions) {
  const { url, params, filenameFallback, successMessage } = options
  const response = await request.get<BlobFullResponse>({
    url,
    params,
    responseType: 'blob',
    returnFullResponse: true
  })
  const disposition = getContentDisposition(response.headers)
  const filename =
    getFilenameFromContentDisposition(disposition) || filenameFallback || 'import_template.xlsx'
  downloadBlob(response.data, filename)
  if (successMessage !== false) {
    ElMessage.success(successMessage ?? '模板下载成功')
  }
}

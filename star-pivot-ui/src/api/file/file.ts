import request from '@/utils/http'
import type {
  SysFile,
  SysFileQueryParams,
  SysFileRecycleQueryParams,
  SysFileRenameParams
} from './types'

export function fetchFileList(params: SysFileQueryParams) {
  return request.post<Api.Common.PageResponse<SysFile>>({
    url: '/api/file/list',
    data: params
  })
}

export function fetchFileDetail(fileId: number) {
  return request.get<SysFile>({
    url: `/api/file/${fileId}`
  })
}

export function uploadFile(formData: FormData) {
  return request.post<SysFile>({
    url: '/api/file/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function fetchFilePreviewUrl(fileId: number) {
  return request.get<{ url: string; objectName: string }>({
    url: `/api/file/preview-url/${fileId}`
  })
}

export function deleteFiles(ids: number[]) {
  return request.del({
    url: '/api/file/remove',
    data: { ids }
  })
}

export function restoreFiles(ids: number[]) {
  return request.put({
    url: '/api/file/restore',
    data: { ids }
  })
}

export function fetchRecycleList(params: SysFileRecycleQueryParams) {
  return request.post<Api.Common.PageResponse<SysFile>>({
    url: '/api/file/recycle/list',
    data: params
  })
}

export function moveFiles(ids: number[], targetFolderId: number) {
  return request.put({
    url: '/api/file/move',
    data: { ids, targetFolderId }
  })
}

export function renameFile(data: SysFileRenameParams) {
  return request.put({
    url: '/api/file/rename',
    data
  })
}

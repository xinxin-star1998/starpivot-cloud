import request from '@/utils/http'
import type { FileCategoryNode, SysFileFolderForm } from './types'

export function fetchFolderTree(category?: string) {
  return request.get<FileCategoryNode[]>({
    url: '/api/file/folder/tree',
    params: category ? { category } : undefined
  })
}

export function createFolder(data: SysFileFolderForm) {
  return request.post<number>({
    url: '/api/file/folder',
    data
  })
}

export function updateFolder(data: SysFileFolderForm) {
  return request.put({
    url: '/api/file/folder',
    data
  })
}

export function deleteFolder(folderId: number) {
  return request.del({
    url: `/api/file/folder/${folderId}`
  })
}

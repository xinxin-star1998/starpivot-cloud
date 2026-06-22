/** 文件中心类型定义 */

export interface SysFileFolder {
  folderId?: number
  category?: string
  folderName?: string
  parentId?: number
  orderNum?: number
  status?: string
  fileCount?: number
}

export interface FileCategoryNode {
  category: string
  categoryLabel: string
  defaultFolderId?: number
  children: SysFileFolder[]
}

export interface SysFile {
  fileId?: number
  folderId?: number
  category?: string
  categoryLabel?: string
  mediaType?: string
  mediaTypeLabel?: string
  fileName?: string
  fileExt?: string
  contentType?: string
  fileSize?: number
  objectName?: string
  storageProvider?: string
  bizType?: string
  bizId?: string
  displayUrl?: string
  previewMode?: 'image' | 'video' | 'audio' | 'pdf' | 'download'
  createBy?: string
  createTime?: string
  remark?: string
  folderName?: string
  updateBy?: string
  updateTime?: string
  deleteBy?: string
  deleteTime?: string
}

export interface SysFileQueryParams {
  pageNum?: number
  pageSize?: number
  folderId?: number
  category?: string
  mediaType?: string
  fileName?: string
  createBy?: string
  beginTime?: string
  endTime?: string
}

export interface SysFileRecycleQueryParams {
  pageNum?: number
  pageSize?: number
  category?: string
  fileName?: string
  deleteBy?: string
  beginTime?: string
  endTime?: string
}

export interface SysFileFolderForm {
  folderId?: number
  category?: string
  folderName?: string
  orderNum?: number
  status?: string
  remark?: string
}

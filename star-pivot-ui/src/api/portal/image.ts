import request from '@/utils/http'

export interface PortalImageUploadResult {
  objectName: string
  displayUrl: string
  permanentUrl?: string
  presignedUrl?: string
}

/** C 端评价晒图上传 */
export function uploadPortalCommentImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<PortalImageUploadResult>({
    url: '/api/portal/image/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    // @ts-expect-error showLoading 运行时支持
    showLoading: false,
    showErrorMessage: true
  })
}

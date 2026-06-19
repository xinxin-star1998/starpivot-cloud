import request from '@/utils/http'

/** 品牌 Logo 上传响应（与后端 FileUploadVO 对齐） */
export interface BrandImageUploadVO {
  objectName: string
  displayUrl: string
  permanentUrl?: string
  presignedUrl?: string
}

/** 上传品牌 Logo */
export function uploadBrandLogo(file: File, brandId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (brandId != null) {
    formData.append('brandId', String(brandId))
  }
  return request.post<BrandImageUploadVO>({
    url: '/api/mall/brand/image/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    // @ts-expect-error showLoading 运行时支持
    showLoading: false,
    showErrorMessage: true
  })
}

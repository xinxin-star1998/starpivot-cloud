import request from '@/utils/http'

/** 商品图片上传响应（与后端 FileUploadVO 对齐） */
export interface GoodsImageUploadVO {
  /** OSS 对象路径，存库用 */
  objectName: string
  /** 前端 img src */
  displayUrl: string
  permanentUrl?: string
  presignedUrl?: string
}

/** 上传商品图片（走文件中心路径，兼容旧接口） */
export function uploadGoodsImage(file: File, goodsId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (goodsId != null) {
    formData.append('goodsId', String(goodsId))
  }
  return request.post<GoodsImageUploadVO>({
    url: '/api/mall/goods/image/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    // @ts-expect-error showLoading 运行时支持
    showLoading: false,
    showErrorMessage: true
  })
}

/** 批量获取预签名 URL */
export function fetchGoodsImagePresignedUrls(objectNames: string[]) {
  return request.post<Record<string, string>>({
    url: '/api/common/upload/presigned-urls',
    data: objectNames,
    showErrorMessage: false
  })
}

/** @deprecated 请使用 fetchGoodsImagePresignedUrls */
export function fetchPresignedUrl(objectName: string) {
  return request.get<{ url: string; objectName: string }>({
    url: '/api/common/upload/presigned-url',
    params: { objectName },
    showErrorMessage: false
  })
}

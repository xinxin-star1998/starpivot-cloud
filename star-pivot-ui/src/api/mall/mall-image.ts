import { uploadFile } from '@/api/file/file'
import request from '@/utils/http'

/** 文件中心「商品素材」默认文件夹 ID */
export const MALL_GOODS_FOLDER_ID = 7

/** 商城图片上传响应（与后端 FileUploadVO 对齐） */
export interface MallImageUploadVO {
  /** OSS 对象路径，存库用（file/goods/...） */
  objectName: string
  /** 前端 img src */
  displayUrl: string
  permanentUrl?: string
  presignedUrl?: string
}

/** 商城通用图片上传：优先文件中心（写入素材库），失败时回退商城接口 */
export async function uploadMallImage(file: File): Promise<MallImageUploadVO> {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folderId', String(MALL_GOODS_FOLDER_ID))
    formData.append('bizType', 'mall')
    const sysFile = await uploadFile(formData)
    if (sysFile?.objectName) {
      return {
        objectName: sysFile.objectName,
        displayUrl: sysFile.displayUrl || '',
        presignedUrl: sysFile.displayUrl
      }
    }
  } catch {
    // 无文件中心权限或文件服务不可用时，回退商城上传接口
  }

  const formData = new FormData()
  formData.append('file', file)
  return request.post<MallImageUploadVO>({
    url: '/api/mall/image/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    // @ts-expect-error showLoading 运行时支持
    showLoading: false,
    showErrorMessage: true
  })
}

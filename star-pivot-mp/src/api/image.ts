import {API_BASE_URL} from '@/config'
import {getToken} from '@/stores/member'
import {request} from '@/utils/request'
import type {PortalImageUploadResult} from '@/api/types'

/** 批量获取商品/营销图展示 URL（C 端公开接口） */
export function fetchImagePresignedUrls(objectNames: string[]) {
  return request<Record<string, string>>({
    url: '/portal/home/presigned-urls',
    method: 'POST',
    data: objectNames,
    auth: false
  })
}

interface ApiResult<T> {
  code: number
  message?: string
  data: T
}

export function uploadImage(filePath: string): Promise<PortalImageUploadResult> {
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${API_BASE_URL}/portal/image/upload`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data) as ApiResult<PortalImageUploadResult>
          if (body.code !== 200) {
            reject(new Error(body.message || '上传失败'))
            return
          }
          resolve(body.data)
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => reject(new Error(err.errMsg || '上传失败'))
    })
  })
}

import {API_BASE_URL} from '@/config'
import {clearSession, getToken} from '@/stores/member'

interface ApiResult<T> {
  code: number
  message?: string
  data: T
}

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown> | unknown
  auth?: boolean
}

export function request<T>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, auth = true } = options
  const header: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  const token = getToken()
  if (auth && token) {
    header.Authorization = `Bearer ${token}`
  }

  const isGet = method === 'GET'
  const query = isGet && data && typeof data === 'object' ? buildQuery(data as Record<string, unknown>) : ''
  const requestUrl = `${API_BASE_URL}${url}${query}`

  return new Promise((resolve, reject) => {
    uni.request({
      url: requestUrl,
      method,
      data: isGet ? undefined : data,
      header,
      timeout: 15000,
      success: (res) => {
        const body = res.data as ApiResult<T>
        if (res.statusCode === 401) {
          if (auth) {
            clearSession()
          }
          reject(new Error(auth ? '登录已过期' : '请求未授权'))
          return
        }
        if (!body || body.code !== 200) {
          reject(new Error(body?.message || '请求失败'))
          return
        }
        resolve(body.data)
      },
      fail: (err) => {
        const msg = err.errMsg || ''
        if (/fail|refused|timeout|connect/i.test(msg)) {
          reject(
            new Error(
              `无法连接后端 ${API_BASE_URL}，请先启动 Docker 与网关（localhost:8080）`
            )
          )
          return
        }
        reject(new Error(msg || '网络错误'))
      }
    })
  })
}

function buildQuery(params: Record<string, unknown>) {
  const parts = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
  return parts.length ? `?${parts.join('&')}` : ''
}

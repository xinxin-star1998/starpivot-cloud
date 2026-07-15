/**
 * HTTP 请求封装模块
 * 基于 Axios 封装的 HTTP 请求工具，提供统一的请求/响应处理
 *
 * ## 主要功能
 *
 * - 请求/响应拦截器（自动添加 Token、统一错误处理）
 * - 401 未授权自动登出（带防抖机制）
 * - 请求失败自动重试（可配置）
 * - 统一的成功/错误消息提示
 * - 支持 GET/POST/PUT/DELETE 等常用方法
 *
 * @module utils/http
 * @author Art Design Pro Team
 */

import axios, {AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig} from 'axios'
import {useUserStore} from '@/store/modules/user'
import {usePortalMemberStore} from '@/store/modules/portal-member'
import {ApiStatus} from './status'
import {handleError, HttpError, showError, showSuccess} from './error'
import {$t} from '@/locales'
import {BaseResponse} from '@/types'
import {fetchRefreshToken} from '@/api/auth'
import {logger} from '@/utils/sys/logger'
import {normalizeRequestUrl as stripDuplicateApiPrefix} from './api-path'

/** 请求配置常量 */
const REQUEST_TIMEOUT = 15000
const LOGOUT_DELAY = 500
const MAX_RETRIES = 3 // 最大重试次数，0表示不重试
const RETRY_DELAY = 1000 // 重试延迟（毫秒）
const UNAUTHORIZED_DEBOUNCE_TIME = 3000

/** 登录/验证码等入口接口：401 属于业务失败，不应触发刷新令牌或登出 */
function isAuthEntryRequest(url?: string): boolean {
  if (!url) return false
  const authEntryPaths = [
    '/auth/login',
    '/auth/captcha',
    '/auth/register',
    '/auth/register/enabled',
    '/auth/refresh'
  ]
  return authEntryPaths.some((path) => url.includes(path))
}

function isPortalApiRequest(url?: string): boolean {
  return !!url && url.includes('/portal/')
}

function isPortalAuthEntryRequest(url?: string): boolean {
  if (!url) return false
  return (
    url.includes('/portal/member/register') ||
    url.includes('/portal/member/login') ||
    url.includes('/portal/auth/sms/login') ||
    url.includes('/portal/auth/login/password') ||
    url.includes('/portal/auth/wechat/login')
  )
}

/** C 端公开接口：无需会员 Token（与网关白名单 / MallSecurityConfig permitAll 对齐） */
function isPortalPublicRequest(url?: string): boolean {
  if (!url) return false
  return (
    url.includes('/portal/home') ||
    url.includes('/portal/product/') ||
    url.includes('/portal/region/') ||
    url.includes('/portal/auth/config')
  )
}

/** 401防抖状态 */
let isUnauthorizedErrorShown = false
let unauthorizedTimer: ReturnType<typeof setTimeout> | null = null

/** 刷新令牌状态管理 */
let isRefreshing = false // 是否正在刷新令牌
let refreshSubscribers: Array<(token: string) => void> = [] // 等待刷新完成的请求队列
let refreshRetryCount = 0 // 刷新令牌重试计数
const MAX_REFRESH_RETRIES = 1 // 最大刷新重试次数

/** 请求去重：存储正在进行的请求，避免重复请求 */
const pendingRequests = new Map<string, Promise<any>>()

/**
 * 订阅刷新完成事件
 * @param cb 刷新完成后的回调函数
 */
function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

/**
 * 通知所有等待的请求刷新已完成
 * @param token 新的访问令牌
 */
function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

/** 刷新失败时清空等待队列，避免挂起请求与下次刷新误通知 */
function clearRefreshSubscribers() {
  refreshSubscribers = []
}

/** 重置刷新令牌状态（登出 / 重新登录后应调用，避免计数残留导致跳过刷新） */
export function resetTokenRefreshState() {
  isRefreshing = false
  refreshRetryCount = 0
  clearRefreshSubscribers()
}

/**
 * 尝试刷新访问令牌
 * @returns 刷新成功返回新的访问令牌，失败返回 null
 */
async function tryRefreshToken(): Promise<string | null> {
  const userStore = useUserStore()
  const refreshToken = userStore.refreshToken
  const username = userStore.getUserInfo.user?.username
  const deviceSessionId = userStore.sessionId

  // 如果没有刷新令牌或用户名，无法刷新
  if (!refreshToken || !username) {
    return null
  }

  try {
    const response = await fetchRefreshToken({
      username,
      refreshToken,
      deviceSessionId: deviceSessionId || undefined
    })

    const {
      token: newAccessToken,
      refreshToken: newRefreshToken,
      deviceSessionId: newDeviceSessionId
    } = response

    if (newAccessToken) {
      // 更新令牌（同时更新访问令牌和刷新令牌）
      userStore.setToken(newAccessToken, newRefreshToken, newDeviceSessionId)
      return newAccessToken
    }

    return null
  } catch (error) {
    if (import.meta.env.DEV) {
      logger.error('[HTTP] 刷新令牌失败:', error)
    }
    return null
  }
}

/** blob 请求时返回的完整响应（含 headers，用于解析 Content-Disposition 等） */
export interface BlobFullResponse {
  data: Blob
  headers: Record<string, string>
}

/** 扩展 AxiosRequestConfig */
interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  url: string
  params?: object
  data?: object
  showErrorMessage?: boolean
  showSuccessMessage?: boolean
  /** blob 请求时是否返回 { data, headers }，便于解析 Content-Disposition 等响应头 */
  returnFullResponse?: boolean
}

const { VITE_WITH_CREDENTIALS } = import.meta.env

/**
 * 获取 API 基础地址（优先使用部署后的运行时配置，无需重新打包）
 * 部署后修改 public/config.js 中的 VITE_API_URL 即可生效
 */
export function getApiBaseUrl(): string {
  if (typeof window !== 'undefined' && window.__APP_RUNTIME_CONFIG__?.VITE_API_URL !== undefined) {
    return window.__APP_RUNTIME_CONFIG__.VITE_API_URL
  }
  return (import.meta.env.VITE_API_URL as string) || ''
}

const VITE_API_URL = getApiBaseUrl()

/** Axios实例 */
const axiosInstance = axios.create({
  timeout: REQUEST_TIMEOUT,
  baseURL: VITE_API_URL,
  withCredentials: VITE_WITH_CREDENTIALS === 'true',
  validateStatus: (status) => status >= 200 && status < 300,
  transformResponse: [
    (data, headers) => {
      const contentType = headers['content-type']
      if (contentType?.includes('application/json')) {
        try {
          return JSON.parse(data)
        } catch {
          return data
        }
      }
      return data
    }
  ]
})

/** 见 api-path.ts：剥离 url 中与 baseURL 重复的 /api 前缀 */
function normalizeRequestUrl(url: string): string {
  return stripDuplicateApiPrefix(url, getApiBaseUrl())
}

/** 请求拦截器 */
axiosInstance.interceptors.request.use(
  (request: InternalAxiosRequestConfig) => {
    if (typeof request.url === 'string') {
      request.url = normalizeRequestUrl(request.url)
    }
    const requestUrl = typeof request.url === 'string' ? request.url : ''
    const userStore = useUserStore()
    const portalStore = usePortalMemberStore()
    const portalToken = portalStore.token
    const adminToken = userStore.accessToken

    if (isPortalApiRequest(request.url) && portalToken && !isPortalPublicRequest(request.url)) {
      request.headers.set(
        'Authorization',
        portalToken.startsWith('Bearer ') ? portalToken : `Bearer ${portalToken}`
      )
    } else if (!isPortalApiRequest(request.url) && adminToken) {
      request.headers.set(
        'Authorization',
        adminToken.startsWith('Bearer ') ? adminToken : `Bearer ${adminToken}`
      )
    }

    // 设置 Content-Type（仅当未设置且数据不是 FormData 时）
    // 注意：axios 会自动将 JavaScript 对象序列化为 JSON，无需手动 stringify
    if (request.data && !(request.data instanceof FormData) && !request.headers['Content-Type']) {
      // 如果 data 已经是字符串，直接使用；否则让 axios 自动处理
      if (typeof request.data === 'string') {
        request.headers.set('Content-Type', 'application/json')
      } else {
        // axios 会自动处理对象类型的数据，设置 Content-Type
        request.headers.set('Content-Type', 'application/json')
      }
    }

    return request
  },
  (error) => {
    showError(createHttpError($t('httpMsg.requestConfigError'), ApiStatus.error))
    return Promise.reject(error)
  }
)

/** 响应拦截器 */
axiosInstance.interceptors.response.use(
  (response: AxiosResponse<BaseResponse | Blob>) => {
    // 如果是 blob 响应类型，直接返回响应，不进行 JSON 解析
    if (response.config.responseType === 'blob' || response.data instanceof Blob) {
      return response
    }

    // JSON 响应类型，进行常规处理
    const data = response.data as BaseResponse
    const { code, message } = data
    const messageText = message || $t('httpMsg.requestFailed')
    if (code === ApiStatus.success) return response

    // 业务层返回 401（HTTP 状态码可能是 200，但业务 code 是 401）
    if (code === ApiStatus.unauthorized) {
      const originalRequest = response.config as InternalAxiosRequestConfig & { _retry?: boolean }

      if (isAuthEntryRequest(originalRequest.url) || isPortalAuthEntryRequest(originalRequest.url)) {
        throw createHttpError(messageText, code)
      }

      if (isPortalApiRequest(originalRequest.url)) {
        usePortalMemberStore().logOut()
        throw createHttpError(messageText, code)
      }

      // 如果未重试过，尝试刷新令牌
      if (!originalRequest._retry) {
        return handleTokenRefresh(originalRequest)
          .then(() => {
            // 刷新成功，重试原请求
            return axiosInstance(originalRequest) as Promise<AxiosResponse<BaseResponse | Blob>>
          })
          .catch(() => {
            // 刷新失败，执行登出流程
            handleUnauthorizedError(messageText)
            throw createHttpError(messageText, code)
          }) as Promise<AxiosResponse<BaseResponse | Blob>>
      } else {
        // 已经重试过，直接处理为未授权错误
        handleUnauthorizedError(messageText)
        throw createHttpError(messageText, code)
      }
    }

    throw createHttpError(messageText, code)
  },
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // C 端商城：会员 Token 无刷新机制，401 时清除会员态，不走后台 refresh
    if (
      error.response?.status === ApiStatus.unauthorized &&
      originalRequest &&
      isPortalApiRequest(originalRequest.url) &&
      !isPortalAuthEntryRequest(originalRequest.url)
    ) {
      usePortalMemberStore().logOut()
      return Promise.reject(handleError(error))
    }

    // HTTP 状态码为 401 且未重试过，尝试刷新令牌（仅后台管理端）
    if (
      error.response?.status === ApiStatus.unauthorized &&
      originalRequest &&
      !originalRequest._retry &&
      !isAuthEntryRequest(originalRequest.url)
    ) {
      try {
        await handleTokenRefresh(originalRequest)
        // 刷新成功，重试原请求
        return axiosInstance(originalRequest)
      } catch {
        // 刷新失败，执行登出流程
        handleUnauthorizedError()
        return Promise.reject(handleError(error))
      }
    }

    return Promise.reject(handleError(error))
  }
)

/**
 * 处理令牌刷新逻辑
 * @param originalRequest 原始请求配置
 * @returns Promise<void>
 */
async function handleTokenRefresh(
  originalRequest: InternalAxiosRequestConfig & { _retry?: boolean }
): Promise<void> {
  if (refreshRetryCount >= MAX_REFRESH_RETRIES) {
    return Promise.reject(new Error('刷新令牌重试次数已达上限'))
  }

  // 如果正在刷新，将当前请求加入等待队列
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      subscribeTokenRefresh((token: string) => {
        originalRequest.headers.set('Authorization', `Bearer ${token}`)
        resolve()
      })
      setTimeout(() => {
        reject(new Error('刷新令牌超时'))
      }, 10000)
    })
  }

  isRefreshing = true
  originalRequest._retry = true

  try {
    const newToken = await tryRefreshToken()

    if (newToken) {
      originalRequest.headers.set('Authorization', `Bearer ${newToken}`)
      onRefreshed(newToken)
      isRefreshing = false
      refreshRetryCount = 0
      return Promise.resolve()
    }
    isRefreshing = false
    refreshRetryCount++
    clearRefreshSubscribers()
    return Promise.reject(new Error('刷新令牌失败'))
  } catch (error) {
    isRefreshing = false
    refreshRetryCount++
    clearRefreshSubscribers()
    return Promise.reject(error)
  }
}

/** 统一创建HttpError */
function createHttpError(message: string, code: number) {
  return new HttpError(message, code)
}

/** 处理401错误（带防抖） */
function handleUnauthorizedError(message?: string): never {
  const error = createHttpError(message || $t('httpMsg.unauthorized'), ApiStatus.unauthorized)

  if (!isUnauthorizedErrorShown) {
    isUnauthorizedErrorShown = true
    logOut()

    unauthorizedTimer = setTimeout(resetUnauthorizedError, UNAUTHORIZED_DEBOUNCE_TIME)

    showError(error, true)
    throw error
  }

  throw error
}

/** 重置401防抖状态 */
function resetUnauthorizedError() {
  isUnauthorizedErrorShown = false
  if (unauthorizedTimer) clearTimeout(unauthorizedTimer)
  unauthorizedTimer = null
}

/** 退出登录函数 */
function logOut() {
  resetTokenRefreshState()
  setTimeout(() => {
    useUserStore().logOut()
  }, LOGOUT_DELAY)
}

/** 是否需要重试 */
function shouldRetry(statusCode: number) {
  return [
    ApiStatus.requestTimeout,
    ApiStatus.internalServerError,
    ApiStatus.badGateway,
    ApiStatus.serviceUnavailable,
    ApiStatus.gatewayTimeout
  ].includes(statusCode)
}

/** 请求重试逻辑（使用指数退避策略） */
async function retryRequest<T>(
  config: ExtendedAxiosRequestConfig,
  retries: number = MAX_RETRIES,
  baseDelay: number = RETRY_DELAY
): Promise<T> {
  try {
    return await executeRequest<T>(config)
  } catch (error) {
    if (retries > 0 && error instanceof HttpError && shouldRetry(error.code)) {
      // 指数退避：延迟时间 = 基础延迟 * 2^(总重试次数 - 当前剩余重试次数)
      const delayTime = baseDelay * Math.pow(2, MAX_RETRIES - retries)
      await delay(delayTime)
      return retryRequest<T>(config, retries - 1, baseDelay)
    }
    throw error
  }
}

/** 延迟函数 */
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 生成请求唯一标识（用于请求去重）
 * @param config 请求配置
 * @returns 请求唯一key
 */
function generateRequestKey(config: ExtendedAxiosRequestConfig): string {
  const method = (config.method || 'GET').toUpperCase()
  const url = config.url || ''
  // FormData 无法稳定序列化，禁止去重，避免并发上传共用同一 Promise
  if (config.data instanceof FormData) {
    return `${method}_${url}_form_${Date.now()}_${Math.random()}`
  }
  const paramsStr = config.params
    ? JSON.stringify(config.params, Object.keys(config.params).sort())
    : ''
  const dataStr = config.data
    ? JSON.stringify(config.data, Object.keys(config.data as object).sort())
    : ''
  return `${method}_${url}_${paramsStr}_${dataStr}`
}

/** 请求函数（带请求去重） */
async function executeRequest<T = any>(config: ExtendedAxiosRequestConfig): Promise<T> {
  // POST | PUT 参数自动填充
  if (
    ['POST', 'PUT'].includes(config.method?.toUpperCase() || '') &&
    config.params &&
    !config.data
  ) {
    config.data = config.params
    config.params = undefined
  }

  // 请求去重：如果存在相同的pending请求，直接返回该Promise
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    return pendingRequests.get(requestKey) as Promise<T>
  }

  // 创建新的请求Promise
  const requestPromise = (async (): Promise<T> => {
    try {
      const res = await axiosInstance.request<BaseResponse<T> | Blob>(config)

      // 如果是 blob 响应类型
      if (config.responseType === 'blob' || res.data instanceof Blob) {
        if (config.returnFullResponse) {
          const headers: Record<string, string> = {}
          const h = res.headers as Record<string, unknown>
          if (h && typeof h === 'object') {
            for (const k of Object.keys(h)) {
              const v = h[k]
              if (typeof v === 'string') headers[k] = v
              else if (Array.isArray(v) && v.length) headers[k] = String(v[0])
            }
          }
          return { data: res.data as Blob, headers } as T
        }
        return res.data as T
      }

      // JSON 响应类型，进行常规处理
      const jsonData = res.data as BaseResponse<T>

      // 显示成功消息
      if (config.showSuccessMessage) {
        const successMsg = jsonData.message
        if (successMsg) {
          showSuccess(successMsg)
        }
      }

      return jsonData.data as T
    } catch (error) {
      if (error instanceof HttpError && error.code !== ApiStatus.unauthorized) {
        const showMsg = config.showErrorMessage !== false
        showError(error, showMsg)
      }
      return Promise.reject(error)
    } finally {
      // 请求完成（成功或失败）后，从pendingRequests中移除
      pendingRequests.delete(requestKey)
    }
  })()

  // 将请求Promise存储到Map中
  pendingRequests.set(requestKey, requestPromise)

  return requestPromise
}

/** API方法集合（写操作不重试，避免重复提交） */
const api = {
  get<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'GET' })
  },
  post<T>(config: ExtendedAxiosRequestConfig) {
    return executeRequest<T>({ ...config, method: 'POST' })
  },
  put<T>(config: ExtendedAxiosRequestConfig) {
    return executeRequest<T>({ ...config, method: 'PUT' })
  },
  del<T>(config: ExtendedAxiosRequestConfig) {
    return executeRequest<T>({ ...config, method: 'DELETE' })
  },
  /** {@link del} 的别名，兼容 axios 风格调用 */
  delete<T>(config: ExtendedAxiosRequestConfig) {
    return executeRequest<T>({ ...config, method: 'DELETE' })
  },
  request<T>(config: ExtendedAxiosRequestConfig) {
    const method = (config.method || 'GET').toUpperCase()
    if (method === 'GET') {
      return retryRequest<T>(config)
    }
    return executeRequest<T>(config)
  }
}

export { isUserCancelError, handleMutationError } from './mutation'
export type { HandleMutationErrorOptions } from './mutation'
export { HttpError, isHttpError, showError, showSuccess } from './error'

export default api

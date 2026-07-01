import request from '@/utils/http'

/**
 * 登录
 * @param data 登录参数
 * @returns 登录响应
 */
export function fetchLogin(data: Api.Auth.LoginParams) {
  const { username, password, captchaToken, captcha } = data

  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/login',
    data: {
      username,
      password,
      captchaToken,
      captcha
    }
    // showSuccessMessage: true // 显示成功消息
    // showErrorMessage: false // 不显示错误消息
  })
}

/**
 * 用户注册
 * @param data 注册参数
 * @returns 注册响应结果
 */
export function fetchRegister(data: Api.Auth.RegisterParams) {
  const { username, password } = data

  return request.post<Api.Auth.RegisterResponse>({
    url: '/api/auth/register',
    data: {
      username,
      password
    }
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/auth/user/info'
    // 自定义请求头
    // headers: {
    //   'X-Custom-Header': 'your-custom-value'
    // }
  })
}

/**
 * 登出
 * @returns 登出响应
 */
export function fetchLogout() {
  return request.post({
    url: '/api/auth/logout'
  })
}

/**
 * 获取验证码
 * @returns 验证码响应
 */
export function fetchCaptcha(scene = 'login') {
  return request.get<Api.Auth.CaptchaResponse>({
    url: '/api/auth/captcha',
    params: { scene }
  })
}

/**
 * 刷新访问令牌
 * @param data 刷新令牌请求参数
 * @returns 新的登录响应（包含新的访问令牌和刷新令牌）
 */
export function fetchRefreshToken(data: {
  username: string
  refreshToken: string
  deviceSessionId?: string
}) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/refresh',
    data,
    showErrorMessage: false // 刷新失败时不显示错误消息，由拦截器统一处理
  })
}

/**
 * 查询用户注册是否开放（读取 sys.account.registerUser）
 */
export async function fetchRegisterEnabled(): Promise<boolean> {
  const response = await request.get<Api.Auth.RegisterConfigResponse>({
    url: '/api/auth/register/enabled',
    showErrorMessage: false
  })
  return response.registerEnabled
}

export async function fetchForgetPasswordEnabled(): Promise<boolean> {
  const response = await request.get<{ forgetPasswordEnabled: boolean }>({
    url: '/api/auth/forgot-password/enabled',
    showErrorMessage: false
  })
  return response.forgetPasswordEnabled
}

export function fetchForgotPassword(data: {
  username: string
  password: string
  captchaToken: string
  captcha: string
}) {
  return request.post({
    url: '/api/auth/forgot-password',
    data
  })
}

/**
 * 获取用户活跃会话列表
 * @param userId 用户ID
 */
export function fetchUserSessions(userId: number) {
  return request.get<Api.Auth.DeviceSession[]>({
    url: `/api/auth/sessions/user/${userId}`
  })
}

/**
 * 强制下线指定会话
 * @param userId 用户ID
 * @param deviceSessionId 设备会话ID
 */
export function forceLogoutSession(userId: number, deviceSessionId: string) {
  return request.del({
    url: `/api/auth/sessions/${userId}/${deviceSessionId}`
  })
}

/**
 * 强制下线用户所有会话
 * @param userId 用户ID
 */
export function forceLogoutAllSessions(userId: number) {
  return request.del({
    url: `/api/auth/sessions/all/${userId}`
  })
}

import { defineStore } from 'pinia'
import { useUserStore } from './user'

/**
 * 认证相关状态（轻量门面）
 *
 * 说明：
 * - 当前实现基于 useUserStore 进行封装，后续可逐步将底层 state 拆离
 * - 新代码推荐优先依赖 useAuthStore，而不是直接依赖 useUserStore 的认证字段
 */
export const useAuthStore = defineStore('authStore', () => {
  const userStore = useUserStore()

  const isLogin = computed(() => userStore.isLogin)
  const accessToken = computed(() => userStore.accessToken)
  const refreshToken = computed(() => userStore.refreshToken)
  const sessionId = computed(() => userStore.sessionId)
  const userInfo = computed(() => userStore.getUserInfo)

  const setToken = (token: string, refresh?: string, session?: string) => {
    userStore.setToken(token, refresh, session)
  }
  const setUserInfo = (info: Partial<Api.Auth.UserInfo>) => {
    userStore.setUserInfo(info)
  }

  const setLoginStatus = (status: boolean) => {
    userStore.setLoginStatus(status)
  }

  const setSessionId = (session: string) => {
    userStore.setSessionId(session)
  }

  const logOut = () => {
    userStore.logOut()
  }

  return {
    // 状态
    isLogin,
    accessToken,
    refreshToken,
    sessionId,
    userInfo,
    // 行为
    setToken,
    setUserInfo,
    setLoginStatus,
    setSessionId,
    logOut
  }
})

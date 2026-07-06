/**
 * 用户状态管理模块
 *
 * 提供用户相关的状态管理
 *
 * ## 主要功能
 *
 * - 用户登录状态管理
 * - 用户信息存储
 * - 访问令牌和刷新令牌管理
 * - 语言设置
 * - 搜索历史记录
 * - 锁屏状态和密码管理
 * - 登出清理逻辑
 *
 * ## 使用场景
 *
 * - 用户登录和认证
 * - 权限验证
 * - 个人信息展示
 * - 多语言切换
 * - 锁屏功能
 * - 搜索历史管理
 *
 * ## 持久化
 *
 * - 使用 localStorage 存储
 * - 存储键：sys-v{version}-user
 * - 登出时自动清理
 *
 * @module store/modules/user
 * @author Art Design Pro Team
 */
import {defineStore} from 'pinia'
import {LanguageEnum} from '@/enums/appEnum'
import {router} from '@/router'
import {useSettingStore} from './setting'
import {useWorktabStore} from './worktab'
import {AppRouteRecord} from '@/types/router'
import {setPageTitle} from '@/utils/router'
import {resetRouterState} from '@/router/guards/dynamicRouteGuard'
import {useMenuStore} from './menu'
import {StorageConfig} from '@/utils/storage/storage-config'
import {logger} from '@/utils/sys/logger'
import {clearAiSessionStorage} from '@/utils/ai/session-storage'

/**
 * 清理 Pinia 持久化存储（只清登录态相关）
 * 说明：
 * - 项目使用版本化 key：sys-v{version}-{storeId}
 * - 部分 store 内又额外声明了 persist.key（如 user/worktab），为兼容两种情况都做清理
 */
function clearAuthRelatedPersistedStorage() {
  const storeIds = ['userStore', 'worktabStore', 'menuStore', 'user', 'worktab', 'menu']
  storeIds.forEach((id) => {
    try {
      localStorage.removeItem(StorageConfig.generateStorageKey(id))
    } catch (error) {
      // 清理失败不应阻塞登出流程，仅记录警告
      logger.warn(`[UserStore] 清理持久化存储失败，storeId: ${id}`, error)
    }
  })
}

/**
 * 用户状态管理
 * 管理用户登录状态、个人信息、语言设置、搜索历史、锁屏状态等
 */
export const useUserStore = defineStore(
  'userStore',
  () => {
    // 语言设置
    const language = ref(LanguageEnum.ZH)
    // 登录状态
    const isLogin = ref(false)
    // 锁屏状态
    const isLock = ref(false)
    // 锁屏密码
    const lockPassword = ref('')
    // 用户信息
    const info = ref<Partial<Api.Auth.UserInfo>>({})
    // 搜索历史记录
    const searchHistory = ref<AppRouteRecord[]>([])
    // 访问令牌
    const accessToken = ref('')
    // 刷新令牌
    const refreshToken = ref('')
    // 当前设备会话ID
    const sessionId = ref('')

    // 计算属性：获取用户信息
    const getUserInfo = computed(() => info.value)
    // 计算属性：获取设置状态
    const getSettingState = computed(() => useSettingStore().$state)
    // 计算属性：获取工作台状态
    const getWorktabState = computed(() => useWorktabStore().$state)
    // 计算属性：获取当前会话ID
    const getSessionId = computed(() => sessionId.value)

    /**
     * 设置用户信息
     * @param newInfo 新的用户信息（可以是部分字段）
     */
    const setUserInfo = (newInfo: Partial<Api.Auth.UserInfo>) => {
      info.value = { ...info.value, ...newInfo }
    }

    /**
     * 设置登录状态
     * @param status 登录状态
     */
    const setLoginStatus = (status: boolean) => {
      isLogin.value = status
    }

    /**
     * 设置语言
     * @param lang 语言枚举值
     */
    const setLanguage = (lang: LanguageEnum) => {
      setPageTitle(router.currentRoute.value)
      language.value = lang
    }

    /**
     * 设置搜索历史
     * @param list 搜索历史列表
     */
    const setSearchHistory = (list: AppRouteRecord[]) => {
      searchHistory.value = list
    }

    /**
     * 设置锁屏状态
     * @param status 锁屏状态
     */
    const setLockStatus = (status: boolean) => {
      isLock.value = status
    }

    /**
     * 设置锁屏密码
     * @param password 锁屏密码
     */
    const setLockPassword = (password: string) => {
      lockPassword.value = password
    }

    /**
     * 设置令牌
     * @param newAccessToken 访问令牌
     * @param newRefreshToken 刷新令牌（可选）
     * @param newSessionId 设备会话ID（可选）
     */
    const setToken = (newAccessToken: string, newRefreshToken?: string, newSessionId?: string) => {
      accessToken.value = newAccessToken
      if (newRefreshToken) {
        refreshToken.value = newRefreshToken
      }
      if (newSessionId) {
        sessionId.value = newSessionId
      }
    }

    /**
     * 设置设备会话ID
     * @param newSessionId 设备会话ID
     */
    const setSessionId = (newSessionId: string) => {
      sessionId.value = newSessionId
    }

    /**
     * 退出登录
     * 清空所有用户相关状态并跳转到登录页
     * 如果是同一账号重新登录，保留工作台标签页
     */
    const logOut = () => {
      // 保存当前用户 ID，用于下次登录时判断是否为同一用户
      const currentUserId = info.value.user?.userId
      if (currentUserId) {
        localStorage.setItem(StorageConfig.LAST_USER_ID_KEY, String(currentUserId))
      }

      // 清理动态路由/菜单（必须早于跳转，否则可能残留动态路由）
      try {
        const menuStore = useMenuStore()
        menuStore.removeAllDynamicRoutes()
        menuStore.setMenuList([])
        menuStore.setRawMenuList([])
        menuStore.clearRemoveRouteFns()
        menuStore.setHomePath('')
        menuStore.clearMenuCacheMeta()
      } catch {
        // ignore
      }

      // 清理工作台标签页（登出即清空登录态相关 UI 状态）
      try {
        useWorktabStore().clearAll()
      } catch {
        // ignore
      }

      // 清空用户信息
      info.value = {}
      // 重置登录状态
      isLogin.value = false
      // 重置锁屏状态
      isLock.value = false
      // 清空锁屏密码
      lockPassword.value = ''
      // 清空访问令牌
      accessToken.value = ''
      // 清空刷新令牌
      refreshToken.value = ''
      // 清空设备会话ID
      sessionId.value = ''
      // 清除会话存储中的登录信息（兼容清除旧版 localStorage）
      sessionStorage.removeItem('login-info')
      localStorage.removeItem('login-info')
      // 清理登录态相关的 Pinia 持久化缓存（避免刷新后回显旧状态）
      clearAuthRelatedPersistedStorage()
      // 移除iframe路由缓存
      sessionStorage.removeItem('iframeRoutes')
      clearAiSessionStorage()
      // 重置路由状态
      resetRouterState(500)
      // 跳转到登录页，携带当前路由作为 redirect 参数
      const currentRoute = router.currentRoute.value
      const redirect = currentRoute.path !== '/login' ? currentRoute.fullPath : undefined
      router.push({
        name: 'Login',
        query: redirect ? { redirect } : undefined
      })
    }

    /**
     * 检查并清理工作台标签页
     * 如果不是同一用户登录，清空工作台标签页
     * 应在登录成功后调用
     */
    const checkAndClearWorktabs = () => {
      const lastUserId = localStorage.getItem(StorageConfig.LAST_USER_ID_KEY)
      const currentUserId = info.value.user?.userId

      // 无法获取当前用户 ID，跳过检查
      if (!currentUserId) return

      // 首次登录或缓存已清除，保留现有标签页
      if (!lastUserId) {
        return
      }

      // 不同用户登录，清空工作台标签页
      if (String(currentUserId) !== lastUserId) {
        const worktabStore = useWorktabStore()
        worktabStore.opened = []
        worktabStore.keepAliveExclude = []
      }

      // 清除临时存储
      localStorage.removeItem(StorageConfig.LAST_USER_ID_KEY)
    }

    return {
      language,
      isLogin,
      isLock,
      lockPassword,
      info,
      searchHistory,
      accessToken,
      refreshToken,
      sessionId,
      getUserInfo,
      getSettingState,
      getWorktabState,
      getSessionId,
      setUserInfo,
      setLoginStatus,
      setLanguage,
      setSearchHistory,
      setLockStatus,
      setLockPassword,
      setToken,
      setSessionId,
      logOut,
      checkAndClearWorktabs
    }
  },
  {
    persist: {
      key: 'user',
      storage: localStorage,
    }
  }
)

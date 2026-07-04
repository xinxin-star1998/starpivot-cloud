import {defineStore} from 'pinia'
import {LanguageEnum} from '@/enums/appEnum'
import {useUserStore} from './user'

/**
 * 用户偏好相关状态（语言、搜索历史、锁屏等）
 *
 * 说明：
 * - 当前实现同样基于 useUserStore 进行封装，避免一次性大范围重构
 * - 未来可以将底层 state 迁移到独立 store，而不影响上层调用者
 */
export const usePreferenceStore = defineStore('preferenceStore', () => {
  const userStore = useUserStore()

  const language = computed({
    get: () => userStore.language as LanguageEnum,
    set: (val: LanguageEnum) => userStore.setLanguage(val)
  })

  const searchHistory = computed({
    get: () => userStore.searchHistory,
    set: (list) => userStore.setSearchHistory(list)
  })

  const isLock = computed({
    get: () => userStore.isLock,
    set: (val: boolean) => userStore.setLockStatus(val)
  })

  const lockPassword = computed({
    get: () => userStore.lockPassword,
    set: (val: string) => userStore.setLockPassword(val)
  })

  return {
    language,
    searchHistory,
    isLock,
    lockPassword
  }
})

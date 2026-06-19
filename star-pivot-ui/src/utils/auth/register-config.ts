import { fetchRegisterEnabled } from '@/api/auth'

let registerEnabledCache: boolean | null = null
let pendingRequest: Promise<boolean> | null = null

/**
 * 读取用户注册开关（带内存缓存，避免重复请求）
 */
export async function isRegisterEnabled(force = false): Promise<boolean> {
  if (!force && registerEnabledCache !== null) {
    return registerEnabledCache
  }

  if (!force && pendingRequest) {
    return pendingRequest
  }

  pendingRequest = fetchRegisterEnabled()
    .then((enabled) => {
      registerEnabledCache = enabled
      return enabled
    })
    .finally(() => {
      pendingRequest = null
    })

  return pendingRequest
}

/** 配置变更后可在管理端保存后调用，强制下次重新拉取 */
export function clearRegisterEnabledCache(): void {
  registerEnabledCache = null
}

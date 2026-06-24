import { useRouter, useRoute } from 'vue-router'
import { usePortalMemberStore } from '@/store/modules/portal-member'

/** C 端会员登录校验，未登录跳转 /portal/login */
export function usePortalAuth() {
  const router = useRouter()
  const route = useRoute()
  const portalStore = usePortalMemberStore()

  function requireLogin(): boolean {
    if (portalStore.isLogin) return true
    router.push({
      path: '/portal/login',
      query: { redirect: route.fullPath }
    })
    return false
  }

  function goLogin() {
    router.push({
      path: '/portal/login',
      query: { redirect: route.fullPath }
    })
  }

  return {
    portalStore,
    requireLogin,
    goLogin
  }
}

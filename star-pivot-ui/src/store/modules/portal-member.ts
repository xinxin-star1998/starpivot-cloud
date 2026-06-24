import { defineStore } from 'pinia'
import { StorageConfig } from '@/utils/storage/storage-config'
import type { PortalMember } from '@/api/portal/types'

export const usePortalMemberStore = defineStore(
  'portalMemberStore',
  () => {
    const token = ref('')
    const member = ref<PortalMember | null>(null)

    const isLogin = computed(() => !!token.value)

    function setLogin(data: { token: string; member: PortalMember }) {
      token.value = data.token
      member.value = data.member
    }

    function setMember(info: PortalMember) {
      member.value = info
    }

    function logOut() {
      token.value = ''
      member.value = null
    }

    return {
      token,
      member,
      isLogin,
      setLogin,
      setMember,
      logOut
    }
  },
  {
    persist: {
      key: StorageConfig.generateStorageKey('portal-member'),
      pick: ['token', 'member']
    }
  }
)

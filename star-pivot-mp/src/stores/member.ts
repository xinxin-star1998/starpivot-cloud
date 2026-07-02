import {STORAGE_MEMBER_KEY, STORAGE_TOKEN_KEY} from '@/config'
import type {PortalLoginResult, PortalMember} from '@/api/types'
import {applyCartBadge} from '@/utils/tabbar-badge'

export function getToken(): string {
  return uni.getStorageSync(STORAGE_TOKEN_KEY) || ''
}

export function getMember(): PortalMember | null {
  const raw = uni.getStorageSync(STORAGE_MEMBER_KEY)
  return raw ? (raw as PortalMember) : null
}

export function isLogin(): boolean {
  return !!getToken()
}

export function setLogin(data: PortalLoginResult) {
  uni.setStorageSync(STORAGE_TOKEN_KEY, data.token)
  uni.setStorageSync(STORAGE_MEMBER_KEY, data.member)
}

export function setMember(member: PortalMember) {
  uni.setStorageSync(STORAGE_MEMBER_KEY, member)
}

export function clearSession() {
  uni.removeStorageSync(STORAGE_TOKEN_KEY)
  uni.removeStorageSync(STORAGE_MEMBER_KEY)
  applyCartBadge(0)
}

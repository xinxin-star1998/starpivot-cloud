import type {PortalBrowseRecord} from '@/api/types'

const STORAGE_KEY = 'sp-mp-browse-history'
const MAX_ITEMS = 20

export function getBrowseHistory(): PortalBrowseRecord[] {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY)
    if (!raw) return []
    const list = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!Array.isArray(list)) return []
    return list.filter((item) => item && Number.isFinite(item.spuId))
  } catch {
    return []
  }
}

export function addBrowseRecord(record: Omit<PortalBrowseRecord, 'viewedAt'>) {
  if (!Number.isFinite(record.spuId)) return
  const next = [
    { ...record, viewedAt: Date.now() },
    ...getBrowseHistory().filter((item) => item.spuId !== record.spuId)
  ].slice(0, MAX_ITEMS)
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(next))
}

export function removeBrowseRecord(spuId: number) {
  const next = getBrowseHistory().filter((item) => item.spuId !== spuId)
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(next))
}

export function clearBrowseHistory() {
  uni.removeStorageSync(STORAGE_KEY)
}

export function formatBrowseTime(ts: number) {
  const date = new Date(ts)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const sameDay =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  if (sameDay) {
    const h = String(date.getHours()).padStart(2, '0')
    const m = String(date.getMinutes()).padStart(2, '0')
    return `今天 ${h}:${m}`
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

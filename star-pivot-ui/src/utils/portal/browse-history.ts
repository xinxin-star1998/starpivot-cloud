import {notifyPortalBrowseChanged} from './browse-event'

export interface PortalBrowseRecord {
  spuId: number
  spuName?: string
  coverImg?: string
  price?: number
  viewedAt: number
}

const STORAGE_KEY = 'portal_browse_history'
const MAX_ITEMS = 20

export function getPortalBrowseHistory(): PortalBrowseRecord[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const list = JSON.parse(raw) as PortalBrowseRecord[]
    if (!Array.isArray(list)) return []
    return list.filter((item) => item && Number.isFinite(item.spuId))
  } catch {
    return []
  }
}

export function addPortalBrowseRecord(record: Omit<PortalBrowseRecord, 'viewedAt'>) {
  if (!Number.isFinite(record.spuId)) return
  const now = Date.now()
  const next = [
    { ...record, viewedAt: now },
    ...getPortalBrowseHistory().filter((item) => item.spuId !== record.spuId)
  ].slice(0, MAX_ITEMS)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  notifyPortalBrowseChanged()
}

export function removePortalBrowseRecord(spuId: number) {
  const next = getPortalBrowseHistory().filter((item) => item.spuId !== spuId)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  notifyPortalBrowseChanged()
}

export function clearPortalBrowseHistory() {
  localStorage.removeItem(STORAGE_KEY)
  notifyPortalBrowseChanged()
}

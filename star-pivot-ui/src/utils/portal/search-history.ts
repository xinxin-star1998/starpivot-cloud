import { notifyPortalSearchHistoryChanged } from './search-event'

const STORAGE_KEY = 'portal_search_history'
const MAX_ITEMS = 10

export const PORTAL_HOT_SEARCH_KEYWORDS = ['手机', '耳机', '笔记本', '运动鞋', '护肤', '零食']

export function getPortalSearchHistory(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const list = JSON.parse(raw)
    return Array.isArray(list) ? list.filter((item) => typeof item === 'string' && item.trim()) : []
  } catch {
    return []
  }
}

export function addPortalSearchKeyword(keyword: string) {
  const text = keyword.trim()
  if (!text) return
  const next = [text, ...getPortalSearchHistory().filter((item) => item !== text)].slice(0, MAX_ITEMS)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  notifyPortalSearchHistoryChanged()
}

export function clearPortalSearchHistory() {
  localStorage.removeItem(STORAGE_KEY)
  notifyPortalSearchHistoryChanged()
}

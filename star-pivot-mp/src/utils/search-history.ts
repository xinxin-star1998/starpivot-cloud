const STORAGE_KEY = 'sp-mp-search-history'
const MAX_ITEMS = 10

export const HOT_SEARCH_KEYWORDS = ['手机', '耳机', '笔记本', '运动鞋', '护肤', '零食']

export function getSearchHistory(): string[] {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY)
    if (!raw) return []
    const list = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(list) ? list.filter((item) => typeof item === 'string' && item.trim()) : []
  } catch {
    return []
  }
}

export function addSearchKeyword(keyword: string) {
  const text = keyword.trim()
  if (!text) return
  const next = [text, ...getSearchHistory().filter((item) => item !== text)].slice(0, MAX_ITEMS)
  uni.setStorageSync(STORAGE_KEY, next)
}

export function clearSearchHistory() {
  uni.removeStorageSync(STORAGE_KEY)
}

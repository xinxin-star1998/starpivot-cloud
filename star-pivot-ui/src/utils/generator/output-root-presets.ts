const STORAGE_KEY = 'gen_external_output_roots'
const MAX_RECENT = 8

export function loadRecentOutputRoots(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as string[]) : []
  } catch {
    return []
  }
}

export function rememberOutputRoot(path: string) {
  const trimmed = path.trim()
  if (!trimmed) return
  const list = loadRecentOutputRoots().filter((p) => p !== trimmed)
  list.unshift(trimmed)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list.slice(0, MAX_RECENT)))
}

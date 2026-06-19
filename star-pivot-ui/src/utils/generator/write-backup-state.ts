const STORAGE_KEY = 'gen_external_last_backup'

export interface LastWriteBackup {
  sessionId: string
  backupId: string
  outputRoot: string
}

export function loadLastWriteBackup(sessionId: string): LastWriteBackup | null {
  if (!sessionId) return null
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const state = JSON.parse(raw) as LastWriteBackup
    return state.sessionId === sessionId ? state : null
  } catch {
    return null
  }
}

export function saveLastWriteBackup(state: LastWriteBackup) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

export function clearLastWriteBackup() {
  localStorage.removeItem(STORAGE_KEY)
}

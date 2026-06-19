import type { ExternalDbConnection } from '@/api/generator/gen-external'

const STORAGE_KEY = 'gen_external_connection_presets'
const LAST_CONN_KEY = 'gen_external_last_connection'

export interface ConnectionPreset {
  name: string
  connection: Omit<ExternalDbConnection, 'password'>
  /** 是否保存密码（本地存储，仅当前浏览器） */
  savePassword?: boolean
  password?: string
}

export function loadConnectionPresets(): ConnectionPreset[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as ConnectionPreset[]) : []
  } catch {
    return []
  }
}

export function persistConnectionPresets(list: ConnectionPreset[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export function loadLastConnection(): Partial<ExternalDbConnection> | null {
  try {
    const raw = localStorage.getItem(LAST_CONN_KEY)
    return raw ? (JSON.parse(raw) as Partial<ExternalDbConnection>) : null
  } catch {
    return null
  }
}

export function saveLastConnection(conn: ExternalDbConnection) {
  localStorage.setItem(LAST_CONN_KEY, JSON.stringify({ ...conn, password: '' }))
}

export function toPresetConnection(
  conn: ExternalDbConnection
): Omit<ExternalDbConnection, 'password'> {
  const { password: _p, ...rest } = conn
  return rest
}

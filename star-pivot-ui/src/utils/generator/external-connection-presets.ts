import type {ExternalDbConnection} from '@/api/generator/gen-external'

const STORAGE_KEY = 'gen_external_connection_presets'
const LAST_CONN_KEY = 'gen_external_last_connection'

/** 持久化到 localStorage 的连接预设（不含密码） */
export interface ConnectionPreset {
  name: string
  connection: Omit<ExternalDbConnection, 'password'>
}

type LegacyConnectionPreset = ConnectionPreset & {
  savePassword?: boolean
  password?: string
}

function stripLegacyPasswordFields(preset: LegacyConnectionPreset): ConnectionPreset {
  const { savePassword: _s, password: _p, ...rest } = preset
  return rest
}

export function loadConnectionPresets(): ConnectionPreset[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw) as LegacyConnectionPreset[]
    if (!Array.isArray(parsed)) return []

    const cleaned = parsed.map(stripLegacyPasswordFields)
    const hadLegacyPassword = parsed.some(
      (p) => Boolean(p.savePassword) || Boolean(p.password)
    )
    if (hadLegacyPassword) {
      persistConnectionPresets(cleaned)
    }
    return cleaned
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
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<ExternalDbConnection>
    if (!parsed || typeof parsed !== 'object') return null
    if (parsed.password) {
      delete parsed.password
      localStorage.setItem(LAST_CONN_KEY, JSON.stringify(parsed))
    }
    return parsed
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

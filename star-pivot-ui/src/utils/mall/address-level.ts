/** 与 address 表一致：0-省 1-市 2-区县 3-乡镇 */
export const ADDRESS_MAX_LEVEL = 3

export const ADDRESS_LEVEL_OPTIONS = [
  { label: '省', value: 0 },
  { label: '市', value: 1 },
  { label: '区县', value: 2 },
  { label: '乡镇', value: 3 }
] as const

const LEVEL_LABELS = ['省', '市', '区县', '乡镇'] as const

export function formatAddressLevel(level?: number | null): string {
  if (level == null || level < 0 || level > ADDRESS_MAX_LEVEL) return '-'
  return LEVEL_LABELS[level] ?? '-'
}

export function addressLevelTagType(level?: number | null) {
  if (level === 0) return 'primary' as const
  if (level === 1) return 'success' as const
  if (level === 2) return 'warning' as const
  if (level === 3) return 'info' as const
  return 'info' as const
}

/** 是否还可挂子级（乡镇为最末级） */
export function addressHasChildren(level?: number | null): boolean {
  return level != null && level < ADDRESS_MAX_LEVEL
}

/** 新增下级时的层级 */
export function nextAddressLevel(parentLevel?: number | null): number {
  return Math.min((parentLevel ?? 0) + 1, ADDRESS_MAX_LEVEL)
}

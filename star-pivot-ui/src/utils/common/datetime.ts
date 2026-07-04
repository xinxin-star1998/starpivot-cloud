/**
 * 日期时间展示工具
 */
import dayjs from 'dayjs'

/** 常见时间字段名（表格列自动格式化） */
const DATETIME_COLUMN_PROPS = new Set([
  'createTime',
  'updateTime',
  'loginTime',
  'loginDate',
  'finishTime',
  'startTime',
  'endTime',
  'deleteTime',
  'lastAccessTime',
  'useTime',
  'enableStartTime',
  'enableEndTime',
  'viewedAt',
  'operTime',
  'payTime',
  'deliveryTime',
  'receiveTime',
  'commentTime',
  'applyTime',
  'approveTime'
])

const DATETIME_PROP_SUFFIX = /(?:Time|Date|At)$/i

/**
 * 判断表格列 prop 是否为时间字段
 */
export function isDateTimeColumnProp(prop?: string): boolean {
  if (!prop) return false
  return DATETIME_COLUMN_PROPS.has(prop) || DATETIME_PROP_SUFFIX.test(prop)
}

/**
 * 格式化为 YYYY-MM-DD HH:mm:ss；兼容 ISO-8601（含 T）与已有标准格式。
 */
export function formatDateTime(value: unknown, fallback = '-'): string {
  if (value == null || value === '') return fallback
  const raw = String(value).trim()
  const normalized = raw.replace('T', ' ')
  const parsed = dayjs(normalized)
  if (!parsed.isValid()) return raw
  return parsed.format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化为 YYYY-MM-DD
 */
export function formatDate(value: unknown, fallback = '-'): string {
  if (value == null || value === '') return fallback
  const raw = String(value).trim()
  const parsed = dayjs(raw.replace('T', ' '))
  if (!parsed.isValid()) return raw.slice(0, 10)
  return parsed.format('YYYY-MM-DD')
}

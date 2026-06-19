/** 可选值分隔符（与谷粒商城库表约定一致） */
export const VALUE_SELECT_SEPARATOR = ';'

type AttrValueSelectRow = {
  valueSelect?: string | null
  value_select?: string | null
}

/** 列表/详情行上读取 valueSelect（兼容 snake_case） */
export function getAttrValueSelect(row?: AttrValueSelectRow | null): string {
  const raw = row?.valueSelect ?? row?.value_select
  return raw != null ? String(raw).trim() : ''
}

/**
 * 库表字符串 → 多选标签数组。
 * 优先按分号拆分；若无分号则兼容英文/中文逗号的历史数据。
 */
export function parseValueSelect(raw?: string | null): string[] {
  const text = raw?.trim()
  if (!text) return []
  const parts = text.includes(VALUE_SELECT_SEPARATOR)
    ? text.split(VALUE_SELECT_SEPARATOR)
    : text.split(/[,，]/)
  return parts.map((s) => s.trim()).filter(Boolean)
}

/** 编辑回显：保留库表 value_type 与 value_select */
export function normalizeAttrValueFields(
  valueType: number | undefined | null,
  valueSelectRaw?: string | null
): { valueType: number; valueSelect: string } {
  const valueSelect = valueSelectRaw?.trim() ?? ''
  const vt = valueType != null && !Number.isNaN(Number(valueType)) ? Number(valueType) : 0
  return { valueType: vt, valueSelect }
}

/** 表格缩略：首项 tag + restCount；tooltip 用 full 展示全部选项 */
export function formatValueSelectBrief(raw?: string | null): {
  tags: string[]
  brief: string
  full: string
  restCount: number
} {
  const tags = parseValueSelect(raw)
  const full = tags.join('、')
  const brief = tags[0] ?? ''
  const restCount = Math.max(0, tags.length - 1)
  return { tags, brief, full, restCount }
}

/** 多选标签数组 → 库表字符串（分号拼接） */
export function joinValueSelect(tags: string[]): string | undefined {
  const list = tags.map((s) => s.trim()).filter(Boolean)
  if (!list.length) return undefined
  return list.join(VALUE_SELECT_SEPARATOR)
}

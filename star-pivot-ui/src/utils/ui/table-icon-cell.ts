import { h, type VNode } from 'vue'
import { Icon } from '@iconify/vue'

/** Iconify 名称：prefix:icon-name */
export function isIconifyIconName(icon?: string | null): boolean {
  const name = icon?.trim()
  if (!name) return false
  return /^[a-z0-9][\w-]*:[a-z0-9][\w-]*$/i.test(name)
}

/** 表格「图标」列：有效 Iconify 名称渲染图标，否则显示原文或占位 */
export function formatTableIconCell(icon?: string | null): VNode {
  const raw = icon?.trim() || ''
  if (!raw) {
    return h('span', { style: 'color: var(--el-text-color-placeholder)' }, '—')
  }
  if (isIconifyIconName(raw)) {
    return h(
      'div',
      {
        style: 'display: flex; align-items: center; justify-content: center;',
        title: raw
      },
      [h(Icon, { icon: raw, style: 'font-size: 20px' })]
    )
  }
  return h('span', { title: raw, style: 'color: var(--el-text-color-secondary)' }, raw)
}

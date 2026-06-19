/** 流程定义状态文案 */
export function defStatusLabel(status?: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    disabled: '已停用'
  }
  return map[status || ''] || status || '-'
}

/** 流程定义状态 Tag 类型 */
export function defStatusTagType(status?: string): 'success' | 'info' | 'warning' {
  if (status === 'published') return 'success'
  if (status === 'disabled') return 'info'
  return 'warning'
}

/** 流程实例状态文案 */
export function instanceStatusLabel(status?: string) {
  const map: Record<string, string> = {
    RUNNING: '进行中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已撤销'
  }
  return map[status || ''] || status || '-'
}

/** 流程实例状态 Tag 类型 */
export function instanceStatusTagType(status?: string): 'success' | 'danger' | 'info' | 'warning' {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

export const DEF_STATUS_OPTIONS = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已停用', value: 'disabled' }
]

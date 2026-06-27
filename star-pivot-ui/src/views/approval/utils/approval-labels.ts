/** 审批实例状态文案 */
export function instanceStatusLabel(status?: string) {
  const map: Record<string, string> = {
    RUNNING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    WITHDRAWN: '已撤回'
  }
  return map[status || ''] || status || '-'
}

/** 审批实例状态 Tag 类型 */
export function instanceStatusTagType(status?: string): 'success' | 'danger' | 'info' | 'warning' {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'info'
  return 'warning'
}

/** 模板状态文案 */
export function templateStatusLabel(status?: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    DISABLED: '已停用'
  }
  return map[status || ''] || status || '-'
}

export function templateStatusTagType(status?: string): 'success' | 'info' | 'warning' {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'DISABLED') return 'info'
  return 'warning'
}

/** 步骤状态文案 */
export function stepStatusLabel(status?: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    DONE: '已完成',
    SKIPPED: '已跳过',
    CANCELLED: '已取消'
  }
  return map[status || ''] || status || '-'
}

/** 审批动作文案 */
export function recordActionLabel(action?: string) {
  const map: Record<string, string> = {
    SUBMIT: '提交',
    APPROVE: '通过',
    REJECT: '驳回',
    WITHDRAW: '撤回',
    SKIP: '跳过'
  }
  return map[action || ''] || action || '-'
}

export const TEMPLATE_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已停用', value: 'DISABLED' }
]

export const ASSIGNEE_TYPE_OPTIONS = [
  { label: '发起人', value: 'STARTER' },
  { label: '部门负责人', value: 'DEPT_LEADER' },
  { label: '角色', value: 'ROLE' },
  { label: '岗位', value: 'POST' },
  { label: '指定用户', value: 'USER' }
]

export const APPROVE_MODE_OPTIONS = [
  { label: '或签（任一人）', value: 'ANY' },
  { label: '会签（全部人）', value: 'ALL' }
]

export const INSTANCE_STATUS_OPTIONS = [
  { label: '审批中', value: 'RUNNING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已撤回', value: 'WITHDRAWN' }
]

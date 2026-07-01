/** 商城业务审批状态（与 SAS 对齐） */
export const MALL_AUDIT_STATUS_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回'
}

export function canSubmitMallAudit(status?: string) {
  return !status || status === 'DRAFT' || status === 'REJECTED' || status === 'WITHDRAWN'
}

export function auditStatusTagType(status?: string): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'APPROVED') return 'success'
  if (status === 'PENDING') return 'warning'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'info'
  return 'info'
}

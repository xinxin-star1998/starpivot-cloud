/**
 * 操作日志业务类型（与字典 sys_oper_type 一致）
 */
export const OPER_BUSINESS_TYPE_MAP: Record<number, string> = {
  0: '其他',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '授权',
  5: '导出',
  6: '导入',
  7: '强退',
  8: '生成代码',
  9: '清空数据'
}

export const OPER_BUSINESS_TYPE_OPTIONS = Object.entries(OPER_BUSINESS_TYPE_MAP)
  .map(([value, label]) => ({
    label,
    value: Number(value)
  }))
  .sort((a, b) => (a.value === 0 ? 1 : b.value === 0 ? -1 : a.value - b.value))

export function getOperBusinessTypeLabel(businessType?: number): string {
  if (businessType == null) {
    return '未知'
  }
  return OPER_BUSINESS_TYPE_MAP[businessType] ?? '未知'
}

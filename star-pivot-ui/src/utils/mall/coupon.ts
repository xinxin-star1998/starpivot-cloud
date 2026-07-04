import type {CouponVo} from '@/api/mall/coupon'

export type CouponRunStatus = 'not_started' | 'claiming' | 'running' | 'expired' | 'depleted'

export type CouponPhaseStatus = 'not_started' | 'active' | 'ended' | 'expired' | 'depleted'

export const COUPON_RUN_STATUS_MAP: Record<
  CouponRunStatus,
  { label: string; tagType: 'info' | 'success' | 'warning' | 'danger' }
> = {
  not_started: { label: '未开始', tagType: 'info' },
  claiming: { label: '领取中', tagType: 'success' },
  running: { label: '可使用', tagType: 'success' },
  expired: { label: '已过期', tagType: 'warning' },
  depleted: { label: '已领完', tagType: 'danger' }
}

export const COUPON_PHASE_STATUS_MAP: Record<
  CouponPhaseStatus,
  { label: string; tagType: 'info' | 'success' | 'warning' | 'danger' }
> = {
  not_started: { label: '未开始', tagType: 'info' },
  active: { label: '进行中', tagType: 'success' },
  ended: { label: '已结束', tagType: 'warning' },
  expired: { label: '已过期', tagType: 'warning' },
  depleted: { label: '已领完', tagType: 'danger' }
}

function parseTime(value?: string | null): number | null {
  if (!value) return null
  const time = new Date(value).getTime()
  return Number.isFinite(time) ? time : null
}

export function formatCouponMoney(value?: number | string | null): string {
  if (value == null || value === '') return '-'
  const num = Number(value)
  return Number.isFinite(num) ? `¥${num.toFixed(2)}` : '-'
}

export function formatCouponDateRange(start?: string, end?: string): string {
  if (!start && !end) return '-'
  if (start && end) return `${start} ~ ${end}`
  return start || end || '-'
}

export function formatCouponStock(row: CouponVo): string {
  const received = row.receiveCount ?? 0
  const total = row.publishCount
  if (total == null) return `${received}/-`
  return `${received}/${total}`
}

/** 领取阶段状态 */
export function getCouponClaimStatus(row: CouponVo, now = Date.now()): CouponPhaseStatus {
  const received = row.receiveCount ?? 0
  const total = row.publishCount ?? 0
  if (total > 0 && received >= total) return 'depleted'

  const enableStart = parseTime(row.enableStartTime)
  const enableEnd = parseTime(row.enableEndTime)
  if (enableStart != null && now < enableStart) return 'not_started'
  if (enableEnd != null && now > enableEnd) return 'ended'
  if (enableStart != null || enableEnd != null) return 'active'
  return 'active'
}

/** 使用阶段状态 */
export function getCouponUseStatus(row: CouponVo, now = Date.now()): CouponPhaseStatus {
  const useStart = parseTime(row.startTime)
  const useEnd = parseTime(row.endTime)
  if (useStart != null && now < useStart) return 'not_started'
  if (useEnd != null && now > useEnd) return 'expired'
  return 'active'
}

/**
 * 综合状态：已领完 > 使用已过期 > 使用未开始（领取窗口内显示领取中）> 可使用
 */
export function getCouponRunStatus(row: CouponVo, now = Date.now()): CouponRunStatus {
  const received = row.receiveCount ?? 0
  const total = row.publishCount ?? 0
  if (total > 0 && received >= total) return 'depleted'

  const useStart = parseTime(row.startTime)
  const useEnd = parseTime(row.endTime)
  const enableStart = parseTime(row.enableStartTime)
  const enableEnd = parseTime(row.enableEndTime)

  if (useEnd != null && now > useEnd) return 'expired'

  if (useStart != null && now < useStart) {
    if (enableStart != null && enableEnd != null && now >= enableStart && now <= enableEnd) {
      return 'claiming'
    }
    return 'not_started'
  }

  return 'running'
}

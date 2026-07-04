/** 将 HH:mm 解析为今日对应时刻的时间戳 */
export function parseTodayTimeLabel(label?: string): number | null {
  if (!label?.trim()) return null
  const match = label.trim().match(/^(\d{1,2}):(\d{2})$/)
  if (!match) return null
  const hours = Number(match[1])
  const minutes = Number(match[2])
  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) return null
  const date = new Date()
  date.setHours(hours, minutes, 0, 0)
  return date.getTime()
}

export function formatCountdown(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000))
  const hours = Math.floor(total / 3600)
  const minutes = Math.floor((total % 3600) / 60)
  const seconds = total % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  if (hours > 0) return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
  return `${pad(minutes)}:${pad(seconds)}`
}

export interface SeckillCountdownTarget {
  state?: 'ongoing' | 'upcoming' | 'ended'
  startLabel?: string
  endLabel?: string
}

/** 根据场次状态计算倒计时目标时刻 */
export function getSeckillCountdownTarget(session: SeckillCountdownTarget): number | null {
  if (session.state === 'ongoing') {
    return parseTodayTimeLabel(session.endLabel)
  }
  if (session.state === 'upcoming') {
    return parseTodayTimeLabel(session.startLabel)
  }
  return null
}

export function getSeckillCountdownPrefix(state?: string): string {
  if (state === 'ongoing') return '距结束'
  if (state === 'upcoming') return '距开始'
  return ''
}

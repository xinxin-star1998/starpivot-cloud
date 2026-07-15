/**
 * 金额展示与简易运算（分整数），避免直接 Number().toFixed 导致的精度噪声。
 * 注意：若接口以 JSON number 传输，IEEE 误差可能已在反序列化阶段产生；
 * 长期建议后端金额字段以字符串输出。
 */

export type MoneyInput = string | number | null | undefined

function formatCents(cents: number, fractionDigits: 0 | 2): string {
  const neg = cents < 0
  const abs = Math.abs(cents)
  if (fractionDigits === 0) {
    const yuan = Math.round(abs / 100)
    return `${neg ? '-' : ''}${yuan}`
  }
  const yuan = Math.floor(abs / 100)
  const fen = String(abs % 100).padStart(2, '0')
  return `${neg ? '-' : ''}${yuan}.${fen}`
}

/** 将金额转为「分」；非法输入返回 null */
export function toCents(value: MoneyInput): number | null {
  if (value == null || value === '') {
    return null
  }
  const raw = String(value).trim()
  if (!/^-?\d+(\.\d+)?$/.test(raw)) {
    const n = Number(value)
    if (!Number.isFinite(n)) {
      return null
    }
    return Math.round(n * 100)
  }
  const negative = raw.startsWith('-')
  const unsigned = negative ? raw.slice(1) : raw
  const [intPart, fracPart = ''] = unsigned.split('.')
  const frac = (fracPart + '00').slice(0, 2)
  let cents = Number(intPart) * 100 + Number(frac)
  if (fracPart.length > 2 && Number(fracPart[2]) >= 5) {
    cents += 1
  }
  return negative ? -cents : cents
}

/**
 * 格式化金额字符串（不含货币符号）
 * @param empty 非法/空值时的占位，默认 '--'
 * @param fractionDigits 小数位，默认 2；券面额等可用 0
 */
export function formatMoney(
  value: MoneyInput,
  empty: string = '--',
  fractionDigits: 0 | 2 = 2
): string {
  const cents = toCents(value)
  if (cents == null) {
    return empty
  }
  return formatCents(cents, fractionDigits)
}

/** 单价 × 数量（数量按整数截断），返回两位小数展示串 */
export function formatMoneyProduct(price: MoneyInput, quantity: number, empty = '0.00'): string {
  const cents = toCents(price)
  if (cents == null || !Number.isFinite(quantity)) {
    return empty
  }
  return formatCents(cents * Math.trunc(quantity), 2)
}

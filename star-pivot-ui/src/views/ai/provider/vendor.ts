export interface AiVendorMeta {
  color: string
  icon: string
  short: string
}

const VENDOR_MAP: Record<string, AiVendorMeta> = {
  deepseek: { color: '#4F6EF7', icon: 'ri:compass-3-line', short: 'DS' },
  kimi: { color: '#6D5CFF', icon: 'ri:moon-clear-line', short: 'KM' },
  dashscope: { color: '#FF6A00', icon: 'ri:cloud-line', short: 'QW' },
  openai: { color: '#10A37F', icon: 'ri:sparkling-2-line', short: 'OA' },
  zhipu: { color: '#3859FF', icon: 'ri:brain-line', short: 'GLM' },
  siliconflow: { color: '#7C5CFC', icon: 'ri:cpu-line', short: 'SF' },
  custom: { color: '#64748B', icon: 'ri:plug-2-line', short: 'API' }
}

export function resolveVendorMeta(code?: string): AiVendorMeta {
  if (!code) return VENDOR_MAP.custom
  return VENDOR_MAP[code.toLowerCase()] || VENDOR_MAP.custom
}

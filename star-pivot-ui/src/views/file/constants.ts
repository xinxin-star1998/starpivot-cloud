/** 文件中心常量 */

export const FILE_CATEGORIES = [
  { code: 'SYSTEM', label: '系统通用' },
  { code: 'OA', label: '办公审批' },
  { code: 'CONTRACT', label: '合同档案' },
  { code: 'CERT', label: '资质证件' },
  { code: 'PROJECT', label: '项目资料' },
  { code: 'CUSTOMER', label: '客户资料' },
  { code: 'GOODS', label: '商品素材' },
  { code: 'FINANCE', label: '财务单据' },
  { code: 'HR', label: '人事档案' },
  { code: 'OTHER', label: '其他附件' }
] as const

export const MEDIA_TYPES = [
  { code: '', label: '全部' },
  { code: 'IMAGE', label: '图片' },
  { code: 'VIDEO', label: '视频' },
  { code: 'DOCUMENT', label: '文档' },
  { code: 'AUDIO', label: '音频' },
  { code: 'OTHER', label: '其他' }
] as const

export const MEDIA_TYPE_TAG: Record<string, string> = {
  IMAGE: 'success',
  VIDEO: 'warning',
  DOCUMENT: 'primary',
  AUDIO: 'info',
  OTHER: ''
}

/** 媒体类型图标（Remix Icon） */
export const MEDIA_TYPE_ICONS: Record<string, string> = {
  IMAGE: 'ri:image-line',
  VIDEO: 'ri:video-line',
  DOCUMENT: 'ri:file-text-line',
  AUDIO: 'ri:music-2-line',
  OTHER: 'ri:file-line'
}

/** 业务分类图标 */
export const CATEGORY_ICONS: Record<string, string> = {
  SYSTEM: 'ri:settings-3-line',
  OA: 'ri:briefcase-line',
  CONTRACT: 'ri:file-paper-2-line',
  CERT: 'ri:award-line',
  PROJECT: 'ri:folder-chart-line',
  CUSTOMER: 'ri:user-star-line',
  GOODS: 'ri:shopping-bag-3-line',
  FINANCE: 'ri:money-cny-circle-line',
  HR: 'ri:team-line',
  OTHER: 'ri:attachment-2-line'
}

export function getMediaTypeIcon(mediaType?: string) {
  return MEDIA_TYPE_ICONS[mediaType || ''] || MEDIA_TYPE_ICONS.OTHER
}

export function getCategoryIcon(category?: string) {
  return CATEGORY_ICONS[category || ''] || 'ri:folder-line'
}

export function getCategoryLabel(code?: string) {
  return FILE_CATEGORIES.find((c) => c.code === code)?.label ?? code ?? ''
}

export function getMediaTypeLabel(code?: string) {
  return MEDIA_TYPES.find((c) => c.code === code)?.label ?? code ?? ''
}

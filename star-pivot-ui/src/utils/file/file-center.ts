import type {SysFile} from '@/api/file/types'

export type PreviewMode = 'image' | 'video' | 'audio' | 'pdf' | 'download'

export function formatFileSize(bytes?: number): string {
  if (bytes == null || bytes <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`
}

export function getPreviewMode(mediaType?: string, fileExt?: string): PreviewMode {
  switch (mediaType) {
    case 'IMAGE':
      return 'image'
    case 'VIDEO':
      return 'video'
    case 'AUDIO':
      return 'audio'
    case 'DOCUMENT':
      return fileExt?.toLowerCase() === 'pdf' ? 'pdf' : 'download'
    default:
      return 'download'
  }
}

export function resolveFileDisplayUrl(file: SysFile): string {
  return file.displayUrl || ''
}

/** 触发浏览器下载/新窗口打开 */
export function openFileUrl(url: string, fileName?: string) {
  if (!url) return
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  if (fileName) link.download = fileName
  link.rel = 'noopener noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

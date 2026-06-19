/**
 * 文件处理工具函数
 * 提供文件下载、Blob 处理等常用功能
 */

/**
 * 下载 Blob 文件
 * @param blob Blob 对象
 * @param filename 文件名
 */
export function downloadBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/** 从响应头对象中按大小写不敏感获取 Content-Disposition */
export function getContentDisposition(headers: Record<string, string>): string | null {
  const key = Object.keys(headers).find((k) => k.toLowerCase() === 'content-disposition')
  return key ? headers[key] : null
}

/**
 * 从响应头获取文件名
 * @param contentDisposition Content-Disposition 响应头
 * @returns 文件名
 */
export function getFilenameFromContentDisposition(contentDisposition: string | null): string {
  if (!contentDisposition) {
    return 'download.xlsx'
  }

  // 尝试从 filename*=UTF-8'' 中提取
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''(.+)/i)
  if (utf8Match) {
    return decodeURIComponent(utf8Match[1])
  }

  // 尝试从 filename="..." 中提取
  const quotedMatch = contentDisposition.match(/filename="(.+)"/i)
  if (quotedMatch) {
    return quotedMatch[1]
  }

  // 尝试从 filename=... 中提取
  const unquotedMatch = contentDisposition.match(/filename=([^;]+)/i)
  if (unquotedMatch) {
    return unquotedMatch[1].trim()
  }

  return 'download.xlsx'
}

import { fetchGoodsImagePresignedUrls } from '@/api/mall/goods-image'
import { extractOssObjectPath, isStorageObjectName } from '@/utils/storage/oss-object-path'

const IMG_SRC_PATTERN = /<img([^>]*?)\ssrc=(['"])(.*?)\2/gi

function isEditorObjectName(value?: string | null): value is string {
  return !!value && isStorageObjectName(value) && value.startsWith('editor/')
}

/** 将 HTML 中 OSS 预签名/永久 URL 还原为 editor/ 对象路径，便于持久化存储 */
export function normalizeEditorHtmlForStorage(html: string): string {
  if (!html) {
    return html
  }
  return html.replace(IMG_SRC_PATTERN, (match, attrs, quote, src) => {
    const objectName = extractOssObjectPath(src)
    if (objectName && objectName.startsWith('editor/')) {
      return `<img${attrs} src=${quote}${objectName}${quote}`
    }
    return match
  })
}

/** 将 HTML 中 editor/ 对象路径替换为预签名 URL，供编辑器或详情页展示 */
export async function resolveEditorHtmlForDisplay(html: string): Promise<string> {
  if (!html) {
    return html
  }

  const objectNames = new Set<string>()
  for (const match of html.matchAll(IMG_SRC_PATTERN)) {
    const src = match[3]
    if (isEditorObjectName(src)) {
      objectNames.add(src)
    }
  }
  if (objectNames.size === 0) {
    return html
  }

  try {
    const urlMap = await fetchGoodsImagePresignedUrls([...objectNames])
    if (!urlMap || typeof urlMap !== 'object') {
      return html
    }
    return html.replace(IMG_SRC_PATTERN, (match, attrs, quote, src) => {
      if (!isEditorObjectName(src)) {
        return match
      }
      const displayUrl = urlMap[src]
      if (!displayUrl) {
        return match
      }
      return `<img${attrs} src=${quote}${displayUrl}${quote}`
    })
  } catch {
    return html
  }
}

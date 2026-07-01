import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

/** 解析评价 resources 字段（逗号分隔 objectName） */
export function parseCommentResourceKeys(resources?: string | null): string[] {
  if (!resources?.trim()) return []
  return resources
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

/** 批量解析评价图片展示 URL */
export async function resolveCommentResourceUrls(
  resources?: string | null
): Promise<string[]> {
  const keys = parseCommentResourceKeys(resources)
  if (!keys.length) return []
  const map = await resolveGoodsImageDisplayUrls(keys)
  return keys.map((k) => map.get(k) || k).filter(Boolean)
}

import { fetchMallBrandList, type MallBrandVo } from '@/api/mall/brand'
import { defaultResponseAdapter, extractTableData } from '@/utils/table/tableUtils'

/** 将品牌列表转为 brandId -> name */
export function buildBrandNameMap(rows: MallBrandVo[]): Record<number, string> {
  const map: Record<number, string> = {}
  for (const row of rows) {
    const id = row.brandId
    if (id != null && row.name) {
      map[id] = row.name
    }
  }
  return map
}

/** 根据映射解析品牌展示名 */
export function getBrandDisplayName(map: Record<number, string>, brandId?: number | null): string {
  if (brandId == null) return '-'
  return map[brandId] || '-'
}

/** 拉取品牌列表并构建 id -> name 映射（供表格展示） */
export async function fetchBrandNameMap(): Promise<Record<number, string>> {
  try {
    const res = await fetchMallBrandList({ pageNum: 1, pageSize: 2000 })
    const rows = extractTableData(defaultResponseAdapter<MallBrandVo>(res))
    return buildBrandNameMap(rows)
  } catch {
    return {}
  }
}

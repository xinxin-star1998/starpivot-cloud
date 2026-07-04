import {fetchGetAttrList, type MallAttr} from '@/api/mall/attr'
import {fetchGetGroupList, type Group} from '@/api/mall/group'
import {defaultResponseAdapter, extractTableData} from '@/utils/table/tableUtils'

/** 兼容后端 PageResponse.rows 与前端 PaginatedResponse.records */
export function pageRows<T>(page: unknown): T[] {
  return extractTableData(defaultResponseAdapter<T>(page))
}

export interface SpuAttrGroupWithAttrs {
  attrGroupId: number
  attrGroupName: string
  attrs: MallAttr[]
}

/** 按三级分类加载属性分组及组内基本属性 */
export async function fetchCatalogBaseAttrGroups(
  catalogId: number
): Promise<SpuAttrGroupWithAttrs[]> {
  const [groupsRes, attrsRes] = await Promise.all([
    fetchGetGroupList({ catelogId: catalogId, pageNum: 1, pageSize: 500 }),
    fetchGetAttrList({
      catelogId: catalogId,
      attrType: 1,
      enable: 1,
      pageNum: 1,
      pageSize: 500
    })
  ])

  const attrsByGroup = new Map<number, MallAttr[]>()
  for (const attr of pageRows<MallAttr>(attrsRes)) {
    const gid = attr.attrGroupId
    if (gid == null) continue
    const list = attrsByGroup.get(gid) ?? []
    list.push(attr)
    attrsByGroup.set(gid, list)
  }

  const groups: SpuAttrGroupWithAttrs[] = []
  for (const g of pageRows<Group>(groupsRes)) {
    const gid = g.attrGroupId
    if (gid == null) continue
    const attrs = attrsByGroup.get(gid)
    if (!attrs?.length) continue
    attrs.sort((a, b) => (a.attrSort ?? 0) - (b.attrSort ?? 0))
    groups.push({
      attrGroupId: gid,
      attrGroupName: g.attrGroupName ?? `分组${gid}`,
      attrs
    })
  }
  return groups
}

/** 三级分类下的销售属性 */
export async function fetchCatalogSaleAttrs(catalogId: number): Promise<MallAttr[]> {
  const res = await fetchGetAttrList({
    catelogId: catalogId,
    attrType: 0,
    enable: 1,
    pageNum: 1,
    pageSize: 500
  })
  return pageRows<MallAttr>(res)
}

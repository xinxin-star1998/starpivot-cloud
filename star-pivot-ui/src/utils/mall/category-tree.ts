import { fetchMallCategoryTree, type MallCategoryTreeNode } from '@/api/mall/category'

/** 将分类树展平为 catId -> name */
export function buildCategoryNameMap(
  nodes: MallCategoryTreeNode[],
  map: Record<number, string> = {}
): Record<number, string> {
  for (const node of nodes) {
    const id = node.catId
    if (id != null && node.name) {
      map[id] = node.name
    }
    if (node.children?.length) {
      buildCategoryNameMap(node.children, map)
    }
  }
  return map
}

/** 根据映射解析分类展示名 */
export function getCategoryDisplayName(map: Record<number, string>, catId?: number | null): string {
  if (catId == null) return '-'
  return map[catId] || '-'
}

/** 拉取完整类目树并构建 id -> name 映射 */
export async function fetchCategoryNameMap(): Promise<Record<number, string>> {
  try {
    const tree = await fetchMallCategoryTree()
    return buildCategoryNameMap(tree || [])
  } catch {
    return {}
  }
}

/** 在树中查找从根到目标 catId 的路径（用于级联回显） */
export function findCategoryPath(
  nodes: MallCategoryTreeNode[],
  targetId: number,
  path: number[] = []
): number[] | null {
  for (const node of nodes) {
    const id = node.catId
    if (id == null) continue
    const next = [...path, id]
    if (id === targetId) return next
    if (node.children?.length) {
      const found = findCategoryPath(node.children, targetId, next)
      if (found) return found
    }
  }
  return null
}

/** 仅保留显示中的节点（showStatus=1），用于商品选类 */
export function filterVisibleCategoryTree(nodes: MallCategoryTreeNode[]): MallCategoryTreeNode[] {
  const out: MallCategoryTreeNode[] = []
  for (const node of nodes) {
    const children = node.children?.length ? filterVisibleCategoryTree(node.children) : []
    const visible = Number(node.showStatus) === 1
    if (visible || children.length > 0) {
      out.push({
        ...node,
        children: children.length > 0 ? children : undefined
      })
    }
  }
  return out
}

/**
 * 级联选项：一、二级可展开，仅三级为叶子可选。
 * （若对一二级设 disabled，Element Plus 级联无法展开子级）
 */
export function mapCategoryCascaderOptions(
  nodes: MallCategoryTreeNode[],
  options?: { boundCatIds?: number[]; depth?: number }
): MallCategoryTreeNode[] {
  const depth = options?.depth ?? 1
  const bound = options?.boundCatIds?.length
    ? new Set(options.boundCatIds.map((id) => Number(id)))
    : null
  return nodes.map((node) => {
    const lv = node.catLevel != null ? Number(node.catLevel) : 0
    const rawChildren = node.children?.length ? node.children : undefined
    const isLevel3 = lv === 3 || depth >= 3
    const children = isLevel3
      ? undefined
      : rawChildren
        ? mapCategoryCascaderOptions(rawChildren, { ...options, depth: depth + 1 })
        : undefined
    const catId = node.catId
    const alreadyBound = bound != null && catId != null && isLevel3 && bound.has(Number(catId))
    return {
      ...node,
      leaf: isLevel3,
      disabled: alreadyBound,
      children
    }
  })
}

/** 在树中查找节点 */
export function findCategoryNode(
  nodes: MallCategoryTreeNode[],
  targetId: number
): MallCategoryTreeNode | null {
  for (const node of nodes) {
    if (node.catId === targetId) return node
    if (node.children?.length) {
      const found = findCategoryNode(node.children, targetId)
      if (found) return found
    }
  }
  return null
}

/** 判断节点在树数据中是否仍有子节点（用于批量删除提示） */
export function categoryHasChildren(data: MallCategoryTreeNode): boolean {
  return Array.isArray(data.children) && data.children.length > 0
}

/** 收集若干目标节点的所有祖先 catId（用于展开到已绑定节点，不含目标自身） */
export function collectAncestorKeysForTargets(
  nodes: MallCategoryTreeNode[],
  targetIds: number[]
): number[] {
  const keys = new Set<number>()
  for (const id of targetIds) {
    const path = findCategoryPath(nodes, id)
    if (!path || path.length < 2) continue
    for (let i = 0; i < path.length - 1; i++) {
      keys.add(path[i]!)
    }
  }
  return [...keys]
}

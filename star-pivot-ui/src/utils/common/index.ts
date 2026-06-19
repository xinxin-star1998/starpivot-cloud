/**
 * 通用工具函数
 * 提供常用的工具函数，如深度克隆、树遍历等
 *
 * @module utils/common
 */

/**
 * 深度克隆对象
 * @param obj 要克隆的对象
 * @returns 克隆后的对象
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as T
  if (obj instanceof RegExp) return new RegExp(obj) as T
  if (Array.isArray(obj)) return obj.map((item) => deepClone(item)) as T

  const cloned = {} as T
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      cloned[key] = deepClone(obj[key])
    }
  }
  return cloned
}

/**
 * 在树形结构中查找节点
 * @param tree 树形数组
 * @param predicate 查找条件函数
 * @param getChildren 获取子节点的函数
 * @returns 找到的节点和其父节点ID
 */
export function findInTree<T>(
  tree: T[],
  predicate: (node: T) => boolean,
  getChildren: (node: T) => T[] | undefined,
  parentId?: unknown
): { node: T | undefined; parentId: unknown } {
  for (const node of tree) {
    if (predicate(node)) {
      return { node, parentId }
    }
    const children = getChildren(node)
    if (children?.length) {
      const found = findInTree(
        children,
        predicate,
        getChildren,
        (node as any).id || (node as any).menuId
      )
      if (found.node) return found
    }
  }
  return { node: undefined, parentId: undefined }
}

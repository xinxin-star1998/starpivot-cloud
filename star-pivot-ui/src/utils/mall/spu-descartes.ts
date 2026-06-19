/**
 * 销售属性笛卡尔积（谷粒商城同款算法）
 */
export function cartesianProduct<T>(lists: T[][]): T[][] {
  if (!lists.length) return []

  const point: Record<string, { parent: string | null; count: number }> = {}
  let pIndex: string | null = null

  for (const index in lists) {
    if (typeof lists[index] === 'object') {
      point[index] = { parent: pIndex, count: 0 }
      pIndex = index
    }
  }

  if (pIndex == null) {
    return lists
  }

  const result: T[][] = []
  let cursor: string = pIndex

  while (true) {
    const temp: T[] = []
    for (const index in lists) {
      const tempCount = point[index]!.count
      temp.push(lists[index]![tempCount]!)
    }
    result.push(temp)

    while (true) {
      if (point[cursor]!.count + 1 >= lists[cursor]!.length) {
        point[cursor]!.count = 0
        const parent = point[cursor]!.parent
        if (parent == null) {
          return result
        }
        cursor = parent
      } else {
        point[cursor]!.count++
        break
      }
    }
  }
}

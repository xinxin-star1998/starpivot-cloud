import type { SpfEdge, SpfNode } from './flow-types'

const H_GAP = 220
const V_GAP = 100
const START_X = 80
const START_Y = 200

function buildOutEdges(edges: SpfEdge[]) {
  const map = new Map<string, SpfEdge[]>()
  for (const edge of edges) {
    const list = map.get(edge.source) || []
    list.push(edge)
    map.set(edge.source, list)
  }
  return map
}

/** 按拓扑层级自动布局（从左到右） */
export function layoutFlowNodes(nodes: SpfNode[], edges: SpfEdge[]): SpfNode[] {
  if (!nodes.length) return nodes

  const start = nodes.find((n) => n.type === 'start')
  const outMap = buildOutEdges(edges)
  const rank = new Map<string, number>()

  if (start) {
    rank.set(start.id, 0)
    const queue = [start.id]
    const visited = new Set([start.id])

    while (queue.length) {
      const id = queue.shift()!
      for (const edge of outMap.get(id) || []) {
        const nextRank = (rank.get(id) || 0) + 1
        rank.set(edge.target, Math.max(rank.get(edge.target) ?? 0, nextRank))
        if (!visited.has(edge.target)) {
          visited.add(edge.target)
          queue.push(edge.target)
        }
      }
    }
  }

  nodes.forEach((n, index) => {
    if (!rank.has(n.id)) rank.set(n.id, index % 3)
  })

  const byRank = new Map<number, SpfNode[]>()
  nodes.forEach((n) => {
    const r = rank.get(n.id) ?? 0
    const list = byRank.get(r) || []
    list.push(n)
    byRank.set(r, list)
  })

  const positioned = new Map<string, { x: number; y: number }>()
  byRank.forEach((list, r) => {
    const totalHeight = (list.length - 1) * V_GAP
    list.forEach((n, i) => {
      positioned.set(n.id, {
        x: START_X + r * H_GAP,
        y: START_Y + i * V_GAP - totalHeight / 2
      })
    })
  })

  return nodes.map((n) => ({
    ...n,
    position: positioned.get(n.id) || n.position
  }))
}

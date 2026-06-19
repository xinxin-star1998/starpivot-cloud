import type { Edge } from '@vue-flow/core'
import type { SpfEdge } from './flow-types'

export const WF_EDGE_TYPE = 'wf'

export function isValidSpfEdge(
  edge: SpfEdge | null | undefined,
  nodeIds?: Set<string>
): edge is SpfEdge {
  if (!edge?.id || !edge.source || !edge.target) return false
  if (nodeIds && (!nodeIds.has(edge.source) || !nodeIds.has(edge.target))) return false
  return true
}

export function toVueFlowEdge(edge: SpfEdge, extra: Partial<Edge> = {}): Edge {
  const { id: _id, source: _source, target: _target, type: _type, ...rest } = extra
  return {
    ...rest,
    id: edge.id,
    source: edge.source,
    target: edge.target,
    type: (extra.type as string | undefined) ?? WF_EDGE_TYPE
  }
}

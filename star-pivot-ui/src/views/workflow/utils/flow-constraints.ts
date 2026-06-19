import type { Connection } from '@vue-flow/core'
import type { FlowNodeType, SpfEdge, SpfNode } from './flow-types'

export interface ConnectCheckResult {
  ok: boolean
  message?: string
}

function getNodeType(nodes: SpfNode[], id: string): FlowNodeType | undefined {
  return nodes.find((n) => n.id === id)?.type
}

function outCount(edges: SpfEdge[], sourceId: string) {
  return edges.filter((e) => e.source === sourceId).length
}

function inCount(edges: SpfEdge[], targetId: string) {
  return edges.filter((e) => e.target === targetId).length
}

export function checkConnection(
  sourceId: string,
  targetId: string,
  nodes: SpfNode[],
  edges: SpfEdge[]
): ConnectCheckResult {
  if (sourceId === targetId) {
    return { ok: false, message: '不能连接自身' }
  }

  const sourceType = getNodeType(nodes, sourceId)
  const targetType = getNodeType(nodes, targetId)

  if (!sourceType || !targetType) {
    return { ok: false, message: '节点不存在' }
  }

  if (sourceType === 'end') {
    return { ok: false, message: '结束节点不能作为连线起点' }
  }

  if (targetType === 'start') {
    return { ok: false, message: '开始节点不能作为连线终点' }
  }

  if (edges.some((e) => e.source === sourceId && e.target === targetId)) {
    return { ok: false, message: '该连线已存在' }
  }

  if (targetType === 'end' && inCount(edges, targetId) >= 1) {
    return { ok: false, message: '结束节点只能有一条入线' }
  }

  if (sourceType === 'start' && outCount(edges, sourceId) >= 1) {
    return { ok: false, message: '开始节点只能有一条出线' }
  }

  if (sourceType === 'approval' && outCount(edges, sourceId) >= 1) {
    return { ok: false, message: '审批节点只能有一条出线' }
  }

  if (sourceType === 'condition' && outCount(edges, sourceId) >= 5) {
    return { ok: false, message: '条件节点最多 5 条出线' }
  }

  return { ok: true }
}

export function createConnectionValidator(nodes: SpfNode[], edges: SpfEdge[]) {
  return (connection: Connection) => {
    if (!connection.source || !connection.target) return false
    return checkConnection(connection.source, connection.target, nodes, edges).ok
  }
}

export function canDeleteNode(node: SpfNode): ConnectCheckResult {
  if (node.type === 'start') {
    return { ok: false, message: '开始节点不可删除' }
  }
  return { ok: true }
}

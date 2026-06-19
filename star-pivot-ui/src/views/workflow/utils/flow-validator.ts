import type { SpfDefinition, SpfEdge } from './flow-types'

export interface FlowValidationResult {
  errors: string[]
  nodeErrors: Record<string, string[]>
}

function pushNodeError(map: Record<string, string[]>, nodeId: string, message: string) {
  if (!map[nodeId]) map[nodeId] = []
  map[nodeId].push(message)
}

export function validateFlowDefinitionDetailed(def: SpfDefinition): FlowValidationResult {
  const errors: string[] = []
  const nodeErrors: Record<string, string[]> = {}
  if (!def.processCode?.trim()) errors.push('流程编码不能为空')
  if (!def.processName?.trim()) errors.push('流程名称不能为空')
  if (!def.nodes?.length) {
    errors.push('至少需要一个节点')
    return { errors, nodeErrors }
  }

  const starts = def.nodes.filter((n) => n.type === 'start')
  if (starts.length !== 1) errors.push('必须有且仅有一个开始节点')

  if (!def.nodes.some((n) => n.type === 'end')) errors.push('至少需要一个结束节点')

  const nodeIds = new Set<string>()
  for (const node of def.nodes) {
    if (!node.id || nodeIds.has(node.id)) errors.push(`节点 ID 重复或为空: ${node.id}`)
    nodeIds.add(node.id)
    if (node.type === 'approval' && !node.data?.assigneeRule?.type) {
      const msg = `需配置审批人规则`
      errors.push(`审批节点「${node.data?.name || node.id}」${msg}`)
      pushNodeError(nodeErrors, node.id, msg)
    }
    if (
      node.type === 'approval' &&
      node.data?.assigneeRule?.type === 'ROLE' &&
      !node.data.assigneeRule.value
    ) {
      const msg = '需选择角色'
      errors.push(`审批节点「${node.data?.name || node.id}」${msg}`)
      pushNodeError(nodeErrors, node.id, msg)
    }
    if (
      node.type === 'approval' &&
      node.data?.assigneeRule?.type === 'POST' &&
      !node.data.assigneeRule.value
    ) {
      const msg = '需选择岗位'
      errors.push(`审批节点「${node.data?.name || node.id}」${msg}`)
      pushNodeError(nodeErrors, node.id, msg)
    }
    if (
      node.type === 'approval' &&
      node.data?.assigneeRule?.type === 'USER' &&
      !node.data.assigneeRule.value
    ) {
      const msg = '需选择用户'
      errors.push(`审批节点「${node.data?.name || node.id}」${msg}`)
      pushNodeError(nodeErrors, node.id, msg)
    }
  }

  const outEdges = buildOutEdges(def.edges || [])
  if (starts[0]) {
    const visited = bfs(starts[0].id, outEdges)
    if (visited.size !== def.nodes.length) {
      errors.push('存在无法从开始节点到达的孤立节点')
      def.nodes.forEach((node) => {
        if (!visited.has(node.id)) {
          pushNodeError(nodeErrors, node.id, '无法从开始节点到达')
        }
      })
    }
  }

  for (const node of def.nodes) {
    if (node.type === 'condition') {
      const edges = outEdges.get(node.id) || []
      if (!edges.length) {
        const msg = '需要配置出线'
        errors.push(`条件节点「${node.data?.name || node.id}」${msg}`)
        pushNodeError(nodeErrors, node.id, msg)
      }
      if (edges.length === 1) {
        const msg = '条件节点至少需要 2 条出线'
        errors.push(`条件节点「${node.data?.name || node.id}」${msg}`)
        pushNodeError(nodeErrors, node.id, msg)
      }
      if (!edges.some((e) => e.data?.condition?.type === 'default')) {
        const msg = '需要默认分支'
        errors.push(`条件节点「${node.data?.name || node.id}」${msg}`)
        pushNodeError(nodeErrors, node.id, msg)
      }
    }
    if (node.type === 'approval') {
      const edges = outEdges.get(node.id) || []
      if (!edges.length) {
        const msg = '需要连接后续节点'
        errors.push(`审批节点「${node.data?.name || node.id}」${msg}`)
        pushNodeError(nodeErrors, node.id, msg)
      }
    }
  }

  return { errors, nodeErrors }
}

export function validateFlowDefinition(def: SpfDefinition): string[] {
  return validateFlowDefinitionDetailed(def).errors
}

function buildOutEdges(edges: SpfEdge[]) {
  const map = new Map<string, SpfEdge[]>()
  for (const edge of edges) {
    const list = map.get(edge.source) || []
    list.push(edge)
    map.set(edge.source, list)
  }
  return map
}

function bfs(startId: string, outEdges: Map<string, SpfEdge[]>) {
  const visited = new Set<string>()
  const queue = [startId]
  visited.add(startId)
  while (queue.length) {
    const current = queue.shift()!
    for (const edge of outEdges.get(current) || []) {
      if (!visited.has(edge.target)) {
        visited.add(edge.target)
        queue.push(edge.target)
      }
    }
  }
  return visited
}

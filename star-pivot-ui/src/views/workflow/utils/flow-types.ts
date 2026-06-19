/** StarPivot Flow Definition (SPF) - 非 BPMN 自定义 JSON */

export type FlowNodeType = 'start' | 'end' | 'approval' | 'condition'

/** Vue Flow 节点尺寸（须与 WfFlowNode 样式一致，否则连线锚点偏移） */
export const NODE_DIMENSIONS: Record<FlowNodeType, { width: number; height: number }> = {
  start: { width: 136, height: 56 },
  end: { width: 136, height: 56 },
  approval: { width: 168, height: 76 },
  condition: { width: 168, height: 76 }
}

export type AssigneeRuleType = 'STARTER_SELF' | 'STARTER_DEPT_LEADER' | 'ROLE' | 'POST' | 'USER'

export interface AssigneeRule {
  type: AssigneeRuleType
  value?: string | number
}

export interface SpfNodeData {
  name?: string
  assigneeRule?: AssigneeRule
  approveMode?: 'OR' | 'AND'
  defaultTarget?: string
}

export interface SpfNode {
  id: string
  type: FlowNodeType
  position: { x: number; y: number }
  data: SpfNodeData
}

export interface ConditionExpr {
  type?: 'default'
  field?: string
  op?: '>' | '>=' | '<' | '<=' | '==' | '!='
  value?: string | number
}

export interface SpfEdge {
  id: string
  source: string
  target: string
  data?: {
    condition?: ConditionExpr
  }
}

export interface SpfDefinition {
  schemaVersion: '1.0'
  processCode: string
  processName: string
  bizModule: 'crm' | 'erp' | 'mall' | 'system'
  nodes: SpfNode[]
  edges: SpfEdge[]
}

export const BIZ_MODULE_OPTIONS = [
  { label: '商城', value: 'mall' },
  { label: 'ERP', value: 'erp' },
  { label: 'CRM', value: 'crm' },
  { label: '系统', value: 'system' }
] as const

export const ASSIGNEE_RULE_OPTIONS = [
  { label: '发起人本人', value: 'STARTER_SELF' },
  { label: '发起人部门负责人', value: 'STARTER_DEPT_LEADER' },
  { label: '指定角色', value: 'ROLE' },
  { label: '指定岗位', value: 'POST' },
  { label: '指定用户ID', value: 'USER' }
] as const

export function createDefaultDefinition(): SpfDefinition {
  return {
    schemaVersion: '1.0',
    processCode: '',
    processName: '',
    bizModule: 'mall',
    nodes: [
      {
        id: 'start_1',
        type: 'start',
        position: { x: 80, y: 200 },
        data: { name: '开始' }
      },
      {
        id: 'end_1',
        type: 'end',
        position: { x: 480, y: 200 },
        data: { name: '结束' }
      }
    ],
    edges: []
  }
}

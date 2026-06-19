import type { SpfDefinition, SpfEdge, SpfNode } from './flow-types'

export interface FlowTemplate {
  id: string
  name: string
  description: string
  build: () => Pick<SpfDefinition, 'nodes' | 'edges'>
}

export const FLOW_TEMPLATES: FlowTemplate[] = [
  {
    id: 'single_approval',
    name: '单级审批',
    description: '开始 → 部门负责人审批 → 结束',
    build: () => ({
      nodes: [
        { id: 'start_1', type: 'start', position: { x: 80, y: 200 }, data: { name: '开始' } },
        {
          id: 'approval_1',
          type: 'approval',
          position: { x: 300, y: 200 },
          data: {
            name: '部门负责人审批',
            approveMode: 'OR',
            assigneeRule: { type: 'STARTER_DEPT_LEADER' }
          }
        },
        { id: 'end_1', type: 'end', position: { x: 520, y: 200 }, data: { name: '结束' } }
      ],
      edges: [
        { id: 'e_start_approval', source: 'start_1', target: 'approval_1', data: {} },
        { id: 'e_approval_end', source: 'approval_1', target: 'end_1', data: {} }
      ]
    })
  },
  {
    id: 'two_level_approval',
    name: '两级审批',
    description: '开始 → 直属领导 → 总监 → 结束',
    build: () => ({
      nodes: [
        { id: 'start_1', type: 'start', position: { x: 80, y: 200 }, data: { name: '开始' } },
        {
          id: 'approval_1',
          type: 'approval',
          position: { x: 300, y: 200 },
          data: {
            name: '直属领导审批',
            approveMode: 'OR',
            assigneeRule: { type: 'STARTER_DEPT_LEADER' }
          }
        },
        {
          id: 'approval_2',
          type: 'approval',
          position: { x: 520, y: 200 },
          data: {
            name: '总监审批',
            approveMode: 'OR',
            assigneeRule: { type: 'ROLE', value: 'director' }
          }
        },
        { id: 'end_1', type: 'end', position: { x: 740, y: 200 }, data: { name: '结束' } }
      ],
      edges: [
        { id: 'e_start_a1', source: 'start_1', target: 'approval_1', data: {} },
        { id: 'e_a1_a2', source: 'approval_1', target: 'approval_2', data: {} },
        { id: 'e_a2_end', source: 'approval_2', target: 'end_1', data: {} }
      ]
    })
  },
  {
    id: 'amount_condition',
    name: '金额条件分支',
    description: '按 amount 分支到不同审批路径',
    build: () => ({
      nodes: [
        { id: 'start_1', type: 'start', position: { x: 80, y: 200 }, data: { name: '开始' } },
        {
          id: 'condition_1',
          type: 'condition',
          position: { x: 300, y: 200 },
          data: { name: '金额判断' }
        },
        {
          id: 'approval_low',
          type: 'approval',
          position: { x: 520, y: 120 },
          data: {
            name: '普通审批',
            approveMode: 'OR',
            assigneeRule: { type: 'STARTER_DEPT_LEADER' }
          }
        },
        {
          id: 'approval_high',
          type: 'approval',
          position: { x: 520, y: 280 },
          data: {
            name: '高额审批',
            approveMode: 'OR',
            assigneeRule: { type: 'ROLE', value: 'director' }
          }
        },
        { id: 'end_1', type: 'end', position: { x: 740, y: 200 }, data: { name: '结束' } }
      ],
      edges: [
        { id: 'e_start_cond', source: 'start_1', target: 'condition_1', data: {} },
        {
          id: 'e_cond_low',
          source: 'condition_1',
          target: 'approval_low',
          data: { condition: { field: 'amount', op: '<=', value: 10000 } }
        },
        {
          id: 'e_cond_high',
          source: 'condition_1',
          target: 'approval_high',
          data: { condition: { type: 'default' } }
        },
        { id: 'e_low_end', source: 'approval_low', target: 'end_1', data: {} },
        { id: 'e_high_end', source: 'approval_high', target: 'end_1', data: {} }
      ]
    })
  }
]

export function applyFlowTemplate(
  templateId: string
): Pick<SpfDefinition, 'nodes' | 'edges'> | null {
  const tpl = FLOW_TEMPLATES.find((t) => t.id === templateId)
  if (!tpl) return null
  return JSON.parse(JSON.stringify(tpl.build()))
}

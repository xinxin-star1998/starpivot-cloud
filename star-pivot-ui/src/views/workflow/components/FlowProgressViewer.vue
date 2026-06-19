<!-- 流程进度只读画布 -->
<template>
  <div class="flow-progress-viewer" :class="{ mini }">
    <VueFlow
      :nodes="displayNodes"
      :edges="displayEdges"
      :node-types="nodeTypes as any"
      :edge-types="edgeTypes as any"
      :nodes-draggable="false"
      :nodes-connectable="false"
      :elements-selectable="false"
      fit-view-on-init
    >
      <Background />
      <Controls v-if="!mini" />
      <FlowCanvasHelper :layout-tick="layoutTick" />
    </VueFlow>
  </div>
</template>

<script setup lang="ts">
  import { VueFlow, Position, MarkerType, type Edge, type Node } from '@vue-flow/core'
  import { Background } from '@vue-flow/background'
  import { Controls } from '@vue-flow/controls'
  import { markRaw } from 'vue'
  import WfFlowNode from '../designer/components/WfFlowNode.vue'
  import WfFlowEdge from '../designer/components/WfFlowEdge.vue'
  import FlowCanvasHelper from '../designer/components/FlowCanvasHelper.vue'
  import { layoutFlowNodes } from '../utils/flow-layout'
  import { toVueFlowEdge, WF_EDGE_TYPE, isValidSpfEdge } from '../utils/flow-vue-flow'
  import {
    NODE_DIMENSIONS,
    type SpfDefinition,
    type SpfEdge,
    type SpfNode
  } from '../utils/flow-types'
  import type { FlowNodeProgressStatus } from '../utils/flow-progress-types'

  import '@vue-flow/core/dist/style.css'
  import '@vue-flow/core/dist/theme-default.css'
  import '@vue-flow/controls/dist/style.css'

  const props = defineProps<{
    defJson?: string
    nodeStatuses?: Record<string, FlowNodeProgressStatus>
    mini?: boolean
  }>()

  const nodeTypes = { wf: markRaw(WfFlowNode) }
  const edgeTypes = { [WF_EDGE_TYPE]: markRaw(WfFlowEdge) }
  const layoutTick = ref(0)

  const parsed = computed(() => {
    if (!props.defJson) return { nodes: [] as SpfNode[], edges: [] as SpfEdge[] }
    try {
      const def = JSON.parse(props.defJson) as SpfDefinition
      const nodes = layoutFlowNodes(def.nodes || [], def.edges || [])
      return { nodes, edges: def.edges || [] }
    } catch {
      return { nodes: [] as SpfNode[], edges: [] as SpfEdge[] }
    }
  })

  watch(
    () => props.defJson,
    () => {
      layoutTick.value += 1
    },
    { immediate: true }
  )

  const displayNodes = computed<Node[]>(() =>
    parsed.value.nodes.map((n) => {
      const dim = NODE_DIMENSIONS[n.type]
      return {
        id: n.id,
        type: 'wf',
        position: n.position,
        width: dim.width,
        height: dim.height,
        sourcePosition: Position.Right,
        targetPosition: Position.Left,
        data: {
          label: n.data.name || n.type,
          nodeType: n.type,
          progressStatus: props.nodeStatuses?.[n.id]
        }
      }
    })
  )

  const displayEdges = computed<Edge[]>(() => {
    const nodeIds = new Set(parsed.value.nodes.map((n) => n.id))
    return parsed.value.edges
      .filter((e) => isValidSpfEdge(e, nodeIds))
      .map((e) =>
        toVueFlowEdge(e, {
          type: WF_EDGE_TYPE,
          label: e.data?.condition?.type === 'default' ? '默认' : e.data?.condition?.field || '',
          animated: props.nodeStatuses?.[e.target] === 'CURRENT',
          style: { stroke: '#64748b', strokeWidth: 2 },
          markerEnd: MarkerType.ArrowClosed
        })
      )
  })
</script>

<style scoped lang="scss">
  .flow-progress-viewer {
    height: 100%;
    min-height: 480px;

    :deep(.vue-flow) {
      height: 100%;
      min-height: 480px;
    }

    &.mini {
      min-height: 280px;

      :deep(.vue-flow) {
        min-height: 280px;
      }
    }

    :deep(.vue-flow__edge-path) {
      stroke: #94a3b8;
      stroke-width: 2;
      fill: none;
    }

    :deep(.vue-flow__arrowhead) {
      fill: #94a3b8;
    }
  }
</style>

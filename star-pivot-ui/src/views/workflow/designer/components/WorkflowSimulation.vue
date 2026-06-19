<template>
  <div class="wf-simulation">
    <div class="simulation-header">
      <div class="simulation-status">
        <span class="status-label">状态:</span>
        <ElTag :type="statusType" size="small">{{ statusText }}</ElTag>
      </div>
      <div class="simulation-controls">
        <ElButton
          v-if="!isRunning"
          type="primary"
          size="small"
          :disabled="!canStart"
          @click="startSimulation"
        >
          <ArtSvgIcon icon="ri:play-line" class="btn-icon" />
          开始模拟
        </ElButton>
        <ElButton v-else type="danger" size="small" @click="stopSimulation">
          <ArtSvgIcon icon="ri:stop-line" class="btn-icon" />
          停止
        </ElButton>
        <ElButton size="small" @click="resetSimulation">
          <ArtSvgIcon icon="ri:refresh-line" class="btn-icon" />
          重置
        </ElButton>
        <ElButton size="small" @click="stepSimulation" :disabled="!canStep">
          <ArtSvgIcon icon="ri:skip-forward-line" class="btn-icon" />
          单步
        </ElButton>
      </div>
    </div>

    <div class="simulation-content">
      <div class="simulation-canvas">
        <div class="canvas-wrapper" ref="canvasWrapper">
          <div
            v-for="node in displayNodes"
            :key="node.id"
            class="sim-node"
            :class="{
              'sim-node--active': activeNodeId === node.id,
              'sim-node--completed': completedNodeIds.includes(node.id),
              'sim-node--pending': pendingNodeIds.includes(node.id),
              'sim-node--error': errorNodeIds.includes(node.id)
            }"
            :style="getNodeStyle(node)"
          >
            <div class="sim-node__icon">
              <ArtSvgIcon :icon="getNodeIcon(node)" />
            </div>
            <div class="sim-node__label">{{ node.data?.label || node.type }}</div>
            <div v-if="activeNodeId === node.id" class="sim-node__pulse"></div>
          </div>

          <svg class="sim-edges" :style="svgStyle">
            <defs>
              <marker
                id="sim-arrow"
                viewBox="0 0 10 10"
                refX="8"
                refY="5"
                markerWidth="6"
                markerHeight="6"
                orient="auto-start-reverse"
              >
                <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b" />
              </marker>
              <marker
                id="sim-arrow-active"
                viewBox="0 0 10 10"
                refX="8"
                refY="5"
                markerWidth="6"
                markerHeight="6"
                orient="auto-start-reverse"
              >
                <path d="M 0 0 L 10 5 L 0 10 z" fill="#409eff" />
              </marker>
            </defs>
            <path
              v-for="edge in displayEdges"
              :key="edge.id"
              :d="getEdgePath(edge)"
              class="sim-edge"
              :class="{
                'sim-edge--active': isEdgeActive(edge),
                'sim-edge--completed': isEdgeCompleted(edge)
              }"
              marker-end="url(#sim-arrow)"
            />
          </svg>
        </div>
      </div>

      <div class="simulation-log">
        <div class="log-header">
          <span>执行日志</span>
          <ElButton size="small" text @click="clearLog">
            <ArtSvgIcon icon="ri:delete-bin-line" />
          </ElButton>
        </div>
        <div class="log-list" ref="logList">
          <div
            v-for="(log, index) in logs"
            :key="index"
            class="log-item"
            :class="`log-item--${log.type}`"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="simulation-footer">
      <div class="node-info" v-if="activeNode">
        <div class="info-label">当前节点:</div>
        <div class="info-value">{{ activeNode.data?.label || activeNode.type }}</div>
      </div>
      <div class="node-info" v-if="currentStep">
        <div class="info-label">执行步骤:</div>
        <div class="info-value">{{ currentStep }}/{{ totalSteps }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch, nextTick } from 'vue'
  import type { SpfNode, SpfEdge, SpfDefinition } from '../../utils/flow-types'

  interface Props {
    definition: SpfDefinition
    nodes: SpfNode[]
    edges: SpfEdge[]
  }

  const props = defineProps<Props>()

  const emit = defineEmits<{
    close: []
  }>()

  // State
  const isRunning = ref(false)
  const currentNodeIndex = ref(0)
  const activeNodeId = ref<string | null>(null)
  const completedNodeIds = ref<string[]>([])
  const errorNodeIds = ref<string[]>([])
  const pendingNodeIds = ref<string[]>([])
  const logs = ref<Array<{ time: string; message: string; type: string }>>([])
  const logList = ref<HTMLElement | null>(null)
  const canvasWrapper = ref<HTMLElement | null>(null)

  // Computed
  const displayNodes = computed(() => props.nodes)
  const displayEdges = computed(() => props.edges)

  const statusType = computed(() => {
    if (errorNodeIds.value.length > 0) return 'danger'
    if (isRunning.value) return 'primary'
    if (completedNodeIds.value.length === displayNodes.value.length) return 'success'
    return 'info'
  })

  const statusText = computed(() => {
    if (errorNodeIds.value.length > 0) return '执行失败'
    if (isRunning.value) return '运行中'
    if (completedNodeIds.value.length === displayNodes.value.length) return '已完成'
    return '就绪'
  })

  const activeNode = computed(() => {
    return displayNodes.value.find((n) => n.id === activeNodeId.value)
  })

  const currentStep = computed(() => completedNodeIds.value.length + 1)
  const totalSteps = computed(() => displayNodes.value.length)

  const canStart = computed(() => {
    return displayNodes.value.length > 0 && !isRunning.value && completedNodeIds.value.length === 0
  })

  const canStep = computed(() => {
    return !isRunning.value && completedNodeIds.value.length < displayNodes.value.length
  })

  const svgStyle = computed(() => {
    return {
      position: 'absolute',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      pointerEvents: 'none'
    }
  })

  // Methods
  function getNodeStyle(node: SpfNode) {
    const width = 160
    const height = 60
    return {
      left: `${node.position.x}px`,
      top: `${node.position.y}px`,
      width: `${width}px`,
      height: `${height}px`
    }
  }

  function getNodeIcon(node: SpfNode) {
    const typeIconMap: Record<string, string> = {
      start: 'ri:play-circle-line',
      end: 'ri:stop-circle-line',
      task: 'ri:task-line',
      gateway: 'ri:share-line',
      event: 'ri:flashlight-line'
    }
    return typeIconMap[node.type] || 'ri:checkbox-blank-circle-line'
  }

  function getEdgePath(edge: SpfEdge) {
    const sourceNode = displayNodes.value.find((n) => n.id === edge.source)
    const targetNode = displayNodes.value.find((n) => n.id === edge.target)
    if (!sourceNode || !targetNode) return ''

    const startX = sourceNode.position.x + 160
    const startY = sourceNode.position.y + 30
    const endX = targetNode.position.x
    const endY = targetNode.position.y + 30

    const midX = (startX + endX) / 2
    return `M ${startX} ${startY} C ${midX} ${startY}, ${midX} ${endY}, ${endX} ${endY}`
  }

  function isEdgeActive(edge: SpfEdge) {
    return activeNodeId.value === edge.source && !completedNodeIds.value.includes(edge.target)
  }

  function isEdgeCompleted(edge: SpfEdge) {
    return (
      completedNodeIds.value.includes(edge.source) && completedNodeIds.value.includes(edge.target)
    )
  }

  function addLog(message: string, type: string = 'info') {
    const now = new Date()
    const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
    logs.value.push({ time, message, type })
    scrollToBottom()
  }

  function scrollToBottom() {
    nextTick(() => {
      if (logList.value) {
        logList.value.scrollTop = logList.value.scrollHeight
      }
    })
  }

  function clearLog() {
    logs.value = []
  }

  function startSimulation() {
    if (isRunning.value) return
    isRunning.value = true
    activeNodeId.value = null
    completedNodeIds.value = []
    errorNodeIds.value = []
    pendingNodeIds.value = displayNodes.value.map((n) => n.id)
    addLog('开始流程模拟', 'info')

    simulateNext()
  }

  function stopSimulation() {
    isRunning.value = false
    addLog('流程模拟已停止', 'warning')
  }

  function resetSimulation() {
    isRunning.value = false
    activeNodeId.value = null
    completedNodeIds.value = []
    errorNodeIds.value = []
    pendingNodeIds.value = []
    logs.value = []
    addLog('流程已重置', 'info')
  }

  function stepSimulation() {
    if (completedNodeIds.value.length >= displayNodes.value.length) {
      addLog('流程已执行完毕', 'success')
      return
    }

    simulateNext()
  }

  async function simulateNext() {
    if (!isRunning.value && !canStep.value) return

    const pendingNodes = displayNodes.value.filter((n) => !completedNodeIds.value.includes(n.id))
    if (pendingNodes.length === 0) {
      addLog('流程模拟完成', 'success')
      isRunning.value = false
      return
    }

    // Find start nodes or next reachable nodes
    let nextNode: SpfNode | undefined
    if (completedNodeIds.value.length === 0) {
      nextNode = pendingNodes.find((n) => n.type === 'start') || pendingNodes[0]
    } else {
      const lastCompleted = completedNodeIds.value[completedNodeIds.value.length - 1]
      const nextEdges = displayEdges.value.filter((e) => e.source === lastCompleted)
      if (nextEdges.length > 0) {
        const nextNodeId = nextEdges[0].target
        nextNode = pendingNodes.find((n) => n.id === nextNodeId)
      }
      if (!nextNode) {
        nextNode = pendingNodes[0]
      }
    }

    if (!nextNode) return

    activeNodeId.value = nextNode.id
    pendingNodeIds.value = pendingNodeIds.value.filter((id) => id !== nextNode!.id)
    addLog(`进入节点: ${nextNode.data?.label || nextNode.type}`, 'info')

    // Simulate execution time
    const delay = isRunning.value ? 1000 : 0
    await new Promise((resolve) => setTimeout(resolve, delay))

    if (!isRunning.value) return

    // Mark as completed
    completedNodeIds.value.push(nextNode.id)
    activeNodeId.value = null
    addLog(`节点执行完成: ${nextNode.data?.label || nextNode.type}`, 'success')

    // Continue if running
    if (isRunning.value && completedNodeIds.value.length < displayNodes.value.length) {
      setTimeout(() => simulateNext(), 500)
    } else if (completedNodeIds.value.length === displayNodes.value.length) {
      addLog('流程模拟全部完成', 'success')
      isRunning.value = false
    }
  }

  // Watch for changes
  watch(
    () => props.nodes,
    () => {
      if (completedNodeIds.value.length === 0) {
        pendingNodeIds.value = displayNodes.value.map((n) => n.id)
      }
    },
    { immediate: true }
  )

  // Initialize
  addLog('流程模拟器已就绪', 'info')
</script>

<style scoped>
  .wf-simulation {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 500px;
    background: var(--el-fill-color-blank);
  }

  .simulation-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--el-border-color-light);
    background: var(--el-fill-color-lighter);
  }

  .simulation-status {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .status-label {
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  .simulation-controls {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .btn-icon {
    margin-right: 4px;
  }

  .simulation-content {
    display: flex;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  .simulation-canvas {
    flex: 1;
    min-width: 0;
    background:
      radial-gradient(circle at 50% 0%, rgb(64 158 255 / 4%) 0%, transparent 50%),
      var(--el-fill-color-blank);
    position: relative;
    overflow: auto;
  }

  .canvas-wrapper {
    position: relative;
    width: 800px;
    height: 400px;
    margin: 20px;
  }

  .sim-node {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 10px;
    border-radius: 8px;
    border: 2px solid var(--el-border-color);
    background: var(--el-bg-color);
    transition: all 0.3s ease;
    z-index: 1;
  }

  .sim-node--pending {
    opacity: 0.5;
    border-style: dashed;
  }

  .sim-node--active {
    border-color: var(--el-color-primary);
    background: rgb(64 158 255 / 10%);
    transform: scale(1.05);
    box-shadow: 0 0 20px rgb(64 158 255 / 30%);
  }

  .sim-node--completed {
    border-color: var(--el-color-success);
    background: rgb(103 194 58 / 10%);
  }

  .sim-node--error {
    border-color: var(--el-color-danger);
    background: rgb(245 108 108 / 10%);
  }

  .sim-node__icon {
    font-size: 20px;
    color: var(--el-text-color-regular);
  }

  .sim-node--active .sim-node__icon {
    color: var(--el-color-primary);
  }

  .sim-node--completed .sim-node__icon {
    color: var(--el-color-success);
  }

  .sim-node__label {
    font-size: 11px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sim-node__pulse {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 100%;
    height: 100%;
    border-radius: 8px;
    border: 2px solid var(--el-color-primary);
    transform: translate(-50%, -50%);
    animation: pulse 1.5s infinite;
    pointer-events: none;
  }

  @keyframes pulse {
    0% {
      opacity: 1;
      transform: translate(-50%, -50%) scale(1);
    }
    100% {
      opacity: 0;
      transform: translate(-50%, -50%) scale(1.3);
    }
  }

  .sim-edges {
    overflow: visible;
  }

  .sim-edge {
    fill: none;
    stroke: var(--el-border-color);
    stroke-width: 2;
  }

  .sim-edge--active {
    stroke: var(--el-color-primary);
    stroke-width: 3;
    stroke-dasharray: 5 5;
    animation: dash 0.5s linear infinite;
  }

  .sim-edge--completed {
    stroke: var(--el-color-success);
    stroke-width: 2;
  }

  @keyframes dash {
    to {
      stroke-dashoffset: -10;
    }
  }

  .simulation-log {
    width: 280px;
    display: flex;
    flex-direction: column;
    border-left: 1px solid var(--el-border-color-light);
    background: var(--el-fill-color-lighter);
  }

  .log-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-light);
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .log-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
  }

  .log-item {
    display: flex;
    gap: 8px;
    padding: 6px 16px;
    font-size: 11px;
    line-height: 1.5;
    border-bottom: 1px solid var(--el-border-color-extra-light);

    &:last-child {
      border-bottom: none;
    }
  }

  .log-time {
    flex-shrink: 0;
    color: var(--el-text-color-placeholder);
    font-family: ui-monospace, monospace;
  }

  .log-message {
    color: var(--el-text-color-regular);
  }

  .log-item--success .log-message {
    color: var(--el-color-success);
  }

  .log-item--warning .log-message {
    color: var(--el-color-warning);
  }

  .log-item--error .log-message {
    color: var(--el-color-danger);
  }

  .simulation-footer {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 12px 16px;
    border-top: 1px solid var(--el-border-color-light);
    background: var(--el-fill-color-lighter);
  }

  .node-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .info-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .info-value {
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }
</style>

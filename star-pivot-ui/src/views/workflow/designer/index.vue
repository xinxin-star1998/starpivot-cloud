<!-- 工作流可视化设计器（约束型 SPF JSON） -->
<template>
  <div class="workflow-designer art-full-height">
    <header class="designer-header">
      <div class="designer-header__left">
        <div class="designer-header__brand">
          <div class="designer-header__logo">
            <ArtSvgIcon icon="ri:node-tree" />
          </div>
          <div class="designer-header__title-group">
            <h1 class="designer-header__title">流程设计器</h1>
            <ElTag v-if="defId" size="small" type="info" effect="plain" round> #{{ defId }} </ElTag>
            <ElTag v-else size="small" type="warning" effect="light" round>未保存</ElTag>
          </div>
        </div>

        <div class="designer-header__divider" />

        <div class="meta-fields">
          <div class="meta-field">
            <label class="meta-field__label">流程编码</label>
            <ElInput
              v-model="meta.processCode"
              placeholder="mall_order_approve"
              class="meta-field__input meta-field__input--code"
            />
          </div>
          <div class="meta-field">
            <label class="meta-field__label">流程名称</label>
            <ElInput
              v-model="meta.processName"
              placeholder="商品上架审批"
              class="meta-field__input"
            />
          </div>
          <div class="meta-field">
            <label class="meta-field__label">业务模块</label>
            <ElSelect v-model="meta.bizModule" class="meta-field__input meta-field__input--sm">
              <ElOption
                v-for="item in BIZ_MODULE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </div>
          <div class="meta-field">
            <label class="meta-field__label">模板</label>
            <ElSelect
              v-model="selectedTemplateId"
              placeholder="从模板创建"
              class="meta-field__input meta-field__input--tpl"
              clearable
              @change="handleApplyTemplate"
            >
              <ElOption
                v-for="tpl in FLOW_TEMPLATES"
                :key="tpl.id"
                :label="tpl.name"
                :value="tpl.id"
              >
                <span>{{ tpl.name }}</span>
                <span class="tpl-desc">{{ tpl.description }}</span>
              </ElOption>
            </ElSelect>
          </div>
        </div>
      </div>

      <div class="designer-header__actions">
        <div class="action-group action-group--tools">
          <ElTooltip content="撤销 (Ctrl+Z)" placement="bottom">
            <ElButton v-ripple :disabled="!canUndo" circle @click="handleUndo">
              <ArtSvgIcon icon="ri:arrow-go-back-line" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="重做 (Ctrl+Y)" placement="bottom">
            <ElButton v-ripple :disabled="!canRedo" circle @click="handleRedo">
              <ArtSvgIcon icon="ri:arrow-go-forward-line" />
            </ElButton>
          </ElTooltip>
        </div>

        <div class="action-divider" />

        <div class="action-group action-group--edit">
          <ElTooltip content="复制 (Ctrl+C)" placement="bottom">
            <ElButton v-ripple :disabled="!selectedNode" circle @click="copySelectedNode">
              <ArtSvgIcon icon="ri:file-copy-line" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="粘贴 (Ctrl+V)" placement="bottom">
            <ElButton v-ripple :disabled="!clipboardNode" circle @click="pasteNode">
              <ArtSvgIcon icon="ri:clipboard-line" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="删除 (Del)" placement="bottom">
            <ElButton
              v-ripple
              :disabled="!selectedNode && !selectedEdgeId"
              circle
              @click="handleDeleteSelected"
            >
              <ArtSvgIcon icon="ri:delete-bin-line" />
            </ElButton>
          </ElTooltip>
        </div>

        <div class="action-divider" />

        <div class="action-group">
          <ElButton v-ripple @click="handleValidate">
            <ArtSvgIcon icon="ri:shield-check-line" class="btn-icon" />
            校验
          </ElButton>
          <ElButton v-ripple @click="() => handleAutoLayout()">
            <ArtSvgIcon icon="ri:layout-grid-line" class="btn-icon" />
            布局
          </ElButton>
          <ElButton v-ripple @click="showJson = true">
            <ArtSvgIcon icon="ri:code-s-slash-line" class="btn-icon" />
            JSON
          </ElButton>
          <ElDropdown trigger="click" @command="handleExportCommand">
            <ElButton v-ripple>
              <ArtSvgIcon icon="ri:download-2-line" class="btn-icon" />
              导出
              <ArtSvgIcon icon="ri:arrow-down-s-line" class="btn-icon--suffix" />
            </ElButton>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem command="json">
                  <ArtSvgIcon icon="ri:file-code-line" class="dropdown-icon" />
                  导出 JSON
                </ElDropdownItem>
                <ElDropdownItem command="image">
                  <ArtSvgIcon icon="ri:image-line" class="dropdown-icon" />
                  导出图片
                </ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
          <ElButton v-ripple @click="triggerImport">
            <ArtSvgIcon icon="ri:upload-2-line" class="btn-icon" />
            导入
          </ElButton>
          <input
            ref="importFileRef"
            type="file"
            accept=".json"
            style="display: none"
            @change="handleImportFile"
          />
        </div>

        <div class="action-divider" />

        <div class="action-group action-group--primary">
          <ElButton v-ripple type="primary" :loading="saving" @click="handleSave">
            <ArtSvgIcon icon="ri:save-3-line" class="btn-icon" />
            保存草稿
          </ElButton>
          <ElButton
            v-auth="'workflow:def:publish'"
            v-ripple
            type="success"
            :disabled="!defId"
            :loading="publishing"
            @click="handlePublish"
          >
            <ArtSvgIcon icon="ri:rocket-2-line" class="btn-icon" />
            发布
          </ElButton>
        </div>
      </div>
    </header>

    <div class="designer-body">
      <aside class="designer-panel palette-panel">
        <div class="panel-header">
          <div class="panel-header__icon palette-panel__icon">
            <ArtSvgIcon icon="ri:drag-drop-line" />
          </div>
          <div class="panel-header__text">
            <span class="panel-header__title">节点库</span>
            <span class="panel-header__subtitle">拖拽到画布添加</span>
          </div>
        </div>

        <div class="panel-content">
          <div
            v-for="item in palette"
            :key="item.type"
            class="palette-card"
            :class="`palette-card--${item.type}`"
            draggable="true"
            @dragstart="(e) => onPaletteDragStart(e, item.type)"
          >
            <div class="palette-card__icon-wrap">
              <ArtSvgIcon :icon="item.icon" class="palette-card__icon" />
            </div>
            <div class="palette-card__body">
              <span class="palette-card__label">{{ item.label }}</span>
              <span class="palette-card__hint">{{ item.hint }}</span>
            </div>
            <ArtSvgIcon icon="ri:drag-move-2-line" class="palette-card__grip" />
          </div>

          <div class="shortcuts-card">
            <div class="shortcuts-card__head">
              <ArtSvgIcon icon="ri:keyboard-box-line" />
              <span>快捷键</span>
            </div>
            <ul class="shortcuts-list">
              <li>
                <span class="shortcuts-list__action">复制 / 粘贴</span>
                <span class="shortcuts-list__keys">
                  <kbd>Ctrl</kbd><span>+</span><kbd>C</kbd> <kbd>Ctrl</kbd><span>+</span
                  ><kbd>V</kbd>
                </span>
              </li>
              <li>
                <span class="shortcuts-list__action">删除</span>
                <span class="shortcuts-list__hint">选中后 Del</span>
              </li>
              <li>
                <span class="shortcuts-list__action">撤销 / 重做</span>
                <span class="shortcuts-list__keys">
                  <kbd>Ctrl</kbd><span>+</span><kbd>Z</kbd> <kbd>Ctrl</kbd><span>+</span
                  ><kbd>Y</kbd>
                </span>
              </li>
              <li>
                <span class="shortcuts-list__action">全选</span>
                <span class="shortcuts-list__hint">
                  <kbd>Ctrl</kbd><span>+</span><kbd>A</kbd>
                </span>
              </li>
              <li>
                <span class="shortcuts-list__action">连线</span>
                <span class="shortcuts-list__hint">右圆点 → 左圆点</span>
              </li>
            </ul>
          </div>
        </div>
      </aside>

      <section class="canvas-panel">
        <div class="canvas-toolbar">
          <div class="canvas-toolbar__left">
            <ArtSvgIcon icon="ri:flow-chart" class="canvas-toolbar__icon" />
            <span class="canvas-toolbar__title">流程画布</span>
            <ElTag v-if="selectedNodes.length > 1" size="small" type="primary" effect="light" round>
              已选 {{ selectedNodes.length }} 个节点
            </ElTag>
          </div>
          <div class="canvas-toolbar__center">
            <div v-if="selectedNodes.length > 1" class="align-tools">
              <ElTooltip content="左对齐" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('left')">
                  <ArtSvgIcon icon="ri:align-left" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="水平居中" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('centerH')">
                  <ArtSvgIcon icon="ri:align-center" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="右对齐" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('right')">
                  <ArtSvgIcon icon="ri:align-right" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="顶部对齐" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('top')">
                  <ArtSvgIcon icon="ri:align-top" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="垂直居中" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('centerV')">
                  <ArtSvgIcon icon="ri:align-vertically" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="底部对齐" placement="bottom">
                <ElButton size="small" circle @click="alignNodes('bottom')">
                  <ArtSvgIcon icon="ri:align-bottom" />
                </ElButton>
              </ElTooltip>
              <div class="align-divider" />
              <ElTooltip content="水平均匀分布" placement="bottom">
                <ElButton size="small" circle @click="distributeNodes('horizontal')">
                  <ArtSvgIcon icon="ri:space" />
                </ElButton>
              </ElTooltip>
              <ElTooltip content="垂直均匀分布" placement="bottom">
                <ElButton size="small" circle @click="distributeNodes('vertical')">
                  <ArtSvgIcon icon="ri:space" style="transform: rotate(90deg)" />
                </ElButton>
              </ElTooltip>
            </div>
          </div>
          <div class="canvas-toolbar__stats">
            <span class="stat-chip">
              <ArtSvgIcon icon="ri:circle-fill" class="stat-chip__dot stat-chip__dot--node" />
              {{ spfNodes.length }} 节点
            </span>
            <span class="stat-chip">
              <ArtSvgIcon icon="ri:circle-fill" class="stat-chip__dot stat-chip__dot--edge" />
              {{ spfEdges.length }} 连线
            </span>
          </div>
        </div>

        <div class="canvas-stage" @click="onCanvasClick">
          <VueFlow
            ref="vueFlowRef"
            :nodes="displayNodes"
            :edges="flowEdges"
            :node-types="nodeTypes as any"
            :edge-types="edgeTypes as any"
            :connection-mode="ConnectionMode.Loose"
            :connection-radius="28"
            :connection-line-type="ConnectionLineType.Bezier"
            :nodes-connectable="true"
            :edges-selectable="true"
            :is-valid-connection="validateConnection"
            :delete-key-code="null"
            :default-edge-options="defaultEdgeOptions"
            :selection-key-code="selectionKeyCode"
            :multi-selection-key-code="multiSelectionKeyCode"
            fit-view-on-init
            @node-click="onNodeClick"
            @edge-click="onEdgeClick"
            @pane-click="clearSelection"
            @connect="onConnect"
            @nodes-change="onNodesChange"
            @edges-change="onEdgesChange"
            @drop="onCanvasDrop"
            @dragover="onCanvasDragOver"
            @selection-start="onSelectionStart"
            @selection-end="onSelectionEnd"
            @selection-context-menu="onSelectionContextMenu"
            @node-drag-start="onNodeDragStart"
            @node-drag-end="onNodeDragEnd"
          >
            <Background :gap="24" :size="1.5" pattern-color="var(--wf-grid-color)" />
            <Controls position="bottom-left" class="canvas-controls" />
            <MiniMap
              position="bottom-right"
              :pannable="true"
              :zoomable="true"
              class="designer-minimap"
            />
            <FlowCanvasHelper ref="canvasHelperRef" :layout-tick="layoutTick" />
          </VueFlow>
        </div>
      </section>

      <aside class="designer-panel props-panel">
        <div class="panel-header">
          <div
            class="panel-header__icon props-panel__icon"
            :class="{ 'props-panel__icon--edge': selectedEdge }"
          >
            <ArtSvgIcon :icon="selectedEdge ? 'ri:links-line' : 'ri:settings-4-line'" />
          </div>
          <div class="panel-header__text">
            <span class="panel-header__title">{{ selectedEdge ? '连线属性' : '节点属性' }}</span>
            <span class="panel-header__subtitle">
              {{ selectedEdge ? '编辑分支条件' : '编辑节点配置' }}
            </span>
          </div>
        </div>
        <div class="panel-content panel-content--props">
          <EdgePropsPanel
            v-if="selectedEdge"
            v-model:edge="selectedEdge"
            v-model:edges="spfEdges"
            v-model:nodes="spfNodes"
            @commit="saveHistory"
            @delete="removeSelectedEdge"
          />
          <NodePropsPanel
            v-else
            v-model:node="selectedNode"
            v-model:edges="spfEdges"
            v-model:nodes="spfNodes"
            @commit="saveHistory"
            @connect-to="(targetId) => connectNodes(selectedNode!.id, targetId)"
            @select-edge="selectEdgeById"
            @delete-edge="removeEdgeById"
          />
        </div>
      </aside>
    </div>

    <!-- JSON 预览对话框 -->
    <ElDialog v-model="showJson" title="SPF 设计态 JSON" width="760px" class="json-dialog">
      <div class="json-dialog__toolbar">
        <span class="json-dialog__hint">只读预览，可用于调试与导出</span>
        <ElButton size="small" @click="copyJson">
          <ArtSvgIcon icon="ri:file-copy-line" class="btn-icon" />
          复制
        </ElButton>
      </div>
      <ElInput
        type="textarea"
        :rows="20"
        :model-value="jsonPreview"
        readonly
        class="json-textarea"
      />
    </ElDialog>

    <!-- 导出图片对话框 -->
    <ElDialog v-model="showExportImage" title="导出流程图" width="520px">
      <div class="export-image-preview">
        <p class="export-hint">点击下方按钮下载流程图为 PNG 图片</p>
        <ElButton type="primary" @click="exportAsImage">
          <ArtSvgIcon icon="ri:download-2-line" class="btn-icon" />
          下载 PNG
        </ElButton>
      </div>
    </ElDialog>

    <!-- 模拟运行对话框 -->
    <ElDialog v-model="showSimulation" title="流程模拟运行" width="700px" class="simulation-dialog">
      <WorkflowSimulation
        v-if="showSimulation"
        :definition="buildDefinition()"
        :nodes="spfNodes"
        :edges="spfEdges"
        @close="showSimulation = false"
      />
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import {
    VueFlow,
    ConnectionMode,
    Position,
    MarkerType,
    ConnectionLineType,
    type Connection,
    type Edge,
    type EdgeChange,
    type Node,
    type NodeChange
  } from '@vue-flow/core'
  import { Background } from '@vue-flow/background'
  import { Controls } from '@vue-flow/controls'
  import { MiniMap } from '@vue-flow/minimap'
  import { useManualRefHistory } from '@vueuse/core'
  import { markRaw, ref, computed, watch, reactive, onMounted, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { v4 as uuidv4 } from 'uuid'
  import WfFlowNode from './components/WfFlowNode.vue'
  import WfFlowEdge from './components/WfFlowEdge.vue'
  import NodePropsPanel from './components/NodePropsPanel.vue'
  import EdgePropsPanel from './components/EdgePropsPanel.vue'
  import FlowCanvasHelper from './components/FlowCanvasHelper.vue'
  import WorkflowSimulation from './components/WorkflowSimulation.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import {
    BIZ_MODULE_OPTIONS,
    createDefaultDefinition,
    NODE_DIMENSIONS,
    type FlowNodeType,
    type SpfDefinition,
    type SpfEdge,
    type SpfNode
  } from '../utils/flow-types'
  import { validateFlowDefinitionDetailed } from '../utils/flow-validator'
  import { checkConnection, canDeleteNode } from '../utils/flow-constraints'
  import { layoutFlowNodes } from '../utils/flow-layout'
  import { toVueFlowEdge, WF_EDGE_TYPE, isValidSpfEdge } from '../utils/flow-vue-flow'
  import { FLOW_TEMPLATES, applyFlowTemplate } from '../utils/flow-templates'
  import {
    fetchWorkflowDefDetail,
    fetchWorkflowDefPublish,
    fetchWorkflowDefSave
  } from '@/api/workflow/def'

  import '@vue-flow/core/dist/style.css'
  import '@vue-flow/core/dist/theme-default.css'
  import '@vue-flow/controls/dist/style.css'
  import '@vue-flow/minimap/dist/style.css'

  defineOptions({ name: 'WorkflowDesigner' })

  interface DesignerSnapshot {
    meta: {
      processCode: string
      processName: string
      bizModule: SpfDefinition['bizModule']
    }
    nodes: SpfNode[]
    edges: SpfEdge[]
  }

  const route = useRoute()
  const router = useRouter()
  const vueFlowRef = ref()
  const canvasHelperRef = ref()
  const importFileRef = ref<HTMLInputElement>()

  const defId = ref<number | undefined>(route.query.defId ? Number(route.query.defId) : undefined)
  const saving = ref(false)
  const publishing = ref(false)
  const showJson = ref(false)
  const showExportImage = ref(false)
  const showSimulation = ref(false)
  const layoutTick = ref(0)
  const selectedTemplateId = ref('')
  const clipboardNode = ref<SpfNode | null>(null)
  const selectedNodes = ref<SpfNode[]>([])
  const isSelecting = ref(false)

  const selectionKeyCode = ref<string | null>(null)
  const multiSelectionKeyCode = ref<string | null>('Shift')

  const meta = reactive({
    processCode: '',
    processName: '',
    bizModule: 'mall' as SpfDefinition['bizModule']
  })

  const spfNodes = ref<SpfNode[]>(createDefaultDefinition().nodes)
  const spfEdges = ref<SpfEdge[]>([])
  const selectedNode = ref<SpfNode | null>(null)
  const selectedEdgeId = ref<string | null>(null)
  const nodeErrors = ref<Record<string, string[]>>({})

  const selectedEdge = computed({
    get: () => {
      if (!selectedEdgeId.value) return null
      return spfEdges.value.find((e) => e.id === selectedEdgeId.value) || null
    },
    set: (edge) => {
      if (!edge) {
        selectedEdgeId.value = null
        return
      }
      const index = spfEdges.value.findIndex((e) => e.id === edge.id)
      if (index >= 0) spfEdges.value[index] = edge
    }
  })

  const nodeTypes = { wf: markRaw(WfFlowNode) }
  const edgeTypes = { [WF_EDGE_TYPE]: markRaw(WfFlowEdge) }

  const displayNodes = computed(() => spfNodes.value.map(buildFlowNode))

  const flowEdges = ref<Edge[]>([])

  function getNodeIdSet() {
    return new Set(spfNodes.value.map((n) => n.id))
  }

  function sanitizeSpfEdges(edges: SpfEdge[]) {
    const nodeIds = getNodeIdSet()
    return edges.filter((e) => isValidSpfEdge(e, nodeIds))
  }

  function syncFlowEdges() {
    const nodeIds = getNodeIdSet()
    flowEdges.value = spfEdges.value.filter((e) => isValidSpfEdge(e, nodeIds)).map(buildFlowEdge)
  }

  const palette = [
    {
      type: 'approval' as FlowNodeType,
      label: '审批节点',
      hint: '配置审批人与模式',
      icon: 'ri:user-star-line'
    },
    {
      type: 'condition' as FlowNodeType,
      label: '条件分支',
      hint: '按变量走不同路径',
      icon: 'ri:git-branch-line'
    }
  ]

  const defaultEdgeOptions = {
    type: WF_EDGE_TYPE,
    animated: false,
    markerEnd: MarkerType.ArrowClosed,
    style: { stroke: '#64748b', strokeWidth: 2 }
  }

  const edgeStroke = '#64748b'
  const edgeStrokeSelected = '#409eff'

  function buildFlowNode(n: SpfNode): Node {
    const dim = NODE_DIMENSIONS[n.type]
    const isSelected =
      selectedNode.value?.id === n.id || selectedNodes.value.some((sn) => sn.id === n.id)
    return {
      id: n.id,
      type: 'wf',
      position: { x: n.position.x, y: n.position.y },
      width: dim.width,
      height: dim.height,
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
      deletable: n.type !== 'start',
      connectable: true,
      selectable: true,
      selected: isSelected,
      data: {
        label: n.data.name || n.type,
        nodeType: n.type,
        hasError: (nodeErrors.value[n.id]?.length ?? 0) > 0,
        errorMessage: nodeErrors.value[n.id]?.join('；')
      }
    } as Node
  }

  function buildFlowEdge(e: SpfEdge): Edge {
    const selected = selectedEdgeId.value === e.id
    const stroke = selected ? edgeStrokeSelected : edgeStroke
    return toVueFlowEdge(e, {
      type: WF_EDGE_TYPE,
      label: e.data?.condition?.type === 'default' ? '默认' : e.data?.condition?.field || '',
      selected,
      selectable: true,
      style: { stroke, strokeWidth: selected ? 2.5 : 2 },
      markerEnd: MarkerType.ArrowClosed,
      labelStyle: { fill: '#606266', fontSize: 11, fontWeight: 500 },
      labelShowBg: true,
      labelBgStyle: { fill: '#ffffff', fillOpacity: 0.92 },
      labelBgPadding: [4, 6] as [number, number],
      labelBgBorderRadius: 4
    } as Partial<Edge>) as Edge
  }

  watch(
    spfEdges,
    () => {
      syncFlowEdges()
      layoutTick.value += 1
    },
    { deep: true }
  )
  watch(selectedEdgeId, syncFlowEdges)
  watch(
    spfNodes,
    () => {
      syncFlowEdges()
      layoutTick.value += 1
    },
    { deep: true }
  )

  function validateConnection(connection: Connection) {
    if (!connection.source || !connection.target) return false
    return checkConnection(connection.source, connection.target, spfNodes.value, spfEdges.value).ok
  }

  const jsonPreview = computed(() => JSON.stringify(buildDefinition(), null, 2))

  function getSnapshot(): DesignerSnapshot {
    return {
      meta: { ...meta },
      nodes: JSON.parse(JSON.stringify(spfNodes.value)),
      edges: JSON.parse(JSON.stringify(spfEdges.value))
    }
  }

  function applySnapshot(snapshot: DesignerSnapshot) {
    Object.assign(meta, snapshot.meta)
    spfNodes.value = snapshot.nodes
    spfEdges.value = sanitizeSpfEdges(snapshot.edges)
    selectedNode.value = null
    selectedEdgeId.value = null
    selectedNodes.value = []
    syncFlowEdges()
    refreshValidation()
  }

  const historySnapshot = ref(getSnapshot())
  const { undo, redo, commit, canUndo, canRedo } = useManualRefHistory(historySnapshot, {
    clone: true,
    capacity: 40
  })

  watch(historySnapshot, (snapshot) => {
    if (
      JSON.stringify(snapshot.nodes) !== JSON.stringify(spfNodes.value) ||
      JSON.stringify(snapshot.edges) !== JSON.stringify(spfEdges.value) ||
      snapshot.meta.processCode !== meta.processCode ||
      snapshot.meta.processName !== meta.processName ||
      snapshot.meta.bizModule !== meta.bizModule
    ) {
      applySnapshot(snapshot)
    }
  })

  function saveHistory() {
    historySnapshot.value = getSnapshot()
    commit()
    refreshValidation()
  }

  function handleUndo() {
    undo()
  }

  function handleRedo() {
    redo()
  }

  onMounted(async () => {
    window.addEventListener('keydown', onKeyDown)
    if (defId.value) {
      await loadDefinition(defId.value)
    } else {
      syncFlowEdges()
    }
    historySnapshot.value = getSnapshot()
    commit()
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', onKeyDown)
  })

  async function loadDefinition(id: number) {
    const data = await fetchWorkflowDefDetail(id)
    defId.value = data.defId
    meta.processCode = data.processCode || ''
    meta.processName = data.processName || ''
    meta.bizModule = (data.bizModule as SpfDefinition['bizModule']) || 'mall'
    if (data.defJson) {
      const parsed = JSON.parse(data.defJson) as SpfDefinition
      spfNodes.value = parsed.nodes || []
      spfEdges.value = sanitizeSpfEdges(parsed.edges || [])
      handleAutoLayout(false)
      syncFlowEdges()
    }
    refreshValidation()
  }

  function buildDefinition(): SpfDefinition {
    return {
      schemaVersion: '1.0',
      processCode: meta.processCode,
      processName: meta.processName,
      bizModule: meta.bizModule,
      nodes: spfNodes.value,
      edges: spfEdges.value
    }
  }

  function refreshValidation() {
    nodeErrors.value = validateFlowDefinitionDetailed(buildDefinition()).nodeErrors
  }

  function createNode(
    type: FlowNodeType,
    position: { x: number; y: number },
    customData?: Partial<SpfNode['data']>
  ) {
    const id = `${type}_${uuidv4().slice(0, 8)}`
    const node: SpfNode = {
      id,
      type,
      position,
      data: {
        name:
          customData?.name ||
          (type === 'approval' ? '审批节点' : type === 'condition' ? '条件分支' : type),
        approveMode: customData?.approveMode || 'OR',
        assigneeRule:
          customData?.assigneeRule ||
          (type === 'approval' ? { type: 'STARTER_DEPT_LEADER' } : undefined)
      }
    }
    spfNodes.value.push(node)
    selectedNode.value = node
    selectedNodes.value = [node]
    saveHistory()
    return node
  }

  function onPaletteDragStart(event: DragEvent, type: FlowNodeType) {
    event.dataTransfer?.setData('application/workflow-node', type)
    event.dataTransfer!.effectAllowed = 'move'
  }

  function onCanvasDragOver(event: DragEvent) {
    event.preventDefault()
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'
  }

  function onCanvasDrop(event: DragEvent) {
    event.preventDefault()
    const type = event.dataTransfer?.getData('application/workflow-node') as FlowNodeType
    if (!type || (type !== 'approval' && type !== 'condition')) return
    const bounds = (event.currentTarget as HTMLElement).getBoundingClientRect()
    createNode(type, {
      x: event.clientX - bounds.left - 60,
      y: event.clientY - bounds.top - 24
    })
  }

  function onNodeClick({ node }: { node: Node }) {
    selectedNode.value = spfNodes.value.find((n) => n.id === node.id) || null
    selectedEdgeId.value = null
  }

  function onNodeDragStart() {
    canvasHelperRef.value?.onDragStart()
  }

  function onNodeDragEnd() {
    canvasHelperRef.value?.onDragEnd()
  }

  function onEdgeClick({ edge }: { edge: Edge }) {
    selectedEdgeId.value = edge.id
    selectedNode.value = null
    selectedNodes.value = []
  }

  function clearSelection() {
    selectedNode.value = null
    selectedEdgeId.value = null
    selectedNodes.value = []
  }

  function onCanvasClick(event: MouseEvent) {
    // 如果点击的是画布空白区域，清除选择
    const target = event.target as HTMLElement
    if (target.closest('.vue-flow__pane')) {
      clearSelection()
    }
  }

  function onConnect(connection: Connection) {
    if (!connection.source || !connection.target) return
    connectNodes(connection.source, connection.target)
  }

  function connectNodes(sourceId: string, targetId: string) {
    const check = checkConnection(sourceId, targetId, spfNodes.value, spfEdges.value)
    if (!check.ok) {
      ElMessage.warning(check.message || '无法建立连线')
      return
    }
    const newEdge: SpfEdge = {
      id: `e_${uuidv4().slice(0, 8)}`,
      source: sourceId,
      target: targetId,
      data: {}
    }
    spfEdges.value = [...spfEdges.value, newEdge]
    selectedEdgeId.value = newEdge.id
    selectedNode.value = null
    selectedNodes.value = []
    syncFlowEdges()
    saveHistory()
    layoutTick.value += 1
  }

  function onNodesChange(changes: NodeChange[]) {
    let moved = false
    changes.forEach((change) => {
      if (change.type === 'position' && change.position && change.dragging === false) {
        const sn = spfNodes.value.find((n) => n.id === change.id)
        if (sn) {
          sn.position = { ...change.position }
          moved = true
        }
      }
      if (change.type === 'remove') {
        removeNodeById(change.id)
      }
      if (change.type === 'select') {
        const node = spfNodes.value.find((n) => n.id === change.id)
        if (node) {
          if (change.selected) {
            if (!selectedNodes.value.find((n) => n.id === node.id)) {
              selectedNodes.value.push(node)
            }
          } else {
            selectedNodes.value = selectedNodes.value.filter((n) => n.id !== node.id)
          }
        }
      }
    })
    if (moved) saveHistory()
  }

  function onEdgesChange(changes: EdgeChange[]) {
    // 忽略 Vue Flow 内部 connect 产生的 add 变更（缺少 source/target 会导致警告）
    if (!changes.some((c) => c.type === 'remove')) return

    const removeIds = changes.filter((c) => c.type === 'remove').map((c) => c.id)
    spfEdges.value = spfEdges.value.filter((e) => !removeIds.includes(e.id))
    if (selectedEdgeId.value && removeIds.includes(selectedEdgeId.value)) {
      selectedEdgeId.value = null
    }
    syncFlowEdges()
    saveHistory()
  }

  function onSelectionStart() {
    isSelecting.value = true
  }

  function onSelectionEnd({ nodes }: { nodes: Node[] }) {
    isSelecting.value = false
    selectedNodes.value = nodes
      .map((n) => spfNodes.value.find((sn) => sn.id === n.id))
      .filter(Boolean) as SpfNode[]
    if (selectedNodes.value.length === 1) {
      selectedNode.value = selectedNodes.value[0]
    }
  }

  function onSelectionContextMenu({ event, nodes }: { event: MouseEvent; nodes: Node[] }) {
    event.preventDefault()
    selectedNodes.value = nodes
      .map((n) => spfNodes.value.find((sn) => sn.id === n.id))
      .filter(Boolean) as SpfNode[]
  }

  function removeNodeById(nodeId: string) {
    const node = spfNodes.value.find((n) => n.id === nodeId)
    if (!node) return
    const check = canDeleteNode(node)
    if (!check.ok) {
      ElMessage.warning(check.message || '无法删除')
      return
    }
    spfNodes.value = spfNodes.value.filter((n) => n.id !== nodeId)
    spfEdges.value = spfEdges.value.filter((e) => e.source !== nodeId && e.target !== nodeId)
    if (selectedNode.value?.id === nodeId) selectedNode.value = null
    selectedNodes.value = selectedNodes.value.filter((n) => n.id !== nodeId)
    saveHistory()
  }

  function removeEdgeById(edgeId: string) {
    spfEdges.value = spfEdges.value.filter((e) => e.id !== edgeId)
    if (selectedEdgeId.value === edgeId) selectedEdgeId.value = null
    syncFlowEdges()
    saveHistory()
    ElMessage.success('连线已删除')
  }

  function removeSelectedEdge() {
    if (!selectedEdgeId.value) return
    removeEdgeById(selectedEdgeId.value)
  }

  function selectEdgeById(edgeId: string) {
    selectedEdgeId.value = edgeId
    selectedNode.value = null
    selectedNodes.value = []
  }

  function handleDeleteSelected() {
    if (selectedEdgeId.value) {
      removeSelectedEdge()
    } else if (selectedNode.value) {
      removeNodeById(selectedNode.value.id)
    }
  }

  function onKeyDown(event: KeyboardEvent) {
    const target = event.target as HTMLElement
    if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable) {
      return
    }

    // 撤销
    if ((event.ctrlKey || event.metaKey) && event.key === 'z') {
      event.preventDefault()
      handleUndo()
      return
    }
    // 重做
    if (
      (event.ctrlKey || event.metaKey) &&
      (event.key === 'y' || (event.shiftKey && event.key === 'z'))
    ) {
      event.preventDefault()
      handleRedo()
      return
    }
    // 复制
    if ((event.ctrlKey || event.metaKey) && event.key === 'c') {
      event.preventDefault()
      copySelectedNode()
      return
    }
    // 粘贴
    if ((event.ctrlKey || event.metaKey) && event.key === 'v') {
      event.preventDefault()
      pasteNode()
      return
    }
    // 全选
    if ((event.ctrlKey || event.metaKey) && event.key === 'a') {
      event.preventDefault()
      selectAllNodes()
      return
    }
    // 删除
    if (event.key === 'Delete' || event.key === 'Backspace') {
      if (selectedEdgeId.value) {
        removeSelectedEdge()
      } else if (selectedNode.value) {
        removeNodeById(selectedNode.value.id)
      }
    }
  }

  // 复制选中节点
  function copySelectedNode() {
    if (!selectedNode.value) {
      ElMessage.warning('请先选择一个节点')
      return
    }
    clipboardNode.value = JSON.parse(JSON.stringify(selectedNode.value))
    ElMessage.success('节点已复制')
  }

  // 粘贴节点
  function pasteNode() {
    if (!clipboardNode.value) {
      ElMessage.warning('剪贴板为空')
      return
    }
    const original = clipboardNode.value
    const newNode = createNode(
      original.type,
      {
        x: original.position.x + 30,
        y: original.position.y + 30
      },
      original.data
    )

    // 更新剪贴板位置，以便下次粘贴时偏移
    clipboardNode.value = {
      ...original,
      position: { x: original.position.x + 30, y: original.position.y + 30 }
    }

    ElMessage.success('节点已粘贴')
  }

  // 全选节点
  function selectAllNodes() {
    selectedNodes.value = [...spfNodes.value]
    if (spfNodes.value.length > 0) {
      selectedNode.value = spfNodes.value[0]
    }
  }

  // 节点对齐
  function alignNodes(align: 'left' | 'right' | 'top' | 'bottom' | 'centerH' | 'centerV') {
    if (selectedNodes.value.length < 2) {
      ElMessage.warning('请至少选择两个节点')
      return
    }

    const nodes = selectedNodes.value
    const positions = nodes.map((n) => n.position)

    let targetValue: number

    switch (align) {
      case 'left':
        targetValue = Math.min(...positions.map((p) => p.x))
        nodes.forEach((n) => {
          n.position.x = targetValue
        })
        break
      case 'right': {
        const maxX = Math.max(...positions.map((p) => p.x))
        const maxWidth = Math.max(...nodes.map((n) => NODE_DIMENSIONS[n.type].width))
        nodes.forEach((n) => {
          n.position.x = maxX + maxWidth - NODE_DIMENSIONS[n.type].width
        })
        break
      }
      case 'top':
        targetValue = Math.min(...positions.map((p) => p.y))
        nodes.forEach((n) => {
          n.position.y = targetValue
        })
        break
      case 'bottom': {
        const maxY = Math.max(...positions.map((p) => p.y))
        const maxHeight = Math.max(...nodes.map((n) => NODE_DIMENSIONS[n.type].height))
        nodes.forEach((n) => {
          n.position.y = maxY + maxHeight - NODE_DIMENSIONS[n.type].height
        })
        break
      }
      case 'centerH': {
        const minX = Math.min(...positions.map((p) => p.x))
        const maxX = Math.max(...positions.map((p) => p.x))
        const maxWidth = Math.max(...nodes.map((n) => NODE_DIMENSIONS[n.type].width))
        const center = minX + (maxX + maxWidth - minX) / 2
        nodes.forEach((n) => {
          n.position.x = center - NODE_DIMENSIONS[n.type].width / 2
        })
        break
      }
      case 'centerV': {
        const minY = Math.min(...positions.map((p) => p.y))
        const maxY = Math.max(...positions.map((p) => p.y))
        const maxHeight = Math.max(...nodes.map((n) => NODE_DIMENSIONS[n.type].height))
        const center = minY + (maxY + maxHeight - minY) / 2
        nodes.forEach((n) => {
          n.position.y = center - NODE_DIMENSIONS[n.type].height / 2
        })
        break
      }
    }

    syncFlowEdges()
    saveHistory()
    ElMessage.success('对齐完成')
  }

  // 节点均匀分布
  function distributeNodes(direction: 'horizontal' | 'vertical') {
    if (selectedNodes.value.length < 3) {
      ElMessage.warning('请至少选择三个节点')
      return
    }

    const nodes = [...selectedNodes.value].sort((a, b) => {
      if (direction === 'horizontal') {
        return a.position.x - b.position.x
      }
      return a.position.y - b.position.y
    })

    if (direction === 'horizontal') {
      const minX = nodes[0].position.x
      const maxX = nodes[nodes.length - 1].position.x
      const totalWidth = maxX - minX
      const gap = totalWidth / (nodes.length - 1)
      nodes.forEach((n, i) => {
        n.position.x = minX + gap * i
      })
    } else {
      const minY = nodes[0].position.y
      const maxY = nodes[nodes.length - 1].position.y
      const totalHeight = maxY - minY
      const gap = totalHeight / (nodes.length - 1)
      nodes.forEach((n, i) => {
        n.position.y = minY + gap * i
      })
    }

    syncFlowEdges()
    saveHistory()
    ElMessage.success('分布完成')
  }

  // 导出命令处理
  function handleExportCommand(command: string) {
    if (command === 'json') {
      exportAsJson()
    } else if (command === 'image') {
      showExportImage.value = true
    }
  }

  // 导出 JSON
  function exportAsJson() {
    const dataStr = JSON.stringify(buildDefinition(), null, 2)
    const blob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${meta.processCode || 'workflow'}_${Date.now()}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('JSON 导出成功')
  }

  // 导出图片
  async function exportAsImage() {
    try {
      const canvas = document.querySelector('.vue-flow__viewport') as HTMLElement
      if (!canvas) {
        ElMessage.warning('无法找到画布元素')
        return
      }

      // 使用 html2canvas 或类似库导出
      // 这里使用简单的 SVG 导出方式
      const svgData = new XMLSerializer().serializeToString(canvas)
      const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' })
      const url = URL.createObjectURL(svgBlob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${meta.processCode || 'workflow'}_${Date.now()}.svg`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      ElMessage.success('图片导出成功')
      showExportImage.value = false
    } catch (error) {
      ElMessage.error('导出失败')
    }
  }

  // 触发导入
  function triggerImport() {
    importFileRef.value?.click()
  }

  // 处理导入文件
  function handleImportFile(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return

    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const content = e.target?.result as string
        const parsed = JSON.parse(content) as SpfDefinition

        if (!parsed.nodes || !Array.isArray(parsed.nodes)) {
          throw new Error('无效的流程定义文件')
        }

        ElMessageBox.confirm('导入将覆盖当前画布，是否继续？', '导入流程', {
          type: 'warning'
        })
          .then(() => {
            meta.processCode = parsed.processCode || ''
            meta.processName = parsed.processName || ''
            meta.bizModule = parsed.bizModule || 'mall'
            spfNodes.value = parsed.nodes || []
            spfEdges.value = sanitizeSpfEdges(parsed.edges || [])
            handleAutoLayout(false)
            syncFlowEdges()
            saveHistory()
            ElMessage.success('导入成功')
          })
          .catch(() => {
            // 用户取消
          })
      } catch (error) {
        ElMessage.error('文件解析失败，请检查文件格式')
      }
    }
    reader.readAsText(file)
    input.value = ''
  }

  function handleAutoLayout(showMessage = true) {
    spfNodes.value = layoutFlowNodes(spfNodes.value, spfEdges.value)
    layoutTick.value += 1
    saveHistory()
    if (showMessage) ElMessage.success('已自动布局')
  }

  async function handleApplyTemplate(templateId: string) {
    if (!templateId) return
    const hasContent =
      spfNodes.value.length > 2 || spfEdges.value.length > 0 || meta.processCode || meta.processName
    if (hasContent) {
      try {
        await ElMessageBox.confirm('应用模板将覆盖当前画布，是否继续？', '应用模板', {
          type: 'warning'
        })
      } catch {
        return
      }
    }
    const applied = applyFlowTemplate(templateId)
    if (!applied) return
    spfNodes.value = applied.nodes
    spfEdges.value = applied.edges
    handleAutoLayout(false)
    saveHistory()
    selectedTemplateId.value = ''
    ElMessage.success('模板已应用')
  }

  function handleValidate() {
    const result = validateFlowDefinitionDetailed(buildDefinition())
    nodeErrors.value = result.nodeErrors
    if (result.errors.length) {
      ElMessage.warning(result.errors.join('；'))
      return
    }
    ElMessage.success('校验通过')
  }

  async function handleSave() {
    const result = validateFlowDefinitionDetailed(buildDefinition())
    nodeErrors.value = result.nodeErrors
    if (result.errors.length) {
      ElMessage.warning(result.errors.join('；'))
      return
    }
    saving.value = true
    try {
      const def = buildDefinition()
      const res = await fetchWorkflowDefSave({
        defId: defId.value,
        processCode: meta.processCode,
        processName: meta.processName,
        bizModule: meta.bizModule,
        defJson: JSON.stringify(def)
      })
      const id = (res as { data?: number }).data ?? (res as unknown as number)
      defId.value = id
      ElMessage.success('保存成功')
      router.replace({ query: { defId: String(id) } })
    } finally {
      saving.value = false
    }
  }

  async function handlePublish() {
    if (!defId.value) return
    publishing.value = true
    try {
      await handleSave()
      await fetchWorkflowDefPublish(defId.value)
      ElMessage.success('发布成功')
    } finally {
      publishing.value = false
    }
  }

  async function copyJson() {
    try {
      await navigator.clipboard.writeText(jsonPreview.value)
      ElMessage.success('已复制到剪贴板')
    } catch {
      ElMessage.warning('复制失败，请手动选择文本')
    }
  }
</script>

<style scoped lang="scss">
  .workflow-designer {
    --wf-panel-radius: 14px;
    --wf-panel-border: 1px solid var(--el-border-color-lighter);
    --wf-panel-shadow: 0 4px 20px rgb(15 23 42 / 6%);
    --wf-grid-color: rgb(100 116 139 / 12%);
    --wf-canvas-bg: var(--el-fill-color-lighter);

    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: var(--art-page-padding);
    background: var(--default-bg-color);
  }

  html.dark .workflow-designer {
    --wf-grid-color: rgb(148 163 184 / 14%);
    --wf-panel-shadow: 0 4px 24px rgb(0 0 0 / 28%);
  }

  /* ── Header ── */
  .designer-header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px 20px;
    padding: 12px 16px;
    border: var(--wf-panel-border);
    border-radius: var(--wf-panel-radius);
    background: var(--default-box-color);
    box-shadow: var(--wf-panel-shadow);
  }

  .designer-header__left {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 280px;
  }

  .designer-header__brand {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .designer-header__logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 12px;
    font-size: 20px;
    color: #fff;
    background: linear-gradient(135deg, var(--el-color-primary) 0%, #6366f1 100%);
    box-shadow: 0 4px 14px rgb(64 158 255 / 35%);
  }

  .designer-header__title-group {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .designer-header__title {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.01em;
  }

  .designer-header__divider {
    width: 1px;
    height: 36px;
    background: var(--el-border-color-lighter);
  }

  .meta-fields {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    gap: 10px 14px;
  }

  .meta-field {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .meta-field__label {
    font-size: 11px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }

  .meta-field__input {
    width: 160px;

    &--code {
      width: 180px;
    }

    &--sm {
      width: 110px;
    }

    &--tpl {
      width: 170px;
    }
  }

  .designer-header__actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .action-group {
    display: flex;
    align-items: center;
    gap: 6px;

    &--tools {
      gap: 4px;
    }

    &--edit {
      gap: 4px;
    }
  }

  .action-divider {
    width: 1px;
    height: 24px;
    background: var(--el-border-color-lighter);
  }

  .btn-icon {
    margin-right: 4px;
    font-size: 15px;
    vertical-align: -2px;
  }

  .btn-icon--suffix {
    margin-right: 0;
    margin-left: 2px;
    font-size: 12px;
  }

  .dropdown-icon {
    margin-right: 6px;
    font-size: 14px;
    vertical-align: -2px;
  }

  .tpl-desc {
    margin-left: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  /* ── Body layout ── */
  .designer-body {
    flex: 1;
    display: grid;
    grid-template-columns: 220px 1fr 360px;
    gap: 10px;
    min-height: 0;
  }

  .designer-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    border: var(--wf-panel-border);
    border-radius: var(--wf-panel-radius);
    background: var(--default-box-color);
    box-shadow: var(--wf-panel-shadow);
    overflow: hidden;
  }

  .panel-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-bottom: var(--wf-panel-border);
    background: linear-gradient(180deg, var(--el-fill-color-blank) 0%, transparent 100%);
  }

  .panel-header__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    font-size: 18px;
    flex-shrink: 0;
  }

  .palette-panel__icon {
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  .props-panel__icon {
    color: #8b5cf6;
    background: rgb(139 92 246 / 12%);

    &--edge {
      color: #06b6d4;
      background: rgb(6 182 212 / 12%);
    }
  }

  .panel-header__text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  .panel-header__title {
    font-size: 14px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }

  .panel-header__subtitle {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .panel-content {
    flex: 1;
    min-height: 0;
    padding: 12px;
    overflow: auto;

    &--props {
      padding: 0;
    }
  }

  /* ── Palette ── */
  .palette-card {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;
    padding: 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 12px;
    background: var(--el-fill-color-blank);
    cursor: grab;
    user-select: none;
    transition:
      border-color 0.2s,
      box-shadow 0.2s,
      transform 0.18s ease;

    &:last-of-type {
      margin-bottom: 0;
    }

    &:hover {
      border-color: transparent;
      box-shadow:
        0 0 0 1px color-mix(in srgb, var(--palette-accent) 40%, transparent),
        0 8px 24px color-mix(in srgb, var(--palette-accent) 14%, transparent);
      transform: translateY(-2px);
    }

    &:active {
      cursor: grabbing;
      transform: translateY(0);
    }

    &--approval {
      --palette-accent: #409eff;
    }

    &--condition {
      --palette-accent: #e6a23c;
    }
  }

  .palette-card__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 11px;
    background: color-mix(in srgb, var(--palette-accent) 12%, transparent);
    flex-shrink: 0;
  }

  .palette-card__icon {
    font-size: 20px;
    color: var(--palette-accent);
  }

  .palette-card__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .palette-card__label {
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .palette-card__hint {
    font-size: 11px;
    color: var(--el-text-color-secondary);
  }

  .palette-card__grip {
    flex-shrink: 0;
    font-size: 16px;
    color: var(--el-text-color-placeholder);
    opacity: 0.5;
    transition: opacity 0.2s;
  }

  .palette-card:hover .palette-card__grip {
    opacity: 1;
  }

  .shortcuts-card {
    margin-top: 16px;
    padding: 12px;
    border-radius: 12px;
    background: var(--el-fill-color-light);
    border: 1px dashed var(--el-border-color);
  }

  .shortcuts-card__head {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 10px;
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-regular);
  }

  .shortcuts-list {
    margin: 0;
    padding: 0;
    list-style: none;

    li {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      padding: 6px 0;
      font-size: 11px;
      border-bottom: 1px solid var(--el-border-color-extra-light);

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }
    }

    kbd {
      padding: 2px 6px;
      border-radius: 5px;
      border: 1px solid var(--el-border-color);
      background: var(--el-bg-color);
      font-family: ui-monospace, monospace;
      font-size: 10px;
      color: var(--el-text-color-regular);
      box-shadow: 0 1px 0 var(--el-border-color-lighter);
    }
  }

  .shortcuts-list__action {
    color: var(--el-text-color-secondary);
  }

  .shortcuts-list__keys {
    display: flex;
    align-items: center;
    gap: 2px;
    flex-wrap: wrap;
    justify-content: flex-end;

    span {
      color: var(--el-text-color-placeholder);
      font-size: 10px;
    }
  }

  .shortcuts-list__hint {
    font-size: 10px;
    color: var(--el-text-color-placeholder);
  }

  /* ── Canvas ── */
  .canvas-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 520px;
    border: var(--wf-panel-border);
    border-radius: var(--wf-panel-radius);
    background: var(--default-box-color);
    box-shadow: var(--wf-panel-shadow);
    overflow: hidden;
  }

  .canvas-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    border-bottom: var(--wf-panel-border);
    background: linear-gradient(180deg, var(--el-fill-color-blank) 0%, transparent 100%);
  }

  .canvas-toolbar__left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .canvas-toolbar__center {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .canvas-toolbar__icon {
    font-size: 18px;
    color: var(--el-color-primary);
  }

  .canvas-toolbar__title {
    font-size: 14px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }

  .canvas-toolbar__stats {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stat-chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
  }

  .stat-chip__dot {
    font-size: 6px;

    &--node {
      color: var(--el-color-primary);
    }

    &--edge {
      color: #64748b;
    }
  }

  /* Align tools */
  .align-tools {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 8px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
  }

  .align-divider {
    width: 1px;
    height: 20px;
    background: var(--el-border-color-lighter);
    margin: 0 4px;
  }

  .canvas-stage {
    flex: 1;
    min-height: 0;
    background:
      radial-gradient(circle at 50% 0%, rgb(64 158 255 / 4%) 0%, transparent 50%),
      var(--wf-canvas-bg);

    :deep(.vue-flow) {
      height: 100%;
      min-height: 460px;
    }

    :deep(.vue-flow__edges) {
      overflow: visible;
      pointer-events: none;

      .vue-flow__edge {
        pointer-events: visibleStroke;
      }
    }

    :deep(.vue-flow__edge-path) {
      stroke: #64748b;
      stroke-width: 2;
      fill: none;
    }

    :deep(.vue-flow__edge.selected .vue-flow__edge-path) {
      stroke: var(--el-color-primary);
      stroke-width: 2.5;
    }

    :deep(.vue-flow__connection-path) {
      stroke: var(--el-color-primary);
      stroke-width: 2;
      fill: none;
    }

    :deep(marker path) {
      fill: #64748b;
    }

    :deep(.vue-flow__edge.selected marker path) {
      fill: var(--el-color-primary);
    }

    :deep(.vue-flow__controls) {
      border: var(--wf-panel-border);
      border-radius: 12px;
      overflow: hidden;
      box-shadow: var(--wf-panel-shadow);
      backdrop-filter: blur(8px);
    }

    :deep(.vue-flow__controls-button) {
      background: var(--el-fill-color-blank);
      border-bottom: 1px solid var(--el-border-color-light);

      &:hover {
        background: var(--el-fill-color-light);
      }

      svg {
        fill: var(--el-text-color-regular);
      }
    }
  }

  /* JSON Dialog */
  .json-dialog {
    .json-dialog__toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--el-border-color-light);
    }

    .json-dialog__hint {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .json-textarea {
      font-family: ui-monospace, 'Cascadia Code', 'Fira Code', monospace;
      font-size: 12px;
      line-height: 1.6;

      :deep(.el-textarea__inner) {
        background: var(--el-fill-color-lighter);
        resize: none;
      }
    }
  }

  /* Export Image Dialog */
  .export-image-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px 0;
  }

  .export-hint {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  .btn-icon {
    margin-right: 6px;
  }

  /* Simulation Dialog */
  :deep(.simulation-dialog) {
    .el-dialog__body {
      padding: 0;
      max-height: 70vh;
      overflow: auto;
    }
  }

  /* Mini map */
  :deep(.vue-flow__minimap) {
    border-radius: 8px;
    overflow: hidden;
    border: var(--wf-panel-border);
    box-shadow: var(--wf-panel-shadow);
  }

  /* Selection box */
  :deep(.vue-flow__selection) {
    background: rgb(64 158 255 / 8%);
    border: 1px dashed var(--el-color-primary);
  }

  /* Panner */
  :deep(.vue-flow__panner) {
    background: rgb(64 158 255 / 5%);
    border: 1px dashed var(--el-color-primary);
  }
</style>

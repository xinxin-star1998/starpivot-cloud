<script setup lang="ts">
  import type { SpfEdge, SpfNode, FlowNodeType } from '../../utils/flow-types'
  import AssigneePicker from './AssigneePicker.vue'
  import PropsEmptyState from './PropsEmptyState.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  const NODE_TYPE_META: Record<
    FlowNodeType,
    {
      label: string
      tag: '' | 'success' | 'danger' | 'warning' | 'primary'
      icon: string
      color: string
    }
  > = {
    start: { label: '开始', tag: 'success', icon: 'ri:play-circle-line', color: '#22c55e' },
    end: { label: '结束', tag: 'danger', icon: 'ri:stop-circle-line', color: '#ef4444' },
    approval: { label: '审批', tag: 'primary', icon: 'ri:user-star-line', color: '#3b82f6' },
    condition: { label: '条件', tag: 'warning', icon: 'ri:git-branch-line', color: '#f59e0b' }
  }

  const node = defineModel<SpfNode | null>('node', { required: true })
  const edges = defineModel<SpfEdge[]>('edges', { required: true })
  const nodes = defineModel<SpfNode[]>('nodes', { required: true })

  const emit = defineEmits<{
    commit: []
    connectTo: [targetId: string]
    selectEdge: [edgeId: string]
    deleteEdge: [edgeId: string]
  }>()

  const connectTargetId = ref('')

  const candidateTargets = computed(() => {
    if (!node.value) return []
    return nodes.value
      .filter((n) => n.id !== node.value!.id)
      .map((n) => ({
        id: n.id,
        label: n.data?.name || n.type
      }))
  })

  const nodeMeta = computed(() => (node.value ? NODE_TYPE_META[node.value.type] : null))

  function handleConnectTo(targetId: string) {
    if (!targetId || !node.value) return
    emit('connectTo', targetId)
    connectTargetId.value = ''
  }

  const selectedEdge = computed(() => {
    if (!node.value) return []
    return edges.value.filter((e) => e.source === node.value!.id)
  })

  function nodeNameById(id: string) {
    const target = nodes.value.find((n) => n.id === id)
    return target?.data?.name || id
  }

  function updateNodeName(name: string) {
    if (!node.value) return
    node.value.data.name = name
    emit('commit')
  }

  function ensureAssigneeRule() {
    if (!node.value) return
    if (!node.value.data.assigneeRule) {
      node.value.data.assigneeRule = { type: 'STARTER_DEPT_LEADER' }
    }
  }

  function updateEdgeCondition(edgeId: string, field: 'field' | 'op' | 'value', val: string) {
    const edge = edges.value.find((e) => e.id === edgeId)
    if (!edge) return
    if (!edge.data) edge.data = {}
    if (!edge.data.condition) edge.data.condition = {}
    if (field === 'field') edge.data.condition.field = val
    if (field === 'op') edge.data.condition.op = val as '>'
    if (field === 'value') edge.data.condition.value = val
    emit('commit')
  }

  function setDefaultEdge(edgeId: string) {
    edges.value.forEach((edge) => {
      if (edge.source !== node.value?.id) return
      if (!edge.data) edge.data = {}
      if (edge.id === edgeId) {
        edge.data.condition = { type: 'default' }
      } else if (edge.data.condition?.type === 'default') {
        edge.data.condition = { field: '', op: '>', value: '' }
      }
    })
    emit('commit')
  }

  function addConditionBranch() {
    if (!node.value || node.value.type !== 'condition') return
    const branchId = `approval_${Date.now().toString(36)}`
    const endNode = nodes.value.find((n) => n.type === 'end')
    nodes.value.push({
      id: branchId,
      type: 'approval',
      position: { x: node.value.position.x + 220, y: node.value.position.y + 80 },
      data: {
        name: '审批节点',
        approveMode: 'OR',
        assigneeRule: { type: 'STARTER_DEPT_LEADER' }
      }
    })
    edges.value.push({
      id: `e_${Date.now().toString(36)}`,
      source: node.value.id,
      target: branchId,
      data: { condition: { field: '', op: '>', value: '' } }
    })
    if (endNode) {
      edges.value.push({
        id: `e_${Date.now().toString(36)}_end`,
        source: branchId,
        target: endNode.id,
        data: {}
      })
    }
    emit('commit')
  }
</script>

<template>
  <div v-if="node && nodeMeta" class="node-props-panel">
    <div class="node-hero" :style="{ '--hero-color': nodeMeta.color }">
      <div class="node-hero__icon">
        <ArtSvgIcon :icon="nodeMeta.icon" />
      </div>
      <div class="node-hero__info">
        <ElTag :type="nodeMeta.tag" size="small" effect="light" round>{{ nodeMeta.label }}</ElTag>
        <span class="node-hero__name">{{ node.data.name || node.id }}</span>
      </div>
    </div>

    <div class="props-sections">
      <section class="props-section">
        <div class="props-section__head">
          <ArtSvgIcon icon="ri:information-line" />
          <span>基本信息</span>
        </div>
        <ElForm label-position="top" size="default" class="props-form">
          <ElFormItem label="节点 ID">
            <ElInput :model-value="node.id" disabled>
              <template #prefix>
                <ArtSvgIcon icon="ri:fingerprint-line" class="field-prefix-icon" />
              </template>
            </ElInput>
          </ElFormItem>
          <ElFormItem label="显示名称">
            <ElInput :model-value="node.data.name" @update:model-value="updateNodeName">
              <template #prefix>
                <ArtSvgIcon icon="ri:text" class="field-prefix-icon" />
              </template>
            </ElInput>
          </ElFormItem>
        </ElForm>
      </section>

      <section v-if="node.type !== 'end'" class="props-section">
        <div class="props-section__head">
          <ArtSvgIcon icon="ri:share-forward-line" />
          <span>连线</span>
        </div>
        <ElForm label-position="top" size="default" class="props-form">
          <ElFormItem label="连接到">
            <ElSelect
              v-model="connectTargetId"
              placeholder="选择目标节点"
              clearable
              filterable
              @change="handleConnectTo"
            >
              <ElOption
                v-for="target in candidateTargets"
                :key="target.id"
                :label="target.label"
                :value="target.id"
              />
            </ElSelect>
          </ElFormItem>

          <div v-if="selectedEdge.length" class="edge-list">
            <div v-for="edge in selectedEdge" :key="edge.id" class="edge-list-item">
              <button
                type="button"
                class="edge-list-item__main"
                @click="emit('selectEdge', edge.id)"
              >
                <ArtSvgIcon icon="ri:arrow-right-line" />
                <span>{{ nodeNameById(edge.target) }}</span>
              </button>
              <ElButton
                link
                type="danger"
                class="edge-list-item__delete"
                @click="emit('deleteEdge', edge.id)"
              >
                <ArtSvgIcon icon="ri:delete-bin-line" />
              </ElButton>
            </div>
          </div>
        </ElForm>
      </section>

      <section v-if="node.type === 'approval'" class="props-section">
        <div class="props-section__head">
          <ArtSvgIcon icon="ri:user-settings-line" />
          <span>审批配置</span>
        </div>
        <ElForm label-position="top" size="default" class="props-form">
          <ElFormItem label="审批人">
            <AssigneePicker
              v-if="node.data.assigneeRule"
              v-model="node.data.assigneeRule"
              @update:model-value="emit('commit')"
            />
            <ElButton v-else type="primary" plain class="w-full" @click="ensureAssigneeRule">
              <ArtSvgIcon icon="ri:add-line" class="btn-icon" />
              配置审批人
            </ElButton>
          </ElFormItem>
          <ElFormItem label="审批模式">
            <ElRadioGroup
              v-model="node.data.approveMode"
              class="mode-radio"
              @change="emit('commit')"
            >
              <ElRadioButton label="OR">或签</ElRadioButton>
              <ElRadioButton label="AND">会签</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElForm>
      </section>

      <section v-if="node.type === 'condition'" class="props-section">
        <div class="props-section__head">
          <ArtSvgIcon icon="ri:git-branch-line" />
          <span>分支条件</span>
          <ElButton
            size="small"
            type="primary"
            plain
            class="section-action"
            @click="addConditionBranch"
          >
            <ArtSvgIcon icon="ri:add-line" />
            添加分支
          </ElButton>
        </div>

        <div v-for="edge in selectedEdge" :key="edge.id" class="branch-card">
          <div class="branch-card__target">
            <ArtSvgIcon icon="ri:arrow-right-circle-line" />
            {{ nodeNameById(edge.target) }}
          </div>
          <ElCheckbox
            :model-value="edge.data?.condition?.type === 'default'"
            @change="(v) => v === true && setDefaultEdge(edge.id)"
          >
            默认分支
          </ElCheckbox>
          <template v-if="edge.data?.condition?.type !== 'default'">
            <div class="condition-row">
              <ElInput
                :model-value="edge.data?.condition?.field"
                placeholder="变量名"
                @update:model-value="(v) => updateEdgeCondition(edge.id, 'field', v)"
              />
              <ElSelect
                :model-value="edge.data?.condition?.op || '>'"
                @update:model-value="(v) => updateEdgeCondition(edge.id, 'op', v)"
              >
                <ElOption label=">" value=">" />
                <ElOption label=">=" value=">=" />
                <ElOption label="<" value="<" />
                <ElOption label="<=" value="<=" />
                <ElOption label="==" value="==" />
                <ElOption label="!=" value="!=" />
              </ElSelect>
              <ElInput
                :model-value="String(edge.data?.condition?.value ?? '')"
                placeholder="值"
                @update:model-value="(v) => updateEdgeCondition(edge.id, 'value', v)"
              />
            </div>
          </template>
        </div>
        <p v-if="!selectedEdge.length" class="section-tip"> 从画布拖拽连线，或点击「添加分支」 </p>
      </section>
    </div>
  </div>
  <PropsEmptyState
    v-else
    icon="ri:cursor-line"
    title="选择节点"
    description="点击画布中的节点，在此编辑属性与连线"
  />
</template>

<style scoped lang="scss">
  .node-props-panel {
    height: 100%;
    overflow: auto;
  }

  .node-hero {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--hero-color) 8%, transparent) 0%,
      transparent 100%
    );
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .node-hero__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: 12px;
    font-size: 22px;
    color: var(--hero-color);
    background: color-mix(in srgb, var(--hero-color) 14%, transparent);
    flex-shrink: 0;
  }

  .node-hero__info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .node-hero__name {
    font-size: 15px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    word-break: break-all;
  }

  .props-sections {
    padding: 12px;
  }

  .props-section {
    margin-bottom: 16px;
    padding: 14px;
    border-radius: 12px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-blank);

    &:last-child {
      margin-bottom: 0;
    }
  }

  .props-section__head {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 12px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);

    .section-action {
      margin-left: auto;
    }
  }

  .props-form {
    :deep(.el-form-item) {
      margin-bottom: 14px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    :deep(.el-form-item__label) {
      font-size: 12px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }
  }

  .field-prefix-icon {
    font-size: 14px;
    color: var(--el-text-color-placeholder);
  }

  .edge-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .edge-list-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 4px 4px 0;
    border-radius: 10px;
    border: 1px solid var(--el-border-color-extra-light);
    background: var(--el-fill-color-light);
  }

  .edge-list-item__main {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
    padding: 6px 10px;
    border: none;
    background: transparent;
    font-size: 12px;
    color: var(--el-text-color-regular);
    cursor: pointer;
    text-align: left;

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &:hover {
      color: var(--el-color-primary);
    }
  }

  .edge-list-item__delete {
    flex-shrink: 0;
    margin-right: 4px;
  }

  .edge-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .edge-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
  }

  .mode-radio {
    width: 100%;

    :deep(.el-radio-button) {
      flex: 1;

      .el-radio-button__inner {
        width: 100%;
      }
    }
  }

  .branch-card {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 10px;
    padding: 12px;
    border-radius: 10px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);

    &:first-of-type {
      margin-top: 0;
    }
  }

  .branch-card__target {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .condition-row {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    gap: 6px;
  }

  .section-tip {
    margin: 8px 0 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  .btn-icon {
    margin-right: 4px;
    vertical-align: -2px;
  }

  .w-full {
    width: 100%;
  }
</style>

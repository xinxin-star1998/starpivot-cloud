<script setup lang="ts">
  import type { SpfEdge, SpfNode } from '../../utils/flow-types'
  import PropsEmptyState from './PropsEmptyState.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  const edge = defineModel<SpfEdge | null>('edge', { required: true })
  const edges = defineModel<SpfEdge[]>('edges', { required: true })
  const nodes = defineModel<SpfNode[]>('nodes', { required: true })

  const emit = defineEmits<{
    commit: []
    delete: []
  }>()

  const sourceNode = computed(() => nodes.value.find((n) => n.id === edge.value?.source))
  const targetNode = computed(() => nodes.value.find((n) => n.id === edge.value?.target))

  const isConditionEdge = computed(() => sourceNode.value?.type === 'condition')

  function updateCondition(field: 'field' | 'op' | 'value', val: string) {
    if (!edge.value) return
    if (!edge.value.data) edge.value.data = {}
    if (!edge.value.data.condition) edge.value.data.condition = {}
    if (field === 'field') edge.value.data.condition.field = val
    if (field === 'op') edge.value.data.condition.op = val as '>'
    if (field === 'value') edge.value.data.condition.value = val
    emit('commit')
  }

  function setDefaultBranch() {
    if (!edge.value) return
    edges.value.forEach((item) => {
      if (item.source !== edge.value?.source) return
      if (!item.data) item.data = {}
      if (item.id === edge.value?.id) {
        item.data.condition = { type: 'default' }
      } else if (item.data.condition?.type === 'default') {
        item.data.condition = { field: '', op: '>', value: '' }
      }
    })
    emit('commit')
  }
</script>

<template>
  <div v-if="edge" class="edge-props-panel">
    <div class="edge-hero">
      <div class="edge-hero__node edge-hero__node--source">
        <ArtSvgIcon icon="ri:circle-fill" class="edge-hero__dot" />
        <span>{{ sourceNode?.data?.name || edge.source }}</span>
      </div>
      <div class="edge-hero__arrow">
        <ArtSvgIcon icon="ri:arrow-right-line" />
      </div>
      <div class="edge-hero__node edge-hero__node--target">
        <ArtSvgIcon icon="ri:circle-fill" class="edge-hero__dot" />
        <span>{{ targetNode?.data?.name || edge.target }}</span>
      </div>
    </div>

    <div class="props-sections">
      <section v-if="isConditionEdge" class="props-section">
        <div class="props-section__head">
          <ArtSvgIcon icon="ri:git-branch-line" />
          <span>分支条件</span>
        </div>

        <ElForm label-position="top" size="default" class="props-form">
          <div class="default-branch">
            <ElCheckbox
              :model-value="edge.data?.condition?.type === 'default'"
              @change="(v) => v === true && setDefaultBranch()"
            >
              设为默认分支
            </ElCheckbox>
            <span class="default-branch__hint">未匹配其他条件时走此路径</span>
          </div>

          <template v-if="edge.data?.condition?.type !== 'default'">
            <ElFormItem label="变量名">
              <ElInput
                :model-value="edge.data?.condition?.field"
                placeholder="如 amount"
                @update:model-value="(v) => updateCondition('field', v)"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:braces-line" class="field-prefix-icon" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElFormItem label="运算符">
              <ElSelect
                :model-value="edge.data?.condition?.op || '>'"
                @update:model-value="(v) => updateCondition('op', v)"
              >
                <ElOption label="大于 >" value=">" />
                <ElOption label="大于等于 >=" value=">=" />
                <ElOption label="小于 <" value="<" />
                <ElOption label="小于等于 <=" value="<=" />
                <ElOption label="等于 ==" value="==" />
                <ElOption label="不等于 !=" value="!=" />
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="比较值">
              <ElInput
                :model-value="String(edge.data?.condition?.value ?? '')"
                placeholder="输入比较值"
                @update:model-value="(v) => updateCondition('value', v)"
              />
            </ElFormItem>
          </template>
        </ElForm>
      </section>

      <section v-else class="props-section props-section--info">
        <div class="info-card">
          <ArtSvgIcon icon="ri:information-line" class="info-card__icon" />
          <div class="info-card__body">
            <p class="info-card__title">普通流转连线</p>
            <p class="info-card__desc">
              此连线无额外条件，流程将直接流转至目标节点。按
              <kbd>Delete</kbd> 可删除。
            </p>
          </div>
        </div>
      </section>

      <div class="edge-actions">
        <ElButton type="danger" plain class="w-full" @click="emit('delete')">
          <ArtSvgIcon icon="ri:delete-bin-line" class="btn-icon" />
          删除连线
        </ElButton>
      </div>
    </div>
  </div>
  <PropsEmptyState
    v-else
    icon="ri:links-line"
    title="选择连线"
    description="点击画布中的连线，在此编辑分支条件"
  />
</template>

<style scoped lang="scss">
  .edge-props-panel {
    height: 100%;
    overflow: auto;
  }

  .edge-hero {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    background: linear-gradient(135deg, rgb(6 182 212 / 8%) 0%, transparent 100%);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .edge-hero__node {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
    padding: 8px 10px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    background: var(--el-fill-color-blank);
    border: 1px solid var(--el-border-color-lighter);

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &--source .edge-hero__dot {
      color: #f59e0b;
    }

    &--target .edge-hero__dot {
      color: #3b82f6;
    }
  }

  .edge-hero__dot {
    font-size: 8px;
    flex-shrink: 0;
  }

  .edge-hero__arrow {
    flex-shrink: 0;
    font-size: 18px;
    color: var(--el-color-primary);
  }

  .props-sections {
    padding: 12px;
  }

  .props-section {
    padding: 14px;
    border-radius: 12px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-blank);

    &--info {
      border-style: dashed;
      background: var(--el-fill-color-light);
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
  }

  .props-form {
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

  .default-branch {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 14px;
    padding: 10px 12px;
    border-radius: 10px;
    background: var(--el-fill-color-light);
  }

  .default-branch__hint {
    padding-left: 24px;
    font-size: 11px;
    color: var(--el-text-color-secondary);
  }

  .info-card {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }

  .info-card__icon {
    flex-shrink: 0;
    font-size: 22px;
    color: var(--el-color-primary-light-3);
    margin-top: 2px;
  }

  .info-card__title {
    margin: 0 0 4px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .info-card__desc {
    margin: 0;
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);

    kbd {
      padding: 1px 5px;
      border-radius: 4px;
      border: 1px solid var(--el-border-color);
      background: var(--el-bg-color);
      font-family: ui-monospace, monospace;
      font-size: 10px;
    }
  }

  .edge-actions {
    padding: 0 12px 12px;
  }

  .btn-icon {
    margin-right: 4px;
    vertical-align: -2px;
  }

  .w-full {
    width: 100%;
  }
</style>

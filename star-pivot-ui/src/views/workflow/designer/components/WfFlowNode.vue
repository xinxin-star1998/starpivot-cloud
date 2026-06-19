<script setup lang="ts">
  import { Handle, Position, useNode } from '@vue-flow/core'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { FlowNodeType } from '../../utils/flow-types'

  const props = defineProps<{
    data: {
      label: string
      nodeType: FlowNodeType
      hasError?: boolean
      errorMessage?: string
      selected?: boolean
      progressStatus?: 'COMPLETED' | 'CURRENT' | 'REJECTED' | 'PENDING'
      assignee?: string
      dueDate?: string
      description?: string
    }
  }>()

  const NODE_META: Record<
    FlowNodeType,
    { label: string; icon: string; accent: string; gradient: string }
  > = {
    start: {
      label: '开始',
      icon: 'ri:play-circle-fill',
      accent: '#22c55e',
      gradient: 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)'
    },
    end: {
      label: '结束',
      icon: 'ri:stop-circle-fill',
      accent: '#ef4444',
      gradient: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)'
    },
    approval: {
      label: '审批',
      icon: 'ri:user-star-line',
      accent: '#3b82f6',
      gradient: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)'
    },
    condition: {
      label: '条件',
      icon: 'ri:git-branch-line',
      accent: '#f59e0b',
      gradient: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
    }
  }

  const meta = computed(() => NODE_META[props.data.nodeType])
  const showTarget = computed(() => props.data.nodeType !== 'start')
  const showSource = computed(() => props.data.nodeType !== 'end')
  const { node } = useNode()

  const tooltipContent = computed(() => {
    const lines = []
    lines.push(`类型: ${meta.value.label}`)
    lines.push(`名称: ${props.data.label}`)
    if (props.data.assignee) {
      lines.push(`审批人: ${props.data.assignee}`)
    }
    if (props.data.dueDate) {
      lines.push(`截止日期: ${props.data.dueDate}`)
    }
    if (props.data.description) {
      lines.push(`描述: ${props.data.description}`)
    }
    if (props.data.hasError && props.data.errorMessage) {
      lines.push(`错误: ${props.data.errorMessage}`)
    }
    return lines.join('\n')
  })
</script>

<template>
  <div
    class="wf-flow-node"
    :class="[
      `wf-flow-node--${props.data.nodeType}`,
      {
        'is-error': props.data.hasError,
        'is-selected': node.selected,
        'is-progress-completed': props.data.progressStatus === 'COMPLETED',
        'is-progress-current': props.data.progressStatus === 'CURRENT',
        'is-progress-rejected': props.data.progressStatus === 'REJECTED',
        'is-progress-pending': props.data.progressStatus === 'PENDING'
      }
    ]"
    :style="{ '--wf-accent': meta.accent, '--wf-gradient': meta.gradient }"
    :title="props.data.errorMessage"
  >
    <Handle
      v-if="showTarget"
      type="target"
      :position="Position.Left"
      :connectable="true"
      class="wf-handle wf-handle--target"
    />

    <div class="wf-flow-node__glow" />
    <div class="wf-flow-node__inner">
      <div class="wf-flow-node__header">
        <div class="wf-flow-node__icon-wrap">
          <ArtSvgIcon :icon="meta.icon" class="wf-flow-node__icon" />
        </div>
        <span class="wf-flow-node__type">{{ meta.label }}</span>
      </div>
      <div class="wf-flow-node__label">{{ props.data.label }}</div>
    </div>

    <div v-if="props.data.hasError" class="wf-flow-node__badge">
      <ArtSvgIcon icon="ri:error-warning-fill" />
    </div>

    <Handle
      v-if="showSource"
      type="source"
      :position="Position.Right"
      :connectable="true"
      class="wf-handle wf-handle--source"
    />
  </div>
</template>

<style scoped lang="scss">
  .wf-flow-node {
    position: relative;
    display: flex;
    align-items: center;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    border-radius: 14px;
    background: var(--el-bg-color);
    border: 1.5px solid var(--el-border-color-lighter);
    box-shadow:
      0 1px 2px rgb(0 0 0 / 4%),
      0 8px 24px rgb(15 23 42 / 8%);
    transition:
      border-color 0.22s ease,
      box-shadow 0.22s ease,
      transform 0.22s ease;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: inherit;
      padding: 1.5px;
      background: var(--wf-gradient);
      mask:
        linear-gradient(#fff 0 0) content-box,
        linear-gradient(#fff 0 0);
      mask-composite: exclude;
      opacity: 0;
      transition: opacity 0.22s ease;
      pointer-events: none;
    }

    &:hover {
      transform: translateY(-1px);
      box-shadow:
        0 4px 8px rgb(0 0 0 / 6%),
        0 12px 32px rgb(15 23 42 / 10%);

      .wf-flow-node__glow {
        opacity: 0.6;
      }
    }

    &.is-selected {
      border-color: transparent;

      &::before {
        opacity: 1;
      }

      .wf-flow-node__glow {
        opacity: 1;
      }
    }

    &.is-error {
      border-color: var(--el-color-danger);

      .wf-flow-node__badge {
        animation: wf-shake 0.4s ease;
      }
    }

    &--start,
    &--end {
      border-radius: 999px;

      .wf-flow-node__inner {
        flex: 1;
        padding: 0 18px;
        text-align: center;
      }

      .wf-flow-node__header {
        justify-content: center;
        margin-bottom: 2px;
      }
    }

    &.is-progress-completed {
      box-shadow: 0 0 0 2px rgb(34 197 94 / 35%);
    }

    &.is-progress-current {
      animation: wf-pulse 1.8s ease-in-out infinite;
    }

    &.is-progress-rejected {
      border-color: var(--el-color-danger);
    }

    &.is-progress-pending {
      opacity: 0.68;
    }
  }

  @keyframes wf-pulse {
    0%,
    100% {
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--wf-accent) 25%, transparent);
    }

    50% {
      box-shadow: 0 0 0 7px color-mix(in srgb, var(--wf-accent) 10%, transparent);
    }
  }

  @keyframes wf-shake {
    0%,
    100% {
      transform: translateX(0);
    }

    25% {
      transform: translateX(-2px);
    }

    75% {
      transform: translateX(2px);
    }
  }

  .wf-flow-node__glow {
    position: absolute;
    inset: -4px;
    border-radius: inherit;
    background: var(--wf-gradient);
    opacity: 0;
    filter: blur(12px);
    transition: opacity 0.22s ease;
    pointer-events: none;
    z-index: -1;
  }

  .wf-flow-node__inner {
    position: relative;
    flex: 1;
    min-width: 0;
    padding: 8px 14px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .wf-flow-node__header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
  }

  .wf-flow-node__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 6px;
    background: color-mix(in srgb, var(--wf-accent) 14%, transparent);
  }

  .wf-flow-node__icon {
    font-size: 13px;
    color: var(--wf-accent);
  }

  .wf-flow-node__type {
    font-size: 10px;
    font-weight: 700;
    color: var(--wf-accent);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .wf-flow-node__label {
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    line-height: 1.45;
    word-break: break-all;
  }

  .wf-flow-node__badge {
    position: absolute;
    top: -8px;
    right: -8px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--el-color-danger);
    color: #fff;
    font-size: 12px;
    box-shadow: 0 2px 8px rgb(239 68 68 / 45%);
    z-index: 1;
  }

  :deep(.wf-handle) {
    width: 10px;
    height: 10px;
    border: 2px solid var(--el-bg-color);
    background: #94a3b8;
    opacity: 0.85;
    transition:
      opacity 0.18s ease,
      transform 0.18s ease,
      background 0.18s ease,
      box-shadow 0.18s ease;
  }

  .wf-flow-node:hover :deep(.wf-handle),
  .wf-flow-node.is-selected :deep(.wf-handle) {
    opacity: 1;
    background: var(--wf-accent);
  }

  :deep(.wf-handle:hover) {
    transform: scale(1.25);
    opacity: 1;
    background: var(--wf-accent);
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--wf-accent) 25%, transparent);
  }
</style>

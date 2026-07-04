<!-- 审批时间轴（阶梯进度） -->
<template>
  <div v-loading="loading" class="approval-timeline">
    <template v-if="timeline">
      <div class="timeline-header">
        <div class="timeline-title">{{ timeline.title }}</div>
        <ElTag :type="instanceStatusTagType(timeline.status)" size="small">
          {{ instanceStatusLabel(timeline.status) }}
        </ElTag>
      </div>
      <div v-if="timeline.bizKey" class="timeline-meta">
        <span>{{ timeline.bizModule }} / {{ timeline.bizType }}</span>
        <span class="meta-divider">·</span>
        <span>{{ timeline.bizKey }}</span>
      </div>

      <ElSteps :active="activeIndex" align-center class="step-bar" finish-status="success">
        <ElStep
          v-for="step in timeline.steps"
          :key="step.stepCode"
          :status="mapStepStatus(step.status)"
          :title="step.stepName"
        />
      </ElSteps>

      <div class="step-records">
        <div v-for="step in timeline.steps" :key="step.stepCode" class="step-block">
          <div class="step-block-head">
            <span class="step-name">{{ step.stepName }}</span>
            <ElTag size="small" type="info">{{ stepStatusLabel(step.status) }}</ElTag>
            <span v-if="step.assignees?.length" class="assignees">
              待办：{{ step.assignees.join('、') }}
            </span>
          </div>
          <div v-if="step.records?.length" class="record-list">
            <div v-for="(rec, idx) in step.records" :key="idx" class="record-item">
              <span class="record-operator">{{ rec.operatorName }}</span>
              <ElTag size="small">{{ recordActionLabel(rec.action) }}</ElTag>
              <span v-if="rec.comment" class="record-comment">{{ rec.comment }}</span>
              <span class="record-time">{{ formatDateTime(rec.time) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    <ElEmpty v-else-if="!loading" description="暂无审批进度" />
  </div>
</template>

<script lang="ts" setup>
import type {ApprovalTimelineVo} from '@/api/approval/types'
import {fetchApprovalTimeline} from '@/api/approval/instance'
import {formatDateTime} from '@/utils/common/datetime'
import {instanceStatusLabel, instanceStatusTagType, recordActionLabel, stepStatusLabel} from '../utils/approval-labels'

const props = defineProps<{
    instanceId?: number
    /** 外部传入则不再请求 */
    data?: ApprovalTimelineVo
  }>()

  const loading = ref(false)
  const timeline = ref<ApprovalTimelineVo | null>(null)

  const activeIndex = computed(() => {
    const steps = timeline.value?.steps || []
    const pendingIdx = steps.findIndex((s) => s.status === 'PENDING')
    if (pendingIdx >= 0) return pendingIdx
    const doneCount = steps.filter((s) => s.status === 'DONE' || s.status === 'SKIPPED').length
    return doneCount > 0 ? doneCount - 1 : 0
  })

  function mapStepStatus(
    status?: string
  ): '' | 'wait' | 'process' | 'finish' | 'error' | 'success' {
    if (status === 'DONE' || status === 'SKIPPED') return 'success'
    if (status === 'PENDING') return 'process'
    if (status === 'CANCELLED') return 'wait'
    return 'wait'
  }

  async function load() {
    if (props.data) {
      timeline.value = props.data
      return
    }
    if (!props.instanceId) {
      timeline.value = null
      return
    }
    loading.value = true
    try {
      timeline.value = await fetchApprovalTimeline(props.instanceId)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.instanceId, props.data] as const,
    () => load(),
    { immediate: true }
  )

  defineExpose({ reload: load })
</script>

<style lang="scss" scoped>
  .approval-timeline {
    min-height: 120px;
  }

  .timeline-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .timeline-title {
    font-size: 16px;
    font-weight: 600;
  }

  .timeline-meta {
    margin-bottom: 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);

    .meta-divider {
      margin: 0 6px;
    }
  }

  .step-bar {
    margin-bottom: 20px;
  }

  .step-block {
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }
  }

  .step-block-head {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }

  .step-name {
    font-weight: 500;
  }

  .assignees {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .record-item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    padding: 6px 0 6px 12px;
    font-size: 13px;
  }

  .record-operator {
    font-weight: 500;
  }

  .record-comment {
    color: var(--el-text-color-regular);
  }

  .record-time {
    margin-left: auto;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>

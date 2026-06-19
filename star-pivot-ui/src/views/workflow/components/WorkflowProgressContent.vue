<!-- 流程进度内容区（画布 + 审批记录） -->
<template>
  <div v-loading="loading" class="workflow-progress-content">
    <template v-if="progress">
      <div class="info-row">
        <span class="info-title">{{ progress.title }}</span>
        <ElTag :type="instanceTagType(progress.status)">
          {{ instanceStatusLabel(progress.status) }}
        </ElTag>
      </div>
      <div class="info-meta">
        <span>实例 ID：{{ progress.instanceId }}</span>
        <span>流程：{{ progress.processName }}</span>
        <span>发起时间：{{ progress.createTime || '-' }}</span>
      </div>
      <div v-if="showLegend && !mini" class="legend">
        <span class="legend-item"><i class="dot completed" />已完成</span>
        <span class="legend-item"><i class="dot current" />进行中</span>
        <span class="legend-item"><i class="dot rejected" />已驳回</span>
        <span class="legend-item"><i class="dot pending" />未到达</span>
      </div>

      <div class="content-grid" :class="{ compact, mini }">
        <div class="canvas-wrap">
          <FlowProgressViewer
            :def-json="progress.defJson"
            :node-statuses="nodeStatusMap"
            :mini="mini"
          />
        </div>
        <div v-if="showHistory" class="history-wrap">
          <div class="history-title">审批记录</div>
          <ElTimeline v-if="progress.histories?.length">
            <ElTimelineItem
              v-for="item in progress.histories"
              :key="item.historyId"
              :timestamp="item.createTime"
              placement="top"
            >
              <div class="history-item">
                <strong>{{ item.nodeName || item.nodeId }}</strong>
                <ElTag size="small">{{ historyActionLabel(item.action) }}</ElTag>
              </div>
              <div class="history-meta">
                {{ item.operatorName || '系统' }}
                <span v-if="item.comment"> · {{ item.comment }}</span>
              </div>
            </ElTimelineItem>
          </ElTimeline>
          <ElEmpty v-else description="暂无审批记录" :image-size="64" />
        </div>
      </div>
    </template>
    <ElEmpty v-else-if="!loading" description="暂无进度数据" />
  </div>
</template>

<script setup lang="ts">
  import FlowProgressViewer from './FlowProgressViewer.vue'
  import type { InstanceProgressVo } from '@/api/workflow/task'
  import { instanceStatusLabel, instanceStatusTagType } from '../utils/workflow-labels'
  import { HISTORY_ACTION_LABEL, toNodeStatusMap } from '../utils/flow-progress-types'

  const props = defineProps<{
    progress?: InstanceProgressVo | null
    loading?: boolean
    showLegend?: boolean
    showHistory?: boolean
    compact?: boolean
    mini?: boolean
  }>()

  const nodeStatusMap = computed(() => toNodeStatusMap(props.progress?.nodeStatuses))

  function instanceTagType(status?: string) {
    return instanceStatusTagType(status) as 'success' | 'danger' | 'info' | 'warning'
  }

  function historyActionLabel(action?: string) {
    return HISTORY_ACTION_LABEL[action || ''] || action || '-'
  }
</script>

<style scoped lang="scss">
  .workflow-progress-content {
    min-height: 200px;
  }

  .info-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .info-title {
    font-size: 15px;
    font-weight: 600;
  }

  .info-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-bottom: 12px;
  }

  .legend {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .legend-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    display: inline-block;

    &.completed {
      background: #67c23a;
    }

    &.current {
      background: #409eff;
    }

    &.rejected {
      background: #f56c6c;
    }

    &.pending {
      background: #c0c4cc;
    }
  }

  .content-grid {
    display: grid;
    grid-template-columns: 1fr 280px;
    gap: 12px;
    min-height: 420px;

    &.compact,
    &.mini {
      grid-template-columns: 1fr;
      min-height: 280px;
    }
  }

  .content-grid.mini {
    .canvas-wrap {
      min-height: 280px;
    }
  }

  .canvas-wrap {
    min-height: 360px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    overflow: hidden;
  }

  .history-wrap {
    max-height: 420px;
    padding: 8px 4px;
    overflow: auto;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .history-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }

  .history-meta {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>

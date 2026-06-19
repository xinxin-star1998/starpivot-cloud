<template>
  <ElDialog
    v-model="visible"
    :title="title"
    :width="instanceId ? '920px' : '480px'"
    destroy-on-close
    class="approval-action-dialog"
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div class="approval-dialog-body" :class="{ 'has-progress': !!instanceId }">
      <div class="approval-form">
        <div v-if="taskTitle" class="task-summary">{{ taskTitle }}</div>
        <ElDescriptions v-if="taskMeta.length" :column="1" size="small" border class="task-meta">
          <ElDescriptionsItem v-for="item in taskMeta" :key="item.label" :label="item.label">
            {{ item.value }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElFormItem label="审批意见" label-position="top" class="comment-field">
          <ElInput v-model="comment" type="textarea" :rows="5" placeholder="审批意见（可选）" />
        </ElFormItem>
      </div>

      <div v-if="instanceId" v-loading="progressLoading" class="approval-progress">
        <div class="progress-header">
          <span class="progress-title">流程进度</span>
          <ElButton link type="primary" size="small" @click="emit('viewProgress')">
            全屏查看
          </ElButton>
        </div>
        <FlowProgressViewer
          v-if="progress?.defJson"
          :def-json="progress.defJson"
          :node-statuses="nodeStatusMap"
          mini
        />
        <ElEmpty v-else-if="!progressLoading" description="暂无流程图" :image-size="64" />
      </div>
    </div>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import FlowProgressViewer from '../../components/FlowProgressViewer.vue'
  import { useWorkflowProgressLoader } from '../../composables/useWorkflowProgressLoader'
  import { toNodeStatusMap } from '../../utils/flow-progress-types'

  interface TaskMetaItem {
    label: string
    value?: string
  }

  interface Props {
    title?: string
    submitting?: boolean
    instanceId?: number
    taskTitle?: string
    taskMeta?: TaskMetaItem[]
  }

  const props = withDefaults(defineProps<Props>(), {
    taskMeta: () => []
  })

  const visible = defineModel<boolean>('visible', { default: false })
  const comment = defineModel<string>('comment', { default: '' })

  const emit = defineEmits<{
    submit: []
    closed: []
    viewProgress: []
  }>()

  const {
    loading: progressLoading,
    progress,
    loadProgress,
    resetProgress
  } = useWorkflowProgressLoader(() => props.instanceId)

  const nodeStatusMap = computed(() => toNodeStatusMap(progress.value?.nodeStatuses))

  function handleOpened() {
    if (props.instanceId) {
      loadProgress()
    }
  }

  function handleSubmit() {
    emit('submit')
  }

  function handleClosed() {
    resetProgress()
    emit('closed')
  }
</script>

<style scoped lang="scss">
  .approval-dialog-body {
    &.has-progress {
      display: grid;
      grid-template-columns: 320px 1fr;
      gap: 16px;
      align-items: start;
    }
  }

  .task-summary {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .task-meta {
    margin-bottom: 12px;
  }

  .comment-field {
    margin-bottom: 0;

    :deep(.el-form-item__label) {
      font-weight: 500;
    }
  }

  .approval-progress {
    min-width: 0;
    min-height: 320px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    padding: 12px;
    background: var(--el-fill-color-blank);
  }

  .progress-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .progress-title {
    font-size: 13px;
    font-weight: 600;
  }
</style>

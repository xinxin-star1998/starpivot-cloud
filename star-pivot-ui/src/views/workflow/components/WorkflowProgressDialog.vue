<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="920px"
    destroy-on-close
    class="workflow-progress-dialog"
    @opened="loadProgress"
  >
    <WorkflowProgressContent :progress="progress" :loading="loading" show-legend show-history />

    <template #footer>
      <ElButton @click="visible = false">关闭</ElButton>
      <ElButton v-if="instanceId" link type="primary" @click="openFullPage">在新页面打开</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import WorkflowProgressContent from './WorkflowProgressContent.vue'
  import { useWorkflowProgressLoader } from '../composables/useWorkflowProgressLoader'
  import { openWorkflowProgress } from '../utils/workflow-nav'

  const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    instanceId?: number
    title?: string
  }>()

  const router = useRouter()
  const { loading, progress, loadProgress } = useWorkflowProgressLoader(() => props.instanceId)

  const dialogTitle = computed(() => props.title || progress.value?.title || '流程进度')

  function openFullPage() {
    if (!props.instanceId) return
    visible.value = false
    openWorkflowProgress(router, props.instanceId)
  }
</script>

<style scoped lang="scss">
  .workflow-progress-dialog {
    :deep(.el-dialog__body) {
      padding-top: 8px;
    }
  }
</style>

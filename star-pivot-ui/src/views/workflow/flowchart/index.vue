<!-- 流程实例进度图 -->
<template>
  <div class="workflow-flowchart-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" @submit.prevent="loadProgress">
        <ElFormItem label="实例 ID">
          <ElInput
            v-model="instanceIdInput"
            placeholder="输入流程实例 ID"
            clearable
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="loadProgress">查询</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard v-if="progress" shadow="never" class="content-card">
      <WorkflowProgressContent :progress="progress" :loading="loading" show-legend show-history />
    </ElCard>

    <ElCard v-else-if="!loading" class="art-table-card" shadow="never">
      <ElEmpty description="输入实例 ID 查看流程进度" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import WorkflowProgressContent from '../components/WorkflowProgressContent.vue'
  import { fetchWorkflowInstanceProgress, type InstanceProgressVo } from '@/api/workflow/task'

  defineOptions({ name: 'WorkflowFlowchart' })

  const route = useRoute()
  const loading = ref(false)
  const instanceIdInput = ref(route.query.instanceId ? String(route.query.instanceId) : '')
  const progress = ref<InstanceProgressVo | null>(null)

  onMounted(() => {
    if (instanceIdInput.value) {
      loadProgress()
    }
  })

  async function loadProgress() {
    const id = Number(instanceIdInput.value)
    if (!id) {
      ElMessage.warning('请输入有效的实例 ID')
      return
    }
    loading.value = true
    try {
      progress.value = await fetchWorkflowInstanceProgress(id)
    } finally {
      loading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .workflow-flowchart-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  .search-card {
    flex-shrink: 0;
  }

  .content-card {
    flex: 1;
    min-height: 520px;
  }
</style>

<template>
  <ElDialog
    v-model="visible"
    :title="title || '流程预览'"
    width="860px"
    destroy-on-close
    @opened="loadDef"
  >
    <div v-loading="loading" class="def-preview">
      <div v-if="meta.processName" class="preview-meta">
        <span>{{ meta.processName }}</span>
        <ElTag size="small">{{ meta.processCode }}</ElTag>
        <ElTag size="small" type="info">{{ meta.bizModule }}</ElTag>
      </div>
      <FlowProgressViewer v-if="defJson" :def-json="defJson" />
      <ElEmpty v-else-if="!loading" description="暂无流程定义" />
    </div>
    <template #footer>
      <ElButton @click="visible = false">关闭</ElButton>
      <ElButton v-if="defId" type="primary" @click="goDesigner">打开设计器</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import FlowProgressViewer from '../../components/FlowProgressViewer.vue'
  import { fetchWorkflowDefDetail } from '@/api/workflow/def'

  const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    defId?: number
    title?: string
  }>()

  const router = useRouter()
  const loading = ref(false)
  const defJson = ref('')
  const meta = reactive({
    processCode: '',
    processName: '',
    bizModule: ''
  })

  async function loadDef() {
    if (!props.defId) return
    loading.value = true
    defJson.value = ''
    try {
      const data = await fetchWorkflowDefDetail(props.defId)
      meta.processCode = data.processCode || ''
      meta.processName = data.processName || ''
      meta.bizModule = data.bizModule || ''
      defJson.value = data.defJson || ''
    } finally {
      loading.value = false
    }
  }

  function goDesigner() {
    if (!props.defId) return
    visible.value = false
    router.push({ path: '/workflow/designer', query: { defId: String(props.defId) } })
  }
</script>

<style scoped lang="scss">
  .def-preview {
    min-height: 400px;
  }

  .preview-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-weight: 600;
  }

  :deep(.flow-progress-viewer) {
    min-height: 400px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }
</style>

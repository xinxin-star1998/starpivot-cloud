<template>
  <ElDialog
    v-model="dialogVisible"
    title="代码预览"
    width="85%"
    align-center
    class="code-preview-dialog"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div v-loading="loading" class="preview-content">
      <CodePreviewTreePanel ref="panelRef" :preview-code-map="previewCodeMap" />
    </div>
    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {ElButton, ElDialog} from 'element-plus'
import {type ExternalGenScope, fetchExternalPreview} from '@/api/generator/gen-external'
import CodePreviewTreePanel from '@views/tools/generator/modules/code-preview-tree-panel.vue'

interface Props {
    visible: boolean
    sessionId?: string
    tableName?: string
    genScope?: ExternalGenScope
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const loading = ref(false)
  const previewCodeMap = ref<Record<string, string>>({})
  const panelRef = ref<InstanceType<typeof CodePreviewTreePanel>>()

  async function loadPreview() {
    if (!props.sessionId || !props.tableName) return
    try {
      loading.value = true
      previewCodeMap.value = await fetchExternalPreview(
        props.sessionId,
        props.tableName,
        props.genScope
      )
      panelRef.value?.selectFirstFile()
    } catch {
      previewCodeMap.value = {}
      panelRef.value?.reset()
    } finally {
      loading.value = false
    }
  }

  function handleClosed() {
    previewCodeMap.value = {}
    panelRef.value?.reset()
  }

  watch(
    () => [props.visible, props.sessionId, props.tableName, props.genScope] as const,
    ([vis, sid, name]) => {
      if (vis && sid && name) loadPreview()
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .preview-content {
    height: 70vh;
    min-height: 420px;
  }
</style>

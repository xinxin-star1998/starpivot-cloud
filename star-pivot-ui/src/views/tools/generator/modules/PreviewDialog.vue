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
      <div class="dialog-footer">
        <ElButton @click="handleClose">关闭</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  /**
   * 代码预览弹窗组件
   * 用于预览代码生成器生成的代码文件
   */
  import { ElButton, ElDialog, ElMessage } from 'element-plus'
  import { fetchPreviewCode } from '@/api/generator/gen-table'
  import CodePreviewTreePanel from './code-preview-tree-panel.vue'

  interface Props {
    /** 弹窗可见性，由父组件控制 */
    visible: boolean
    /** 表ID，用于获取预览代码 */
    tableId?: number
  }

  interface Emits {
    /** 更新弹窗显示状态 */
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  const loading = ref(false)
  const previewCodeMap = ref<Record<string, string>>({})
  const panelRef = ref<InstanceType<typeof CodePreviewTreePanel>>()

  const loadPreviewCode = async (tableId: number): Promise<void> => {
    if (!tableId) {
      ElMessage.warning('表ID不能为空')
      return
    }

    try {
      loading.value = true
      const res = await fetchPreviewCode(tableId)
      previewCodeMap.value = res as unknown as Record<string, string>
      if (Object.keys(previewCodeMap.value).length === 0) {
        ElMessage.warning('未获取到预览代码')
      }
      panelRef.value?.selectFirstFile()
    } catch (error) {
      console.error('预览代码失败:', error)
      ElMessage.error('预览代码失败')
      previewCodeMap.value = {}
      panelRef.value?.reset()
    } finally {
      loading.value = false
    }
  }

  const resetPreviewData = (): void => {
    previewCodeMap.value = {}
    panelRef.value?.reset()
  }

  const handleClose = (): void => {
    dialogVisible.value = false
  }

  const handleClosed = (): void => {
    resetPreviewData()
  }

  watch(
    [() => props.visible, () => props.tableId],
    ([newVisible, newTableId]) => {
      if (newVisible && newTableId) {
        loadPreviewCode(newTableId)
      } else if (!newVisible) {
        resetPreviewData()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .code-preview-dialog {
    :deep(.el-dialog) {
      overflow: hidden;
      border-radius: 16px;

      .el-dialog__header {
        padding: 20px 24px;
        margin: 0;
        background: linear-gradient(
          135deg,
          var(--el-color-primary-light-9) 0%,
          var(--el-color-primary-light-8) 100%
        );
        border-bottom: 1px solid var(--art-card-border);

        .el-dialog__title {
          font-size: 18px;
          font-weight: 600;
          color: var(--art-gray-900);
        }
      }

      .el-dialog__body {
        padding: 24px;
      }

      .el-dialog__footer {
        padding: 16px 24px;
        background-color: var(--art-gray-50);
        border-top: 1px solid var(--art-card-border);
      }
    }
  }

  .preview-content {
    height: 70vh;
    min-height: 420px;
  }

  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
</style>

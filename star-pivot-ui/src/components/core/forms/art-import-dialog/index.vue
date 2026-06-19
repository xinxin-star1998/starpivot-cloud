<!--
  @deprecated 已废弃，请使用 ExcelImportDialog（@/components/core/forms/excel-import-dialog）
-->
<template>
  <ExcelImportDialog
    v-if="importFn && downloadTemplateFn"
    v-bind="forwardProps"
    :import-fn="importFn"
    :download-template-fn="downloadTemplateFn"
    @success="$emit('success', $event)"
    @error="$emit('error', $event)"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import ExcelImportDialog from '@/components/core/forms/excel-import-dialog/index.vue'

  defineOptions({ name: 'ArtImportDialog' })

  const props = defineProps<{
    modelValue?: boolean
    businessType?: string
    title?: string
    showOverwrite?: boolean
    overwriteLabel?: string
    closeOnSuccess?: boolean
    importFn?: (file: File, updateSupport: boolean) => Promise<unknown>
    downloadTemplateFn?: () => Promise<void>
  }>()

  defineEmits<{
    'update:modelValue': [value: boolean]
    success: [result?: unknown]
    error: [err: unknown]
  }>()

  if (!props.importFn || !props.downloadTemplateFn) {
    ElMessage.warning(
      `ArtImportDialog 已废弃（business-type="${props.businessType ?? ''}"）。请改用 ExcelImportDialog 并传入 importFn / downloadTemplateFn。`
    )
  }

  const forwardProps = computed(() => ({
    modelValue: props.modelValue,
    title: props.title,
    showOverwrite: props.showOverwrite,
    overwriteLabel: props.overwriteLabel
  }))
</script>

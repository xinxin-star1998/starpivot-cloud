<template>
  <ElDialog v-model="visible" title="迁移到文件夹" width="480px" destroy-on-close @closed="reset">
    <p class="move-tip">
      已选择 <strong>{{ fileIds.length }}</strong> 个文件，将更新所属文件夹（OSS 对象路径不变）。
    </p>
    <ElForm label-width="88px">
      <ElFormItem label="目标文件夹" required>
        <ElCascader
          v-model="selectedPath"
          :options="cascaderOptions"
          :props="cascaderProps"
          filterable
          clearable
          placeholder="请选择业务分类与文件夹"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" :disabled="!targetFolderId" @click="submit">
        确认迁移
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { moveFiles } from '@/api/file/file'
  import type { FileCategoryNode } from '@/api/file/types'
  import { ElMessage } from 'element-plus'
  import { computed, ref, watch } from 'vue'

  const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    categories: FileCategoryNode[]
    fileIds: number[]
    excludeFolderId?: number
  }>()

  const emit = defineEmits<{
    success: [targetFolderId: number]
  }>()

  const submitting = ref(false)
  const selectedPath = ref<[string, number] | undefined>()

  const cascaderProps = {
    expandTrigger: 'hover' as const,
    emitPath: true
  }

  const cascaderOptions = computed(() =>
    props.categories.map((cat) => ({
      value: cat.category,
      label: cat.categoryLabel,
      children: (cat.children || [])
        .filter((folder) => folder.folderId !== props.excludeFolderId)
        .map((folder) => ({
          value: folder.folderId!,
          label: folder.folderName
        }))
    }))
  )

  const targetFolderId = computed(() => selectedPath.value?.[1])

  watch(
    () => visible.value,
    (open) => {
      if (open) {
        selectedPath.value = undefined
      }
    }
  )

  function reset() {
    selectedPath.value = undefined
  }

  async function submit() {
    const folderId = targetFolderId.value
    if (!folderId) {
      ElMessage.warning('请选择目标文件夹')
      return
    }
    if (props.fileIds.length === 0) {
      ElMessage.warning('请选择要迁移的文件')
      return
    }
    submitting.value = true
    try {
      await moveFiles(props.fileIds, folderId)
      ElMessage.success('迁移成功')
      visible.value = false
      emit('success', folderId)
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .move-tip {
    margin: 0 0 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }
</style>

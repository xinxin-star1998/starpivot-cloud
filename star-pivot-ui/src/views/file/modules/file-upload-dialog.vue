<template>
  <ElDialog v-model="visible" title="上传文件" width="540px" destroy-on-close @closed="reset">
    <ElForm label-width="88px" class="upload-form">
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
      <ElFormItem label="备注">
        <ElInput v-model="remark" type="textarea" :rows="2" placeholder="可选" />
      </ElFormItem>
    </ElForm>

    <ElUpload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="20"
      multiple
      :disabled="uploading"
      :on-change="handleChange"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
    >
      <ArtSvgIcon icon="ri:upload-cloud-2-line" class="upload-icon" />
      <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
      <template #tip>
        <div class="upload-tip">单次最多 20 个文件，大小限制按文件类型自动校验</div>
      </template>
    </ElUpload>

    <ElProgress
      v-if="uploading && uploadTotal > 0"
      :percentage="uploadProgress"
      :stroke-width="8"
      class="upload-progress"
    />

    <template #footer>
      <ElButton @click="visible = false" :disabled="uploading">取消</ElButton>
      <ElButton type="primary" :loading="uploading" :disabled="!targetFolderId" @click="submit">
        {{ uploading ? `上传中 ${uploadDone}/${uploadTotal}` : '开始上传' }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { uploadFile } from '@/api/file/file'
  import type { FileCategoryNode } from '@/api/file/types'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { UploadFile, UploadInstance } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { computed, ref, watch } from 'vue'

  const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    categories: FileCategoryNode[]
    defaultFolderId?: number
  }>()

  const emit = defineEmits<{
    success: [folderId: number]
  }>()

  const uploadRef = ref<UploadInstance>()
  const fileList = ref<UploadFile[]>([])
  const remark = ref('')
  const uploading = ref(false)
  const uploadDone = ref(0)
  const uploadTotal = ref(0)
  const selectedPath = ref<[string, number] | undefined>()

  const cascaderProps = {
    expandTrigger: 'hover' as const,
    emitPath: true
  }

  const cascaderOptions = computed(() =>
    props.categories.map((cat) => ({
      value: cat.category,
      label: cat.categoryLabel,
      children: (cat.children || []).map((folder) => ({
        value: folder.folderId!,
        label: folder.folderName
      }))
    }))
  )

  const targetFolderId = computed(() => selectedPath.value?.[1])

  const uploadProgress = computed(() =>
    uploadTotal.value ? Math.round((uploadDone.value / uploadTotal.value) * 100) : 0
  )

  function resolvePathByFolderId(folderId?: number): [string, number] | undefined {
    if (!folderId) return undefined
    for (const cat of props.categories) {
      const folder = cat.children?.find((f) => f.folderId === folderId)
      if (folder?.folderId) {
        return [cat.category, folder.folderId]
      }
    }
    return undefined
  }

  watch(
    () => visible.value,
    (open) => {
      if (open) {
        selectedPath.value = resolvePathByFolderId(props.defaultFolderId)
      }
    }
  )

  function handleChange(_file: UploadFile, files: UploadFile[]) {
    fileList.value = files
  }

  function handleRemove(_file: UploadFile, files: UploadFile[]) {
    fileList.value = files
  }

  function handleExceed() {
    ElMessage.warning('单次最多上传 20 个文件')
  }

  function reset() {
    fileList.value = []
    remark.value = ''
    selectedPath.value = undefined
    uploadDone.value = 0
    uploadTotal.value = 0
    uploadRef.value?.clearFiles()
  }

  async function submit() {
    const folderId = targetFolderId.value
    if (!folderId) {
      ElMessage.warning('请选择目标文件夹')
      return
    }
    const items = fileList.value.filter((f) => f.raw)
    if (items.length === 0) {
      ElMessage.warning('请选择文件')
      return
    }

    uploading.value = true
    uploadDone.value = 0
    uploadTotal.value = items.length
    let failed = 0

    try {
      for (const item of items) {
        if (!item.raw) continue
        try {
          const formData = new FormData()
          formData.append('file', item.raw)
          formData.append('folderId', String(folderId))
          if (remark.value) formData.append('remark', remark.value)
          await uploadFile(formData)
        } catch {
          failed++
        } finally {
          uploadDone.value++
        }
      }

      if (failed === 0) {
        ElMessage.success(`成功上传 ${items.length} 个文件`)
        visible.value = false
        emit('success', folderId)
      } else if (failed < items.length) {
        ElMessage.warning(`部分上传失败：成功 ${items.length - failed}，失败 ${failed}`)
        emit('success', folderId)
      } else {
        ElMessage.error('上传失败，请重试')
      }
    } finally {
      uploading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .upload-form {
    margin-bottom: 8px;
  }

  .upload-icon {
    font-size: 48px;
    color: var(--el-color-primary);
    margin-bottom: 8px;
  }

  .upload-tip {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 1.5;
  }

  .upload-progress {
    margin-top: 16px;
  }
</style>

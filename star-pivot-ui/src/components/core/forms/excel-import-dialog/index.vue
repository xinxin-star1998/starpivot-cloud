<!--
  通用 EasyExcel 导入弹窗（multipart 上传）
  使用：<ExcelImportDialog v-model="visible" title="导入" :import-fn="importFn" :download-template-fn="downloadTpl" @success="reload" />
-->
<template>
  <ElDialog
    :model-value="modelValue"
    :title="title"
    width="520px"
    destroy-on-close
    @update:model-value="handleClose"
  >
    <div class="excel-import-content">
      <ElUpload
        drag
        :auto-upload="false"
        accept=".xls,.xlsx"
        :file-list="fileList"
        :limit="1"
        :on-change="handleUploadChange"
        :on-remove="handleUploadRemove"
      >
        <ElIcon class="excel-import-icon">
          <UploadFilled />
        </ElIcon>
        <div class="el-upload__text">
          将文件拖到此处，或
          <em>点击上传</em>
        </div>
      </ElUpload>

      <div class="excel-import-bottom">
        <ElCheckbox v-if="showOverwrite" v-model="overwrite">
          {{ overwriteLabel }}
        </ElCheckbox>
        <slot name="extra-options" />
        <p class="excel-import-desc">
          仅允许导入 xls、xlsx 格式文件。
          <ElButton type="primary" link :loading="templateLoading" @click="handleDownloadTemplate">
            下载模板
          </ElButton>
        </p>
      </div>
    </div>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleConfirm">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {UploadFile} from 'element-plus'
import {ElMessage} from 'element-plus'
import {UploadFilled} from '@element-plus/icons-vue'
import type {ExcelImportResultVo} from '@/api/common/excel'

defineOptions({ name: 'ExcelImportDialog' })

  const props = withDefaults(
    defineProps<{
      modelValue?: boolean
      title?: string
      showOverwrite?: boolean
      overwriteLabel?: string
      importFn: (file: File, updateSupport: boolean) => Promise<unknown>
      downloadTemplateFn: () => Promise<void>
    }>(),
    {
      modelValue: false,
      title: '数据导入',
      showOverwrite: true,
      overwriteLabel: '是否更新已存在的数据（需填写ID列）'
    }
  )

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    success: []
    error: [err: unknown]
  }>()

  const fileList = ref<UploadFile[]>([])
  const overwrite = ref(false)
  const loading = ref(false)
  const templateLoading = ref(false)

  function handleClose() {
    emit('update:modelValue', false)
    fileList.value = []
    overwrite.value = false
    loading.value = false
    templateLoading.value = false
  }

  function handleUploadChange(_file: UploadFile, list: UploadFile[]) {
    fileList.value = list.slice(-1)
  }

  function handleUploadRemove(_file: UploadFile, list: UploadFile[]) {
    fileList.value = list
  }

  async function handleDownloadTemplate() {
    templateLoading.value = true
    try {
      await props.downloadTemplateFn()
    } catch (err) {
      ElMessage.error('模板下载失败')
      emit('error', err)
    } finally {
      templateLoading.value = false
    }
  }

  async function handleConfirm() {
    const raw = fileList.value[0]?.raw
    if (!raw || !(raw instanceof File)) {
      ElMessage.warning('请先选择 Excel 文件')
      return
    }
    loading.value = true
    try {
      const result = (await props.importFn(raw, overwrite.value)) as ExcelImportResultVo | void
      if (result && result.failCount > 0) {
        const errMsg = result.errorMessages?.length
          ? result.errorMessages.slice(0, 5).join('；') +
            (result.errorMessages.length > 5 ? '...' : '')
          : ''
        ElMessage.warning(
          `导入完成：成功 ${result.successCount} 条，失败 ${result.failCount} 条${errMsg ? '。' + errMsg : ''}`
        )
      } else {
        ElMessage.success(
          result?.successCount != null ? `导入成功：共 ${result.successCount} 条` : '导入成功'
        )
      }
      emit('success')
      handleClose()
    } catch (err) {
      ElMessage.error('导入失败')
      emit('error', err)
    } finally {
      loading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .excel-import-content {
    padding: 4px 0;
  }

  .excel-import-icon {
    margin-bottom: 8px;
    font-size: 48px;
    color: var(--el-color-primary);
  }

  .excel-import-bottom {
    margin-top: 12px;
  }

  .excel-import-desc {
    margin: 8px 0 0;
    font-size: 13px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }
</style>

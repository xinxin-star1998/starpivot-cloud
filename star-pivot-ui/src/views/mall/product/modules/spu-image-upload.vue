<template>
  <div class="spu-image-upload">
    <ElUpload
      v-model:file-list="fileList"
      list-type="picture-card"
      :multiple="multiple"
      :limit="limit"
      accept="image/jpeg,image/png,image/gif,image/webp"
      :http-request="handleUpload"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
    >
      <ElIcon><Plus /></ElIcon>
    </ElUpload>
    <p v-if="hint" class="spu-image-upload__hint">{{ hint }}</p>
  </div>
</template>

<script setup lang="ts">
  import { Plus } from '@element-plus/icons-vue'
  import type { UploadProps, UploadUserFile } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { ref, watch } from 'vue'
  import { type GoodsImageUploadVO, uploadGoodsImage } from '@/api/mall/goods-image'
  import {
    isStorageObjectName,
    normalizeToObjectName,
    resolveGoodsImageDisplayUrl,
    resolveGoodsImageDisplayUrls
  } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'SpuImageUpload' })

  const props = withDefaults(
    defineProps<{
      /** OSS 对象路径列表（goods/...），存库字段 */
      modelValue: string[]
      /** 编辑中的商品 ID，用于 OSS 分目录 */
      goodsId?: number
      multiple?: boolean
      limit?: number
      hint?: string
    }>(),
    {
      multiple: true,
      limit: 10,
      hint: ''
    }
  )

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string[]): void
  }>()

  type SpuUploadFile = UploadUserFile & { response?: GoodsImageUploadVO }

  const fileList = ref<SpuUploadFile[]>([])
  const uploading = ref(0)
  const syncing = ref(false)

  const getObjectName = (file: SpuUploadFile): string | undefined => {
    if (file.response?.objectName) return file.response.objectName
    return normalizeToObjectName(file.url) || undefined
  }

  const collectObjectNames = (): string[] =>
    fileList.value
      .map(getObjectName)
      .filter((name): name is string => !!name && isStorageObjectName(name))

  const emitObjectNames = () => {
    emit('update:modelValue', collectObjectNames())
  }

  const buildFileList = async (objectNames: string[]) => {
    syncing.value = true
    try {
      const urlMap = await resolveGoodsImageDisplayUrls(objectNames)
      fileList.value = objectNames.map((objectName, index) => ({
        name: `image-${index + 1}`,
        url: urlMap.get(objectName) || '',
        status: 'success' as const,
        uid: Date.now() + index,
        response: { objectName, displayUrl: urlMap.get(objectName) || '' }
      }))
    } finally {
      syncing.value = false
    }
  }

  watch(
    () => props.modelValue,
    async (objectNames) => {
      if (uploading.value > 0 || syncing.value) return
      const next = (objectNames ?? [])
        .map(normalizeToObjectName)
        .filter((name): name is string => isStorageObjectName(name))
      const current = collectObjectNames()
      if (next.join('|') === current.join('|')) return
      await buildFileList(next)
    },
    { immediate: true }
  )

  const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(rawFile.type)) {
      ElMessage.warning('仅支持 JPG、PNG、GIF、WEBP 格式')
      return false
    }
    if (rawFile.size > 5 * 1024 * 1024) {
      ElMessage.warning('单张图片不能超过 5MB')
      return false
    }
    return true
  }

  const handleUpload: UploadProps['httpRequest'] = async ({ file, onSuccess, onError }) => {
    uploading.value += 1
    try {
      const vo = await uploadGoodsImage(file as File, props.goodsId)
      if (!vo?.objectName) {
        throw new Error('上传响应无效')
      }

      const displayUrl =
        vo.displayUrl || (await resolveGoodsImageDisplayUrl(vo.objectName, vo.displayUrl))

      const payload: GoodsImageUploadVO = { ...vo, displayUrl }
      onSuccess(payload)

      const target = fileList.value.find((f) => f.uid === file.uid)
      if (target && displayUrl) {
        target.url = displayUrl
      }

      emitObjectNames()
      ElMessage.success('图片上传成功')
    } catch (error) {
      const message = error instanceof Error ? error.message : '图片上传失败'
      ElMessage.error(message)
      onError({ message, name: 'UploadError', status: 500, method: 'POST', url: '' })
    } finally {
      uploading.value -= 1
    }
  }

  const handleRemove = () => {
    emitObjectNames()
  }

  const handleExceed = () => {
    ElMessage.warning(`最多上传 ${props.limit} 张图片`)
  }
</script>

<style scoped lang="scss">
  .spu-image-upload__hint {
    margin: 8px 0 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>

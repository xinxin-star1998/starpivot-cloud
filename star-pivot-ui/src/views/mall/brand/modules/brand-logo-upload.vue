<template>
  <div class="brand-logo-upload">
    <div
      :class="{ 'is-filled': !!previewUrl, 'is-uploading': uploading }"
      class="brand-logo-upload__box"
      @click="handleBoxClick"
    >
      <img v-if="previewUrl" :src="previewUrl" alt="品牌 Logo" class="brand-logo-upload__img" />
      <div v-else class="brand-logo-upload__placeholder">
        <ElIcon><Plus /></ElIcon>
      </div>
      <div v-if="previewUrl && !uploading" class="brand-logo-upload__actions" @click.stop>
        <button class="brand-logo-upload__action" title="更换" type="button" @click="triggerSelect">
          <ArtSvgIcon icon="ri:pencil-line" />
        </button>
        <button
          class="brand-logo-upload__action brand-logo-upload__action--danger"
          title="删除"
          type="button"
          @click="handleRemove"
        >
          <ArtSvgIcon icon="ri:delete-bin-line" />
        </button>
      </div>
      <div v-if="uploading" class="brand-logo-upload__loading">
        <ElIcon class="is-loading"><Loading /></ElIcon>
      </div>
    </div>
    <input
      ref="fileInputRef"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="brand-logo-upload__input"
      type="file"
      @change="handleFileChange"
    />
    <p class="brand-logo-upload__hint">仅支持 1 张 Logo，JPG / PNG / GIF / WEBP，不超过 2MB</p>
  </div>
</template>

<script lang="ts" setup>
  import { Loading, Plus } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { computed, onUnmounted, ref, watch } from 'vue'
  import { uploadBrandLogo } from '@/api/mall/brand-image'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { resolveGoodsImageDisplayUrl } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'BrandLogoUpload' })

  const props = defineProps<{
    modelValue?: string
    brandId?: number
  }>()

  const fileInputRef = ref<HTMLInputElement>()
  const serverPreviewUrl = ref('')
  const blobUrl = ref('')
  const pendingFile = ref<File | null>(null)
  const removed = ref(false)
  const uploading = ref(false)

  const previewUrl = computed(() => {
    if (removed.value) return ''
    if (blobUrl.value) return blobUrl.value
    return serverPreviewUrl.value
  })

  const revokeBlob = () => {
    if (blobUrl.value) {
      URL.revokeObjectURL(blobUrl.value)
      blobUrl.value = ''
    }
  }

  const resetLocalState = () => {
    pendingFile.value = null
    removed.value = false
    revokeBlob()
  }

  const syncServerPreview = async (raw?: string) => {
    const trimmed = raw?.trim()
    if (!trimmed) {
      serverPreviewUrl.value = ''
      return
    }
    serverPreviewUrl.value = (await resolveGoodsImageDisplayUrl(trimmed)) || trimmed
  }

  watch(
    () => props.modelValue,
    async (value) => {
      resetLocalState()
      await syncServerPreview(value)
    },
    { immediate: true }
  )

  onUnmounted(() => {
    revokeBlob()
  })

  const triggerSelect = () => {
    fileInputRef.value?.click()
  }

  const handleBoxClick = () => {
    if (uploading.value) return
    if (!previewUrl.value) {
      triggerSelect()
    }
  }

  const validateFile = (file: File): boolean => {
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(file.type)) {
      ElMessage.warning('仅支持 JPG、PNG、GIF、WEBP 格式')
      return false
    }
    if (file.size > 2 * 1024 * 1024) {
      ElMessage.warning('单张图片不能超过 2MB')
      return false
    }
    return true
  }

  const handleFileChange = (event: Event) => {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file || !validateFile(file)) return

    revokeBlob()
    pendingFile.value = file
    removed.value = false
    blobUrl.value = URL.createObjectURL(file)
  }

  const handleRemove = () => {
    ElMessageBox.confirm('确定删除Logo？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        revokeBlob()
        pendingFile.value = null
        removed.value = true
      })
      .catch(() => {})
  }

  /** 提交表单时上传待选 Logo，返回最终存库路径 */
  const resolveLogo = async (): Promise<string | undefined> => {
    if (removed.value) return undefined
    if (!pendingFile.value) return props.modelValue?.trim() || undefined

    uploading.value = true
    try {
      const vo = await uploadBrandLogo(pendingFile.value, props.brandId)
      if (!vo?.objectName) {
        throw new Error('上传响应无效')
      }
      return vo.objectName
    } finally {
      uploading.value = false
    }
  }

  defineExpose({ resolveLogo })
</script>

<style lang="scss" scoped>
  .brand-logo-upload__box {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    overflow: hidden;
    cursor: pointer;
    background: var(--el-fill-color-lighter);
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    transition: border-color 0.2s;

    &:hover:not(.is-uploading) {
      border-color: var(--el-color-primary);
    }

    &.is-filled {
      cursor: default;
      border-style: solid;
    }

    &.is-uploading {
      cursor: wait;
    }
  }

  .brand-logo-upload__img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .brand-logo-upload__placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: var(--el-text-color-secondary);
  }

  .brand-logo-upload__actions {
    position: absolute;
    inset: 0;
    display: flex;
    gap: 12px;
    align-items: center;
    justify-content: center;
    background: rgb(0 0 0 / 45%);
    opacity: 0;
    transition: opacity 0.2s;
  }

  .brand-logo-upload__box.is-filled:hover .brand-logo-upload__actions {
    opacity: 1;
  }

  .brand-logo-upload__action {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    font-size: 16px;
    color: #fff;
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 4px;
    transition: background-color 0.2s;

    &:hover {
      background: rgb(255 255 255 / 18%);
    }

    &--danger:hover {
      background: rgb(245 108 108 / 35%);
    }
  }

  .brand-logo-upload__loading {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: var(--el-color-primary);
    background: rgb(255 255 255 / 72%);
  }

  .brand-logo-upload__input {
    display: none;
  }

  .brand-logo-upload__hint {
    margin: 8px 0 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>

<template>
  <div class="mall-image-upload">
    <!-- 多图 -->
    <template v-if="multiple">
      <ul class="mall-image-upload__cards">
        <li v-for="(item, index) in displayItems" :key="item.objectName" class="mall-image-upload__card">
          <ElImage
            :src="item.url"
            fit="cover"
            class="mall-image-upload__card-img"
            :preview-src-list="displayItems.map((i) => i.url).filter(Boolean)"
            :initial-index="index"
            preview-teleported
          />
          <button
            class="mall-image-upload__card-remove"
            title="删除"
            type="button"
            @click="removeAt(index)"
          >
            <ArtSvgIcon icon="ri:close-line" />
          </button>
        </li>
        <li
          v-if="canAddMore"
          class="mall-image-upload__card mall-image-upload__card--add"
          @click="openPickerOrUpload"
        >
          <ElIcon><Plus /></ElIcon>
        </li>
      </ul>
    </template>

    <!-- 单图 -->
    <template v-else>
      <div
        :class="{ 'is-filled': !!previewUrl, 'is-uploading': uploading }"
        class="mall-image-upload__single"
        @click="handleSingleClick"
      >
        <img v-if="previewUrl" :src="previewUrl" alt="图片预览" class="mall-image-upload__img" />
        <div v-else class="mall-image-upload__placeholder">
          <ElIcon><Plus /></ElIcon>
          <span v-if="canUseFilePicker" class="mall-image-upload__placeholder-text">选择图片</span>
        </div>
        <div v-if="previewUrl && !uploading" class="mall-image-upload__overlay" @click.stop>
          <button class="mall-image-upload__icon-btn" title="更换" type="button" @click="openPickerOrUpload">
            <ArtSvgIcon icon="ri:pencil-line" />
          </button>
          <button
            class="mall-image-upload__icon-btn mall-image-upload__icon-btn--danger"
            title="删除"
            type="button"
            @click="handleSingleRemove"
          >
            <ArtSvgIcon icon="ri:delete-bin-line" />
          </button>
        </div>
        <div v-if="uploading" class="mall-image-upload__loading">
          <ElIcon class="is-loading"><Loading /></ElIcon>
        </div>
      </div>
    </template>

    <p v-if="hint" class="mall-image-upload__hint">{{ hint }}</p>
    <p v-else-if="canUseFilePicker" class="mall-image-upload__hint">
      点击选择图片，优先从素材库挑选；没有合适的可在弹窗内上传新图
    </p>

    <input
      ref="fallbackInputRef"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="mall-image-upload__input"
      type="file"
      :multiple="multiple"
      @change="handleFallbackUpload"
    />

    <MallFilePickerDialog
      v-model="pickerVisible"
      :limit="pickerLimit"
      :title="pickerTitle"
      @confirm="handlePickerConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import {Loading, Plus} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {computed, onUnmounted, ref, watch} from 'vue'
import {uploadMallImage} from '@/api/mall/mall-image'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {
  isStorageObjectName,
  normalizeToObjectName,
  resolveGoodsImageDisplayUrl,
  resolveGoodsImageDisplayUrls
} from '@/utils/mall/goods-image-url'
import {useMenuStore} from '@/store/modules/menu'
import MallFilePickerDialog from './mall-file-picker-dialog.vue'

defineOptions({ name: 'MallImageUpload' })

  const props = withDefaults(
    defineProps<{
      modelValue?: string | string[]
      multiple?: boolean
      limit?: number
      maxSizeMb?: number
      hint?: string
      singleSize?: number
      enablePicker?: boolean
      pickerTitle?: string
    }>(),
    {
      multiple: false,
      limit: 10,
      maxSizeMb: 5,
      hint: '',
      singleSize: 80,
      enablePicker: true,
      pickerTitle: '选择商品图片'
    }
  )

  const emit = defineEmits<{
    'update:modelValue': [string | string[]]
  }>()

  const menuStore = useMenuStore()
  /** 全局菜单权限判断，避免商城页路由 meta 不含 file:* 导致弹窗无法打开 */
  const canUseFilePicker = computed(
    () => props.enablePicker && menuStore.hasPerm('file:resource:query')
  )

  const pickerVisible = ref(false)
  const uploading = ref(false)
  const fallbackInputRef = ref<HTMLInputElement>()

  const serverPreviewUrl = ref('')
  const blobUrl = ref('')
  const pendingFile = ref<File | null>(null)
  const singleRemoved = ref(false)

  const displayItems = ref<{ objectName: string; url: string }[]>([])

  const maxSizeBytes = computed(() => props.maxSizeMb * 1024 * 1024)
  const singleSizePx = computed(() => `${props.singleSize}px`)

  const objectNames = computed((): string[] => {
    if (props.multiple) {
      return (props.modelValue as string[] | undefined)?.map(normalizeToObjectName).filter(
        (name): name is string => !!name && isStorageObjectName(name)
      ) ?? []
    }
    const single = normalizeToObjectName(props.modelValue as string | undefined)
    return single && isStorageObjectName(single) ? [single] : []
  })

  const canAddMore = computed(() => objectNames.value.length < props.limit)

  const pickerLimit = computed(() =>
    props.multiple ? Math.max(1, props.limit - objectNames.value.length) : 1
  )

  const previewUrl = computed(() => {
    if (singleRemoved.value) return ''
    if (blobUrl.value) return blobUrl.value
    return serverPreviewUrl.value
  })

  const emitValue = (names: string[]) => {
    if (props.multiple) {
      emit('update:modelValue', names)
    } else {
      emit('update:modelValue', names[0] || '')
    }
  }

  const syncDisplayItems = async (names: string[]) => {
    if (!names.length) {
      displayItems.value = []
      return
    }
    const urlMap = await resolveGoodsImageDisplayUrls(names)
    displayItems.value = names.map((objectName) => ({
      objectName,
      url: urlMap.get(objectName) || ''
    }))
  }

  watch(
    () => props.modelValue,
    async (value) => {
      if (props.multiple) {
        const next = ((value as string[] | undefined) ?? [])
          .map(normalizeToObjectName)
          .filter((name): name is string => isStorageObjectName(name))
        await syncDisplayItems(next)
        return
      }

      pendingFile.value = null
      singleRemoved.value = false
      revokeBlob()
      const raw = typeof value === 'string' ? value : ''
      if (!raw?.trim()) {
        serverPreviewUrl.value = ''
        return
      }
      serverPreviewUrl.value = (await resolveGoodsImageDisplayUrl(raw)) || ''
    },
    { immediate: true, deep: true }
  )

  const revokeBlob = () => {
    if (blobUrl.value) {
      URL.revokeObjectURL(blobUrl.value)
      blobUrl.value = ''
    }
  }

  onUnmounted(revokeBlob)

  const openPickerOrUpload = () => {
    if (canUseFilePicker.value) {
      pickerVisible.value = true
      return
    }
    fallbackInputRef.value?.click()
  }

  const handleSingleClick = () => {
    if (uploading.value) return
    if (!previewUrl.value) openPickerOrUpload()
  }

  const removeAt = (index: number) => {
    const next = [...objectNames.value]
    next.splice(index, 1)
    emitValue(next)
  }

  const validateFile = (file: File): boolean => {
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(file.type)) {
      ElMessage.warning('仅支持 JPG、PNG、GIF、WEBP 格式')
      return false
    }
    if (file.size > maxSizeBytes.value) {
      ElMessage.warning(`单张图片不能超过 ${props.maxSizeMb}MB`)
      return false
    }
    return true
  }

  const handleFallbackUpload = async (event: Event) => {
    const input = event.target as HTMLInputElement
    const files = input.files ? Array.from(input.files) : []
    input.value = ''
    if (!files.length) return

    const validFiles = files.filter((file) => validateFile(file))
    if (!validFiles.length) return

    if (props.multiple) {
      const remain = props.limit - objectNames.value.length
      if (remain <= 0) {
        ElMessage.warning(`最多 ${props.limit} 张图片`)
        return
      }
      const batch = validFiles.slice(0, remain)
      uploading.value = true
      try {
        const uploaded: string[] = []
        for (const file of batch) {
          const vo = await uploadMallImage(file)
          if (vo?.objectName) uploaded.push(vo.objectName)
        }
        if (!uploaded.length) throw new Error('上传响应无效')
        emitValue([...objectNames.value, ...uploaded])
        ElMessage.success('图片上传成功')
      } catch (error) {
        const message = error instanceof Error ? error.message : '图片上传失败'
        ElMessage.error(message)
      } finally {
        uploading.value = false
      }
      return
    }

    const file = validFiles[0]
    uploading.value = true
    try {
      const vo = await uploadMallImage(file)
      if (!vo?.objectName) throw new Error('上传响应无效')
      emitValue([vo.objectName])
      ElMessage.success('图片上传成功')
    } catch (error) {
      const message = error instanceof Error ? error.message : '图片上传失败'
      ElMessage.error(message)
    } finally {
      uploading.value = false
    }
  }

  const handleSingleRemove = () => {
    ElMessageBox.confirm('确定删除图片？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        revokeBlob()
        pendingFile.value = null
        singleRemoved.value = true
        emitValue([])
      })
      .catch(() => {})
  }

  const handlePickerConfirm = (names: string[]) => {
    if (!names.length) return
    if (props.multiple) {
      const merged = [...objectNames.value]
      for (const name of names) {
        if (!merged.includes(name)) merged.push(name)
      }
      if (merged.length > props.limit) {
        ElMessage.warning(`最多 ${props.limit} 张图片`)
        emitValue(merged.slice(0, props.limit))
      } else {
        emitValue(merged)
      }
    } else {
      emitValue([names[0]])
    }
  }

  /** 单图延迟上传模式：提交前调用（兼容品牌 Logo 表单） */
  const resolveObjectName = async (): Promise<string | undefined> => {
    if (singleRemoved.value) return undefined
    if (pendingFile.value) {
      uploading.value = true
      try {
        const vo = await uploadMallImage(pendingFile.value)
        if (!vo?.objectName) throw new Error('上传响应无效')
        pendingFile.value = null
        revokeBlob()
        return vo.objectName
      } finally {
        uploading.value = false
      }
    }
    const single = normalizeToObjectName(props.modelValue as string | undefined)
    return single && isStorageObjectName(single) ? single : undefined
  }

  defineExpose({ resolveObjectName, openPicker: openPickerOrUpload })
</script>

<style scoped lang="scss">
  .mall-image-upload__cards {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0;
    margin: 0;
    list-style: none;
  }

  .mall-image-upload__card {
    position: relative;
    width: 148px;
    height: 148px;
    overflow: hidden;
    border: 1px solid var(--el-border-color);
    border-radius: 6px;
  }

  .mall-image-upload__card-img {
    width: 100%;
    height: 100%;
  }

  .mall-image-upload__card-remove {
    position: absolute;
    top: 4px;
    right: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    padding: 0;
    font-size: 14px;
    color: #fff;
    cursor: pointer;
    background: rgb(0 0 0 / 45%);
    border: none;
    border-radius: 50%;

    &:hover {
      background: rgb(0 0 0 / 65%);
    }
  }

  .mall-image-upload__card--add {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: var(--el-text-color-secondary);
    cursor: pointer;
    background: var(--el-fill-color-lighter);
    border-style: dashed;
    transition: border-color 0.2s, color 0.2s;

    &:hover {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
    }
  }

  .mall-image-upload__hint {
    margin: 8px 0 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .mall-image-upload__single {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: v-bind(singleSizePx);
    height: v-bind(singleSizePx);
    overflow: hidden;
    cursor: pointer;
    background: var(--el-fill-color-lighter);
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;

    &.is-filled {
      cursor: default;
      border-style: solid;
    }

    &.is-uploading {
      cursor: wait;
    }
  }

  .mall-image-upload__img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .mall-image-upload__placeholder {
    display: flex;
    flex-direction: column;
    gap: 4px;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: var(--el-text-color-secondary);
  }

  .mall-image-upload__placeholder-text {
    font-size: 12px;
  }

  .mall-image-upload__overlay {
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

  .mall-image-upload__single.is-filled:hover .mall-image-upload__overlay {
    opacity: 1;
  }

  .mall-image-upload__icon-btn {
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

    &:hover {
      background: rgb(255 255 255 / 18%);
    }

    &--danger:hover {
      background: rgb(245 108 108 / 35%);
    }
  }

  .mall-image-upload__loading {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: var(--el-color-primary);
    background: rgb(255 255 255 / 72%);
  }

  .mall-image-upload__input {
    display: none;
  }
</style>

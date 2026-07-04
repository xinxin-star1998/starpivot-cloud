<template>
  <div class="art-avatar-upload">
    <div
      class="avatar-preview"
      :class="{ 'avatar-editable': showActions }"
      :style="{ width: size + 'px', height: size + 'px' }"
      @click="showActions ? triggerFileInput() : null"
    >
      <img v-if="imageUrl && imageUrl !== ''" :src="imageUrl" alt="头像预览" class="avatar-img" />
      <div v-else class="avatar-placeholder">
        <i class="iconfont icon-camera"></i>
        <span>{{ placeholder }}</span>
      </div>
      <!-- 删除图标：悬浮时显示在右上角 -->
      <div
        v-if="showActions && imageUrl && imageUrl !== ''"
        class="avatar-delete-icon"
        @click.stop="handleDelete"
        title="删除头像"
      >
        <ArtSvgIcon icon="ri:close-line" />
      </div>
    </div>
    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="file-input"
      @change="handleFileChange"
    />
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
  </div>
</template>

<script setup lang="ts">
import {fetchDeleteAvatar, fetchGetAvatarPresignedUrl, fetchUploadAvatar} from '@/api/user/user'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {extractOssObjectPath, needsOssPresignedDisplay} from '@/utils/storage/oss-object-path'

// Props
  const props = defineProps({
    modelValue: {
      type: String,
      default: ''
    },
    userId: {
      type: [String, Number],
      default: ''
    },
    size: {
      type: Number,
      default: 120
    },
    placeholder: {
      type: String,
      default: '点击上传头像'
    },
    // 是否自动上传到服务器，新增用户时设为false，编辑用户时设为true
    autoUpload: {
      type: Boolean,
      default: true
    },
    // 是否使用临时访问链接
    usePresignedUrl: {
      type: Boolean,
      default: false
    },
    // 是否显示操作按钮（上传/删除），设为 false 时只显示头像预览
    showActions: {
      type: Boolean,
      default: true
    }
  })

  // 新增：保存当前选择的文件，用于延迟上传
  const currentFile = ref<File | null>(null)

  // Emits
  const emit = defineEmits(['update:modelValue', 'success', 'error'])

  // Refs
  const fileInput = ref<HTMLInputElement | null>(null)
  const imageUrl = ref(props.modelValue)
  const errorMessage = ref('')

  // Watch for modelValue changes
  watch(
    () => props.modelValue,
    (newVal) => {
      // OSS 私有桶永久地址直接赋给 imageUrl 会 403，先不展示，等 presigned 再展示
      const isPrivateBucketUrl = props.usePresignedUrl && newVal && needsOssPresignedDisplay(newVal)
      const filePath = newVal ? extractOssObjectPath(newVal) : null
      if (isPrivateBucketUrl && filePath) {
        imageUrl.value = ''
        getPresignedUrlForExistingAvatar(filePath)
      } else {
        imageUrl.value = newVal ?? ''
      }
    }
  )

  // Watch for usePresignedUrl changes
  watch(
    () => props.usePresignedUrl,
    (newVal) => {
      const url = imageUrl.value
      if (!newVal || !url || !url.includes('/')) return
      const filePath = extractOssObjectPath(url)
      if (filePath && needsOssPresignedDisplay(url)) {
        imageUrl.value = ''
        getPresignedUrlForExistingAvatar(filePath)
      }
    }
  )

  // 获取现有头像的临时访问链接
  const getPresignedUrlForExistingAvatar = async (filePath: string) => {
    try {
      const response = (await fetchGetAvatarPresignedUrl(filePath)) as any
      const presigned = response?.presignedUrl ?? response?.data?.presignedUrl
      if (presigned) {
        imageUrl.value = presigned
        emit('update:modelValue', presigned)
      } else {
        imageUrl.value = ''
      }
    } catch (error: any) {
      if (import.meta.env.DEV) console.error('获取临时访问链接失败：', error)
      imageUrl.value = ''
    }
  }

  // 组件挂载时初始化（OSS 永久地址不先赋给 imageUrl，避免 403 导致不显示）
  onMounted(() => {
    const val = props.modelValue
    if (!props.usePresignedUrl || !val || !val.includes('/')) {
      imageUrl.value = val ?? ''
      return
    }
    const filePath = extractOssObjectPath(val)
    if (filePath && needsOssPresignedDisplay(val)) {
      imageUrl.value = ''
      getPresignedUrlForExistingAvatar(filePath)
    } else {
      imageUrl.value = val
    }
  })

  // Methods
  const triggerFileInput = () => {
    fileInput.value?.click()
  }

  // 手动上传方法，用于新增用户后调用
  const uploadImageToServer = async () => {
    if (!currentFile.value || !props.userId) {
      return Promise.resolve()
    }

    try {
      errorMessage.value = ''
      const formData = new FormData()
      formData.append('file', currentFile.value)
      formData.append('userId', String(props.userId))
      formData.append('usePresignedUrl', String(props.usePresignedUrl))

      const response = (await fetchUploadAvatar(formData)) as any
      // 私有桶场景：后端同时返回 avatarUrl（永久，存库）和 presignedUrl（临时，展示用）
      const urlForSave = response.avatarUrl
      const urlForDisplay = response.presignedUrl || response.avatarUrl
      imageUrl.value = urlForDisplay
      emit('update:modelValue', urlForSave)
      emit('success', urlForSave)
      // 上传成功后清空临时文件
      currentFile.value = null
    } catch (uploadError: any) {
      // 上传失败时，保持本地预览，不恢复原来的头像
      errorMessage.value = uploadError.message || '上传失败，请重试'
      emit('error', uploadError)
      throw uploadError
    }
  }

  // 暴露手动上传方法给父组件
  defineExpose({
    uploadImageToServer
  })

  const handleFileChange = async (event: Event) => {
    const target = event.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return

    try {
      errorMessage.value = ''

      // 先显示本地预览
      const reader = new FileReader()
      reader.onload = async (e) => {
        // 显示本地预览
        const localPreviewUrl = e.target?.result as string
        imageUrl.value = localPreviewUrl

        // 保存当前文件
        currentFile.value = file

        // 根据autoUpload属性决定是否立即上传到服务器
        if (props.autoUpload) {
          try {
            await uploadImageToServer()
          } catch {
            // 错误已在uploadImageToServer中处理
          }
        } else {
          // 非自动上传模式下（新增用户时），只显示本地预览，不触发服务器上传
          // 不更新modelValue，避免将临时的data URL保存到表单中
        }
      }
      reader.readAsDataURL(file)
    } catch (error: any) {
      errorMessage.value = error.message || '预览失败，请重试'
      emit('error', error)
    } finally {
      // Reset file input
      if (target) {
        target.value = ''
      }
    }
  }

  const handleDelete = async () => {
    if (!props.userId) {
      errorMessage.value = '请先输入用户ID'
      return
    }

    try {
      errorMessage.value = ''
      await fetchDeleteAvatar(String(props.userId))
      imageUrl.value = ''
      emit('update:modelValue', '')
      emit('success', '')
    } catch (error: any) {
      errorMessage.value = error.message || '删除失败，请重试'
      emit('error', error)
    }
  }
</script>

<style scoped lang="scss">
  .art-avatar-upload {
    display: flex;
    flex-direction: column;
    gap: 16px;
    align-items: center;

    .avatar-preview {
      position: relative;
      overflow: hidden;
      border: 2px dashed #d9d9d9;
      border-radius: 50%;
      transition: all 0.3s ease;

      &.avatar-editable {
        cursor: pointer;

        &:hover {
          border-color: #1890ff;

          // 悬浮时显示删除图标
          .avatar-delete-icon {
            visibility: visible;
            opacity: 1;
          }
        }
      }

      .avatar-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .avatar-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        color: #999;
        background-color: #f5f5f5;

        i {
          margin-bottom: 8px;
          font-size: 24px;
        }

        span {
          font-size: 14px;
        }
      }

      // 删除图标：悬浮时显示在右上角
      .avatar-delete-icon {
        position: absolute;
        top: 0;
        right: 0;
        z-index: 10;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        font-size: 16px;
        color: #fff;
        cursor: pointer;
        visibility: hidden;
        user-select: none;
        background-color: rgb(0 0 0 / 60%);
        border-radius: 0 0 0 50%;
        // 默认隐藏，悬浮时显示
        opacity: 0;
        transition: all 0.3s ease;

        .art-svg-icon {
          font-size: 16px;
          color: #fff;
        }

        &:hover {
          background-color: rgb(255 77 79 / 80%);
        }
      }
    }

    .file-input {
      display: none;
    }

    .error-message {
      margin-top: 8px;
      font-size: 12px;
      color: #ff4d4f;
    }
  }
</style>

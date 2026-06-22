<template>
  <ElDrawer
    v-model="visible"
    direction="rtl"
    size="480px"
    destroy-on-close
    :close-on-click-modal="true"
    class="file-preview-drawer"
    @closed="reset"
  >
    <template #header>
      <div class="preview-header">
        <div class="preview-header__main">
          <ArtSvgIcon :icon="fileIcon" class="preview-header__type-icon" />
          <span class="preview-header__title" :title="detail?.fileName">{{ shortFileName }}</span>
          <ElTag v-if="detail?.mediaTypeLabel" size="small" :type="mediaTagType" effect="light">
            {{ detail.mediaTypeLabel }}
          </ElTag>
        </div>
        <div v-if="detail?.fileName" class="preview-header__sub" :title="detail.fileName">
          {{ detail.fileName }}
        </div>
      </div>
    </template>

    <div v-loading="loading" class="drawer-body">
      <div class="preview-body">
        <template v-if="previewUrl">
          <img
            v-if="mode === 'image'"
            :src="previewUrl"
            class="preview-image"
            alt="preview"
            @load="onImageLoad"
          />
          <video v-else-if="mode === 'video'" :src="previewUrl" class="preview-media" controls />
          <audio v-else-if="mode === 'audio'" :src="previewUrl" class="preview-audio" controls />
          <iframe v-else-if="mode === 'pdf'" :src="previewUrl" class="preview-pdf" title="pdf" />
          <div v-else class="preview-fallback">
            <ArtSvgIcon :icon="fileIcon" class="preview-fallback__icon" />
            <p>该文件类型不支持在线预览</p>
          </div>
        </template>
      </div>

      <div v-if="detail" class="info-grid">
        <div class="info-card">
          <span class="info-card__label">STORAGE</span>
          <span class="info-card__value">{{ storageLabel }}</span>
        </div>
        <div class="info-card">
          <span class="info-card__label">SIZE</span>
          <span class="info-card__value">{{ formatFileSize(detail.fileSize) }}</span>
        </div>
        <div class="info-card">
          <span class="info-card__label">KIND</span>
          <span class="info-card__value">{{ detail.mediaType || '-' }}</span>
        </div>
        <div class="info-card">
          <span class="info-card__label">FOLDER</span>
          <span class="info-card__value" :title="folderDisplay">{{ folderDisplay }}</span>
        </div>
      </div>

      <div v-if="detail" class="meta-section">
        <div class="meta-section__title">元数据</div>
        <dl class="meta-list">
          <div v-if="detail.fileId" class="meta-row">
            <dt>文件 ID</dt>
            <dd>{{ detail.fileId }}</dd>
          </div>
          <div v-if="detail.contentType" class="meta-row">
            <dt>MIME</dt>
            <dd>{{ detail.contentType }}</dd>
          </div>
          <div v-if="detail.fileExt" class="meta-row">
            <dt>扩展名</dt>
            <dd>.{{ detail.fileExt }}</dd>
          </div>
          <div v-if="imageSize" class="meta-row">
            <dt>尺寸</dt>
            <dd>{{ imageSize }}</dd>
          </div>
          <div v-if="detail.categoryLabel" class="meta-row">
            <dt>业务分类</dt>
            <dd>{{ detail.categoryLabel }}</dd>
          </div>
          <div v-if="detail.objectName" class="meta-row">
            <dt>对象 Key</dt>
            <dd class="meta-row__mono">
              <span :title="detail.objectName">{{ detail.objectName }}</span>
              <ElButton link type="primary" @click="copyText(detail.objectName, '对象 Key')">
                复制
              </ElButton>
            </dd>
          </div>
          <div v-if="detail.bizType || detail.bizId" class="meta-row">
            <dt>业务关联</dt>
            <dd>{{ bizRef }}</dd>
          </div>
          <div v-if="detail.createBy" class="meta-row">
            <dt>上传人</dt>
            <dd>{{ detail.createBy }}</dd>
          </div>
          <div v-if="detail.createTime" class="meta-row">
            <dt>上传时间</dt>
            <dd>{{ formatDateTime(detail.createTime) }}</dd>
          </div>
          <div v-if="detail.updateTime" class="meta-row">
            <dt>更新时间</dt>
            <dd>{{ formatDateTime(detail.updateTime) }}</dd>
          </div>
          <div v-if="detail.remark" class="meta-row">
            <dt>备注</dt>
            <dd>{{ detail.remark }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <template #footer>
      <div class="preview-footer">
        <ElButton v-if="showMove" @click="handleMove">迁移</ElButton>
        <ElButton type="primary" :disabled="!previewUrl" @click="download">下载</ElButton>
        <ElButton :disabled="!previewUrl" @click="copyPreviewLink">复制链接</ElButton>
        <ElButton v-if="showDelete" type="danger" plain @click="handleDelete">删除</ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { fetchFileDetail, fetchFilePreviewUrl } from '@/api/file/file'
  import type { SysFile } from '@/api/file/types'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import {
    formatFileSize,
    getPreviewMode,
    openFileUrl,
    type PreviewMode
  } from '@/utils/file/file-center'
  import { getMediaTypeIcon, MEDIA_TYPE_TAG } from '../constants'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { computed, ref, watch } from 'vue'

  const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    file?: SysFile | null
  }>()

  const emit = defineEmits<{
    delete: [fileId: number]
    move: [fileIds: number[]]
  }>()

  const { hasAuth } = useAuth()

  const loading = ref(false)
  const previewUrl = ref('')
  const detail = ref<SysFile | null>(null)
  const imageSize = ref('')

  const mode = computed<PreviewMode>(() =>
    getPreviewMode(detail.value?.mediaType, detail.value?.fileExt)
  )

  const fileIcon = computed(() => getMediaTypeIcon(detail.value?.mediaType))

  const mediaTagType = computed(
    () =>
      (MEDIA_TYPE_TAG[detail.value?.mediaType || ''] || 'info') as
        | ''
        | 'success'
        | 'warning'
        | 'info'
        | 'primary'
        | 'danger'
  )

  const shortFileName = computed(() => {
    const name = detail.value?.fileName || '文件预览'
    return name.length > 22 ? `${name.slice(0, 20)}...` : name
  })

  const storageLabel = computed(() => {
    const p = detail.value?.storageProvider?.toUpperCase()
    if (p === 'OSS') return '阿里云 OSS'
    if (p === 'LOCAL') return '本地存储'
    return detail.value?.storageProvider || '-'
  })

  const folderDisplay = computed(() => {
    const cat = detail.value?.categoryLabel
    const folder = detail.value?.folderName
    if (cat && folder) return `${cat} / ${folder}`
    return folder || cat || '-'
  })

  const bizRef = computed(() => {
    const type = detail.value?.bizType
    const id = detail.value?.bizId
    if (type && id) return `${type} #${id}`
    return type || id || '-'
  })

  const showDelete = computed(() => hasAuth('file:resource:delete') && !!detail.value?.fileId)

  const showMove = computed(() => hasAuth('file:resource:move') && !!detail.value?.fileId)

  watch(
    () => [visible.value, props.file?.fileId] as const,
    async ([open, fileId]) => {
      if (!open || !fileId) return
      loading.value = true
      previewUrl.value = ''
      detail.value = props.file ? { ...props.file } : null
      imageSize.value = ''
      try {
        const [fileDetail, urlRes] = await Promise.all([
          fetchFileDetail(fileId),
          fetchFilePreviewUrl(fileId)
        ])
        detail.value = fileDetail
        previewUrl.value = urlRes.url
      } catch {
        previewUrl.value = props.file?.displayUrl || ''
      } finally {
        loading.value = false
      }
    }
  )

  function onImageLoad(e: Event) {
    const img = e.target as HTMLImageElement
    if (img.naturalWidth && img.naturalHeight) {
      imageSize.value = `${img.naturalWidth} × ${img.naturalHeight}`
    }
  }

  function formatDateTime(value: string) {
    const d = new Date(value.replace(/-/g, '/'))
    if (Number.isNaN(d.getTime())) return value
    return d.toLocaleString('zh-CN', { hour12: false })
  }

  async function copyText(text: string, label: string) {
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success(`${label}已复制`)
    } catch {
      ElMessage.error('复制失败')
    }
  }

  function copyPreviewLink() {
    if (previewUrl.value) {
      copyText(previewUrl.value, '访问链接')
    }
  }

  function download() {
    if (previewUrl.value) {
      openFileUrl(previewUrl.value, detail.value?.fileName)
    }
  }

  async function handleDelete() {
    const fileId = detail.value?.fileId
    if (!fileId) return
    await ElMessageBox.confirm('确认将文件移入回收站？', '提示', { type: 'warning' })
    emit('delete', fileId)
    visible.value = false
  }

  function handleMove() {
    const fileId = detail.value?.fileId
    if (!fileId) return
    emit('move', [fileId])
    visible.value = false
  }

  function reset() {
    previewUrl.value = ''
    detail.value = null
    imageSize.value = ''
  }
</script>

<style scoped lang="scss">
  .preview-header {
    &__main {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 0;
    }

    &__type-icon {
      flex-shrink: 0;
      font-size: 20px;
      color: var(--el-color-primary);
    }

    &__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__sub {
      margin-top: 6px;
      padding-left: 28px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .drawer-body {
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 100%;
    padding-bottom: 8px;
  }

  .preview-body {
    border-radius: 10px;
    background: var(--el-fill-color-lighter);
    overflow: hidden;
    min-height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .preview-image {
    display: block;
    max-width: 100%;
    max-height: min(42vh, 360px);
    object-fit: contain;
  }

  .preview-media {
    width: 100%;
    max-height: min(42vh, 360px);
    background: #000;
  }

  .preview-audio {
    width: 100%;
    padding: 24px 16px;
  }

  .preview-pdf {
    width: 100%;
    height: min(50vh, 420px);
    border: none;
    background: #fff;
  }

  .preview-fallback {
    text-align: center;
    padding: 40px 16px;
    color: var(--el-text-color-secondary);

    &__icon {
      font-size: 48px;
      margin-bottom: 8px;
      opacity: 0.5;
    }

    p {
      margin: 0;
      font-size: 13px;
    }
  }

  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .info-card {
    padding: 12px 14px;
    border-radius: 8px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-blank);

    &__label {
      display: block;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.04em;
      color: var(--el-text-color-placeholder);
      margin-bottom: 6px;
    }

    &__value {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .meta-section {
    &__title {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin-bottom: 10px;
    }
  }

  .meta-list {
    margin: 0;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    overflow: hidden;
  }

  .meta-row {
    display: grid;
    grid-template-columns: 80px 1fr;
    gap: 10px;
    padding: 10px 12px;
    font-size: 13px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }

    dt {
      margin: 0;
      color: var(--el-text-color-secondary);
    }

    dd {
      margin: 0;
      color: var(--el-text-color-primary);
      word-break: break-all;
    }

    &__mono {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      font-size: 12px;
      line-height: 1.5;

      span {
        flex: 1;
      }
    }
  }

  .preview-footer {
    display: flex;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 10px;
    width: 100%;
  }
</style>

<style lang="scss">
  .file-preview-drawer {
    .el-drawer__header {
      margin-bottom: 0;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    .el-drawer__body {
      padding-top: 16px;
      overflow-y: auto;
    }

    .el-drawer__footer {
      border-top: 1px solid var(--el-border-color-lighter);
      padding-top: 12px;
    }
  }
</style>

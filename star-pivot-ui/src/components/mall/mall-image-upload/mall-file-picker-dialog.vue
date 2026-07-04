<template>
  <ElDialog
    v-model="visible"
    append-to-body
    destroy-on-close
    :title="title"
    width="920px"
    class="mall-file-picker-dialog"
    @closed="handleClosed"
  >
    <div class="mall-file-picker">
      <!-- 左侧目录 -->
      <aside class="mall-file-picker__sidebar">
        <div class="mall-file-picker__sidebar-head">
          <span>素材目录</span>
          <ElButton link type="primary" @click="loadFolders">刷新</ElButton>
        </div>
        <ElScrollbar class="mall-file-picker__folder-scroll">
          <ul class="mall-file-picker__folders">
            <li
              :class="{ 'is-active': selectedFolderId == null }"
              class="mall-file-picker__folder"
              @click="selectFolder(undefined)"
            >
              <ArtSvgIcon icon="ri:folder-open-line" />
              <span class="mall-file-picker__folder-name">全部</span>
              <span v-if="allCount != null" class="mall-file-picker__folder-count">{{ allCount }}</span>
            </li>
            <li
              v-for="folder in folders"
              :key="folder.folderId"
              :class="{ 'is-active': selectedFolderId === folder.folderId }"
              class="mall-file-picker__folder"
              @click="selectFolder(folder.folderId)"
            >
              <ArtSvgIcon icon="ri:folder-3-line" />
              <span class="mall-file-picker__folder-name" :title="folder.folderName">{{
                folder.folderName
              }}</span>
              <span v-if="folder.fileCount != null" class="mall-file-picker__folder-count">{{
                folder.fileCount
              }}</span>
            </li>
          </ul>
        </ElScrollbar>
      </aside>

      <!-- 右侧内容 -->
      <section class="mall-file-picker__main">
        <div class="mall-file-picker__toolbar">
          <ElInput
            v-model="searchForm.fileName"
            clearable
            placeholder="搜索素材名称"
            class="mall-file-picker__search"
            @keyup.enter="handleSearch"
          />
          <ElButton type="primary" :loading="uploading" @click="triggerUpload">
            上传素材
          </ElButton>
          <ElButton @click="handleSearch">查询</ElButton>
          <input
            ref="uploadInputRef"
            accept="image/jpeg,image/png,image/gif,image/webp"
            class="mall-file-picker__upload-input"
            type="file"
            @change="handleUploadChange"
          />
        </div>

        <div v-loading="loading" class="mall-file-picker__grid">
          <div
            v-for="file in files"
            :key="file.fileId ?? file.objectName"
            :class="{ 'is-selected': isSelected(file) }"
            class="mall-file-picker__item"
            @click="toggleSelect(file)"
          >
            <ElImage
              :src="file.displayUrl"
              fit="cover"
              class="mall-file-picker__thumb"
              loading="lazy"
            >
              <template #error>
                <div class="mall-file-picker__thumb-error">
                  <ArtSvgIcon icon="ri:image-line" />
                </div>
              </template>
            </ElImage>
            <p class="mall-file-picker__name" :title="file.fileName">{{ shortName(file.fileName) }}</p>
          </div>
          <ElEmpty v-if="!loading && files.length === 0" description="暂无图片，可点击「上传素材」添加" />
        </div>

        <div v-if="total > pageSize" class="mall-file-picker__pager">
          <ElPagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            background
            layout="prev, pager, next"
            @current-change="loadFiles"
          />
        </div>
      </section>
    </div>

    <template #footer>
      <div class="mall-file-picker__footer">
        <span class="mall-file-picker__selected">已选 {{ selected.length }} / {{ limit }} 项</span>
        <div>
          <ElButton @click="visible = false">取消</ElButton>
          <ElButton type="primary" :disabled="selected.length === 0" @click="confirmSelect">
            确认
          </ElButton>
        </div>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {fetchFileList} from '@/api/file/file'
import {fetchFolderTree} from '@/api/file/folder'
import type {SysFile, SysFileFolder} from '@/api/file/types'
import {uploadMallImage} from '@/api/mall/mall-image'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {ElMessage} from 'element-plus'
import {defaultResponseAdapter, extractTableData} from '@/utils/table/tableUtils'
import {isStorageObjectName, normalizeToObjectName} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'MallFilePickerDialog' })

  const props = withDefaults(
    defineProps<{
      modelValue: boolean
      /** 最多可选数量 */
      limit?: number
      title?: string
    }>(),
    {
      limit: 1,
      title: '选择商品图片'
    }
  )

  const emit = defineEmits<{
    'update:modelValue': [boolean]
    confirm: [string[]]
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
  })

  const loading = ref(false)
  const uploading = ref(false)
  const files = ref<SysFile[]>([])
  const selected = ref<SysFile[]>([])
  const folders = ref<SysFileFolder[]>([])
  const allCount = ref<number>()
  const selectedFolderId = ref<number>()
  const pageNum = ref(1)
  const pageSize = 24
  const total = ref(0)
  const uploadInputRef = ref<HTMLInputElement>()

  const searchForm = ref({ fileName: '' })

  const isSelected = (file: SysFile) =>
    selected.value.some((item) => sameFile(item, file))

  const sameFile = (a: SysFile, b: SysFile) => {
    if (a.fileId != null && b.fileId != null) return a.fileId === b.fileId
    return normalizeToObjectName(a.objectName) === normalizeToObjectName(b.objectName)
  }

  const shortName = (name?: string) => {
    if (!name) return '-'
    return name.length > 12 ? `${name.slice(0, 10)}...` : name
  }

  const toggleSelect = (file: SysFile) => {
    const objectName = file.objectName?.trim()
    if (!objectName || !isStorageObjectName(objectName)) return

    const idx = selected.value.findIndex((item) => sameFile(item, file))
    if (idx >= 0) {
      selected.value.splice(idx, 1)
      return
    }

    if (props.limit <= 1) {
      selected.value = [file]
      return
    }

    if (selected.value.length >= props.limit) {
      ElMessage.warning(`最多选择 ${props.limit} 张图片`)
      return
    }
    selected.value.push(file)
  }

  const selectFolder = (folderId?: number) => {
    selectedFolderId.value = folderId
    pageNum.value = 1
    loadFiles()
  }

  const loadFolders = async () => {
    try {
      const tree = await fetchFolderTree('GOODS')
      const goodsNode = tree?.find((n) => n.category === 'GOODS')
      folders.value = goodsNode?.children ?? []
    } catch (error) {
      folders.value = []
      console.warn('[MallFilePicker] 加载素材目录失败', error)
    }
  }

  const loadFiles = async () => {
    loading.value = true
    try {
      const query: Parameters<typeof fetchFileList>[0] = {
        pageNum: pageNum.value,
        pageSize,
        mediaType: 'IMAGE',
        folderId: selectedFolderId.value,
        fileName: searchForm.value.fileName || undefined
      }
      // 「全部」按业务分类筛；选中具体文件夹时仅按 folderId 查，兼容历史数据 category 不一致
      if (selectedFolderId.value == null) {
        query.category = 'GOODS'
      }
      const res = await fetchFileList(query)
      const adapted = defaultResponseAdapter<SysFile>(res)
      const list = extractTableData(adapted)
      files.value = list.filter((f) => !!f.objectName?.trim())
      total.value = adapted.total ?? 0
      if (selectedFolderId.value == null && pageNum.value === 1 && !searchForm.value.fileName) {
        allCount.value = total.value
      }
    } catch (error) {
      files.value = []
      total.value = 0
      const message = error instanceof Error ? error.message : '加载素材失败'
      ElMessage.error(message.includes('403') ? '无文件中心查询权限，请联系管理员分配 file:resource:query' : message)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    pageNum.value = 1
    loadFiles()
  }

  const triggerUpload = () => {
    uploadInputRef.value?.click()
  }

  const handleUploadChange = async (event: Event) => {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) return

    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(file.type)) {
      ElMessage.warning('仅支持 JPG、PNG、GIF、WEBP 格式')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.warning('单张图片不能超过 10MB')
      return
    }

    uploading.value = true
    try {
      const vo = await uploadMallImage(file)
      if (!vo?.objectName) throw new Error('上传响应无效')

      const uploaded: SysFile = {
        objectName: vo.objectName,
        displayUrl: vo.displayUrl,
        fileName: file.name
      }

      if (props.limit <= 1) {
        selected.value = [uploaded]
      } else if (selected.value.length < props.limit) {
        selected.value.push(uploaded)
      } else {
        ElMessage.warning(`最多选择 ${props.limit} 张，请先取消部分选中项`)
      }

      await loadFiles()
      ElMessage.success('上传成功')
    } catch (error) {
      const message = error instanceof Error ? error.message : '上传失败'
      ElMessage.error(message)
    } finally {
      uploading.value = false
    }
  }

  const confirmSelect = () => {
    const objectNames = selected.value
      .map((f) => normalizeToObjectName(f.objectName))
      .filter((name): name is string => !!name && isStorageObjectName(name))
    if (!objectNames.length) {
      ElMessage.warning('请选择有效图片')
      return
    }
    emit('confirm', objectNames)
    visible.value = false
  }

  const handleClosed = () => {
    selected.value = []
    searchForm.value.fileName = ''
    pageNum.value = 1
    selectedFolderId.value = undefined
  }

  watch(
    () => props.modelValue,
    async (open) => {
      if (open) {
        selected.value = []
        await loadFolders()
        await loadFiles()
      }
    }
  )
</script>

<style scoped lang="scss">
  .mall-file-picker {
    display: flex;
    gap: 16px;
    min-height: 420px;
  }

  .mall-file-picker__sidebar {
    flex-shrink: 0;
    width: 200px;
    padding-right: 12px;
    border-right: 1px solid var(--el-border-color-lighter);
  }

  .mall-file-picker__sidebar-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .mall-file-picker__folder-scroll {
    height: 380px;
  }

  .mall-file-picker__folders {
    padding: 0;
    margin: 0;
    list-style: none;
  }

  .mall-file-picker__folder {
    display: flex;
    gap: 6px;
    align-items: center;
    padding: 8px 10px;
    margin-bottom: 2px;
    font-size: 13px;
    cursor: pointer;
    border-radius: 6px;
    transition: background-color 0.15s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.is-active {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }

  .mall-file-picker__folder-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mall-file-picker__folder-count {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .mall-file-picker__main {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
  }

  .mall-file-picker__toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .mall-file-picker__search {
    flex: 1;
  }

  .mall-file-picker__upload-input {
    display: none;
  }

  .mall-file-picker__grid {
    display: grid;
    flex: 1;
    grid-template-columns: repeat(auto-fill, minmax(108px, 1fr));
    gap: 12px;
    min-height: 280px;
    max-height: 360px;
    padding: 4px;
    overflow-y: auto;
  }

  .mall-file-picker__item {
    cursor: pointer;
    border: 2px solid transparent;
    border-radius: 8px;
    transition: border-color 0.2s;

    &:hover {
      border-color: var(--el-color-primary-light-5);
    }

    &.is-selected {
      border-color: var(--el-color-primary);
    }
  }

  .mall-file-picker__thumb {
    width: 100%;
    height: 96px;
    border-radius: 6px;
    background: var(--el-fill-color-lighter);
  }

  .mall-file-picker__thumb-error {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    font-size: 28px;
    color: var(--el-text-color-secondary);
  }

  .mall-file-picker__name {
    margin: 6px 0 0;
    overflow: hidden;
    font-size: 12px;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mall-file-picker__pager {
    display: flex;
    justify-content: center;
    margin-top: 12px;
  }

  .mall-file-picker__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .mall-file-picker__selected {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
</style>

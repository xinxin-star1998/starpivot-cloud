<template>
  <div class="file-page art-full-height">
    <div class="file-layout">
      <ElCard v-if="activeTab === 'all'" shadow="never" class="file-sidebar">
        <FileFolderTree
          :categories="categoryTree"
          :active-folder-id="selectedFolderId"
          @select-folder="handleSelectFolder"
          @add-folder="openFolderDialog('add', undefined, $event)"
          @edit-folder="handleEditFolder"
          @delete-folder="handleDeleteFolder"
        />
      </ElCard>

      <div class="file-main">
        <FileSearch
          v-model="searchForm"
          :recycle="activeTab === 'recycle'"
          @search="handleSearch"
          @reset="resetSearch"
        />

        <ElCard class="art-table-card file-table-card" shadow="never">
          <div class="file-table-card__top">
            <div class="panel-toolbar">
              <ElTabs v-model="activeTab" class="panel-tabs" @tab-change="handleTabChange">
                <ElTabPane label="全部文件" name="all" />
                <ElTabPane label="回收站" name="recycle" />
              </ElTabs>

              <div v-if="activeTab === 'all' && locationText" class="location-bar">
                <ArtSvgIcon class="location-icon" icon="ri:folder-open-line" />
                <span class="location-text">{{ locationText }}</span>
              </div>
            </div>

            <div v-if="activeTab === 'all'" class="media-filter">
              <ElRadioGroup v-model="mediaTypeFilter" size="small" @change="handleSearch">
                <ElRadioButton v-for="item in MEDIA_TYPES" :key="item.code" :value="item.code">
                  <ArtSvgIcon
                    v-if="item.code"
                    :icon="getMediaTypeIcon(item.code)"
                    class="filter-icon"
                  />
                  {{ item.label }}
                </ElRadioButton>
              </ElRadioGroup>
            </div>

            <ArtTableHeader
              v-model:columns="columnChecks"
              :loading="loading"
              @refresh="refreshData"
            >
              <template #left>
                <ElSpace wrap>
                  <ElButton
                    v-if="activeTab === 'all'"
                    v-auth="'file:resource:add'"
                    v-ripple
                    type="primary"
                    @click="uploadVisible = true"
                  >
                    <ArtSvgIcon class="mr-1" icon="ri:upload-2-line" />
                    上传
                  </ElButton>
                  <ElButton
                    v-if="activeTab === 'all'"
                    v-auth="'file:resource:move'"
                    v-ripple
                    :disabled="selectedRows.length === 0"
                    @click="openMoveDialog(selectedRows.map((r) => r.fileId!))"
                  >
                    <ArtSvgIcon class="mr-1" icon="ri:folder-transfer-line" />
                    迁移
                  </ElButton>
                  <ElButton
                    v-if="activeTab === 'all'"
                    v-auth="'file:resource:delete'"
                    v-ripple
                    :disabled="selectedRows.length === 0"
                    type="danger"
                    @click="handleBatchDelete"
                  >
                    批量删除
                  </ElButton>
                  <ElButton
                    v-if="activeTab === 'recycle'"
                    v-auth="'file:resource:restore'"
                    v-ripple
                    :disabled="selectedRows.length === 0"
                    type="primary"
                    @click="handleBatchRestore"
                  >
                    批量恢复
                  </ElButton>
                  <ElButton
                    v-if="activeTab === 'recycle'"
                    v-auth="'file:resource:purge'"
                    v-ripple
                    :disabled="selectedRows.length === 0"
                    type="danger"
                    @click="handleBatchPurge"
                  >
                    永久删除
                  </ElButton>
                </ElSpace>
              </template>
            </ArtTableHeader>
          </div>

          <div class="file-table-card__body">
            <ElEmpty
              v-if="activeTab === 'all' && !selectedFolderId && !loading"
              :image-size="100"
              description="请在左侧选择文件夹"
            />

            <ArtTable
              v-else
              :columns="columns"
              :data="data"
              :loading="loading"
              :pagination="pagination"
              :pagination-options="{ align: 'right' }"
              :show-table-header="false"
              @selection-change="handleSelectionChange"
              @pagination:size-change="handleSizeChange"
              @pagination:current-change="handleCurrentChange"
            />
          </div>
        </ElCard>
      </div>
    </div>

    <FileUploadDialog
      v-model:visible="uploadVisible"
      :categories="categoryTree"
      :default-folder-id="selectedFolderId"
      @success="onUploadSuccess"
    />

    <FilePreviewDialog
      v-model:visible="previewVisible"
      :file="previewFile"
      @delete="handlePreviewDelete"
      @move="openMoveDialog"
      @renamed="refreshData"
    />

    <FileMoveDialog
      v-model:visible="moveVisible"
      :categories="categoryTree"
      :file-ids="moveFileIds"
      :exclude-folder-id="selectedFolderId"
      @success="onMoveSuccess"
    />

    <FolderDialog
      v-model:visible="folderDialogVisible"
      :type="folderDialogType"
      :data="folderDialogData"
      :default-category="folderDialogCategory"
      @success="loadFolderTree"
    />
  </div>
</template>

<script setup lang="ts">
import {
  deleteFiles,
  fetchFileList,
  fetchFilePreviewUrl,
  fetchRecycleList,
  purgeRecycleFiles,
  renameFile,
  restoreFiles
} from '@/api/file/file'
import {deleteFolder, fetchFolderTree} from '@/api/file/folder'
import type {FileCategoryNode, SysFile, SysFileFolderForm} from '@/api/file/types'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {useAuth} from '@/hooks/core/useAuth'
import {formatFileSize, openFileUrl, resolveFileDisplayUrl} from '@/utils/file/file-center'
import {handleMutationError} from '@/utils/http/mutation'
import {ElImage, ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {computed, h, onActivated, onMounted, ref} from 'vue'
import {getCategoryLabel, getMediaTypeIcon, MEDIA_TYPE_TAG, MEDIA_TYPES} from './constants'
import FileFolderTree from './modules/file-folder-tree.vue'
import FileMoveDialog from './modules/file-move-dialog.vue'
import FilePreviewDialog from './modules/file-preview-dialog.vue'
import FileSearch from './modules/file-search.vue'
import FileUploadDialog from './modules/file-upload-dialog.vue'
import FolderDialog from './modules/folder-dialog.vue'

defineOptions({ name: 'FileManage' })

  const { hasAuth } = useAuth()

  const activeTab = ref<'all' | 'recycle'>('all')
  /** 与 activeTab 同步，供 listApi 读取，避免 Tab 切换瞬间 API 选错 */
  const listMode = ref<'all' | 'recycle'>('all')
  const categoryTree = ref<FileCategoryNode[]>([])
  const selectedFolderId = ref<number>()
  const selectedCategory = ref('')
  const selectedFolderName = ref('')
  const mediaTypeFilter = ref('')
  const selectedRows = ref<SysFile[]>([])

  const uploadVisible = ref(false)
  const moveVisible = ref(false)
  const moveFileIds = ref<number[]>([])
  const pageInitialized = ref(false)
  const previewVisible = ref(false)
  const previewFile = ref<SysFile | null>(null)

  const folderDialogVisible = ref(false)
  const folderDialogType = ref<'add' | 'edit'>('add')
  const folderDialogData = ref<SysFileFolderForm>()
  const folderDialogCategory = ref('')

  const searchForm = ref<Record<string, unknown>>({
    fileName: undefined,
    createBy: undefined,
    deleteBy: undefined,
    category: undefined,
    timeRange: undefined
  })

  const isRecycle = computed(() => activeTab.value === 'recycle')

  const locationText = computed(() => {
    if (!selectedFolderId.value) return ''
    const catLabel = getCategoryLabel(selectedCategory.value)
    return `${catLabel} / ${selectedFolderName.value || '默认'}`
  })

  const listApi = (params: Record<string, unknown>) => {
    if (listMode.value === 'recycle') {
      return fetchRecycleList(params as never)
    }
    return fetchFileList(params as never)
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    clearData,
    cancelRequest,
    resetPagination,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    resetColumns
  } = useTable({
    core: {
      apiFn: listApi,
      apiParams: {
        pageNum: 1,
        pageSize: 20
      },
      columnsFactory: () => buildColumns(isRecycle.value),
      immediate: false
    }
  })

  function renderFileThumb(row: SysFile) {
    const url = resolveFileDisplayUrl(row)
    if (row.mediaType === 'IMAGE' && url) {
      return h(ElImage, {
        src: url,
        fit: 'cover',
        previewSrcList: [url],
        previewTeleported: true,
        lazy: true,
        class: 'file-thumb',
        onClick: (event: Event) => event.stopPropagation()
      })
    }
    return h(ArtSvgIcon, {
      icon: getMediaTypeIcon(row.mediaType),
      class: 'file-type-icon'
    })
  }

  function buildColumns(recycle: boolean) {
    const cols = [
      { type: 'selection' as const },
      { type: 'index' as const, width: 60, label: '序号' },
      {
        prop: 'fileName',
        label: '文件名',
        minWidth: 280,
        formatter: (row: SysFile) =>
          h(
            'div',
            {
              class: 'file-name-cell',
              onClick: () => !recycle && hasAuth('file:resource:query') && openPreview(row)
            },
            [
              renderFileThumb(row),
              h(
                'span',
                {
                  class: [
                    'file-name-text',
                    !recycle && hasAuth('file:resource:query') ? 'is-link' : ''
                  ]
                },
                row.fileName
              )
            ]
          )
      }
    ]

    if (recycle) {
      cols.push({
        prop: 'categoryLabel',
        label: '业务分类',
        width: 110,
        formatter: (row: SysFile) => row.categoryLabel || getCategoryLabel(row.category)
      } as never)
    }

    cols.push(
      {
        prop: 'mediaTypeLabel',
        label: '类型',
        width: 96,
        formatter: (row: SysFile) =>
          h(
            ElTag,
            { type: MEDIA_TYPE_TAG[row.mediaType || ''] || 'info', size: 'small' },
            () => row.mediaTypeLabel || row.mediaType
          )
      } as never,
      {
        prop: 'fileSize',
        label: '大小',
        width: 96,
        formatter: (row: SysFile) => formatFileSize(row.fileSize)
      } as never,
      {
        prop: 'refCount',
        label: '引用',
        width: 72,
        align: 'center' as const,
        formatter: (row: SysFile) => {
          const count = row.refCount ?? 0
          if (count > 0) {
            return h(ElTag, { type: 'warning', size: 'small' }, () => String(count))
          }
          return h('span', { class: 'text-g-500' }, '0')
        }
      } as never,
      {
        prop: recycle ? 'deleteBy' : 'createBy',
        label: recycle ? '删除人' : '上传人',
        width: 100
      } as never,
      {
        prop: recycle ? 'deleteTime' : 'createTime',
        label: recycle ? '删除时间' : '上传时间',
        width: 168
      } as never,
      {
        prop: 'operation',
        label: '操作',
        width: recycle ? 220 : 240,
        fixed: 'right' as const,
        formatter: (row: SysFile) => {
          const refCount = row.refCount ?? 0
          const purgeBlocked = refCount > 0
          return h('div', { class: 'file-op-cell' }, [
            !recycle &&
              hasAuth('file:resource:query') &&
              h(ArtButtonTable, { type: 'view', onClick: () => openPreview(row) }),
            !recycle &&
              hasAuth('file:resource:query') &&
              h(ArtButtonTable, {
                type: 'download',
                tooltip: '下载',
                onClick: () => handleDownload(row)
              }),
            !recycle &&
              hasAuth('file:resource:move') &&
              h(ArtButtonTable, {
                type: 'edit',
                tooltip: '迁移',
                onClick: () => openMoveDialog([row.fileId!])
              }),
            !recycle &&
              hasAuth('file:resource:edit') &&
              h(ArtButtonTable, {
                icon: 'ri:input-method-line',
                tooltip: '重命名',
                onClick: () => handleRename(row)
              }),
            !recycle &&
              hasAuth('file:resource:delete') &&
              h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete([row.fileId!]) }),
            recycle &&
              hasAuth('file:resource:restore') &&
              h(ArtButtonTable, { type: 'resume', tooltip: '恢复', onClick: () => handleRestore([row.fileId!]) }),
            recycle &&
              hasAuth('file:resource:purge') &&
              h(ArtButtonTable, {
                type: 'delete',
                tooltip: purgeBlocked
                  ? `仍有 ${refCount} 个业务引用，不可永久删除`
                  : '永久删除',
                iconClass: purgeBlocked ? 'bg-g-300/30 text-g-400 cursor-not-allowed' : undefined,
                onClick: () => {
                  if (purgeBlocked) {
                    ElMessage.warning(`文件仍被 ${refCount} 个业务引用，请先解绑`)
                    return
                  }
                  handlePurge([row.fileId!])
                }
              })
          ])
        }
      } as never
    )

    return cols
  }

  async function loadFolderTree() {
    const tree = await fetchFolderTree()
    categoryTree.value = tree || []
  }

  function applyDefaultFolderIfNeeded() {
    if (selectedFolderId.value || categoryTree.value.length === 0) return
    const first = categoryTree.value[0]
    const defaultFolder = first.children?.[0]
    if (!defaultFolder?.folderId) return
    selectedFolderId.value = defaultFolder.folderId
    selectedCategory.value = first.category
    selectedFolderName.value = defaultFolder.folderName || '默认'
  }

  async function initPage() {
    await loadFolderTree()
    applyDefaultFolderIfNeeded()
    if (selectedFolderId.value || isRecycle.value) {
      await handleSearch()
    }
  }

  function handleSelectFolder(payload: { folderId: number; category: string; folderName: string }) {
    selectedFolderId.value = payload.folderId
    selectedCategory.value = payload.category
    selectedFolderName.value = payload.folderName
    handleSearch()
  }

  function parseTimeRange(range: unknown) {
    if (!Array.isArray(range) || range.length !== 2) {
      return { beginTime: undefined, endTime: undefined }
    }
    return { beginTime: range[0], endTime: range[1] }
  }

  function clearTableSearchParams() {
    const paramsRecord = searchParams as Record<string, unknown>
    Object.keys(searchParams).forEach((key) => {
      delete paramsRecord[key]
    })
    Object.assign(searchParams, { pageNum: 1, pageSize: 20 })
    resetPagination()
  }

  async function handleSearch(tab: 'all' | 'recycle' = listMode.value) {
    const { beginTime, endTime } = parseTimeRange(searchForm.value.timeRange)
    const params: Record<string, unknown> = {
      pageNum: 1,
      fileName: searchForm.value.fileName,
      beginTime,
      endTime
    }
    if (tab === 'recycle') {
      params.category = searchForm.value.category
      params.deleteBy = searchForm.value.deleteBy
    } else {
      params.folderId = selectedFolderId.value
      params.category = selectedCategory.value
      params.mediaType = mediaTypeFilter.value || undefined
      params.createBy = searchForm.value.createBy
    }
    Object.assign(searchParams, params)
    await getData()
  }

  async function resetSearch(tab: 'all' | 'recycle' = listMode.value) {
    searchForm.value = {
      fileName: undefined,
      createBy: undefined,
      deleteBy: undefined,
      category: undefined,
      timeRange: undefined
    }
    mediaTypeFilter.value = ''
    clearTableSearchParams()
    await handleSearch(tab)
  }

  async function handleTabChange(tab: 'all' | 'recycle') {
    listMode.value = tab
    selectedRows.value = []
    resetColumns?.()
    cancelRequest()
    clearData()
    resetPagination()
    await resetSearch(tab)
  }

  function handleSelectionChange(rows: SysFile[]) {
    selectedRows.value = rows
  }

  function openMoveDialog(ids: number[]) {
    if (!ids.length) return
    moveFileIds.value = ids
    moveVisible.value = true
  }

  function onMoveSuccess(targetFolderId: number) {
    selectedRows.value = []
    previewVisible.value = false
    if (targetFolderId === selectedFolderId.value) {
      refreshData()
    } else {
      for (const cat of categoryTree.value) {
        const folder = cat.children?.find((f) => f.folderId === targetFolderId)
        if (folder) {
          handleSelectFolder({
            folderId: targetFolderId,
            category: cat.category,
            folderName: folder.folderName || ''
          })
          break
        }
      }
    }
    loadFolderTree()
  }

  function openPreview(file: SysFile) {
    previewFile.value = file
    previewVisible.value = true
  }

  async function handlePreviewDelete(fileId: number) {
    await handleDelete([fileId])
  }

  async function handleDownload(file: SysFile) {
    try {
      const res = await fetchFilePreviewUrl(file.fileId!)
      openFileUrl(res.url, file.fileName)
    } catch {
      if (file.displayUrl) {
        openFileUrl(file.displayUrl, file.fileName)
      }
    }
  }

  async function handleRename(file: SysFile) {
    if (!file.fileId) return
    try {
      const { value } = await ElMessageBox.prompt('请输入新的文件名', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: file.fileName || '',
        inputPlaceholder: '例如：avatar1.webp',
        inputValidator: (val) => {
          const name = val?.trim()
          if (!name) return '文件名不能为空'
          if (name.includes('/') || name.includes('\\')) return '文件名不能包含路径分隔符'
          return true
        }
      })
      const fileName = value.trim()
      await renameFile({ fileId: file.fileId, fileName })
      ElMessage.success('重命名成功')
      refreshData()
    } catch (error) {
      handleMutationError(error, '重命名失败')
    }
  }

  function openFolderDialog(type: 'add' | 'edit', data?: SysFileFolderForm, category?: string) {
    folderDialogType.value = type
    folderDialogData.value = data
    folderDialogCategory.value = category || selectedCategory.value
    folderDialogVisible.value = true
  }

  function handleEditFolder(payload: { folderId: number; category: string; folderName: string }) {
    openFolderDialog('edit', {
      folderId: payload.folderId,
      category: payload.category,
      folderName: payload.folderName
    })
  }

  async function handleDeleteFolder(folderId: number) {
    try {
      await ElMessageBox.confirm('删除后文件夹不可恢复，且须为空文件夹。确认删除？', '提示', {
        type: 'warning'
      })
      await deleteFolder(folderId)
      ElMessage.success('文件夹已删除')
      if (selectedFolderId.value === folderId) {
        selectedFolderId.value = undefined
        selectedFolderName.value = ''
      }
      await loadFolderTree()
    } catch (error) {
      handleMutationError(error, '删除文件夹失败')
    }
  }

  async function handleDelete(ids: number[]) {
    try {
      await ElMessageBox.confirm('确认将选中文件移入回收站？', '提示', { type: 'warning' })
      await deleteFiles(ids)
      ElMessage.success('已移入回收站')
      selectedRows.value = []
      refreshData()
      loadFolderTree()
    } catch (error) {
      handleMutationError(error, '删除失败')
    }
  }

  async function handleBatchDelete() {
    await handleDelete(selectedRows.value.map((r) => r.fileId!))
  }

  async function handleRestore(ids: number[]) {
    try {
      await ElMessageBox.confirm('确认恢复选中文件？', '提示', { type: 'info' })
      await restoreFiles(ids)
      ElMessage.success('恢复成功')
      selectedRows.value = []
      refreshData()
    } catch (error) {
      handleMutationError(error, '恢复失败')
    }
  }

  async function handleBatchRestore() {
    await handleRestore(selectedRows.value.map((r) => r.fileId!))
  }

  async function handlePurge(ids: number[]) {
    try {
      await ElMessageBox.confirm(
        '永久删除后文件及存储对象不可恢复，确认继续？',
        '永久删除',
        { type: 'warning', confirmButtonText: '永久删除', confirmButtonClass: 'el-button--danger' }
      )
      await purgeRecycleFiles(ids)
      ElMessage.success('已永久删除')
      selectedRows.value = []
      refreshData()
    } catch (error) {
      handleMutationError(error, '永久删除失败')
    }
  }

  async function handleBatchPurge() {
    const blocked = selectedRows.value.filter((row) => (row.refCount ?? 0) > 0)
    if (blocked.length > 0) {
      ElMessage.warning(`选中有 ${blocked.length} 个文件仍被业务引用，无法永久删除`)
      return
    }
    await handlePurge(selectedRows.value.map((r) => r.fileId!))
  }

  function onUploadSuccess(folderId: number) {
    if (folderId && folderId !== selectedFolderId.value) {
      for (const cat of categoryTree.value) {
        const folder = cat.children?.find((f) => f.folderId === folderId)
        if (folder) {
          handleSelectFolder({
            folderId,
            category: cat.category,
            folderName: folder.folderName || ''
          })
          break
        }
      }
    } else {
      refreshData()
    }
    loadFolderTree()
  }

  onMounted(async () => {
    await initPage()
    pageInitialized.value = true
  })

  onActivated(() => {
    if (!pageInitialized.value) return
    if (selectedFolderId.value && activeTab.value === 'all') {
      handleSearch()
    }
  })
</script>

<style scoped lang="scss">
  .file-page {
    min-height: 0;
  }

  .file-layout {
    display: flex;
    gap: 12px;
    height: 100%;
    min-height: 0;
  }

  .file-sidebar {
    width: 272px;
    flex-shrink: 0;

    :deep(.el-card__body) {
      height: calc(100vh - 148px);
      min-height: 420px;
      padding: 14px 12px;
    }
  }

  .file-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 0;
  }

  .file-table-card {
    flex: 1;
    min-height: 0;

    :deep(.el-card__body) {
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
  }

  .file-table-card__top {
    flex-shrink: 0;
  }

  .file-table-card__body {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  .panel-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px 16px;
    margin-bottom: 4px;
  }

  .panel-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 0;
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }
  }

  .location-bar {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: 6px;
    background: var(--el-fill-color-light);
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  .location-icon {
    color: var(--el-color-primary);
    font-size: 16px;
  }

  .location-text {
    max-width: 360px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .media-filter {
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .filter-icon {
    margin-right: 2px;
    vertical-align: -2px;
    font-size: 13px;
  }

  :deep(.file-name-cell) {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    max-width: 100%;
    cursor: default;
  }

  :deep(.file-thumb) {
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
    cursor: zoom-in;
  }

  :deep(.file-thumb .el-image__inner) {
    border-radius: 4px;
  }

  :deep(.file-type-icon) {
    flex-shrink: 0;
    font-size: 18px;
    color: var(--el-color-primary);
  }

  :deep(.file-name-text) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.is-link {
      cursor: pointer;
      color: var(--el-color-primary);

      &:hover {
        text-decoration: underline;
      }
    }
  }

  :deep(.file-op-cell) {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
  }
</style>

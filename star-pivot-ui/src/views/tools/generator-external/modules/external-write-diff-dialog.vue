<template>
  <ElDialog
    v-model="dialogVisible"
    title="写盘 Diff 预览"
    width="90%"
    align-center
    class="write-diff-dialog"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div v-loading="loading" class="diff-wrap">
      <div v-if="diffResult" class="diff-summary">
        <ElTag type="success" effect="plain">新增 {{ diffResult.newCount }}</ElTag>
        <ElTag type="warning" effect="plain">修改 {{ diffResult.modifiedCount }}</ElTag>
        <ElTag type="info" effect="plain">无变化 {{ diffResult.unchangedCount }}</ElTag>
        <span class="diff-root" :title="diffResult.outputRoot"
          >根目录：{{ diffResult.outputRoot }}</span
        >
      </div>

      <div v-if="diffResult?.files?.length" class="diff-layout">
        <aside class="file-panel">
          <div class="file-panel__toolbar">
            <ElRadioGroup v-model="statusFilter" size="small">
              <ElRadioButton value="ALL">全部</ElRadioButton>
              <ElRadioButton value="NEW">新增</ElRadioButton>
              <ElRadioButton value="MODIFIED">修改</ElRadioButton>
              <ElRadioButton value="UNCHANGED">无变化</ElRadioButton>
            </ElRadioGroup>
            <div v-if="writableFiles.length > 0" class="select-actions">
              <ExternalActionBtn
                what="勾选全部变更"
                usage="选中所有「新增」和「修改」状态的文件，用于批量写盘。"
                link
                type="primary"
                size="small"
                @click="selectAllWritable"
              >
                全选变更
              </ExternalActionBtn>
              <ExternalActionBtn
                what="取消勾选"
                usage="清空已选文件列表，写盘按钮将不可用直至重新勾选。"
                link
                size="small"
                @click="clearWritableSelection"
              >
                清空
              </ExternalActionBtn>
              <span class="select-count"
                >已选 {{ selectedPaths.size }} / {{ writableFiles.length }}</span
              >
            </div>
          </div>
          <ElScrollbar class="file-panel__scroll">
            <template v-if="filteredFiles.length > 0">
              <div
                v-for="file in filteredFiles"
                :key="file.path"
                :class="['file-item', { 'is-active': activePath === file.path }]"
                @click="selectFile(file)"
              >
                <ElCheckbox
                  v-if="isWritable(file)"
                  :model-value="selectedPaths.has(file.path)"
                  @click.stop
                  @change="(val: boolean) => togglePath(file.path, val)"
                />
                <span v-else class="file-item__spacer" />
                <ElTag :type="statusTagType(file.status)" size="small" effect="plain">
                  {{ statusLabel(file.status) }}
                </ElTag>
                <span class="file-item__path" :title="file.path">{{ file.path }}</span>
              </div>
            </template>
            <ElEmpty v-else description="当前筛选下暂无文件" class="file-panel__empty" />
          </ElScrollbar>
        </aside>

        <section v-loading="detailLoading" class="code-panel">
          <div class="code-panel__header" :title="activePath">{{ activePath || '请选择文件' }}</div>
          <div v-if="activeFile && activeInFiltered" class="code-panel__body">
            <div v-if="activeFile.status === 'MODIFIED'" class="diff-unified">
              <LineDiffView
                :old-text="activeDetail?.oldContent"
                :new-text="activeDetail?.newContent"
              />
            </div>
            <div v-else-if="activeFile.status === 'NEW'" class="diff-unified">
              <LineDiffView :new-text="activeDetail?.newContent" mode="new" />
            </div>
            <ElEmpty
              v-else-if="activeFile.status === 'UNCHANGED'"
              description="文件内容与磁盘一致，无需写入"
              class="empty-tip"
            />
          </div>
          <ElEmpty
            v-else-if="filteredFiles.length === 0"
            description="请切换「全部」或「新增」等标签查看文件"
            class="empty-tip"
          />
          <ElEmpty v-else description="请选择左侧文件" class="empty-tip" />
        </section>
      </div>
      <ElEmpty v-else-if="!loading" description="暂无待写入文件" class="empty-tip" />
    </div>

    <template #footer>
      <ExternalActionBtn
        what="关闭预览"
        usage="关闭对话框，不写入任何文件。已选勾选状态不会保留到下次打开。"
        @click="dialogVisible = false"
      >
        关闭
      </ExternalActionBtn>
      <ExternalActionBtn
        what="写入选中文件"
        usage="仅将左侧勾选的文件写入写盘根目录，不会写入未勾选或无变化的文件。"
        type="warning"
        v-auth="'tool:external:create'"
        :loading="writing"
        :disabled="selectedPaths.size === 0"
        @click="emitConfirmWrite"
      >
        写入选中 ({{ selectedPaths.size }})
      </ExternalActionBtn>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    ElCheckbox,
    ElDialog,
    ElEmpty,
    ElRadioButton,
    ElRadioGroup,
    ElScrollbar,
    ElTag
  } from 'element-plus'
  import {
    type ExternalGenScope,
    type ExternalWriteDiffItem,
    type ExternalWriteDiffResult,
    type ExternalWriteDiffStatus,
    fetchExternalWriteDiff,
    fetchExternalWriteDiffFile
  } from '@/api/generator/gen-external'
  import LineDiffView from './line-diff-view.vue'
  import ExternalActionBtn from './external-action-btn.vue'

  interface Props {
    visible: boolean
    sessionId?: string
    tableNames?: string[]
    outputRoot?: string
    genScope?: ExternalGenScope
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{
    'update:visible': [boolean]
    confirmWrite: [string[]]
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const loading = ref(false)
  const detailLoading = ref(false)
  const writing = ref(false)
  const diffResult = ref<ExternalWriteDiffResult | null>(null)
  const statusFilter = ref<'ALL' | ExternalWriteDiffStatus>('ALL')
  const activePath = ref('')
  const selectedPaths = ref<Set<string>>(new Set())
  const detailCache = ref<Record<string, ExternalWriteDiffItem>>({})

  const filteredFiles = computed(() => {
    const files = diffResult.value?.files ?? []
    if (statusFilter.value === 'ALL') return files
    return files.filter((f) => f.status === statusFilter.value)
  })

  const writableFiles = computed(() =>
    (diffResult.value?.files ?? []).filter((f) => f.status === 'NEW' || f.status === 'MODIFIED')
  )

  const activeFile = computed(() =>
    (diffResult.value?.files ?? []).find((f) => f.path === activePath.value)
  )

  const activeDetail = computed(() => {
    if (!activePath.value) return null
    return detailCache.value[activePath.value] ?? activeFile.value ?? null
  })

  const activeInFiltered = computed(() =>
    filteredFiles.value.some((f) => f.path === activePath.value)
  )

  function isWritable(file: ExternalWriteDiffItem) {
    return file.status === 'NEW' || file.status === 'MODIFIED'
  }

  function statusLabel(status: ExternalWriteDiffStatus) {
    if (status === 'NEW') return '新增'
    if (status === 'MODIFIED') return '修改'
    return '相同'
  }

  function statusTagType(status: ExternalWriteDiffStatus) {
    if (status === 'NEW') return 'success'
    if (status === 'MODIFIED') return 'warning'
    return 'info'
  }

  function togglePath(path: string, checked: boolean) {
    const next = new Set(selectedPaths.value)
    if (checked) next.add(path)
    else next.delete(path)
    selectedPaths.value = next
  }

  function selectAllWritable() {
    selectedPaths.value = new Set(writableFiles.value.map((f) => f.path))
  }

  function clearWritableSelection() {
    selectedPaths.value = new Set()
  }

  async function loadFileDetail(path: string) {
    if (!props.sessionId || !props.tableNames?.length || detailCache.value[path]) return
    if (detailLoading.value) return
    detailLoading.value = true
    try {
      detailCache.value[path] = await fetchExternalWriteDiffFile(
        props.sessionId,
        props.tableNames,
        path,
        props.outputRoot?.trim() || undefined,
        props.genScope
      )
    } finally {
      detailLoading.value = false
    }
  }

  async function selectFile(file: ExternalWriteDiffItem) {
    activePath.value = file.path
    if (file.status !== 'UNCHANGED') {
      await loadFileDetail(file.path)
    }
  }

  async function loadDiff() {
    if (!props.sessionId || !props.tableNames?.length) return
    loading.value = true
    try {
      diffResult.value = await fetchExternalWriteDiff(
        props.sessionId,
        props.tableNames,
        props.outputRoot?.trim() || undefined,
        props.genScope,
        false
      )
      detailCache.value = {}
      const writable = diffResult.value.files.filter(
        (f) => f.status === 'NEW' || f.status === 'MODIFIED'
      )
      selectedPaths.value = new Set(writable.map((f) => f.path))
      const firstChanged = diffResult.value.files.find((f) => f.status !== 'UNCHANGED')
      if (firstChanged) {
        await selectFile(firstChanged)
      } else {
        activePath.value = diffResult.value.files[0]?.path || ''
      }
    } catch {
      diffResult.value = null
      activePath.value = ''
      selectedPaths.value = new Set()
    } finally {
      loading.value = false
    }
  }

  function handleClosed() {
    diffResult.value = null
    activePath.value = ''
    statusFilter.value = 'ALL'
    selectedPaths.value = new Set()
    detailCache.value = {}
  }

  function emitConfirmWrite() {
    emit('confirmWrite', Array.from(selectedPaths.value))
  }

  watch(statusFilter, () => {
    if (filteredFiles.value.length === 0) {
      activePath.value = ''
      return
    }
    if (!filteredFiles.value.some((f) => f.path === activePath.value)) {
      void selectFile(filteredFiles.value[0])
    }
  })

  watch(
    () =>
      [props.visible, props.sessionId, props.tableNames, props.outputRoot, props.genScope] as const,
    ([vis, sid, names]) => {
      if (vis && sid && names?.length) loadDiff()
    }
  )

  defineExpose({ reload: loadDiff, setWriting: (v: boolean) => (writing.value = v) })
</script>

<style scoped lang="scss">
  .diff-wrap {
    height: 72vh;
    min-height: 460px;
  }

  .diff-summary {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-bottom: 12px;
  }

  .diff-root {
    overflow: hidden;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .diff-layout {
    display: flex;
    height: calc(100% - 40px);
    overflow: hidden;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .file-panel {
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    width: 400px;
    background: var(--el-fill-color-lighter);
    border-right: 1px solid var(--el-border-color-lighter);

    &__toolbar {
      padding: 10px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    &__scroll {
      flex: 1;
      min-height: 0;
    }

    &__empty {
      padding: 24px 12px;
    }
  }

  .select-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: flex-start;
    margin-top: 8px;
  }

  .select-count {
    margin-left: auto;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .file-item {
    display: flex;
    gap: 8px;
    align-items: flex-start;
    padding: 8px 10px;
    cursor: pointer;
    border-bottom: 1px solid var(--el-border-color-extra-light);

    &.is-active {
      background: var(--el-color-primary-light-9);
    }

    &__spacer {
      flex-shrink: 0;
      width: 14px;
    }

    &__path {
      flex: 1;
      font-family: Menlo, Monaco, Consolas, monospace;
      font-size: 12px;
      line-height: 1.4;
      word-break: break-all;
    }
  }

  .code-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
    background: #1e1e1e;

    &__header {
      flex-shrink: 0;
      padding: 8px 12px;
      overflow: hidden;
      font-family: Menlo, Monaco, Consolas, monospace;
      font-size: 12px;
      color: #9cdcfe;
      text-overflow: ellipsis;
      white-space: nowrap;
      background: #252526;
      border-bottom: 1px solid #3c3c3c;
    }

    &__body {
      flex: 1;
      min-height: 0;
    }
  }

  .diff-unified {
    height: 100%;
  }

  .empty-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 360px;
  }
</style>

<style lang="scss">
  .write-diff-dialog .el-dialog__footer {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    justify-content: flex-end;
  }
</style>

<template>
  <div class="ai-knowledge-page art-full-height">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ t('ai.knowledge.title') }}</h2>
        <p class="page-desc">{{ t('ai.knowledge.pageDesc') }}</p>
      </div>
      <div class="page-actions">
        <ElButton :loading="loading" @click="refreshData">{{ t('ai.common.refresh') }}</ElButton>
        <ElButton v-auth="'ai:knowledge:edit'" type="primary" @click="openKbEdit()">
          {{ t('ai.knowledge.addKb') }}
        </ElButton>
      </div>
    </div>

    <div class="tip-strip">{{ t('ai.knowledge.tipStrip') }}</div>

    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm" class="search-form">
        <ElFormItem :label="t('ai.knowledge.name')">
          <ElInput
            v-model="searchForm.kbName"
            clearable
            class="!w-52"
            :placeholder="t('ai.knowledge.namePlaceholder')"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">{{ t('ai.common.search') }}</ElButton>
          <ElButton @click="resetSearch">{{ t('common.reset') }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div v-loading="loading" class="kb-grid">
      <div v-if="!loading && !data.length" class="kb-empty">{{ t('ai.knowledge.emptyHint') }}</div>
      <article v-for="row in data" :key="row.kbId" class="kb-card">
        <div class="kb-card__head">
          <div class="kb-card__identity">
            <span class="kb-avatar">
              <ArtSvgIcon icon="ri:book-open-line" />
            </span>
            <div class="min-w-0">
              <h3 class="kb-card__title">{{ row.kbName }}</h3>
              <p class="kb-card__desc">{{
                row.description ||
                t('ai.knowledge.docStats', {
                  docs: row.docCount ?? 0,
                  chunks: row.chunkCount ?? 0
                })
              }}</p>
            </div>
          </div>
          <ElTag :type="row.status === '0' ? 'success' : 'info'" size="small" effect="plain">
            {{ row.status === '0' ? t('common.normal') : t('common.disabled') }}
          </ElTag>
        </div>

        <div class="kb-card__stats">
          <span class="stat">{{
            t('ai.knowledge.indexedCount', { n: row.indexedCount ?? 0 })
          }}</span>
          <span class="stat">{{
            t('ai.knowledge.indexingCount', { n: row.indexingCount ?? 0 })
          }}</span>
          <span class="stat" :class="{ 'is-bad': (row.failedCount || 0) > 0 }">
            {{ t('ai.knowledge.failedCount', { n: row.failedCount ?? 0 }) }}
          </span>
        </div>

        <div class="kb-card__meta">
          <span>TopK {{ row.topK ?? '-' }}</span>
          <span>{{ t('ai.knowledge.chunkSizeShort') }} {{ row.chunkSize ?? '-' }}</span>
          <ElTag size="small" :type="healthTag(row).type" effect="plain">{{
            healthTag(row).label
          }}</ElTag>
        </div>

        <div class="kb-card__actions">
          <ElButton type="primary" plain size="small" @click="openDocs(row)">
            {{ t('ai.knowledge.openDocs') }}
          </ElButton>
          <ElButton
            v-auth="'ai:knowledge:edit'"
            size="small"
            :loading="reindexingKbId === row.kbId"
            :disabled="!(row.docCount && row.docCount > 0)"
            @click="reindexKb(row)"
          >
            {{ t('ai.knowledge.reindexAll') }}
          </ElButton>
          <ElButton v-auth="'ai:knowledge:edit'" text size="small" @click="openKbEdit(row)">
            {{ t('ai.common.edit') }}
          </ElButton>
          <ElButton
            v-auth="'ai:knowledge:delete'"
            text
            type="danger"
            size="small"
            @click="removeKb(row)"
          >
            {{ t('common.delete') }}
          </ElButton>
        </div>
      </article>
    </div>

    <div v-if="pagination.total > pagination.size" class="kb-pagination">
      <ElPagination
        background
        layout="total, prev, pager, next"
        :total="pagination.total"
        :page-size="pagination.size"
        :current-page="pagination.current"
        @current-change="handleCurrentChange"
      />
    </div>

    <ElDialog
      v-model="kbEditVisible"
      :title="kbForm.kbId ? t('ai.knowledge.editKb') : t('ai.knowledge.addKb')"
      destroy-on-close
      width="560px"
    >
      <ElForm ref="kbFormRef" :model="kbForm" :rules="kbRules" label-width="100px">
        <ElFormItem :label="t('ai.knowledge.name')" prop="kbName">
          <ElInput v-model="kbForm.kbName" />
        </ElFormItem>
        <ElFormItem :label="t('ai.knowledge.description')">
          <ElInput v-model="kbForm.description" type="textarea" :rows="2" />
        </ElFormItem>
        <ElFormItem :label="t('ai.knowledge.topK')">
          <ElInputNumber v-model="kbForm.topK" :min="1" :max="20" />
        </ElFormItem>
        <ElFormItem :label="t('ai.knowledge.chunkSize')">
          <ElInputNumber v-model="kbForm.chunkSize" :min="200" :max="4000" :step="100" />
        </ElFormItem>
        <ElFormItem :label="t('ai.knowledge.chunkOverlap')">
          <ElInputNumber v-model="kbForm.chunkOverlap" :min="0" :max="500" :step="20" />
        </ElFormItem>
        <ElFormItem :label="t('common.status')">
          <ElRadioGroup v-model="kbForm.status">
            <ElRadio value="0">{{ t('common.normal') }}</ElRadio>
            <ElRadio value="1">{{ t('common.disabled') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="kbEditVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="kbSaving" @click="saveKb">{{
          t('ai.common.save')
        }}</ElButton>
      </template>
    </ElDialog>

    <ElDrawer
      v-model="docDrawerVisible"
      :title="t('ai.knowledge.docManage', { name: activeKb?.kbName || '' })"
      size="820px"
      @closed="stopDocPoll"
    >
      <div v-if="activeKb" class="drawer-summary">
        <div>
          <strong>{{
            t('ai.knowledge.docStats', {
              docs: docList.length,
              chunks: docList.reduce((sum, item) => sum + (item.chunkCount || 0), 0)
            })
          }}</strong>
          <p v-if="docBusyCount" class="drawer-summary__hint">
            {{ t('ai.knowledge.indexingCount', { n: docBusyCount }) }}
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <ElUpload
            v-auth="'ai:knowledge:edit'"
            :show-file-list="false"
            :auto-upload="false"
            accept=".pdf,.docx,.md,.markdown,.txt"
            @change="handleFileSelect"
          >
            <ElButton type="primary" :loading="fileUploading">{{
              t('ai.knowledge.uploadDoc')
            }}</ElButton>
          </ElUpload>
          <ElButton v-auth="'ai:knowledge:edit'" @click="openDocEdit()">
            {{ t('ai.knowledge.pasteText') }}
          </ElButton>
          <ElButton
            v-auth="'ai:knowledge:edit'"
            :loading="reindexingKbId === activeKb.kbId"
            :disabled="!docList.length"
            @click="reindexKb(activeKb)"
          >
            {{ t('ai.knowledge.reindexAll') }}
          </ElButton>
        </div>
      </div>

      <ElTable v-loading="docLoading" :data="docList" class="doc-table">
        <ElTableColumn prop="title" :label="t('ai.knowledge.docTitle')" min-width="180" />
        <ElTableColumn prop="sourceType" :label="t('ai.knowledge.source')" width="80">
          <template #default="{ row }">
            {{
              row.sourceType === 'FILE'
                ? t('ai.knowledge.sourceFile')
                : t('ai.knowledge.sourceText')
            }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="indexStatus" :label="t('ai.knowledge.parseStatus')" width="110">
          <template #default="{ row }">
            <ElTag :type="indexStatusTag(row.indexStatus).type" size="small">
              {{ indexStatusTag(row.indexStatus).label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="chunkCount" :label="t('ai.knowledge.chunkCount')" width="80" />
        <ElTableColumn :label="t('ai.knowledge.errorDetail')" min-width="160">
          <template #default="{ row }">
            <span v-if="row.indexStatus === 'FAILED' && row.errorMsg" class="error-text">{{
              row.errorMsg
            }}</span>
            <span v-else class="text-g-400">—</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="updateTime" :label="t('ai.common.updateTime')" min-width="150" />
        <ElTableColumn :label="t('common.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <ElButton v-auth="'ai:knowledge:edit'" link type="primary" @click="openDocEdit(row)">
              {{ t('ai.common.edit') }}
            </ElButton>
            <ElButton v-auth="'ai:knowledge:edit'" link type="primary" @click="reindexDoc(row)">
              {{ t('ai.knowledge.reindex') }}
            </ElButton>
            <ElButton v-auth="'ai:knowledge:delete'" link type="danger" @click="removeDoc(row)">
              {{ t('common.delete') }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElDrawer>

    <ElDialog
      v-model="docEditVisible"
      :title="docForm.docId ? t('ai.knowledge.editDoc') : t('ai.knowledge.addDoc')"
      destroy-on-close
      width="720px"
    >
      <ElForm ref="docFormRef" :model="docForm" :rules="docRules" label-width="80px">
        <ElFormItem :label="t('ai.knowledge.docTitle')" prop="title">
          <ElInput v-model="docForm.title" />
        </ElFormItem>
        <ElFormItem :label="t('ai.knowledge.content')" prop="content">
          <ElInput
            v-model="docForm.content"
            :rows="14"
            :placeholder="t('ai.knowledge.contentPlaceholder')"
            type="textarea"
          />
        </ElFormItem>
        <ElFormItem :label="t('common.status')">
          <ElRadioGroup v-model="docForm.status">
            <ElRadio value="0">{{ t('common.normal') }}</ElRadio>
            <ElRadio value="1">{{ t('common.disabled') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="docEditVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="docSaving" @click="saveDoc">{{
          t('ai.common.save')
        }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script lang="ts" setup>
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import {
    type AiKnowledgeBaseItem,
    type AiKnowledgeBaseSavePayload,
    type AiKnowledgeDocumentItem,
    type AiKnowledgeDocumentSavePayload,
    fetchAiKnowledgeBaseList,
    fetchAiKnowledgeBaseReindex,
    fetchAiKnowledgeBaseRemove,
    fetchAiKnowledgeBaseSave,
    fetchAiKnowledgeDocumentDetail,
    fetchAiKnowledgeDocumentList,
    fetchAiKnowledgeDocumentReindex,
    fetchAiKnowledgeDocumentRemove,
    fetchAiKnowledgeDocumentSave,
    fetchAiKnowledgeDocumentUpload
  } from '@/api/ai/knowledge'
  import { handleMutationError } from '@/utils/http/mutation'

  defineOptions({ name: 'AiKnowledge' })

  const { t } = useI18n()

  const searchForm = ref({ kbName: '' })
  const kbEditVisible = ref(false)
  const kbSaving = ref(false)
  const reindexingKbId = ref<number | null>(null)
  const kbFormRef = ref<FormInstance>()
  const kbForm = ref<AiKnowledgeBaseSavePayload>({
    kbName: '',
    description: '',
    topK: 5,
    chunkSize: 600,
    chunkOverlap: 80,
    status: '0'
  })

  const kbRules = computed<FormRules>(() => ({
    kbName: [{ required: true, message: t('ai.knowledge.nameRequired'), trigger: 'blur' }]
  }))

  const docDrawerVisible = ref(false)
  const docLoading = ref(false)
  const docList = ref<AiKnowledgeDocumentItem[]>([])
  const activeKb = ref<AiKnowledgeBaseItem | null>(null)
  let docPollTimer: ReturnType<typeof setInterval> | null = null

  const docEditVisible = ref(false)
  const docSaving = ref(false)
  const fileUploading = ref(false)
  const docFormRef = ref<FormInstance>()
  const docForm = ref<AiKnowledgeDocumentSavePayload>({
    kbId: 0,
    title: '',
    content: '',
    status: '0'
  })

  const docRules = computed<FormRules>(() => ({
    title: [{ required: true, message: t('ai.knowledge.titleRequired'), trigger: 'blur' }],
    content: [{ required: true, message: t('ai.knowledge.contentRequired'), trigger: 'blur' }]
  }))

  const docBusyCount = computed(
    () =>
      docList.value.filter(
        (item) => item.indexStatus === 'PENDING' || item.indexStatus === 'PROCESSING'
      ).length
  )

  const { data, loading, pagination, searchParams, getData, handleCurrentChange, refreshData } =
    useTable({
      core: {
        apiFn: fetchAiKnowledgeBaseList,
        apiParams: { pageNum: 1, pageSize: 12, ...searchForm.value },
        columnsFactory: () => []
      }
    })

  function healthTag(row: AiKnowledgeBaseItem): {
    label: string
    type: 'success' | 'warning' | 'danger' | 'info'
  } {
    if ((row.failedCount || 0) > 0) {
      return { label: t('ai.knowledge.healthBad'), type: 'danger' }
    }
    if ((row.indexingCount || 0) > 0) {
      return { label: t('ai.knowledge.healthWarn'), type: 'warning' }
    }
    if ((row.docCount || 0) > 0) {
      return { label: t('ai.knowledge.healthGood'), type: 'success' }
    }
    return { label: t('ai.knowledge.emptyHint'), type: 'info' }
  }

  function handleSearch(): void {
    Object.assign(searchParams, searchForm.value)
    getData()
  }

  function resetSearch(): void {
    searchForm.value = { kbName: '' }
    handleSearch()
  }

  function openKbEdit(row?: AiKnowledgeBaseItem): void {
    kbForm.value = row?.kbId
      ? {
          kbId: row.kbId,
          kbName: row.kbName || '',
          description: row.description || '',
          topK: row.topK ?? 5,
          chunkSize: row.chunkSize ?? 600,
          chunkOverlap: row.chunkOverlap ?? 80,
          status: row.status || '0'
        }
      : {
          kbName: '',
          description: '',
          topK: 5,
          chunkSize: 600,
          chunkOverlap: 80,
          status: '0'
        }
    kbEditVisible.value = true
    nextTick(() => kbFormRef.value?.clearValidate())
  }

  async function saveKb(): Promise<void> {
    const valid = await kbFormRef.value?.validate().catch(() => false)
    if (!valid) return
    kbSaving.value = true
    try {
      await fetchAiKnowledgeBaseSave(kbForm.value)
      ElMessage.success(t('ai.common.saveSuccess'))
      kbEditVisible.value = false
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.saveFail'))
    } finally {
      kbSaving.value = false
    }
  }

  async function removeKb(row: AiKnowledgeBaseItem): Promise<void> {
    if (!row.kbId) return
    try {
      await ElMessageBox.confirm(
        t('ai.knowledge.deleteKbConfirm', { name: row.kbName }),
        t('common.tips'),
        { type: 'warning' }
      )
    } catch {
      return
    }
    try {
      await fetchAiKnowledgeBaseRemove(row.kbId)
      ElMessage.success(t('ai.common.deleteSuccess'))
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.deleteFail'))
    }
  }

  async function reindexKb(row: AiKnowledgeBaseItem): Promise<void> {
    if (!row.kbId) return
    try {
      await ElMessageBox.confirm(
        t('ai.knowledge.reindexAllConfirm', { name: row.kbName }),
        t('common.tips'),
        { type: 'warning', confirmButtonText: t('ai.knowledge.reindexAll') }
      )
    } catch {
      return
    }
    reindexingKbId.value = row.kbId
    try {
      const result = await fetchAiKnowledgeBaseReindex(row.kbId)
      const skipped =
        result?.skipped && result.skipped > 0
          ? t('ai.knowledge.reindexAllSkipped', { n: result.skipped })
          : ''
      ElMessage.success(
        t('ai.knowledge.reindexAllSuccess', {
          submitted: result?.submitted ?? 0,
          skipped
        })
      )
      await refreshData()
      if (docDrawerVisible.value && activeKb.value?.kbId === row.kbId) {
        await loadDocs()
        startDocPoll()
      }
    } catch (error) {
      handleMutationError(error, t('ai.knowledge.reindexAllFail'))
    } finally {
      reindexingKbId.value = null
    }
  }

  async function openDocs(row: AiKnowledgeBaseItem): Promise<void> {
    activeKb.value = row
    docDrawerVisible.value = true
    await loadDocs()
    startDocPoll()
  }

  async function loadDocs(): Promise<void> {
    if (!activeKb.value?.kbId) return
    docLoading.value = true
    try {
      const result = await fetchAiKnowledgeDocumentList({
        kbId: activeKb.value.kbId,
        pageNum: 1,
        pageSize: 100
      })
      docList.value = result?.rows || []
    } catch {
      docList.value = []
    } finally {
      docLoading.value = false
    }
  }

  function startDocPoll(): void {
    stopDocPoll()
    docPollTimer = setInterval(async () => {
      if (!docDrawerVisible.value || !docBusyCount.value) {
        if (!docBusyCount.value) {
          stopDocPoll()
          await refreshData()
        }
        return
      }
      await loadDocs()
    }, 3000)
  }

  function stopDocPoll(): void {
    if (docPollTimer) {
      clearInterval(docPollTimer)
      docPollTimer = null
    }
  }

  function openDocEdit(row?: AiKnowledgeDocumentItem): void {
    if (!activeKb.value?.kbId) return
    if (row?.docId) {
      fetchAiKnowledgeDocumentDetail(row.docId)
        .then((detail) => {
          docForm.value = {
            docId: detail.docId,
            kbId: activeKb.value!.kbId!,
            title: detail.title || '',
            content: detail.content || '',
            status: detail.status || '0'
          }
          docEditVisible.value = true
        })
        .catch((error) => handleMutationError(error, t('ai.knowledge.loadDocFail')))
    } else {
      docForm.value = {
        kbId: activeKb.value.kbId,
        title: '',
        content: '',
        status: '0'
      }
      docEditVisible.value = true
    }
    nextTick(() => docFormRef.value?.clearValidate())
  }

  async function saveDoc(): Promise<void> {
    const valid = await docFormRef.value?.validate().catch(() => false)
    if (!valid) return
    docSaving.value = true
    try {
      await fetchAiKnowledgeDocumentSave(docForm.value)
      ElMessage.success(t('ai.knowledge.saveDocSuccess'))
      docEditVisible.value = false
      await loadDocs()
      startDocPoll()
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.saveFail'))
    } finally {
      docSaving.value = false
    }
  }

  async function removeDoc(row: AiKnowledgeDocumentItem): Promise<void> {
    if (!row.docId) return
    try {
      await ElMessageBox.confirm(
        t('ai.knowledge.deleteDocConfirm', { name: row.title }),
        t('common.tips'),
        { type: 'warning' }
      )
    } catch {
      return
    }
    try {
      await fetchAiKnowledgeDocumentRemove(row.docId)
      ElMessage.success(t('ai.common.deleteSuccess'))
      await loadDocs()
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.deleteFail'))
    }
  }

  async function reindexDoc(row: AiKnowledgeDocumentItem): Promise<void> {
    if (!row.docId) return
    try {
      await fetchAiKnowledgeDocumentReindex(row.docId)
      ElMessage.success(t('ai.knowledge.reindexSuccess'))
      await loadDocs()
      startDocPoll()
    } catch (error) {
      handleMutationError(error, t('ai.knowledge.reindexFail'))
    }
  }

  function indexStatusTag(status?: string): {
    label: string
    type: 'success' | 'warning' | 'info' | 'danger'
  } {
    switch (status) {
      case 'DONE':
        return { label: t('ai.knowledge.indexed'), type: 'success' }
      case 'PROCESSING':
        return { label: t('ai.knowledge.indexing'), type: 'warning' }
      case 'PENDING':
        return { label: t('ai.knowledge.pending'), type: 'info' }
      case 'FAILED':
        return { label: t('ai.knowledge.failed'), type: 'danger' }
      default:
        return { label: status || t('ai.common.unknown'), type: 'info' }
    }
  }

  async function handleFileSelect(uploadFile: { raw?: File }): Promise<void> {
    const file = uploadFile.raw
    if (!file || !activeKb.value?.kbId) return
    fileUploading.value = true
    try {
      await fetchAiKnowledgeDocumentUpload(activeKb.value.kbId, file)
      ElMessage.success(t('ai.knowledge.uploadSuccess'))
      await loadDocs()
      startDocPoll()
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.knowledge.uploadFail'))
    } finally {
      fileUploading.value = false
    }
  }

  onUnmounted(() => stopDocPoll())
</script>

<style scoped lang="scss">
  .ai-knowledge-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .page-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--el-text-color-primary);
  }

  .page-desc {
    margin: 4px 0 0;
    max-width: 640px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  .page-actions,
  .search-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .tip-strip {
    padding: 10px 14px;
    font-size: 13px;
    line-height: 1.5;
    color: var(--el-color-primary);
    background: color-mix(in srgb, var(--el-color-primary) 8%, transparent);
    border: 1px solid color-mix(in srgb, var(--el-color-primary) 18%, transparent);
    border-radius: 10px;
  }

  .search-card {
    :deep(.el-card__body) {
      padding-bottom: 2px;
    }
  }

  .kb-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 14px;
    min-height: 120px;
  }

  .kb-empty {
    grid-column: 1 / -1;
    padding: 48px 16px;
    text-align: center;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }

  .kb-card {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 14px;
    background: var(--el-bg-color);
    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &:hover {
      border-color: color-mix(in srgb, var(--el-color-primary) 35%, var(--el-border-color));
      box-shadow: 0 8px 24px rgb(0 0 0 / 4%);
    }
  }

  .kb-card__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .kb-card__identity {
    display: flex;
    min-width: 0;
    gap: 12px;
  }

  .kb-avatar {
    display: inline-flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    font-size: 18px;
    color: var(--el-color-primary);
    background: color-mix(in srgb, var(--el-color-primary) 12%, transparent);
    border-radius: 12px;
  }

  .kb-card__title {
    margin: 0;
    overflow: hidden;
    font-size: 15px;
    font-weight: 600;
    line-height: 1.4;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .kb-card__desc {
    margin: 4px 0 0;
    overflow: hidden;
    font-size: 12px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .kb-card__stats,
  .kb-card__meta,
  .kb-card__actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .stat {
    padding: 4px 8px;
    font-size: 12px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border-radius: 999px;

    &.is-bad {
      color: var(--el-color-danger);
      background: color-mix(in srgb, var(--el-color-danger) 10%, transparent);
    }
  }

  .kb-card__meta {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .kb-pagination {
    display: flex;
    justify-content: flex-end;
  }

  .drawer-summary {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 16px;
  }

  .drawer-summary__hint {
    margin: 4px 0 0;
    font-size: 12px;
    color: var(--el-color-warning);
  }

  .doc-table {
    width: 100%;
  }

  .error-text {
    font-size: 12px;
    line-height: 1.4;
    color: var(--el-color-danger);
  }
</style>

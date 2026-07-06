<template>
  <div class="ai-knowledge-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="名称">
          <ElInput v-model="searchForm.kbName" clearable placeholder="知识库名称" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'ai:knowledge:edit'" type="primary" @click="openKbEdit()">新增知识库</ElButton>
        </template>
      </ArtTableHeader>
      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog v-model="kbEditVisible" :title="kbForm.kbId ? '编辑知识库' : '新增知识库'" width="560px" destroy-on-close>
      <ElForm ref="kbFormRef" :model="kbForm" :rules="kbRules" label-width="100px">
        <ElFormItem label="名称" prop="kbName">
          <ElInput v-model="kbForm.kbName" />
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="kbForm.description" type="textarea" :rows="2" />
        </ElFormItem>
        <ElFormItem label="检索条数">
          <ElInputNumber v-model="kbForm.topK" :min="1" :max="20" />
        </ElFormItem>
        <ElFormItem label="分块大小">
          <ElInputNumber v-model="kbForm.chunkSize" :min="200" :max="4000" :step="100" />
        </ElFormItem>
        <ElFormItem label="分块重叠">
          <ElInputNumber v-model="kbForm.chunkOverlap" :min="0" :max="500" :step="20" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="kbForm.status">
            <ElRadio value="0">正常</ElRadio>
            <ElRadio value="1">停用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="kbEditVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="kbSaving" @click="saveKb">保存</ElButton>
      </template>
    </ElDialog>

    <ElDrawer v-model="docDrawerVisible" size="760px" :title="`文档管理 - ${activeKb?.kbName || ''}`">
      <div class="mb-3 flex justify-end gap-2">
        <ElUpload
          v-auth="'ai:knowledge:edit'"
          :show-file-list="false"
          :auto-upload="false"
          accept=".pdf,.docx,.md,.markdown,.txt"
          @change="handleFileSelect"
        >
          <ElButton type="primary" :loading="fileUploading">上传文件</ElButton>
        </ElUpload>
        <ElButton v-auth="'ai:knowledge:edit'" @click="openDocEdit()">粘贴文本</ElButton>
      </div>
      <ElTable v-loading="docLoading" :data="docList" border>
        <ElTableColumn prop="title" label="标题" min-width="160" />
        <ElTableColumn prop="sourceType" label="来源" width="80">
          <template #default="{ row }">
            {{ row.sourceType === 'FILE' ? '文件' : '文本' }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="indexStatus" label="索引" width="100">
          <template #default="{ row }">
            <ElTag :type="indexStatusTag(row.indexStatus).type" size="small">
              {{ indexStatusTag(row.indexStatus).label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="chunkCount" label="分块数" width="80" />
        <ElTableColumn prop="updateTime" label="更新时间" min-width="160" />
        <ElTableColumn label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <ElButton v-auth="'ai:knowledge:edit'" link type="primary" @click="openDocEdit(row)">编辑</ElButton>
            <ElButton v-auth="'ai:knowledge:edit'" link type="primary" @click="reindexDoc(row)">重建索引</ElButton>
            <ElButton v-auth="'ai:knowledge:delete'" link type="danger" @click="removeDoc(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElDrawer>

    <ElDialog v-model="docEditVisible" :title="docForm.docId ? '编辑文档' : '新增文档'" width="720px" destroy-on-close>
      <ElForm ref="docFormRef" :model="docForm" :rules="docRules" label-width="80px">
        <ElFormItem label="标题" prop="title">
          <ElInput v-model="docForm.title" />
        </ElFormItem>
        <ElFormItem label="内容" prop="content">
          <ElInput v-model="docForm.content" type="textarea" :rows="14" placeholder="粘贴文档正文，保存后自动分块" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="docForm.status">
            <ElRadio value="0">正常</ElRadio>
            <ElRadio value="1">停用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="docEditVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="docSaving" @click="saveDoc">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchAiKnowledgeBaseList,
  fetchAiKnowledgeBaseRemove,
  fetchAiKnowledgeBaseSave,
  fetchAiKnowledgeDocumentDetail,
  fetchAiKnowledgeDocumentList,
  fetchAiKnowledgeDocumentReindex,
  fetchAiKnowledgeDocumentRemove,
  fetchAiKnowledgeDocumentSave,
  fetchAiKnowledgeDocumentUpload,
  type AiKnowledgeBaseItem,
  type AiKnowledgeDocumentItem,
  type AiKnowledgeBaseSavePayload,
  type AiKnowledgeDocumentSavePayload
} from '@/api/ai/knowledge'
import { handleMutationError } from '@/utils/http/mutation'

defineOptions({ name: 'AiKnowledge' })

const searchForm = ref({ kbName: '' })
const kbEditVisible = ref(false)
const kbSaving = ref(false)
const kbFormRef = ref<FormInstance>()
const kbForm = ref<AiKnowledgeBaseSavePayload>({
  kbName: '',
  description: '',
  topK: 5,
  chunkSize: 600,
  chunkOverlap: 80,
  status: '0'
})

const kbRules: FormRules = {
  kbName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const docDrawerVisible = ref(false)
const docLoading = ref(false)
const docList = ref<AiKnowledgeDocumentItem[]>([])
const activeKb = ref<AiKnowledgeBaseItem | null>(null)

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

const docRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const {
  columns,
  columnChecks,
  data,
  loading,
  pagination,
  searchParams,
  getData,
  handleSizeChange,
  handleCurrentChange,
  refreshData
} = useTable({
  core: {
    apiFn: fetchAiKnowledgeBaseList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'kbName', label: '名称', minWidth: 140 },
      { prop: 'topK', label: 'TopK', width: 70 },
      { prop: 'chunkSize', label: '分块', width: 80 },
      {
        prop: 'status',
        label: '状态',
        width: 80,
        formatter: (row: AiKnowledgeBaseItem) =>
          h(ElTag, { type: row.status === '0' ? 'success' : 'info', size: 'small' }, () =>
            row.status === '0' ? '正常' : '停用'
          )
      },
      { prop: 'updateTime', label: '更新时间', minWidth: 160 },
      {
        prop: 'actions',
        label: '操作',
        width: 220,
        fixed: 'right',
        formatter: (row: AiKnowledgeBaseItem) =>
          h('div', { class: 'flex gap-2' }, [
            h('a', { class: 'text-primary cursor-pointer', onClick: () => openDocs(row) }, '文档'),
            h('a', { class: 'text-primary cursor-pointer', onClick: () => openKbEdit(row) }, '编辑'),
            h('a', { class: 'text-danger cursor-pointer', onClick: () => removeKb(row) }, '删除')
          ])
      }
    ]
  }
})

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
    ElMessage.success('保存成功')
    kbEditVisible.value = false
    await refreshData()
  } catch (error) {
    handleMutationError(error, '保存失败')
  } finally {
    kbSaving.value = false
  }
}

async function removeKb(row: AiKnowledgeBaseItem): Promise<void> {
  if (!row.kbId) return
  try {
    await ElMessageBox.confirm(`确定删除知识库「${row.kbName}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await fetchAiKnowledgeBaseRemove(row.kbId)
    ElMessage.success('删除成功')
    await refreshData()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}

async function openDocs(row: AiKnowledgeBaseItem): Promise<void> {
  activeKb.value = row
  docDrawerVisible.value = true
  await loadDocs()
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
      .catch((error) => handleMutationError(error, '加载文档失败'))
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
    ElMessage.success('保存成功，已自动分块')
    docEditVisible.value = false
    await loadDocs()
  } catch (error) {
    handleMutationError(error, '保存失败')
  } finally {
    docSaving.value = false
  }
}

async function removeDoc(row: AiKnowledgeDocumentItem): Promise<void> {
  if (!row.docId) return
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await fetchAiKnowledgeDocumentRemove(row.docId)
    ElMessage.success('删除成功')
    await loadDocs()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}

async function reindexDoc(row: AiKnowledgeDocumentItem): Promise<void> {
  if (!row.docId) return
  try {
    await fetchAiKnowledgeDocumentReindex(row.docId)
    ElMessage.success('已提交重建索引')
    await loadDocs()
  } catch (error) {
    handleMutationError(error, '重建失败')
  }
}

function indexStatusTag(status?: string): { label: string; type: 'success' | 'warning' | 'info' | 'danger' } {
  switch (status) {
    case 'DONE':
      return { label: '已完成', type: 'success' }
    case 'PROCESSING':
      return { label: '索引中', type: 'warning' }
    case 'PENDING':
      return { label: '待索引', type: 'info' }
    case 'FAILED':
      return { label: '失败', type: 'danger' }
    default:
      return { label: status || '未知', type: 'info' }
  }
}

async function handleFileSelect(uploadFile: { raw?: File }): Promise<void> {
  const file = uploadFile.raw
  if (!file || !activeKb.value?.kbId) return
  fileUploading.value = true
  try {
    await fetchAiKnowledgeDocumentUpload(activeKb.value.kbId, file)
    ElMessage.success('上传成功，正在后台索引')
    await loadDocs()
  } catch (error) {
    handleMutationError(error, '上传失败')
  } finally {
    fileUploading.value = false
  }
}
</script>

<style scoped lang="scss">
.ai-knowledge-page {
  .search-card {
    margin-bottom: 12px;
  }
}
</style>

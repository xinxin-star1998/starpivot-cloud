<template>
  <div class="ai-config-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="配置名称">
          <ElInput v-model="searchForm.configName" clearable placeholder="配置名称" />
        </ElFormItem>
        <ElFormItem label="助手名称">
          <ElInput v-model="searchForm.botName" clearable placeholder="助手名称" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" clearable placeholder="全部" class="!w-28">
            <ElOption label="正常" value="0" />
            <ElOption label="停用" value="1" />
          </ElSelect>
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
          <ElButton v-auth="'ai:config:edit'" type="primary" @click="openEdit()">新增配置</ElButton>
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

    <ConfigEditDialog
      v-model:visible="editVisible"
      :config-data="currentConfig"
      :saving="saving"
      @submit="handleSave"
    />
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchAiConfigDetail,
  fetchAiConfigList,
  fetchAiConfigRemove,
  fetchAiConfigSave,
  fetchAiConfigSetDefault,
  type AiConfigItem,
  type AiConfigSavePayload
} from '@/api/ai/config'
import ConfigEditDialog from './modules/config-edit-dialog.vue'
import { handleMutationError } from '@/utils/http/mutation'

defineOptions({ name: 'AiConfig' })

const searchForm = ref({ configName: '', botName: '', status: '' })
const editVisible = ref(false)
const saving = ref(false)
const currentConfig = ref<AiConfigItem | null>(null)

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
    apiFn: fetchAiConfigList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'configName', label: '配置名称', minWidth: 110 },
      { prop: 'botName', label: '助手名称', minWidth: 110 },
      { prop: 'defaultModel', label: '默认模型', minWidth: 130 },
      {
        prop: 'isDefault',
        label: '默认',
        width: 80,
        formatter: (row: AiConfigItem) =>
          h(
            ElTag,
            { type: row.isDefault === '0' ? 'success' : 'info', size: 'small' },
            () => (row.isDefault === '0' ? '是' : '否')
          )
      },
      {
        prop: 'status',
        label: '状态',
        width: 80,
        formatter: (row: AiConfigItem) =>
          h(
            ElTag,
            { type: row.status === '0' ? 'success' : 'info', size: 'small' },
            () => (row.status === '0' ? '正常' : '停用')
          )
      },
      { prop: 'updateBy', label: '更新人', minWidth: 90 },
      { prop: 'updateTime', label: '更新时间', minWidth: 160 },
      {
        prop: 'actions',
        label: '操作',
        width: 220,
        fixed: 'right',
        formatter: (row: AiConfigItem) =>
          h('div', { class: 'flex flex-wrap gap-2' }, [
            h(
              'a',
              { class: 'text-primary cursor-pointer', onClick: () => openEdit(row) },
              '编辑'
            ),
            row.isDefault !== '0'
              ? h(
                  'a',
                  {
                    class: 'text-primary cursor-pointer',
                    onClick: () => handleSetDefault(row)
                  },
                  '设为默认'
                )
              : null,
            row.isDefault !== '0'
              ? h(
                  'a',
                  { class: 'text-danger cursor-pointer', onClick: () => handleDelete(row) },
                  '删除'
                )
              : null
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
  searchForm.value = { configName: '', botName: '', status: '' }
  handleSearch()
}

async function openEdit(row?: AiConfigItem): Promise<void> {
  if (row?.configId) {
    try {
      currentConfig.value = await fetchAiConfigDetail(row.configId)
    } catch (error) {
      handleMutationError(error, '加载配置失败')
      return
    }
  } else {
    currentConfig.value = null
  }
  editVisible.value = true
}

async function handleSave(payload: AiConfigSavePayload): Promise<void> {
  saving.value = true
  try {
    await fetchAiConfigSave(payload)
    ElMessage.success('保存成功')
    editVisible.value = false
    await refreshData()
  } catch (error) {
    handleMutationError(error, '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSetDefault(row: AiConfigItem): Promise<void> {
  if (!row.configId) return
  try {
    await fetchAiConfigSetDefault(row.configId)
    ElMessage.success('已设为默认配置')
    await refreshData()
  } catch (error) {
    handleMutationError(error, '操作失败')
  }
}

async function handleDelete(row: AiConfigItem): Promise<void> {
  if (!row.configId) return
  try {
    await ElMessageBox.confirm(`确定删除配置「${row.configName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await fetchAiConfigRemove(row.configId)
    ElMessage.success('删除成功')
    await refreshData()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}
</script>

<style scoped lang="scss">
.ai-config-page {
  .search-card {
    margin-bottom: 12px;
  }
}
</style>

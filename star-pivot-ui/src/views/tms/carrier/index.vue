<template>
  <div class="tms-carrier-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="编码">
          <ElInput v-model="searchForm.carrierCode" clearable placeholder="承运商编码" />
        </ElFormItem>
        <ElFormItem label="名称">
          <ElInput v-model="searchForm.carrierName" clearable placeholder="承运商名称" />
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
          <ElButton v-auth="'tms:carrier:edit'" type="primary" @click="openEdit()">新增承运商</ElButton>
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

    <ElDialog v-model="editVisible" :title="editForm.id ? '编辑承运商' : '新增承运商'" width="520px" destroy-on-close>
      <ElForm ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <ElFormItem label="承运商编码" prop="carrierCode">
          <ElInput v-model="editForm.carrierCode" :disabled="!!editForm.id" />
        </ElFormItem>
        <ElFormItem label="承运商名称" prop="carrierName">
          <ElInput v-model="editForm.carrierName" />
        </ElFormItem>
        <ElFormItem label="快递100编码" prop="kuaidi100Com">
          <ElInput v-model="editForm.kuaidi100Com" placeholder="如 shunfeng" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="editForm.sortOrder" :min="0" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="editForm.status">
            <ElRadio value="0">正常</ElRadio>
            <ElRadio value="1">停用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="editForm.remark" type="textarea" :rows="2" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="editVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="handleSave">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script lang="ts" setup>
import {h} from 'vue'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchTmsCarrierList,
  fetchTmsCarrierRemove,
  fetchTmsCarrierSave,
  type TmsCarrier,
  type TmsCarrierSavePayload
} from '@/api/tms/carrier'
import type {FormInstance, FormRules} from 'element-plus'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'TmsCarrier' })

const searchForm = ref({ carrierCode: '', carrierName: '' })
const editVisible = ref(false)
const saving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref<TmsCarrierSavePayload>({
  carrierCode: '',
  carrierName: '',
  kuaidi100Com: '',
  sortOrder: 0,
  status: '0',
  remark: ''
})

const editRules: FormRules = {
  carrierCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  carrierName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  kuaidi100Com: [{ required: true, message: '请输入快递100编码', trigger: 'blur' }]
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
    apiFn: fetchTmsCarrierList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'carrierCode', label: '编码', minWidth: 100 },
      { prop: 'carrierName', label: '名称', minWidth: 120 },
      { prop: 'kuaidi100Com', label: '快递100', minWidth: 110 },
      { prop: 'sortOrder', label: '排序', width: 80 },
      {
        prop: 'status',
        label: '状态',
        width: 90,
        formatter: (row: TmsCarrier) =>
          h(ElTag, { type: row.status === '0' ? 'success' : 'info' }, () =>
            row.status === '0' ? '正常' : '停用'
          )
      },
      {
        prop: 'actions',
        label: '操作',
        width: 140,
        fixed: 'right',
        formatter: (row: TmsCarrier) =>
          h('div', { class: 'flex gap-2' }, [
            h(
              'a',
              {
                class: 'text-primary cursor-pointer',
                onClick: () => openEdit(row)
              },
              '编辑'
            ),
            h(
              'a',
              {
                class: 'text-danger cursor-pointer',
                onClick: () => handleRemove(row)
              },
              '删除'
            )
          ])
      }
    ]
  }
})

function handleSearch() {
  Object.assign(searchParams, searchForm.value, { pageNum: 1 })
  getData()
}

function resetSearch() {
  searchForm.value = { carrierCode: '', carrierName: '' }
  handleSearch()
}

function openEdit(row?: TmsCarrier) {
  editForm.value = row
    ? {
        id: row.id,
        carrierCode: row.carrierCode || '',
        carrierName: row.carrierName || '',
        kuaidi100Com: row.kuaidi100Com || '',
        sortOrder: row.sortOrder ?? 0,
        status: row.status || '0',
        remark: row.remark
      }
    : {
        carrierCode: '',
        carrierName: '',
        kuaidi100Com: '',
        sortOrder: 0,
        status: '0',
        remark: ''
      }
  editVisible.value = true
}

async function handleSave() {
  await editFormRef.value?.validate()
  saving.value = true
  try {
    await fetchTmsCarrierSave(editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    refreshData()
  } catch (error) {
    handleMutationError(error, '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleRemove(row: TmsCarrier) {
  await ElMessageBox.confirm(`确认删除承运商「${row.carrierName}」？`, '提示', { type: 'warning' })
  try {
    await fetchTmsCarrierRemove(row.id!)
    ElMessage.success('删除成功')
    refreshData()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}
</script>

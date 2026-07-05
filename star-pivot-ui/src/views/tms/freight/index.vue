<template>
  <div class="tms-freight-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="名称">
          <ElInput v-model="searchForm.ruleName" clearable placeholder="规则名称" />
        </ElFormItem>
        <ElFormItem label="类型">
          <ElSelect v-model="searchForm.ruleType" clearable placeholder="全部" style="width: 140px">
            <ElOption label="固定运费" value="FIXED" />
            <ElOption label="按重量" value="WEIGHT" />
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
          <ElButton v-auth="'tms:freight:edit'" type="primary" @click="openEdit()">新增规则</ElButton>
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

    <ElDialog v-model="editVisible" :title="editForm.id ? '编辑规则' : '新增规则'" width="560px" destroy-on-close>
      <ElForm ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <ElFormItem label="规则名称" prop="ruleName">
          <ElInput v-model="editForm.ruleName" />
        </ElFormItem>
        <ElFormItem label="规则类型" prop="ruleType">
          <ElSelect v-model="editForm.ruleType" style="width: 100%">
            <ElOption label="固定运费" value="FIXED" />
            <ElOption label="按重量" value="WEIGHT" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="默认规则">
          <ElSwitch v-model="editForm.defaultFlag" active-value="1" inactive-value="0" />
        </ElFormItem>
        <template v-if="editForm.ruleType === 'FIXED'">
          <ElFormItem label="固定运费" prop="baseFee">
            <ElInputNumber v-model="editForm.baseFee" :min="0" :precision="2" style="width: 100%" />
          </ElFormItem>
        </template>
        <template v-else>
          <ElFormItem label="首重(kg)" prop="firstWeightKg">
            <ElInputNumber v-model="editForm.firstWeightKg" :min="0.001" :precision="3" style="width: 100%" />
          </ElFormItem>
          <ElFormItem label="首重费用" prop="firstFee">
            <ElInputNumber v-model="editForm.firstFee" :min="0" :precision="2" style="width: 100%" />
          </ElFormItem>
          <ElFormItem label="续重单价" prop="continueFeePerKg">
            <ElInputNumber v-model="editForm.continueFeePerKg" :min="0" :precision="2" style="width: 100%" />
          </ElFormItem>
        </template>
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
  fetchTmsFreightList,
  fetchTmsFreightRemove,
  fetchTmsFreightSave,
  freightRuleTypeLabel,
  type TmsFreightRule,
  type TmsFreightSavePayload
} from '@/api/tms/freight'
import type {FormInstance, FormRules} from 'element-plus'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'TmsFreight' })

const searchForm = ref({ ruleName: '', ruleType: '' })
const editVisible = ref(false)
const saving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref<TmsFreightSavePayload>({
  ruleName: '',
  ruleType: 'FIXED',
  defaultFlag: '0',
  baseFee: 10,
  status: '0',
  sortOrder: 0
})

const editRules: FormRules = {
  ruleName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择类型', trigger: 'change' }]
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
    apiFn: fetchTmsFreightList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'ruleName', label: '名称', minWidth: 140 },
      {
        prop: 'ruleType',
        label: '类型',
        width: 100,
        formatter: (row: TmsFreightRule) => freightRuleTypeLabel[row.ruleType || ''] || row.ruleType
      },
      {
        prop: 'defaultFlag',
        label: '默认',
        width: 80,
        formatter: (row: TmsFreightRule) =>
          h(ElTag, { type: row.defaultFlag === '1' ? 'success' : 'info' }, () =>
            row.defaultFlag === '1' ? '是' : '否'
          )
      },
      {
        prop: 'feeDesc',
        label: '计费说明',
        minWidth: 180,
        formatter: (row: TmsFreightRule) =>
          row.ruleType === 'WEIGHT'
            ? `首${row.firstWeightKg}kg ${row.firstFee}元，续${row.continueFeePerKg}元/kg`
            : `固定 ${row.baseFee ?? 0} 元`
      },
      {
        prop: 'actions',
        label: '操作',
        width: 140,
        fixed: 'right',
        formatter: (row: TmsFreightRule) =>
          h('div', { class: 'flex gap-2' }, [
            h('a', { class: 'text-primary cursor-pointer', onClick: () => openEdit(row) }, '编辑'),
            h(
              'a',
              {
                class: row.defaultFlag === '1' ? 'text-gray-400' : 'text-danger cursor-pointer',
                onClick: () => row.defaultFlag !== '1' && handleRemove(row)
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
  searchForm.value = { ruleName: '', ruleType: '' }
  handleSearch()
}

function openEdit(row?: TmsFreightRule) {
  editForm.value = row
    ? {
        id: row.id,
        ruleName: row.ruleName || '',
        ruleType: row.ruleType || 'FIXED',
        defaultFlag: row.defaultFlag || '0',
        baseFee: row.baseFee,
        firstWeightKg: row.firstWeightKg,
        firstFee: row.firstFee,
        continueFeePerKg: row.continueFeePerKg,
        status: row.status || '0',
        sortOrder: row.sortOrder ?? 0,
        remark: row.remark
      }
    : {
        ruleName: '',
        ruleType: 'FIXED',
        defaultFlag: '0',
        baseFee: 10,
        status: '0',
        sortOrder: 0
      }
  editVisible.value = true
}

async function handleSave() {
  await editFormRef.value?.validate()
  saving.value = true
  try {
    await fetchTmsFreightSave(editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    refreshData()
  } catch (error) {
    handleMutationError(error, '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleRemove(row: TmsFreightRule) {
  await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」？`, '提示', { type: 'warning' })
  try {
    await fetchTmsFreightRemove(row.id!)
    ElMessage.success('删除成功')
    refreshData()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}
</script>

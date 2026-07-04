<template>
  <div class="dict-data-panel">
    <ArtSearchBar
      v-model="searchFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ArtTableHeader
      :showZebra="false"
      :loading="loading"
      v-model:columns="columnChecks"
      @refresh="handleRefresh"
    >
      <template #left>
        <ElButton @click="handleAdd" v-ripple v-auth="'system:data:add'" :disabled="!dictType">
          新增字典数据
        </ElButton>
      </template>
    </ArtTableHeader>

    <ArtTable
      ref="tableRef"
      rowKey="dictCode"
      class="dict-table"
      :loading="loading"
      :columns="columns"
      :data="tableData"
      :stripe="false"
      :pagination="pagination"
      @pagination:size-change="handleSizeChange"
      @pagination:current-change="handleCurrentChange"
    />

    <DictDataDialog
      v-model:visible="dialogVisible"
      :editData="editData"
      :dictType="dictType"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import {safeError} from '@/utils'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTableColumns} from '@/hooks/core/useTableColumns'
import DictDataDialog from './dict-data-dialog.vue'
import {
  type DictDataFormData,
  fetchAddDictData,
  fetchDeleteDictData,
  fetchGetDictDataList,
  fetchUpdateDictData,
  type SysDictData
} from '@/api/dict/data'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'DictDataPanel' })

  interface Props {
    dictType: string
  }

  const props = withDefaults(defineProps<Props>(), {
    dictType: ''
  })

  const { hasAuth } = useAuth()

  const loading = ref(false)
  const tableRef = ref()

  const dialogVisible = ref(false)
  const editData = ref<DictDataFormData | null>(null)

  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const searchFilters = reactive({
    dictLabel: '',
    status: ''
  })

  const formItems = computed(() => [
    {
      label: '字典标签',
      key: 'dictLabel',
      type: 'input',
      props: { clearable: true, placeholder: '请输入字典标签' }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择状态',
        options: [
          { label: '正常', value: '0' },
          { label: '停用', value: '1' }
        ]
      }
    }
  ])

  const STATUS_CONFIG = {
    '0': { text: '正常', type: 'success' as const },
    '1': { text: '停用', type: 'danger' as const }
  }

  const { columnChecks, columns } = useTableColumns(() => [
    { type: 'index', width: 60, label: '序号' },
    // { prop: 'dictCode', label: '字典编码', width: 80 },
    { prop: 'dictLabel', label: '字典标签', minWidth: 120 },
    { prop: 'dictValue', label: '字典键值', minWidth: 120 },
    // { prop: 'dictType', label: '字典类型', minWidth: 150 },
    // {
    //   prop: 'dictSort',
    //   label: '字典排序',
    //   width: 100,
    //   formatter: (row: SysDictData) => row.dictSort || 0
    // },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: SysDictData) => {
        const status = (row.status || '0') as keyof typeof STATUS_CONFIG
        const statusInfo = STATUS_CONFIG[status] || STATUS_CONFIG['0']
        return h(ElTag, { type: statusInfo.type }, () => statusInfo.text)
      }
    },
    {
      prop: 'remark',
      label: '备注',
      minWidth: 150,
      formatter: (row: SysDictData) =>
        row.remark || h('span', { style: 'color: var(--art-gray-500)' }, '无')
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      formatter: (row: SysDictData) =>
        row.createTime || h('span', { style: 'color: var(--art-gray-500)' }, '暂无')
    },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      align: 'right',
      formatter: (row: SysDictData) => {
        const actions: any[] = []

        if (hasAuth('system:data:edit')) {
          actions.push(h(ArtButtonTable, { type: 'edit', onClick: () => handleEdit(row) }))
        }
        if (hasAuth('system:data:delete')) {
          actions.push(h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) }))
        }

        if (actions.length === 0) return h('span', { style: 'color: var(--art-gray-500)' }, '')
        return h('div', { style: 'text-align: right' }, actions)
      }
    }
  ])

  const tableData = ref<SysDictData[]>([])

  const getDictDataList = async (): Promise<void> => {
    if (!props.dictType) {
      tableData.value = []
      pagination.total = 0
      return
    }

    loading.value = true
    try {
      const params: any = {
        pageNum: pagination.current,
        pageSize: pagination.size,
        dictType: props.dictType
      }
      if (searchFilters.dictLabel) params.dictLabel = searchFilters.dictLabel
      if (searchFilters.status) params.status = searchFilters.status

      const result = await fetchGetDictDataList(params)
      tableData.value = result?.rows || []
      pagination.total = result?.total || 0
    } catch (error) {
      safeError('获取字典数据列表失败:', error)
      ElMessage.error('获取字典数据列表失败')
    } finally {
      loading.value = false
    }
  }

  const handleReset = (): void => {
    Object.assign(searchFilters, { dictLabel: '', status: '' })
    pagination.current = 1
    getDictDataList()
  }

  const handleSearch = (): void => {
    pagination.current = 1
    getDictDataList()
  }

  const handleSizeChange = (size: number): void => {
    pagination.size = size
    pagination.current = 1
    getDictDataList()
  }

  const handleCurrentChange = (current: number): void => {
    pagination.current = current
    getDictDataList()
  }

  const handleRefresh = (): void => {
    getDictDataList()
  }

  const handleAdd = (): void => {
    if (!props.dictType) {
      ElMessage.warning('请先选择字典类型')
      return
    }
    editData.value = null
    dialogVisible.value = true
  }

  const handleEdit = (row: SysDictData): void => {
    editData.value = {
      dictCode: row.dictCode,
      dictSort: row.dictSort || 0,
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      dictType: row.dictType,
      cssClass: row.cssClass || '',
      listClass: row.listClass || '',
      isDefault: row.isDefault || 'N',
      status: row.status || '0',
      remark: row.remark || ''
    }
    dialogVisible.value = true
  }

  const handleDelete = async (row: SysDictData): Promise<void> => {
    if (!row.dictCode) {
      ElMessage.warning('字典数据ID不存在，无法删除')
      return
    }

    try {
      await ElMessageBox.confirm(`确定要删除字典数据"${row.dictLabel}"吗？删除后无法恢复`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchDeleteDictData([row.dictCode])
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && pagination.current > 1) pagination.current--
      await getDictDataList()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        safeError('删除字典数据失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  const handleSubmit = async (formData: DictDataFormData): Promise<void> => {
    try {
      const isEdit = !!formData.dictCode
      if (isEdit) {
        await fetchUpdateDictData(formData)
        ElMessage.success('修改字典数据成功')
      } else {
        await fetchAddDictData(formData)
        ElMessage.success('新增字典数据成功')
      }
      dialogVisible.value = false
      await getDictDataList()
    } catch (error) {
      safeError('保存字典数据失败:', error)
      ElMessage.error(formData.dictCode ? '修改字典数据失败' : '新增字典数据失败')
    }
  }

  watch(
    () => props.dictType,
    () => {
      Object.assign(searchFilters, { dictLabel: '', status: '' })
      pagination.current = 1
      getDictDataList()
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .dict-data-panel {
    display: flex;
    flex-direction: column;
    gap: 10px;
    min-height: 0;
  }

  .dict-table {
    flex: 1;
    min-height: 0;
  }
</style>

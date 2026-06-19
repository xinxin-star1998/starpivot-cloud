<!-- 仓储-仓库管理（wms_ware_info） -->
<template>
  <div class="wms-warehouse-page art-full-height">
    <WarehouseSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'mall:ware:add'" type="primary" @click="showDialog('add')" v-ripple>
              新增仓库
            </ElButton>
            <ElButton
              v-auth="'mall:ware:delete'"
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
            >
              批量删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <WarehouseDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :ware-data="currentWare"
      @submit="handleDialogSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchWmsWareInfoList,
    fetchWmsWareInfoRemove,
    type WmsWareInfoVo
  } from '@/api/mall/wareinfo'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import WarehouseSearch from './modules/warehouse-search.vue'
  import WarehouseDialog from './modules/warehouse-dialog.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { DialogType } from '@/types'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'WmsWarehouse' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    name: undefined as string | undefined,
    address: undefined as string | undefined,
    areacode: undefined as string | undefined
  })

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentWare = ref<Partial<WmsWareInfoVo>>({})
  const selectedRows = ref<WmsWareInfoVo[]>([])

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchWmsWareInfoList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          type: 'index',
          label: '序号',
          width: 70,
          index: (index: number) => (pagination.current - 1) * pagination.size + index + 1
        },
        { prop: 'name', label: '仓库名称', minWidth: 140 },
        { prop: 'areacode', label: '区域编码', width: 120, showOverflowTooltip: true },
        { prop: 'address', label: '详细地址', minWidth: 200, showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: WmsWareInfoVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:ware:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '编辑',
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }
            if (hasAuth('mall:ware:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '删除',
                  type: 'delete',
                  onClick: () => deleteOne(row)
                })
              )
            }
            if (actions.length === 0) {
              return h('span', { style: 'color: #999' }, '')
            }
            return h('div', actions)
          }
        }
      ]
    }
  })

  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, params)
    getData()
  }

  const showDialog = (type: DialogType, row?: WmsWareInfoVo) => {
    dialogType.value = type
    currentWare.value = row ? { ...row } : {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = () => {
    currentWare.value = {}
    refreshData()
  }

  const handleSelectionChange = (selection: WmsWareInfoVo[]) => {
    selectedRows.value = selection
  }

  const deleteOne = (row: WmsWareInfoVo) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除仓库「${row.name || row.id}」吗？`, '删除仓库', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchWmsWareInfoRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }

  const handleBatchDelete = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的仓库')
      return
    }
    const names = selectedRows.value.map((r) => r.name || r.id).join('、')
    ElMessageBox.confirm(`确定删除以下仓库吗？\n${names}`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        const ids = selectedRows.value.map((r) => r.id!).filter(Boolean)
        await fetchWmsWareInfoRemove(ids)
        selectedRows.value = []
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .wms-warehouse-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }
</style>

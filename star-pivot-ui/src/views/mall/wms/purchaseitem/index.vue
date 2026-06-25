<!-- 采购需求 -->
<template>
  <div class="purchase-item-page art-full-height">
    <PurchaseItemSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton
              v-auth="'mall:purchase:item'"
              type="primary"
              @click="dialogVisible = true"
              v-ripple
            >
              新增需求
            </ElButton>
            <ElButton
              v-auth="'mall:purchase:item'"
              type="success"
              :disabled="selectedRows.length === 0"
              @click="mergeVisible = true"
              v-ripple
            >
              合并到采购单
            </ElButton>
            <ElButton
              v-auth="'mall:purchase:item'"
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

    <PurchaseItemDialog v-model:visible="dialogVisible" @submit="refreshData" />
    <PurchaseMergeDialog
      v-model:visible="mergeVisible"
      :detail-ids="selectedIds"
      @submit="onMergeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchPurchaseDetailList,
    fetchPurchaseDetailRemove,
    PURCHASE_DETAIL_STATUS_MAP,
    type PurchaseDetailVo
  } from '@/api/mall/purchase'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import PurchaseItemSearch from './modules/purchase-item-search.vue'
  import PurchaseItemDialog from './modules/purchase-item-dialog.vue'
  import PurchaseMergeDialog from './modules/purchase-merge-dialog.vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'PurchaseItem' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    skuId: undefined as number | undefined,
    status: 0 as number | undefined
  })

  const dialogVisible = ref(false)
  const mergeVisible = ref(false)
  const selectedRows = ref<PurchaseDetailVo[]>([])

  const selectedIds = computed(() =>
    selectedRows.value.map((r) => r.id).filter((id): id is number => id != null)
  )

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
      apiFn: (params) => {
        const { status, ...rest } = params as Record<string, unknown>
        return fetchPurchaseDetailList({
          ...rest,
          status: status as number | undefined,
          onlyDemand: status === 0 || status === undefined || status === null
        })
      },
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'skuId', label: 'SKU ID', width: 100 },
        {
          prop: 'skuName',
          label: 'SKU 名称',
          minWidth: 160,
          showOverflowTooltip: true,
          formatter: (row) => row.skuName || '-'
        },
        { prop: 'skuNum', label: '数量', width: 80 },
        { prop: 'skuPrice', label: '单价', width: 100 },
        { prop: 'wareId', label: '仓库', width: 100 },
        { prop: 'purchaseId', label: '采购单', width: 90 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row) =>
            h(
              ElTag,
              { type: row.status === 0 ? 'info' : 'success', size: 'small' },
              () => PURCHASE_DETAIL_STATUS_MAP[row.status ?? 0] ?? row.status
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row) => {
            if (!hasAuth('mall:purchase:item') || row.status !== 0) return ''
            return h(ArtButtonTable, {
              type: 'delete',
              onClick: () => deleteItem(row)
            })
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }

  function handleSelectionChange(selection: PurchaseDetailVo[]) {
    selectedRows.value = selection.filter((r) => r.status === 0)
  }

  function onMergeSuccess() {
    selectedRows.value = []
    refreshData()
  }

  async function deleteItem(row: PurchaseDetailVo) {
    if (row.id == null) return
    await ElMessageBox.confirm('确定删除该采购需求？', '提示', { type: 'warning' })
    await fetchPurchaseDetailRemove([row.id])
    refreshData()
  }

  async function handleBatchDelete() {
    const ids = selectedIds.value
    if (!ids.length) {
      ElMessage.warning('请选择新建状态的需求')
      return
    }
    await ElMessageBox.confirm(`确定删除 ${ids.length} 条采购需求？`, '批量删除', {
      type: 'warning'
    })
    await fetchPurchaseDetailRemove(ids)
    selectedRows.value = []
    refreshData()
  }
</script>

<!-- 商品库存管理页面 -->
<template>
  <div class="sku-page art-full-height">
    <SkuSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'mall:sku:add'" v-ripple @click="openInbound">快速入库</ElButton>
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
              v-auth="'mall:sku:delete'"
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

    <SkuDialog v-model:visible="dialogVisible" @submit="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchDeleteSku, fetchGetSkuList, type WareSku } from '@/api/mall/ware-sku'
  import SkuSearch from './modules/sku-search.vue'
  import SkuDialog from './modules/sku-dialog.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'WareSku' })

  const { hasAuth } = useAuth()

  const dialogVisible = ref(false)
  const selectedRows = ref<WareSku[]>([])

  const searchForm = ref({
    skuId: undefined,
    wareId: undefined,
    stock: undefined,
    skuName: undefined,
    stockLocked: undefined
  })

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
      apiFn: fetchGetSkuList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        { prop: 'skuId', label: 'SKU ID', width: 90 },
        { prop: 'skuName', label: 'SKU 名称', minWidth: 180, showOverflowTooltip: true },
        { prop: 'wareName', label: '仓库', width: 120, showOverflowTooltip: true },
        { prop: 'stock', label: '总库存', width: 90 },
        { prop: 'stockLocked', label: '锁定', width: 80 },
        { prop: 'availableStock', label: '可售', width: 80 },
        { prop: 'stockWarning', label: '预警值', width: 80 },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row) => {
            if (!hasAuth('mall:sku:delete')) return ''
            return h(ArtButtonTable, {
              type: 'delete',
              onClick: () => deleteSku(row)
            })
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, any>) {
    Object.assign(searchParams, params)
    getData()
  }

  function openInbound() {
    dialogVisible.value = true
  }

  async function deleteSku(row: WareSku): Promise<void> {
    if (row.id == null) {
      ElMessage.warning('无效的记录')
      return
    }
    try {
      await ElMessageBox.confirm('确定要删除该库存记录吗？', '删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteSku([row.id])
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  function handleBatchDelete(): void {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的记录')
      return
    }
    ElMessageBox.confirm(`确定删除 ${selectedRows.value.length} 条库存记录？`, '批量删除', {
      type: 'warning'
    })
      .then(async () => {
        const ids = selectedRows.value.map((row) => row.id).filter((id): id is number => id != null)
        if (!ids.length) return
        await fetchDeleteSku(ids)
        selectedRows.value = []
        refreshData()
      })
      .catch(() => {})
  }

  function handleSelectionChange(selection: WareSku[]): void {
    selectedRows.value = selection
  }
</script>

<style scoped lang="scss">
  .sku-page {
    padding: 20px;
  }
</style>

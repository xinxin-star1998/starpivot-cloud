<!-- 退款流水查询 -->
<template>
  <div class="oms-refund-page art-full-height">
    <RefundSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchRefundList,
    REFUND_CHANNEL_MAP,
    REFUND_STATUS_MAP,
    type RefundVo
  } from '@/api/mall/refund'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import RefundSearch from './modules/refund-search.vue'
  import { ElTag } from 'element-plus'

  defineOptions({ name: 'OmsRefundLog' })

  const searchForm = ref({
    orderSn: undefined as string | undefined
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
      apiFn: fetchRefundList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'refundSn', label: '退款单号', minWidth: 180, showOverflowTooltip: true },
        { prop: 'orderReturnId', label: '退货单ID', width: 100 },
        {
          prop: 'refund',
          label: '退款金额',
          width: 100,
          formatter: (row: RefundVo) =>
            row.refund != null ? `¥${Number(row.refund).toFixed(2)}` : '-'
        },
        {
          prop: 'refundStatus',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const status = row.refundStatus ?? 0
            const type = status === 2 ? 'success' : status === 3 ? 'danger' : 'info'
            return h(ElTag, { type, size: 'small' }, () => REFUND_STATUS_MAP[status] ?? status)
          }
        },
        {
          prop: 'refundChannel',
          label: '渠道',
          width: 90,
          formatter: (row) => REFUND_CHANNEL_MAP[row.refundChannel ?? 0] ?? row.refundChannel ?? '-'
        },
        { prop: 'refundContent', label: '说明', minWidth: 140, showOverflowTooltip: true }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }
</script>

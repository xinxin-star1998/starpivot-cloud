<!-- 支付流水查询 -->
<template>
  <div class="oms-payment-page art-full-height">
    <PaymentSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
  import { useTable } from '@/hooks/core/useTable'
  import { fetchPaymentList, type PaymentVo } from '@/api/mall/payment'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import PaymentSearch from './modules/payment-search.vue'

  defineOptions({ name: 'OmsPaymentLog' })

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
      apiFn: fetchPaymentList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'orderSn', label: '订单号', minWidth: 180, showOverflowTooltip: true },
        { prop: 'alipayTradeNo', label: '交易号', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'totalAmount',
          label: '金额',
          width: 100,
          formatter: (row: PaymentVo) =>
            row.totalAmount != null ? `¥${Number(row.totalAmount).toFixed(2)}` : '-'
        },
        { prop: 'subject', label: '标题', minWidth: 140, showOverflowTooltip: true },
        { prop: 'paymentStatus', label: '支付状态', width: 100 },
        { prop: 'createTime', label: '创建时间', minWidth: 160 },
        { prop: 'confirmTime', label: '确认时间', minWidth: 160 }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }
</script>

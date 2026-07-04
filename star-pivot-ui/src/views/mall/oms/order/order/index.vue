<!-- 订单查询 -->
<template>
  <div class="oms-order-page art-full-height">
    <OrderSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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

    <OrderDetailDrawer
      v-model:visible="detailVisible"
      :order-id="currentOrderId"
      @deliver="openDeliver"
      @submit="refreshData"
    />
    <DeliverDialog
      v-model:visible="deliverVisible"
      :order-id="currentOrderId"
      @submit="onDeliverSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {useTable} from '@/hooks/core/useTable'
import {fetchOmsOrderList, OMS_ORDER_STATUS_MAP} from '@/api/mall/order'
import {getPortalOrderStatusLabel, getPortalOrderStatusType} from '@/utils/portal/order-status'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import OrderSearch from './modules/order-search.vue'
import OrderDetailDrawer from './modules/order-detail-drawer.vue'
import DeliverDialog from './modules/deliver-dialog.vue'
import {ElButton, ElSpace, ElTag} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'OmsOrderQuery' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    orderSn: undefined as string | undefined,
    memberUsername: undefined as string | undefined,
    status: undefined as number | undefined,
    startTime: undefined as string | undefined,
    endTime: undefined as string | undefined
  })

  const detailVisible = ref(false)
  const deliverVisible = ref(false)
  const currentOrderId = ref<number>()

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
      apiFn: fetchOmsOrderList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'orderSn', label: '订单号', minWidth: 180, showOverflowTooltip: true },
        { prop: 'memberUsername', label: '会员', minWidth: 100, showOverflowTooltip: true },
        {
          prop: 'payAmount',
          label: '应付金额',
          width: 110,
          formatter: (row) => (row.payAmount != null ? `¥${Number(row.payAmount).toFixed(2)}` : '-')
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row) =>
            h(
              ElTag,
              { type: getPortalOrderStatusType(row.status), size: 'small' },
              () => OMS_ORDER_STATUS_MAP[row.status ?? -1] ?? getPortalOrderStatusLabel(row.status)
            )
        },
        { prop: 'receiverName', label: '收货人', width: 100 },
        { prop: 'receiverPhone', label: '电话', width: 120 },
        { prop: 'createTime', label: '下单时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:order:query')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => openDetail(row.id) },
                  () => '详情'
                )
              )
            }
            if (hasAuth('mall:order:deliver') && row.status === 1) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'success', onClick: () => openDeliver(row.id) },
                  () => '发货'
                )
              )
            }
            return actions.length ? h(ElSpace, null, () => actions) : ''
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }

  function openDetail(id?: number) {
    currentOrderId.value = id
    detailVisible.value = true
  }

  function openDeliver(id?: number) {
    currentOrderId.value = id
    deliverVisible.value = true
  }

  function onDeliverSuccess() {
    deliverVisible.value = false
    detailVisible.value = false
    refreshData()
  }
</script>

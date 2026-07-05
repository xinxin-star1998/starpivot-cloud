<!-- 退款流水查询 -->
<template>
  <div class="oms-refund-page art-full-height">
    <RefundSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElAlert
      v-if="alertSummary.unreadCount && alertSummary.unreadCount > 0"
      class="refund-alert-banner"
      type="error"
      :closable="false"
      show-icon
      :title="`有 ${alertSummary.unreadCount} 笔退款失败待处理`"
    >
      <template #default>
        <div v-for="item in alertSummary.recentItems" :key="item.id" class="alert-item">
          订单 {{ item.orderSn || '-' }} / 退款单 {{ item.refundSn || '-' }}
          <ElButton link type="primary" @click="openDetail(item.id!)">查看</ElButton>
        </div>
      </template>
    </ElAlert>

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

    <RefundDetailDrawer
      v-model:visible="detailVisible"
      :refund-id="currentRefundId"
      @changed="handleRefundChanged"
    />
  </div>
</template>

<script setup lang="ts">
import {h, onMounted} from 'vue'
import {useTable} from '@/hooks/core/useTable'
import {
  fetchRefundAlertSummary,
  fetchRefundList,
  REFUND_CHANNEL_MAP,
  REFUND_STATUS_MAP,
  type RefundAlertSummary,
  type RefundVo
} from '@/api/mall/refund'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import RefundSearch from './modules/refund-search.vue'
import RefundDetailDrawer from './modules/refund-detail-drawer.vue'
import {ElButton, ElTag} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'OmsRefundLog' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    orderSn: undefined as string | undefined
  })

  const detailVisible = ref(false)
  const currentRefundId = ref<number>()
  const alertSummary = ref<RefundAlertSummary>({ unreadCount: 0, recentItems: [] })

  onMounted(() => {
    loadAlertSummary()
  })

  async function loadAlertSummary() {
    if (!hasAuth('mall:refund:query')) {
      return
    }
    try {
      alertSummary.value = await fetchRefundAlertSummary()
    } catch {
      alertSummary.value = { unreadCount: 0, recentItems: [] }
    }
  }

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
        { prop: 'orderSn', label: '订单号', minWidth: 160, showOverflowTooltip: true },
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
            const type =
              status === 2 ? 'success' : status === 3 ? 'danger' : status === 1 ? 'warning' : 'info'
            return h(ElTag, { type, size: 'small' }, () => REFUND_STATUS_MAP[status] ?? status)
          }
        },
        {
          prop: 'refundChannel',
          label: '渠道',
          width: 90,
          formatter: (row) => REFUND_CHANNEL_MAP[row.refundChannel ?? 0] ?? row.refundChannel ?? '-'
        },
        {
          prop: 'operation',
          label: '操作',
          width: 90,
          fixed: 'right',
          formatter: (row) => {
            if (!hasAuth('mall:refund:query') || row.id == null) {
              return '-'
            }
            return h(
              ElButton,
              { link: true, type: 'primary', onClick: () => openDetail(row.id!) },
              () => '详情'
            )
          }
        }
      ]
    }
  })

  function openDetail(id: number) {
    currentRefundId.value = id
    detailVisible.value = true
  }

  function handleRefundChanged() {
    refreshData()
    loadAlertSummary()
  }

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }
</script>

<style scoped>
.refund-alert-banner {
  margin-top: 12px;
}

.alert-item + .alert-item {
  margin-top: 4px;
}
</style>

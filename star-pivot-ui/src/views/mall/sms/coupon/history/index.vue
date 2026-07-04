<!-- 优惠券发放记录 -->
<template>
  <div class="coupon-history-page art-full-height">
    <HistorySearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

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
import {h} from 'vue'
import {useTable} from '@/hooks/core/useTable'
import {
  COUPON_GET_TYPE_MAP,
  COUPON_HISTORY_USE_TYPE_MAP,
  type CouponHistoryVo,
  fetchCouponHistoryList
} from '@/api/mall/coupon-history'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import HistorySearch from './modules/history-search.vue'
import {ElTag} from 'element-plus'

defineOptions({ name: 'CouponHistory' })

  const searchForm = ref({
    memberId: undefined as number | undefined,
    couponId: undefined as number | undefined
  })

  function normalizeParams(params: Record<string, unknown>) {
    const next = { ...params }
    for (const key of ['memberId', 'couponId'] as const) {
      const raw = next[key]
      if (raw === '' || raw == null) {
        delete next[key]
      } else {
        const num = Number(raw)
        if (Number.isFinite(num) && num > 0) next[key] = num
        else delete next[key]
      }
    }
    return next
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
      apiFn: fetchCouponHistoryList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 70 },
        { prop: 'couponId', label: '优惠券 ID', width: 100 },
        { prop: 'memberId', label: '会员 ID', width: 100 },
        { prop: 'memberNickName', label: '会员昵称', minWidth: 110, showOverflowTooltip: true },
        {
          prop: 'getType',
          label: '领取方式',
          width: 100,
          formatter: (row) => COUPON_GET_TYPE_MAP[row.getType ?? 0] ?? row.getType ?? '-'
        },
        {
          prop: 'useType',
          label: '使用状态',
          width: 100,
          formatter: (row: CouponHistoryVo) => {
            const status = row.useType ?? 0
            const type = status === 1 ? 'success' : status === 2 ? 'info' : 'warning'
            return h(ElTag, { type, size: 'small' }, () =>
              COUPON_HISTORY_USE_TYPE_MAP[status] ?? status
            )
          }
        },
        { prop: 'createTime', label: '领取时间', minWidth: 160 },
        { prop: 'useTime', label: '使用时间', minWidth: 160 },
        { prop: 'orderSn', label: '订单号', minWidth: 160, showOverflowTooltip: true }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeParams(params), { pageNum: 1 })
    getData()
  }

  function handleReset() {
    searchForm.value = { memberId: undefined, couponId: undefined }
    resetSearchParams()
    getData()
  }
</script>

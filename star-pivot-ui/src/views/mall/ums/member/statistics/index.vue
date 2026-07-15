<!-- 会员-统计信息 -->
<template>
  <div class="member-statistics-page art-full-height">
    <StatisticsSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

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

    <StatisticsDetailDrawer
      v-model:visible="detailVisible"
      :member-id="currentMemberId"
      @submit="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {useTable} from '@/hooks/core/useTable'
import {
  fetchMemberStatisticsList,
  fetchMemberStatisticsRefresh,
  type MemberStatisticsVo
} from '@/api/mall/member-statistics'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import StatisticsSearch from './modules/statistics-search.vue'
import StatisticsDetailDrawer from './modules/statistics-detail-drawer.vue'
import {ElButton, ElMessage, ElMessageBox} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'
import {formatMoney} from '@/utils/mall/money'

defineOptions({ name: 'MemberStatistics' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    memberId: undefined as number | undefined,
    username: undefined as string | undefined,
    mobile: undefined as string | undefined
  })

  const detailVisible = ref(false)
  const currentMemberId = ref<number>()
  const refreshingMemberId = ref<number>()

  function formatAmount(value?: number) {
    return formatMoney(value, '0.00')
  }

  function normalizeSearchParams(params: Record<string, unknown>) {
    const next = { ...params }
    const rawMemberId = next.memberId
    if (rawMemberId === '' || rawMemberId == null) {
      delete next.memberId
    } else {
      const memberId = Number(rawMemberId)
      if (Number.isFinite(memberId) && memberId > 0) {
        next.memberId = memberId
      } else {
        delete next.memberId
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
      apiFn: fetchMemberStatisticsList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        {
          type: 'index',
          label: '序号',
          width: 70,
          index: (index: number) => (pagination.current - 1) * pagination.size + index + 1
        },
        { prop: 'memberId', label: '会员 ID', width: 90 },
        { prop: 'username', label: '用户名', minWidth: 120, showOverflowTooltip: true },
        { prop: 'nickname', label: '昵称', minWidth: 120, showOverflowTooltip: true },
        { prop: 'mobile', label: '手机号', width: 130 },
        {
          prop: 'consumeAmount',
          label: '累计消费',
          width: 110,
          formatter: (row) => formatAmount(row.consumeAmount)
        },
        {
          prop: 'orderCount',
          label: '订单数',
          width: 90
        },
        {
          prop: 'couponCount',
          label: '优惠券',
          width: 90
        },
        {
          prop: 'loginCount',
          label: '登录次数',
          width: 100
        },
        {
          prop: 'collectProductCount',
          label: '收藏商品',
          width: 100
        },
        {
          prop: 'operation',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: MemberStatisticsVo) => {
            if (!hasAuth('mall:member:statistics')) return ''
            const actions: ReturnType<typeof h>[] = [
              h(
                ElButton,
                { link: true, type: 'primary', onClick: () => openDetail(row.memberId) },
                () => '详情'
              ),
              h(
                ElButton,
                {
                  link: true,
                  type: 'warning',
                  loading: refreshingMemberId.value === row.memberId,
                  onClick: () => refreshOne(row)
                },
                () => '刷新'
              )
            ]
            return h('div', actions)
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeSearchParams(params))
    getData()
  }

  function handleReset() {
    searchForm.value = {
      memberId: undefined,
      username: undefined,
      mobile: undefined
    }
    resetSearchParams()
  }

  function openDetail(memberId?: number) {
    if (!memberId) return
    currentMemberId.value = memberId
    detailVisible.value = true
  }

  async function refreshOne(row: MemberStatisticsVo) {
    if (!row.memberId) return
    try {
      await ElMessageBox.confirm(
        `确定从业务数据重新聚合会员「${row.nickname || row.username || row.memberId}」的统计吗？`,
        '刷新统计',
        { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
      )
    } catch {
      return
    }
    refreshingMemberId.value = row.memberId
    try {
      await fetchMemberStatisticsRefresh(row.memberId)
      ElMessage.success('刷新成功')
      refreshData()
    } finally {
      refreshingMemberId.value = undefined
    }
  }
</script>

<style scoped lang="scss">
  .member-statistics-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }
</style>

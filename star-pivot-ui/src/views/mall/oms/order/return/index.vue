<!-- 退货处理 -->
<template>
  <div class="oms-return-page art-full-height">
    <ReturnSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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

    <ReturnDetailDrawer v-model:visible="detailVisible" :return-id="currentReturnId" />
    <AuditDialog v-model:visible="auditVisible" :return-id="currentReturnId" @submit="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchReturnList, RETURN_STATUS_MAP, type ReturnVo } from '@/api/mall/order-return'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ReturnSearch from './modules/return-search.vue'
  import ReturnDetailDrawer from './modules/return-detail-drawer.vue'
  import AuditDialog from './modules/audit-dialog.vue'
  import { ElButton, ElSpace, ElTag } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'OmsOrderReturn' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    orderSn: undefined as string | undefined,
    memberUsername: undefined as string | undefined,
    status: undefined as number | undefined
  })

  const detailVisible = ref(false)
  const auditVisible = ref(false)
  const currentReturnId = ref<number>()

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
      apiFn: fetchReturnList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'orderSn', label: '订单号', minWidth: 160, showOverflowTooltip: true },
        { prop: 'memberUsername', label: '会员', minWidth: 100 },
        { prop: 'skuName', label: '商品', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'returnAmount',
          label: '退款金额',
          width: 100,
          formatter: (row) => (row.returnAmount != null ? `¥${Number(row.returnAmount).toFixed(2)}` : '-')
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const type =
              row.status === 2 ? 'success' : row.status === 3 ? 'danger' : row.status === 1 ? 'warning' : 'info'
            return h(ElTag, { type, size: 'small' }, () =>
              RETURN_STATUS_MAP[row.status ?? 0] ?? row.status
            )
          }
        },
        { prop: 'createTime', label: '申请时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:return:query')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => openDetail(row.id) },
                  () => '详情'
                )
              )
            }
            if (hasAuth('mall:return:audit') && row.status === 0) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'success', onClick: () => openAudit(row.id) },
                  () => '审核'
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
    currentReturnId.value = id
    detailVisible.value = true
  }

  function openAudit(id?: number) {
    currentReturnId.value = id
    auditVisible.value = true
  }
</script>

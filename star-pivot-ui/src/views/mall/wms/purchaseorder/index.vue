<!-- 采购单 -->
<template>
  <div class="purchase-order-page art-full-height">
    <PurchaseOrderSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElTooltip
              content="仅「审批已通过」且业务状态为「新建/已分配」的采购单可领取"
              placement="top"
            >
              <ElButton
                v-auth="'mall:purchase:edit'"
                v-ripple
                :disabled="selectedRows.length === 0"
                type="primary"
                @click="handleReceive"
              >
                领取采购单
              </ElButton>
            </ElTooltip>
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

    <PurchaseDetailDrawer
      v-model:visible="detailVisible"
      :purchase-id="currentPurchaseId"
      @changed="refreshData"
    />
    <PurchaseDoneDialog
      v-model:visible="doneVisible"
      :purchase-id="currentPurchaseId"
      @submit="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {useTable} from '@/hooks/core/useTable'
import {
  canReceivePurchase,
  fetchPurchaseList,
  fetchPurchaseReceived,
  PURCHASE_AUDIT_STATUS_MAP,
  PURCHASE_STATUS_MAP,
  type PurchaseVo
} from '@/api/mall/purchase'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import PurchaseOrderSearch from './modules/purchase-order-search.vue'
import PurchaseDetailDrawer from './modules/purchase-detail-drawer.vue'
import PurchaseDoneDialog from './modules/purchase-done-dialog.vue'
import {ElButton, ElMessage, ElMessageBox, ElSpace, ElTag, ElTooltip} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'PurchaseOrder' })

  const route = useRoute()
  const { hasAuth } = useAuth()

  const formatTime = (t?: string) => (t ? t.replace('T', ' ').slice(0, 19) : '-')

  const searchForm = ref({
    assigneeName: undefined as string | undefined,
    status: undefined as number | undefined,
    wareId: undefined as number | undefined
  })

  const selectedRows = ref<PurchaseVo[]>([])
  const detailVisible = ref(false)
  const doneVisible = ref(false)
  const currentPurchaseId = ref<number>()

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
      apiFn: fetchPurchaseList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        {
          type: 'selection',
          selectable: (row: PurchaseVo) => canReceivePurchase(row)
        },
        { prop: 'id', label: 'ID', width: 80 },
        {
          prop: 'assigneeName',
          label: '采购人',
          minWidth: 100,
          showOverflowTooltip: true,
          formatter: (row) => row.assigneeName || '-'
        },
        { prop: 'phone', label: '电话', width: 120, formatter: (row) => row.phone || '-' },
        {
          prop: 'wareName',
          label: '仓库',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row) => row.wareName || (row.wareId != null ? `仓库#${row.wareId}` : '-')
        },
        {
          prop: 'amount',
          label: '金额',
          width: 120,
          formatter: (row) => (row.amount != null && row.amount !== '' ? row.amount : '-')
        },
        {
          prop: 'auditStatus',
          label: '审批',
          width: 100,
          formatter: (row) => {
            const label = PURCHASE_AUDIT_STATUS_MAP[row.auditStatus || ''] || row.auditStatus || '-'
            const type =
              row.auditStatus === 'APPROVED'
                ? 'success'
                : row.auditStatus === 'REJECTED'
                  ? 'danger'
                  : row.auditStatus === 'PENDING'
                    ? 'warning'
                    : 'info'
            return h(ElTag, { type, size: 'small' }, () => label)
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const type = row.status === 3 ? 'success' : row.status === 4 ? 'danger' : 'info'
            return h(
              ElTag,
              { type, size: 'small' },
              () => PURCHASE_STATUS_MAP[row.status ?? 0] ?? row.status
            )
          }
        },
        {
          prop: 'createTime',
          label: '创建时间',
          minWidth: 160,
          formatter: (row) => formatTime(row.createTime)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 200,
          fixed: 'right',
          formatter: (row) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:purchase:query')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => openDetail(row.id) },
                  () => '详情'
                )
              )
            }
            if (hasAuth('mall:purchase:edit') && row.status === 2) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'success', onClick: () => openDone(row.id) },
                  () => '完成采购'
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

  function handleSelectionChange(selection: PurchaseVo[]) {
    selectedRows.value = selection
  }

  function openDetail(id?: number) {
    currentPurchaseId.value = id
    detailVisible.value = true
  }

  onMounted(() => {
    const raw = route.query.openId
    if (raw && /^\d+$/.test(String(raw))) {
      openDetail(Number(raw))
    }
  })

  function openDone(id?: number) {
    currentPurchaseId.value = id
    doneVisible.value = true
  }

  async function handleReceive() {
    const ids = selectedRows.value.map((r) => r.id).filter((id): id is number => id != null)
    if (!ids.length) {
      ElMessage.warning('请选择已审批通过且处于新建/已分配状态的采购单')
      return
    }
    await ElMessageBox.confirm(`确定领取 ${ids.length} 个采购单？`, '领取采购单', { type: 'info' })
    await fetchPurchaseReceived(ids)
    selectedRows.value = []
    refreshData()
  }
</script>

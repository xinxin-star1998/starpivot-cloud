<!-- 会员收货地址 -->
<template>
  <div class="member-receive-address-page art-full-height">
    <ReceiveAddressSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

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
import { fetchMemberReceiveAddressList } from '@/api/mall/member-receive-address'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ReceiveAddressSearch from './modules/receive-address-search.vue'

defineOptions({ name: 'MemberReceiveAddress' })

const searchForm = ref({
  memberId: undefined as number | undefined,
  name: undefined as string | undefined,
  phone: undefined as string | undefined,
  province: undefined as string | undefined,
  defaultStatus: undefined as number | undefined
})

function normalizeParams(params: Record<string, unknown>) {
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
  if (!next.name) delete next.name
  if (!next.phone) delete next.phone
  if (!next.province) delete next.province
  if (next.defaultStatus === '' || next.defaultStatus == null) delete next.defaultStatus
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
    apiFn: fetchMemberReceiveAddressList,
    apiParams: { pageNum: 1, pageSize: 10 },
    columnsFactory: () => [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'memberId', label: '会员 ID', width: 90 },
      { prop: 'memberUsername', label: '用户名', minWidth: 110 },
      { prop: 'memberNickname', label: '昵称', minWidth: 110 },
      { prop: 'name', label: '收货人', minWidth: 100 },
      { prop: 'phone', label: '手机号', minWidth: 120 },
      {
        prop: 'regionText',
        label: '省市区',
        minWidth: 200,
        formatter: (row) => [row.province, row.city, row.region].filter(Boolean).join(' / ')
      },
      { prop: 'detailAddress', label: '详细地址', minWidth: 180 },
      { prop: 'defaultStatusLabel', label: '默认', width: 80 }
    ]
  },
  hooks: {
    beforeRequest: (params) => normalizeParams(params)
  }
})

function handleSearch(params: Record<string, unknown>) {
  Object.assign(searchParams, normalizeParams(params))
  getData()
}

function handleReset() {
  resetSearchParams()
  getData()
}
</script>

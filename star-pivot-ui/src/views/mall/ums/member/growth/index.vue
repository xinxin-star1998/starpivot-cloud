<!-- 积分 / 成长值变动记录 -->
<template>
  <div class="member-growth-page art-full-height">
    <GrowthSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ElTabs v-model="activeTab" class="growth-tabs">
        <ElTabPane label="积分变动" name="integration" />
        <ElTabPane label="成长值变动" name="growth" />
      </ElTabs>

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
    CHANGE_SOURCE_TYPE_MAP,
    fetchGrowthHistoryList,
    fetchIntegrationHistoryList,
    type MemberGrowthListParams
  } from '@/api/mall/member-growth'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import GrowthSearch from './modules/growth-search.vue'
  import { ElTag } from 'element-plus'

  defineOptions({ name: 'MemberGrowth' })

  const activeTab = ref<'integration' | 'growth'>('integration')

  const searchForm = ref({
    memberId: undefined as number | undefined
  })

  function normalizeParams(params: Record<string, unknown>) {
    const next = { ...params }
    const raw = next.memberId
    if (raw === '' || raw == null) {
      delete next.memberId
    } else {
      const memberId = Number(raw)
      if (Number.isFinite(memberId) && memberId > 0) {
        next.memberId = memberId
      } else {
        delete next.memberId
      }
    }
    return next
  }

  function fetchHistoryPage(params: MemberGrowthListParams) {
    return activeTab.value === 'integration'
      ? fetchIntegrationHistoryList(params)
      : fetchGrowthHistoryList(params)
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
      apiFn: fetchHistoryPage,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'memberId', label: '会员 ID', width: 100 },
        {
          prop: 'changeCount',
          label: '变动数量',
          width: 100,
          formatter: (row) => {
            const count = row.changeCount ?? 0
            const type = count >= 0 ? 'success' : 'danger'
            const prefix = count >= 0 ? '+' : ''
            return h(ElTag, { type, size: 'small' }, () => `${prefix}${count}`)
          }
        },
        {
          prop: 'sourceType',
          label: '来源',
          width: 110,
          formatter: (row) => CHANGE_SOURCE_TYPE_MAP[row.sourceType ?? 0] ?? row.sourceType ?? '-'
        },
        { prop: 'note', label: '备注', minWidth: 160, showOverflowTooltip: true },
        { prop: 'createTime', label: '时间', minWidth: 160 }
      ]
    }
  })

  watch(activeTab, () => {
    Object.assign(searchParams, { pageNum: 1 })
    getData()
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeParams(params), { pageNum: 1 })
    getData()
  }

  function handleReset() {
    searchForm.value = { memberId: undefined }
    resetSearchParams()
    getData()
  }
</script>

<style scoped>
  .growth-tabs {
    margin-bottom: 12px;
  }
</style>

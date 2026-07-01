<!-- 会员收藏 -->
<template>
  <div class="member-collect-page art-full-height">
    <CollectSearch v-model="searchForm" :active-tab="activeTab" @search="handleSearch" @reset="handleReset" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ElTabs v-model="activeTab" class="collect-tabs" @tab-change="handleTabChange">
        <ElTabPane label="商品收藏" name="spu" />
        <ElTabPane label="专题收藏" name="subject" />
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
import {useTable} from '@/hooks/core/useTable'
import {
  fetchMemberCollectSpuList,
  fetchMemberCollectSubjectList,
  type MemberCollectListParams
} from '@/api/mall/member-collect'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import CollectSearch from './modules/collect-search.vue'

defineOptions({ name: 'MemberCollect' })

  const activeTab = ref<'spu' | 'subject'>('spu')

  const searchForm = ref({
    memberId: undefined as number | undefined,
    spuName: undefined as string | undefined,
    subjectName: undefined as string | undefined
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
    if (!next.spuName) delete next.spuName
    if (!next.subjectName) delete next.subjectName
    return next
  }

  function fetchPage(params: MemberCollectListParams) {
    return activeTab.value === 'spu'
      ? fetchMemberCollectSpuList(params)
      : fetchMemberCollectSubjectList(params)
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
      apiFn: fetchPage,
      apiParams: { pageNum: 1, pageSize: 10 },
      columnsFactory: () =>
        activeTab.value === 'spu'
          ? [
              { prop: 'id', label: 'ID', width: 80 },
              { prop: 'memberId', label: '会员 ID', width: 90 },
              { prop: 'memberUsername', label: '用户名', minWidth: 110 },
              { prop: 'spuId', label: 'SPU ID', width: 90 },
              { prop: 'spuName', label: '商品名称', minWidth: 180 },
              { prop: 'createTime', label: '收藏时间', minWidth: 170 }
            ]
          : [
              { prop: 'id', label: 'ID', width: 80 },
              { prop: 'memberId', label: '会员 ID', width: 90 },
              { prop: 'memberUsername', label: '用户名', minWidth: 110 },
              { prop: 'subjectId', label: '专题 ID', width: 90 },
              { prop: 'subjectName', label: '专题名称', minWidth: 180 },
              { prop: 'subjectUrl', label: '链接', minWidth: 160 }
            ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeParams(params))
    getData()
  }

  function handleReset() {
    searchForm.value = { memberId: undefined, spuName: undefined, subjectName: undefined }
    resetSearchParams()
    getData()
  }

  function handleTabChange() {
    const spuColumns = [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'memberId', label: '会员 ID', width: 90 },
      { prop: 'memberUsername', label: '用户名', minWidth: 110 },
      { prop: 'spuId', label: 'SPU ID', width: 90 },
      { prop: 'spuName', label: '商品名称', minWidth: 180 },
      { prop: 'createTime', label: '收藏时间', minWidth: 170 }
    ]
    const subjectColumns = [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'memberId', label: '会员 ID', width: 90 },
      { prop: 'memberUsername', label: '用户名', minWidth: 110 },
      { prop: 'subjectId', label: '专题 ID', width: 90 },
      { prop: 'subjectName', label: '专题名称', minWidth: 180 },
      { prop: 'subjectUrl', label: '链接', minWidth: 160 }
    ]
    columns.value = activeTab.value === 'spu' ? spuColumns : subjectColumns
    resetSearchParams()
    getData()
  }
</script>

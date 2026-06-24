<!-- 会员列表 -->
<template>
  <div class="member-list-page art-full-height">
    <MemberSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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

    <MemberDetailDrawer v-model:visible="detailVisible" :member-id="currentMemberId" />
    <MemberEditDialog
      v-model:visible="editVisible"
      :member-id="currentMemberId"
      @submit="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchMemberList,
    MEMBER_STATUS_MAP,
    type MemberVo
  } from '@/api/mall/member'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import MemberSearch from './modules/member-search.vue'
  import MemberDetailDrawer from './modules/member-detail-drawer.vue'
  import MemberEditDialog from './modules/member-edit-dialog.vue'
  import { ElButton, ElSpace, ElTag } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'MemberList' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    username: undefined as string | undefined,
    mobile: undefined as string | undefined,
    status: undefined as number | undefined
  })

  const detailVisible = ref(false)
  const editVisible = ref(false)
  const currentMemberId = ref<number>()

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
      apiFn: fetchMemberList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'username', label: '用户名', minWidth: 110, showOverflowTooltip: true },
        { prop: 'nickname', label: '昵称', minWidth: 110, showOverflowTooltip: true },
        { prop: 'mobile', label: '手机号', width: 120 },
        { prop: 'integration', label: '积分', width: 90 },
        { prop: 'growth', label: '成长值', width: 90 },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row) =>
            h(
              ElTag,
              { type: row.status === 1 ? 'success' : 'info', size: 'small' },
              () => MEMBER_STATUS_MAP[row.status ?? 0] ?? row.status
            )
        },
        { prop: 'createTime', label: '注册时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 140,
          fixed: 'right',
          formatter: (row: MemberVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:member:query')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => openDetail(row.id) },
                  () => '详情'
                )
              )
            }
            if (hasAuth('mall:member:edit')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'success', onClick: () => openEdit(row.id) },
                  () => '编辑'
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
    currentMemberId.value = id
    detailVisible.value = true
  }

  function openEdit(id?: number) {
    currentMemberId.value = id
    editVisible.value = true
  }
</script>

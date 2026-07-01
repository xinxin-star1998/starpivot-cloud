<!-- 会员登录日志 -->
<template>
  <div class="member-login-log-page art-full-height">
    <LoginLogSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

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
import {useTable} from '@/hooks/core/useTable'
import {fetchMemberLoginLogList, MEMBER_LOGIN_TYPE_MAP} from '@/api/mall/member-login-log'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import LoginLogSearch from './modules/login-log-search.vue'

defineOptions({ name: 'MemberLoginLog' })

  const searchForm = ref({
    memberId: undefined as number | undefined,
    loginType: undefined as number | undefined,
    ip: undefined as string | undefined
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
    if (next.loginType === '' || next.loginType == null) delete next.loginType
    if (!next.ip) delete next.ip
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
      apiFn: fetchMemberLoginLogList,
      apiParams: { pageNum: 1, pageSize: 10 },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'memberId', label: '会员 ID', width: 90 },
        { prop: 'memberUsername', label: '用户名', minWidth: 110 },
        { prop: 'memberNickname', label: '昵称', minWidth: 110 },
        {
          prop: 'loginType',
          label: '登录方式',
          width: 100,
          formatter: (row) =>
            row.loginTypeLabel ||
            MEMBER_LOGIN_TYPE_MAP[row.loginType ?? 0] ||
            String(row.loginType ?? '-')
        },
        { prop: 'ip', label: 'IP', minWidth: 130 },
        { prop: 'city', label: '城市', width: 100 },
        { prop: 'createTime', label: '登录时间', minWidth: 170 }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeParams(params))
    getData()
  }

  function handleReset() {
    searchForm.value = { memberId: undefined, loginType: undefined, ip: undefined }
    resetSearchParams()
    getData()
  }
</script>

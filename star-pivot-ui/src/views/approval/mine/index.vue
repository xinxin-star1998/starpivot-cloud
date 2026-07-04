<!-- 审批中心-我发起的 -->
<template>
  <div class="approval-mine-page art-full-height">
    <MineSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />

      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ApprovalTimelineDialog
      v-model:instance-id="progressInstanceId"
      v-model:visible="progressVisible"
    />
  </div>
</template>

<script lang="ts" setup>
import {h} from 'vue'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import {useAuth} from '@/hooks/core/useAuth'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {fetchApprovalMineList, fetchApprovalWithdraw} from '@/api/approval/instance'
import type {ApInstanceVo} from '@/api/approval/types'
import {instanceStatusLabel, instanceStatusTagType} from '../utils/approval-labels'
import {handleMutationError} from '@/utils/http/mutation'
import MineSearch from './modules/mine-search.vue'
import ApprovalTimelineDialog from '../components/ApprovalTimelineDialog.vue'
import {useApprovalTimelineDialog} from '../composables/useApprovalTimelineDialog'

defineOptions({ name: 'ApprovalMine' })

  const { hasAuth } = useAuth()
  const { progressVisible, progressInstanceId, openTimelineDialog } = useApprovalTimelineDialog()

  const searchForm = ref({
    title: undefined as string | undefined,
    status: undefined as string | undefined
  })

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
      apiFn: fetchApprovalMineList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'title',
          label: '标题',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: ApInstanceVo) =>
            h(
              'span',
              {
                class: 'link-title',
                onClick: () => openTimelineDialog(row.instanceId)
              },
              row.title || '-'
            )
        },
        { prop: 'templateCode', label: '模板', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'bizKey',
          label: '业务键',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: ApInstanceVo) =>
            h(ElTag, { type: instanceStatusTagType(row.status) }, () =>
              instanceStatusLabel(row.status)
            )
        },
        {
          prop: 'createTime',
          label: '发起时间',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: ApInstanceVo) => {
            const actions: any[] = [
              h(ArtButtonTable, {
                type: 'view',
                label: '进度',
                onClick: () => openTimelineDialog(row.instanceId)
              })
            ]

            if (row.status === 'RUNNING' && hasAuth('approval:instance:withdraw')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'pause',
                  label: '撤回',
                  onClick: () => handleWithdraw(row)
                })
              )
            }

            return h('div', actions)
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, any>) {
    Object.assign(searchParams, params)
    getData()
  }

  async function handleWithdraw(row: ApInstanceVo) {
    if (!row.instanceId) return
    try {
      await ElMessageBox.confirm(`确定要撤回审批「${row.title || row.bizKey}」吗？`, '撤回审批', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchApprovalWithdraw(row.instanceId)
      refreshData()
      ElMessage.success('已撤回')
    } catch (error) {
      handleMutationError(error, '撤回失败')
    }
  }
</script>

<style lang="scss" scoped>
  :deep(.link-title) {
    color: var(--el-color-primary);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
</style>

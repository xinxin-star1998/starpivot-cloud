<!-- 工作流-我发起的 -->
<template>
  <div class="workflow-mine-page art-full-height">
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

    <WorkflowProgressDialog v-model:visible="progressVisible" :instance-id="progressInstanceId" />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { fetchWorkflowCancel, fetchWorkflowMineList, type TaskVo } from '@/api/workflow/task'
  import { instanceStatusLabel, instanceStatusTagType } from '../utils/workflow-labels'
  import MineSearch from './modules/mine-search.vue'
  import WorkflowProgressDialog from '../components/WorkflowProgressDialog.vue'
  import { useWorkflowProgressDialog } from '../composables/useWorkflowProgressDialog'

  defineOptions({ name: 'WorkflowMine' })

  const { hasAuth } = useAuth()
  const { progressVisible, progressInstanceId, openProgressDialog } = useWorkflowProgressDialog()

  const searchForm = ref({ title: undefined as string | undefined })

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
      apiFn: fetchWorkflowMineList,
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
          formatter: (row: TaskVo) =>
            h(
              'span',
              {
                class: 'link-title',
                onClick: () => openProgressDialog(row.instanceId)
              },
              row.title || '-'
            )
        },
        {
          prop: 'processName',
          label: '流程',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'businessKey',
          label: '业务键',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: TaskVo) =>
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
          formatter: (row: TaskVo) => {
            const actions: any[] = [
              h(ArtButtonTable, {
                type: 'view',
                label: '进度',
                onClick: () => openProgressDialog(row.instanceId)
              })
            ]

            if (row.status === 'RUNNING' && hasAuth('workflow:instance:cancel')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'pause',
                  label: '撤销',
                  onClick: () => handleCancel(row)
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

  async function handleCancel(row: TaskVo) {
    if (!row.instanceId) return
    try {
      await ElMessageBox.confirm(
        `确定要撤销流程「${row.title || row.businessKey}」吗？`,
        '撤销流程',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchWorkflowCancel(row.instanceId)
      refreshData()
      ElMessage.success('已撤销')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('撤销失败')
      }
    }
  }
</script>

<style scoped lang="scss">
  :deep(.link-title) {
    color: var(--el-color-primary);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
</style>

<!-- 工作流-待办审批 -->
<template>
  <div class="workflow-approval-page art-full-height">
    <ApprovalSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <ElTabs v-model="activeTab" class="approval-tabs" @tab-change="handleTabChange">
        <ElTabPane label="待办" name="todo" />
        <ElTabPane label="已办" name="done" />
      </ElTabs>

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

    <ApprovalActionDialog
      v-model:visible="dialogVisible"
      v-model:comment="comment"
      :title="dialogTitle"
      :submitting="submitting"
      :instance-id="currentTask?.instanceId"
      :task-title="currentTask?.title"
      :task-meta="approvalTaskMeta"
      @submit="submitAction"
      @closed="currentTask = null"
      @view-progress="openProgressFromApproval"
    />

    <WorkflowProgressDialog v-model:visible="progressVisible" :instance-id="progressInstanceId" />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import {
    fetchWorkflowApprove,
    fetchWorkflowDoneList,
    fetchWorkflowReject,
    fetchWorkflowTodoList,
    type TaskVo
  } from '@/api/workflow/task'
  import ApprovalSearch from './modules/approval-search.vue'
  import ApprovalActionDialog from './modules/approval-action-dialog.vue'
  import WorkflowProgressDialog from '../components/WorkflowProgressDialog.vue'
  import { useWorkflowProgressDialog } from '../composables/useWorkflowProgressDialog'

  defineOptions({ name: 'WorkflowApproval' })

  const { hasAuth } = useAuth()
  const { progressVisible, progressInstanceId, openProgressDialog } = useWorkflowProgressDialog()

  const activeTab = ref<'todo' | 'done'>('todo')
  const searchForm = ref({ title: undefined as string | undefined })

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const dialogMode = ref<'approve' | 'reject'>('approve')
  const currentTask = ref<TaskVo | null>(null)
  const comment = ref('')
  const submitting = ref(false)

  const approvalTaskMeta = computed(() => {
    const task = currentTask.value
    if (!task) return []
    return [
      { label: '流程', value: task.processName },
      { label: '当前节点', value: task.nodeName },
      { label: '发起人', value: task.starterName },
      { label: '业务键', value: task.businessKey }
    ].filter((item) => item.value)
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
    refreshData,
    resetColumns
  } = useTable({
    core: {
      apiFn: (params: any) =>
        activeTab.value === 'todo' ? fetchWorkflowTodoList(params) : fetchWorkflowDoneList(params),
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => {
        const cols: any[] = [
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
          { prop: 'nodeName', label: '当前节点', width: 120 },
          { prop: 'starterName', label: '发起人', width: 100 },
          {
            prop: 'businessKey',
            label: '业务键',
            minWidth: 160,
            showOverflowTooltip: true
          },
          {
            prop: 'createTime',
            label: activeTab.value === 'todo' ? '到达时间' : '处理时间',
            minWidth: 160,
            showOverflowTooltip: true
          }
        ]

        cols.push({
          prop: 'operation',
          label: '操作',
          width: activeTab.value === 'todo' ? 200 : 100,
          fixed: 'right',
          formatter: (row: TaskVo) => {
            const actions: any[] = [
              h(ArtButtonTable, {
                type: 'view',
                label: '进度',
                onClick: () => openProgressDialog(row.instanceId)
              })
            ]

            if (activeTab.value === 'todo' && hasAuth('workflow:task:approve')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'execute',
                  label: '通过',
                  onClick: () => openApprove(row)
                }),
                h(ArtButtonTable, {
                  type: 'delete',
                  label: '驳回',
                  onClick: () => openReject(row)
                })
              )
            }

            return h('div', actions)
          }
        })

        return cols
      }
    }
  })

  function handleSearch(params: Record<string, any>) {
    Object.assign(searchParams, params)
    getData()
  }

  function handleTabChange() {
    resetColumns?.()
    Object.assign(searchParams, { pageNum: 1 })
    getData()
  }

  function openApprove(row: TaskVo) {
    currentTask.value = row
    dialogMode.value = 'approve'
    dialogTitle.value = '审批通过'
    comment.value = ''
    dialogVisible.value = true
  }

  function openReject(row: TaskVo) {
    currentTask.value = row
    dialogMode.value = 'reject'
    dialogTitle.value = '审批驳回'
    comment.value = ''
    dialogVisible.value = true
  }

  function openProgressFromApproval() {
    if (currentTask.value?.instanceId) {
      openProgressDialog(currentTask.value.instanceId)
    }
  }

  async function submitAction() {
    if (!currentTask.value?.taskId) return
    submitting.value = true
    try {
      const payload = { taskId: currentTask.value.taskId, comment: comment.value }
      if (dialogMode.value === 'approve') {
        await fetchWorkflowApprove(payload)
        ElMessage.success('审批通过')
      } else {
        await fetchWorkflowReject(payload)
        ElMessage.success('已驳回')
      }
      dialogVisible.value = false
      refreshData()
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .approval-tabs {
    padding: 0 4px;

    :deep(.el-tabs__header) {
      margin-bottom: 12px;
    }
  }

  :deep(.link-title) {
    color: var(--el-color-primary);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
</style>

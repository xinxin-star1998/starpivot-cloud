<!-- 审批中心-待办审批 -->
<template>
  <div class="approval-todo-page art-full-height">
    <ApprovalSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <div class="todo-toolbar">
        <ElTabs v-model="activeTab" class="approval-tabs" @tab-change="handleTabChange">
          <ElTabPane label="待办" name="todo" />
          <ElTabPane label="已办" name="done" />
        </ElTabs>
        <ElBadge :hidden="!notifyUnread" :max="99" :value="notifyUnread">
          <ElButton @click="notifyVisible = true">通知</ElButton>
        </ElBadge>
      </div>

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
      v-model:comment="comment"
      v-model:visible="dialogVisible"
      :mode="dialogMode"
      :biz-key="currentTask?.bizKey"
      :biz-module="currentTask?.bizModule"
      :biz-type="currentTask?.bizType"
      :instance-id="currentTask?.instanceId"
      :submitting="submitting"
      :task-meta="approvalTaskMeta"
      :task-title="currentTask?.title"
      :title="dialogTitle"
      @closed="currentTask = null"
      @submit="submitAction"
    />

    <ApprovalTimelineDialog
      v-model:instance-id="progressInstanceId"
      v-model:visible="progressVisible"
    />

    <ApprovalNotificationDrawer
      ref="notifyDrawerRef"
      v-model:visible="notifyVisible"
      @navigate="openTimelineDialog"
      @unread-change="handleNotifyUnreadChange"
    />
  </div>
</template>

<script lang="ts" setup>
import {h} from 'vue'
import {ElMessage} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import {useAuth} from '@/hooks/core/useAuth'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchApprovalApprove,
  fetchApprovalDoneList,
  fetchApprovalReject,
  fetchApprovalTodoList
} from '@/api/approval/task'
import type {ApTaskVo} from '@/api/approval/types'
import ApprovalSearch from './modules/approval-search.vue'
import ApprovalActionDialog from './modules/approval-action-dialog.vue'
import {handleMutationError} from '@/utils/http/mutation'
import ApprovalTimelineDialog from '../components/ApprovalTimelineDialog.vue'
import ApprovalNotificationDrawer from '../components/ApprovalNotificationDrawer.vue'
import {useApprovalTimelineDialog} from '../composables/useApprovalTimelineDialog'
import {fetchApprovalUnreadCount} from '@/api/approval/notification'

defineOptions({ name: 'ApprovalTodo' })

  const { hasAuth } = useAuth()
  const { progressVisible, progressInstanceId, openTimelineDialog } = useApprovalTimelineDialog()

  const notifyVisible = ref(false)
  const notifyUnread = ref(0)
  const notifyDrawerRef = ref<InstanceType<typeof ApprovalNotificationDrawer>>()

  async function refreshNotifyCount() {
    try {
      notifyUnread.value = Number(await fetchApprovalUnreadCount()) || 0
    } catch {
      notifyUnread.value = 0
    }
  }

  function handleNotifyUnreadChange(count: number) {
    notifyUnread.value = Math.max(0, Number(count) || 0)
  }

  watch(notifyVisible, (open, wasOpen) => {
    if (!open && wasOpen) {
      refreshNotifyCount()
    }
  })

  onMounted(() => {
    refreshNotifyCount()
  })

  const activeTab = ref<'todo' | 'done'>('todo')
  const searchForm = ref({
    title: undefined as string | undefined,
    bizModule: undefined as string | undefined,
    bizType: undefined as string | undefined
  })

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const dialogMode = ref<'approve' | 'reject'>('approve')
  const currentTask = ref<ApTaskVo | null>(null)
  const comment = ref('')
  const submitting = ref(false)

  const approvalTaskMeta = computed(() => {
    const task = currentTask.value
    if (!task) return []
    return [
      { label: '当前步骤', value: task.stepName },
      { label: '业务域', value: task.bizModule },
      { label: '单据类型', value: task.bizType },
      { label: '业务键', value: task.bizKey }
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
        activeTab.value === 'todo' ? fetchApprovalTodoList(params) : fetchApprovalDoneList(params),
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
            formatter: (row: ApTaskVo) =>
              h(
                'span',
                {
                  class: 'link-title',
                  onClick: () => openTimelineDialog(row.instanceId)
                },
                row.title || '-'
              )
          },
          { prop: 'stepName', label: '当前步骤', width: 120 },
          { prop: 'bizModule', label: '业务域', width: 90 },
          { prop: 'bizType', label: '单据类型', width: 100 },
          {
            prop: 'bizKey',
            label: '业务键',
            minWidth: 160,
            showOverflowTooltip: true
          },
          {
            prop: 'createTime',
            label: activeTab.value === 'todo' ? '到达时间' : '处理时间',
            minWidth: 160,
            showOverflowTooltip: true,
            formatter: (row: ApTaskVo) =>
              activeTab.value === 'todo' ? row.createTime : row.finishTime || row.createTime
          }
        ]

        cols.push({
          prop: 'operation',
          label: '操作',
          width: activeTab.value === 'todo' ? 200 : 100,
          fixed: 'right',
          formatter: (row: ApTaskVo) => {
            const actions: any[] = [
              h(ArtButtonTable, {
                type: 'view',
                label: '进度',
                onClick: () => openTimelineDialog(row.instanceId)
              })
            ]

            if (activeTab.value === 'todo' && hasAuth('approval:task:action')) {
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

  function openApprove(row: ApTaskVo) {
    currentTask.value = row
    dialogMode.value = 'approve'
    dialogTitle.value = '审批通过'
    comment.value = ''
    dialogVisible.value = true
  }

  function openReject(row: ApTaskVo) {
    currentTask.value = row
    dialogMode.value = 'reject'
    dialogTitle.value = '审批驳回'
    comment.value = ''
    dialogVisible.value = true
  }

  async function submitAction() {
    if (!currentTask.value?.taskId) return
    if (dialogMode.value === 'reject' && !comment.value?.trim()) {
      ElMessage.warning('驳回时必须填写审批意见')
      return
    }
    submitting.value = true
    try {
      const payload = { taskId: currentTask.value.taskId, comment: comment.value }
      if (dialogMode.value === 'approve') {
        await fetchApprovalApprove(payload)
        ElMessage.success('审批通过')
      } else {
        await fetchApprovalReject(payload)
        ElMessage.success('已驳回')
      }
      dialogVisible.value = false
      refreshData()
      refreshNotifyCount()
      notifyDrawerRef.value?.refresh()
    } catch (error) {
      handleMutationError(error, dialogMode.value === 'approve' ? '审批通过失败' : '审批驳回失败')
    } finally {
      submitting.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .approval-tabs {
    flex: 1;
    padding: 0 4px;

    :deep(.el-tabs__header) {
      margin-bottom: 12px;
    }
  }

  .todo-toolbar {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    justify-content: space-between;
  }

  :deep(.link-title) {
    color: var(--el-color-primary);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
</style>

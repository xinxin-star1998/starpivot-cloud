<template>
  <div class="job-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'monitor:job:add'" @click="showDialog('add')">新增任务</ElButton>
            <ElButton
              v-auth="'monitor:job:delete'"
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
            >
              批量删除
            </ElButton>
            <ElButton v-auth="'monitor:job:query'" @click="showLogDialog">执行日志</ElButton>
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

      <JobDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :job-data="currentJob"
        @submit="handleDialogSubmit"
      />
      <JobLogDialog v-model="logDialogVisible" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {useTable} from '@/hooks/core/useTable'
import {fetchChangeJobStatus, fetchDeleteJob, fetchJobList, fetchRunJobOnce, type SysJob} from '@/api/monitor/job'
import JobDialog from './modules/job-dialog.vue'
import JobLogDialog from './modules/job-log-dialog.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useAuth} from '@/hooks/core/useAuth'
import {ElMessage, ElMessageBox} from 'element-plus'
import type {DialogType} from '@/types'

defineOptions({ name: 'MonitorJob' })

  const { hasAuth } = useAuth()

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const logDialogVisible = ref(false)
  const currentJob = ref<Partial<SysJob>>({})
  const selectedRows = ref<SysJob[]>([])

  const searchForm = ref({
    jobName: undefined as string | undefined,
    jobGroup: undefined as string | undefined,
    status: undefined as string | undefined
  })

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchJobList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      immediate: true,
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        { prop: 'jobName', label: '任务名称', minWidth: 120, showOverflowTooltip: true },
        { prop: 'jobGroup', label: '任务组', width: 90 },
        { prop: 'invokeTarget', label: '调用目标', minWidth: 200, showOverflowTooltip: true },
        { prop: 'cronExpression', label: 'Cron 表达式', width: 130 },
        {
          prop: 'status',
          label: '状态',
          width: 80,
          formatter: (row: SysJob) => (row.status === '0' ? '正常' : '暂停')
        },
        { prop: 'createTime', label: '创建时间', width: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 300,
          fixed: 'right',
          formatter: (row: SysJob) => {
            try {
              const actions: any[] = []
              if (hasAuth('monitor:job:edit')) {
                actions.push(
                  h(ArtButtonTable, {
                    type: 'edit',
                    label: '编辑',
                    onClick: () => showDialog('edit', row)
                  })
                )
                actions.push(
                  h(ArtButtonTable, {
                    type: row.status === '0' ? 'pause' : 'resume',
                    label: row.status === '0' ? '暂停' : '恢复',
                    onClick: () => toggleStatus(row)
                  })
                )
                actions.push(
                  h(ArtButtonTable, {
                    type: 'execute',
                    label: '执行',
                    onClick: () => runOnce(row)
                  })
                )
              }
              if (hasAuth('monitor:job:delete')) {
                actions.push(
                  h(ArtButtonTable, {
                    type: 'delete',
                    label: '删除',
                    onClick: () => deleteOne(row)
                  })
                )
              }
              return actions.length ? h('div', actions) : h('span', { style: 'color:#999' }, '-')
            } catch (e) {
              console.error('[MonitorJob] operation formatter error', e)
              return h('span', { style: 'color:#999' }, '-')
            }
          }
        }
      ]
    }
  })

  const showDialog = (type: DialogType, row?: SysJob) => {
    dialogType.value = type
    currentJob.value = row ? { ...row } : {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const showLogDialog = () => {
    logDialogVisible.value = true
  }

  const handleDialogSubmit = () => {
    dialogVisible.value = false
    currentJob.value = {}
    refreshData()
  }

  const toggleStatus = async (row: SysJob) => {
    const jobId = row.jobId
    if (jobId == null) return
    const newStatus = row.status === '0' ? '1' : '0'
    await fetchChangeJobStatus(jobId, newStatus)
    ElMessage.success('操作成功')
    refreshData()
  }

  const runOnce = async (row: SysJob) => {
    if (row.jobId == null) return
    await fetchRunJobOnce(row.jobId)
    ElMessage.success('已触发执行')
    refreshData()
  }

  const deleteOne = async (row: SysJob) => {
    await ElMessageBox.confirm('确定删除该定时任务吗？', '提示', { type: 'warning' })
    if (row.jobId == null) return
    await fetchDeleteJob([row.jobId])
    ElMessage.success('删除成功')
    refreshData()
  }

  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的任务')
      return
    }
    const ids = selectedRows.value.map((r) => r.jobId).filter((id): id is number => id != null)
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条任务吗？`, '提示', {
      type: 'warning'
    })
    await fetchDeleteJob(ids)
    selectedRows.value = []
    ElMessage.success('删除成功')
    refreshData()
  }

  const handleSelectionChange = (selection: SysJob[]) => {
    selectedRows.value = selection
  }
</script>

<style scoped lang="scss">
  .job-page {
    padding: 20px;
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgb(0 0 0 / 8%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgb(0 0 0 / 12%);
    }
  }

  :deep(.el-table) {
    border-radius: 8px;

    .el-table__header-wrapper {
      th {
        font-weight: 600;
        color: var(--art-gray-800);
        background-color: var(--art-gray-100) !important;
      }
    }

    .el-table__body-wrapper {
      tr {
        transition: all 0.2s ease;

        &:hover > td {
          background-color: var(--art-gray-50) !important;
        }
      }
    }
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }
</style>

<template>
  <ElDialog v-model="visible" title="执行日志" width="800px" align-center>
    <ElTable v-loading="loading" :data="logList" border max-height="400" style="width: 100%">
      <ElTableColumn prop="jobName" label="任务名称" width="120" />
      <ElTableColumn prop="jobGroup" label="任务组" width="80" />
      <ElTableColumn prop="status" label="状态" width="70">
        <template #default="{ row }">
          <ElTag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? '成功' : '失败' }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="jobMessage" label="日志信息" min-width="120" show-overflow-tooltip />
      <ElTableColumn prop="exceptionInfo" label="异常信息" min-width="150" show-overflow-tooltip />
      <ElTableColumn prop="createTime" label="执行时间" width="160" />
    </ElTable>
    <ElPagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="mt-3"
      @current-change="loadLogs"
      @size-change="loadLogs"
    />
    <template #footer>
      <ElButton @click="visible = false">关闭</ElButton>
      <ElButton type="primary" :loading="clearing" @click="handleClear">清空日志</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchJobLogList, fetchClearJobLog, type SysJobLog } from '@/api/monitor/job'

  interface Props {
    modelValue: boolean
    jobName?: string
    jobGroup?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    jobName: '',
    jobGroup: ''
  })

  const emit = defineEmits<{
    (e: 'update:modelValue', v: boolean): void
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
  })

  const loading = ref(false)
  const clearing = ref(false)
  const logList = ref<SysJobLog[]>([])
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const loadLogs = async () => {
    loading.value = true
    try {
      const res = await fetchJobLogList({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        jobName: props.jobName || undefined,
        jobGroup: props.jobGroup || undefined
      })
      logList.value = res?.rows ?? []
      total.value = res?.total ?? 0
    } finally {
      loading.value = false
    }
  }

  const handleClear = async () => {
    await ElMessageBox.confirm('确定清空所有执行日志吗？', '提示', {
      type: 'warning'
    })
    clearing.value = true
    try {
      await fetchClearJobLog()
      ElMessage.success('已清空')
      loadLogs()
    } finally {
      clearing.value = false
    }
  }

  watch(visible, (v) => {
    if (v) {
      pageNum.value = 1
      loadLogs()
    }
  })
</script>

<style scoped lang="scss">
  :deep(.el-dialog) {
    overflow: hidden;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px;
      margin: 0;
      background: linear-gradient(
        135deg,
        var(--el-color-primary-light-9) 0%,
        var(--el-color-primary-light-8) 100%
      );
      border-bottom: 1px solid var(--art-card-border);

      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: var(--art-gray-900);
      }
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      background-color: var(--art-gray-50);
      border-top: 1px solid var(--art-card-border);
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

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }

  :deep(.el-pagination) {
    margin-top: 16px;
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

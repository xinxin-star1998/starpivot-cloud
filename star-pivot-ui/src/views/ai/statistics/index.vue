<template>
  <div class="ai-statistics-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="开始时间">
          <ElDatePicker
            v-model="searchForm.beginTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="开始"
          />
        </ElFormItem>
        <ElFormItem label="结束时间">
          <ElDatePicker
            v-model="searchForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="结束"
          />
        </ElFormItem>
        <ElFormItem label="用户 ID">
          <ElInputNumber v-model="searchForm.userId" :min="1" controls-position="right" class="!w-36" />
        </ElFormItem>
        <ElFormItem label="模型">
          <ElInput v-model="searchForm.model" clearable placeholder="模型名称" class="!w-40" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadAll">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div class="mb-3 grid grid-cols-2 gap-3 md:grid-cols-4">
      <ElCard shadow="never">
        <div class="text-xs text-g-500">总调用次数</div>
        <div class="mt-1 text-2xl font-semibold">{{ summary.totalRequests ?? 0 }}</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="text-xs text-g-500">总 Tokens</div>
        <div class="mt-1 text-2xl font-semibold">{{ summary.totalTokens ?? 0 }}</div>
      </ElCard>
      <ElCard shadow="never">
        <div class="text-xs text-g-500">成功 / 失败</div>
        <div class="mt-1 text-2xl font-semibold">
          {{ summary.successRequests ?? 0 }} / {{ summary.failedRequests ?? 0 }}
        </div>
      </ElCard>
      <ElCard shadow="never">
        <div class="text-xs text-g-500">平均耗时 (ms)</div>
        <div class="mt-1 text-2xl font-semibold">{{ Math.round(summary.avgLatencyMs ?? 0) }}</div>
      </ElCard>
    </div>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadLogs" />
      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue'
import { ElTag } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchAiUsageLogList,
  fetchAiUsageSummary,
  type AiUsageLogItem,
  type AiUsageSummary
} from '@/api/ai/statistics'
import { handleMutationError } from '@/utils/http/mutation'

defineOptions({ name: 'AiStatistics' })

const summary = ref<AiUsageSummary>({})
const searchForm = ref<{
  beginTime?: string
  endTime?: string
  userId?: number
  model?: string
}>({})

const {
  columns,
  columnChecks,
  data,
  loading,
  pagination,
  searchParams,
  getData,
  handleSizeChange,
  handleCurrentChange
} = useTable({
  core: {
    apiFn: fetchAiUsageLogList,
    apiParams: { pageNum: 1, pageSize: 20 },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'createTime', label: '时间', minWidth: 160 },
      { prop: 'userId', label: '用户', width: 80 },
      { prop: 'model', label: '模型', minWidth: 120 },
      { prop: 'requestType', label: '类型', width: 90 },
      { prop: 'totalTokens', label: 'Tokens', width: 90 },
      { prop: 'latencyMs', label: '耗时(ms)', width: 90 },
      {
        prop: 'success',
        label: '结果',
        width: 80,
        formatter: (row: AiUsageLogItem) =>
          h(
            ElTag,
            { type: row.success === '0' ? 'success' : 'danger', size: 'small' },
            () => (row.success === '0' ? '成功' : '失败')
          )
      },
      { prop: 'conversationId', label: '会话 ID', minWidth: 180 }
    ]
  }
})

async function loadSummary(): Promise<void> {
  try {
    summary.value =
      (await fetchAiUsageSummary({
        beginTime: searchForm.value.beginTime,
        endTime: searchForm.value.endTime
      })) || {}
  } catch (error) {
    handleMutationError(error, '加载汇总失败')
  }
}

function loadLogs(): void {
  Object.assign(searchParams, searchForm.value)
  getData()
}

async function loadAll(): Promise<void> {
  await loadSummary()
  loadLogs()
}

function resetSearch(): void {
  searchForm.value = {}
  loadAll()
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped lang="scss">
.ai-statistics-page {
  .search-card {
    margin-bottom: 12px;
  }
}
</style>

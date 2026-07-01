<!-- 审批中心-统计看板 -->
<template>
  <div v-loading="loading" class="approval-statistics-page">
    <StatisticsSearch v-model="searchForm" @reset="handleReset" @search="handleSearch" />

    <ElCard class="overview-card" shadow="never">
      <div class="section-head">
        <span class="section-title">实例概览</span>
        <ElButton :loading="loading" link type="primary" @click="loadData">
          <ArtSvgIcon class="refresh-icon" icon="ri:refresh-line" />
          刷新
        </ElButton>
      </div>
      <ElRow :gutter="16" class="kpi-row">
        <ElCol v-for="card in instanceCards" :key="card.key" :lg="6" :md="12" :sm="12" :xs="24">
          <div class="kpi-card">
            <div class="kpi-main">
              <span class="kpi-label">{{ card.label }}</span>
              <ArtCountTo
                v-if="card.numeric"
                class="kpi-value"
                :class="card.type"
                :decimals="card.decimals ?? 0"
                :duration="1200"
                :target="card.numeric"
              />
              <span v-else class="kpi-value" :class="card.type">{{ card.text }}</span>
            </div>
            <div class="kpi-icon" :class="card.type">
              <ArtSvgIcon :icon="card.icon" />
            </div>
          </div>
        </ElCol>
      </ElRow>

      <div class="section-head section-head--sub">
        <span class="section-title">任务与时效</span>
      </div>
      <ElRow :gutter="16" class="kpi-row">
        <ElCol v-for="card in taskCards" :key="card.key" :lg="6" :md="12" :sm="12" :xs="24">
          <div class="kpi-card">
            <div class="kpi-main">
              <span class="kpi-label">{{ card.label }}</span>
              <ArtCountTo
                v-if="card.numeric != null"
                class="kpi-value"
                :class="card.type"
                :decimals="card.decimals ?? 0"
                :duration="1200"
                :target="card.numeric"
              />
              <span v-else class="kpi-value" :class="card.type">{{ card.text }}</span>
              <span v-if="card.hint" class="kpi-hint">{{ card.hint }}</span>
            </div>
            <div class="kpi-icon" :class="card.type">
              <ArtSvgIcon :icon="card.icon" />
            </div>
          </div>
        </ElCol>
      </ElRow>
    </ElCard>

    <ElRow :gutter="16" class="chart-row">
      <ElCol :lg="15" :xs="24">
        <ElCard class="chart-card" shadow="never">
          <template #header>
            <div class="chart-header">
              <span>近 30 日完结趋势</span>
              <span class="chart-sub">按完结日期统计</span>
            </div>
          </template>
          <ArtLineChart
            v-if="hasDailyData"
            :data="dailyChart.data"
            :height="'300px'"
            :show-area-color="true"
            :show-axis-line="false"
            :x-axis-data="dailyChart.xAxisData"
          />
          <ElEmpty v-else :image-size="88" description="近 30 日暂无完结记录" />
        </ElCard>
      </ElCol>
      <ElCol :lg="9" :xs="24">
        <ElCard class="chart-card" shadow="never">
          <template #header>
            <div class="chart-header">
              <span>实例状态分布</span>
              <span class="chart-sub">当前筛选范围</span>
            </div>
          </template>
          <ArtRingChart
            v-if="hasStatusData"
            :center-text="String(stats?.totalInstances ?? 0)"
            :data="statusRingData"
            :height="'300px'"
            :show-legend="true"
            legend-position="bottom"
          />
          <ElEmpty v-else :image-size="88" description="暂无实例数据" />
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard class="table-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span>业务类型明细</span>
          <span class="chart-sub">Top 20 · 含通过率</span>
        </div>
      </template>
      <ElTable
        :data="stats?.bizTypeStats || []"
        border
        empty-text="暂无业务类型数据"
        size="small"
        stripe
      >
        <ElTableColumn label="业务类型" min-width="140" prop="bizType">
          <template #default="{ row }">
            <div class="biz-type-cell">
              <span class="biz-type-name">{{ bizTypeLabel(row.bizType) }}</span>
              <span class="biz-type-code">{{ row.bizType }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn align="right" label="总量" prop="total" width="88" />
        <ElTableColumn align="right" label="已通过" prop="approved" width="88">
          <template #default="{ row }">
            <span class="text-success">{{ row.approved ?? 0 }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn align="right" label="已驳回" prop="rejected" width="88">
          <template #default="{ row }">
            <span class="text-danger">{{ row.rejected ?? 0 }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="通过率" min-width="180">
          <template #default="{ row }">
            <div class="rate-cell">
              <ElProgress
                :color="rateColor(row.approved, row.total)"
                :percentage="ratePercent(row.approved, row.total)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="rate-text">{{ formatRate(row.approved, row.total) }}</span>
            </div>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import {computed, onMounted, ref} from 'vue'
import {type ApStatisticsVo, fetchApprovalStatistics} from '@/api/approval/statistics'
import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
import StatisticsSearch from './modules/statistics-search.vue'
import {bizTypeLabel} from '../utils/approval-labels'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'ApprovalStatistics' })

  interface KpiCard {
    key: string
    label: string
    icon: string
    type?: string
    numeric?: number
    text?: string
    decimals?: number
    hint?: string
  }

  const loading = ref(false)
  const searchForm = ref({ bizModule: '' })
  const stats = ref<ApStatisticsVo>()

  const instanceCards = computed<KpiCard[]>(() => {
    const s = stats.value
    return [
      {
        key: 'total',
        label: '实例总数',
        icon: 'mdi:file-document-multiple-outline',
        numeric: s?.totalInstances ?? 0
      },
      {
        key: 'running',
        label: '审批中',
        icon: 'mdi:clock-outline',
        type: 'warning',
        numeric: s?.runningCount ?? 0
      },
      {
        key: 'approved',
        label: '已通过',
        icon: 'mdi:check-circle-outline',
        type: 'success',
        numeric: s?.approvedCount ?? 0
      },
      {
        key: 'rejected',
        label: '已驳回',
        icon: 'mdi:close-circle-outline',
        type: 'danger',
        numeric: s?.rejectedCount ?? 0
      }
    ]
  })

  const taskCards = computed<KpiCard[]>(() => {
    const s = stats.value
    const overdue = s?.overdueTaskCount ?? 0
    return [
      {
        key: 'withdrawn',
        label: '已撤回',
        icon: 'mdi:undo',
        type: 'info',
        numeric: s?.withdrawnCount ?? 0
      },
      {
        key: 'pending',
        label: '待办任务',
        icon: 'mdi:clipboard-text-clock-outline',
        numeric: s?.pendingTaskCount ?? 0
      },
      {
        key: 'overdue',
        label: '已超时待办',
        icon: 'mdi:alarm-light-outline',
        type: overdue > 0 ? 'danger' : '',
        numeric: overdue,
        hint: overdue > 0 ? '需尽快处理' : undefined
      },
      {
        key: 'avgHours',
        label: '平均完结时长',
        icon: 'mdi:timer-outline',
        numeric: s?.avgFinishHours != null ? s.avgFinishHours : undefined,
        text: s?.avgFinishHours != null ? undefined : '-',
        decimals: 1,
        hint: s?.avgFinishHours != null ? '小时' : undefined
      }
    ]
  })

  const dailyChart = computed(() => buildDailySeries(stats.value?.dailyFinished || []))

  const hasDailyData = computed(() => dailyChart.value.data.some((n) => n > 0))

  const statusRingData = computed(() => {
    const s = stats.value
    return [
      { name: '已通过', value: s?.approvedCount ?? 0 },
      { name: '审批中', value: s?.runningCount ?? 0 },
      { name: '已驳回', value: s?.rejectedCount ?? 0 },
      { name: '已撤回', value: s?.withdrawnCount ?? 0 }
    ].filter((item) => item.value > 0)
  })

  const hasStatusData = computed(() => statusRingData.value.length > 0)

  function buildDailySeries(items: { day?: string; count?: number }[]) {
    const map = new Map(items.map((i) => [i.day, i.count ?? 0]))
    const xAxisData: string[] = []
    const data: number[] = []
    for (let i = 29; i >= 0; i--) {
      const day = dayjs().subtract(i, 'day').format('YYYY-MM-DD')
      xAxisData.push(day.slice(5))
      data.push(map.get(day) ?? 0)
    }
    return { xAxisData, data }
  }

  function ratePercent(approved?: number, total?: number) {
    if (!total) return 0
    return Math.round(((approved ?? 0) / total) * 100)
  }

  function rateColor(approved?: number, total?: number) {
    const p = ratePercent(approved, total)
    if (p >= 80) return 'var(--el-color-success)'
    if (p >= 50) return 'var(--el-color-warning)'
    return 'var(--el-color-danger)'
  }

  function formatRate(approved?: number, total?: number) {
    if (!total) return '-'
    return `${ratePercent(approved, total)}%`
  }

  async function loadData() {
    loading.value = true
    try {
      const module = searchForm.value.bizModule?.trim()
      stats.value = await fetchApprovalStatistics(module || undefined)
    } catch (error) {
      handleMutationError(error, '加载统计数据失败')
    } finally {
      loading.value = false
    }
  }

  function handleSearch(params: Record<string, unknown>) {
    searchForm.value = { bizModule: String(params.bizModule ?? '') }
    loadData()
  }

  function handleReset() {
    searchForm.value = { bizModule: '' }
    loadData()
  }

  onMounted(() => {
    loadData()
  })
</script>

<style lang="scss" scoped>
  .approval-statistics-page {
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: var(--art-full-height);
    padding: var(--art-page-padding);
    padding-bottom: 24px;
    background-color: var(--default-bg-color);
  }

  .overview-card,
  .table-card,
  .chart-card {
    flex-shrink: 0;
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  .overview-card,
  .table-card {
    :deep(.el-card__body) {
      height: auto;
      padding-top: 8px;
      overflow: visible;
    }
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    &--sub {
      margin-top: 4px;
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .refresh-icon {
    margin-right: 4px;
  }

  .kpi-row {
    margin-bottom: 4px;
  }

  .kpi-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 88px;
    padding: 14px 16px;
    margin-bottom: 12px;
    background: var(--el-fill-color-blank);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 4px 12px rgb(0 0 0 / 6%);
    }
  }

  .kpi-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .kpi-value {
    display: block;
    margin-top: 6px;
    font-size: 26px;
    font-weight: 600;
    line-height: 1.2;
    color: var(--el-text-color-primary);

    &.success {
      color: var(--el-color-success);
    }

    &.warning {
      color: var(--el-color-warning);
    }

    &.danger {
      color: var(--el-color-danger);
    }

    &.info {
      color: var(--el-text-color-secondary);
    }
  }

  .kpi-hint {
    display: block;
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }

  .kpi-icon {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    font-size: 22px;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    border-radius: 10px;

    &.success {
      color: var(--el-color-success);
      background: var(--el-color-success-light-9);
    }

    &.warning {
      color: var(--el-color-warning);
      background: var(--el-color-warning-light-9);
    }

    &.danger {
      color: var(--el-color-danger);
      background: var(--el-color-danger-light-9);
    }

    &.info {
      color: var(--el-text-color-secondary);
      background: var(--el-fill-color-light);
    }
  }

  .chart-row {
    flex-shrink: 0;

    .chart-card {
      margin-bottom: 0;
    }
  }

  .chart-card {
    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    :deep(.el-card__body) {
      min-height: 300px;
      height: auto;
      padding: 8px 12px 12px;
      overflow: visible;
    }
  }

  .chart-header {
    display: flex;
    gap: 8px;
    align-items: baseline;
    font-size: 14px;
    font-weight: 600;
  }

  .chart-sub {
    font-size: 12px;
    font-weight: 400;
    color: var(--el-text-color-secondary);
  }

  .biz-type-cell {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .biz-type-name {
    font-weight: 500;
  }

  .biz-type-code {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }

  .rate-cell {
    display: flex;
    gap: 10px;
    align-items: center;

    :deep(.el-progress) {
      flex: 1;
    }
  }

  .rate-text {
    flex-shrink: 0;
    width: 42px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: right;
  }

  .text-success {
    color: var(--el-color-success);
  }

  .text-danger {
    color: var(--el-color-danger);
  }
</style>

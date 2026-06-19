<!-- Druid 监控页面 -->
<template>
  <div class="druid-monitor-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>Druid 数据库监控</span>
          <div>
            <ElButton type="info" :icon="Link" @click="openBuiltInPage" style="margin-right: 10px">
              打开内置页面
            </ElButton>
            <ElButton type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
              刷新
            </ElButton>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 数据源非 Druid 或未配置时展示空状态 -->
        <ElEmpty
          v-if="druidInfo && druidInfo.available === false"
          :description="druidInfo.message || '数据源不是 Druid 数据源或数据源未配置'"
          class="druid-empty"
        />
        <!-- 数据源为 Druid 时展示监控卡片 -->
        <div v-else-if="druidInfo && druidInfo.available !== false">
          <div class="druid-stack">
            <!-- 数据源信息 -->
            <ElCard class="stack-card" shadow="hover">
              <template #header>数据源信息</template>
              <ElDescriptions :column="1" border>
                <ElDescriptionsItem label="数据源名称">
                  {{ druidInfo?.name || '-' }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="数据库类型">
                  {{ druidInfo?.dbType || '-' }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="驱动类名">
                  {{ druidInfo?.driverClassName || '-' }}
                </ElDescriptionsItem>
              </ElDescriptions>
            </ElCard>

            <!-- 连接池信息 -->
            <ElCard class="stack-card" shadow="hover">
              <template #header>连接池信息</template>
              <ElDescriptions :column="1" border>
                <ElDescriptionsItem label="初始连接数">
                  {{ druidInfo?.connectionPool?.initialSize || 0 }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="最小空闲连接数">
                  {{ druidInfo?.connectionPool?.minIdle || 0 }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="最大活跃连接数">
                  {{ druidInfo?.connectionPool?.maxActive || 0 }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="当前连接数">
                  {{ druidInfo?.connectionPool?.activeCount || 0 }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="活跃连接峰值">
                  {{ druidInfo?.connectionPool?.activePeak || 0 }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="连接池使用率">
                  <span :class="getUsageClass(druidInfo.connectionPool?.usage || 0)">
                    {{ formatPercent(druidInfo?.connectionPool?.usage || 0) }}
                  </span>
                </ElDescriptionsItem>
              </ElDescriptions>
              <ElProgress
                :percentage="druidInfo.connectionPool?.usage || 0"
                :color="getProgressColor(druidInfo.connectionPool?.usage || 0)"
                :stroke-width="8"
                style="margin-top: 10px"
              />
            </ElCard>

            <!-- SQL 统计信息 -->
            <ElCard class="stack-card" shadow="hover">
              <template #header>SQL 统计信息</template>
              <ElDescriptions :column="1" border>
                <ElDescriptionsItem label="SQL 执行总数">
                  {{ formatNumber(druidInfo?.sqlStat?.executeCount || 0) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="SQL 执行总耗时">
                  {{ formatNumber(druidInfo?.sqlStat?.executeMillisTotal || 0) }} ms
                </ElDescriptionsItem>
                <ElDescriptionsItem label="平均执行时间">
                  {{ formatNumber(druidInfo?.sqlStat?.executeMillisAvg || 0, 2) }} ms
                </ElDescriptionsItem>
                <ElDescriptionsItem label="慢 SQL 数量">
                  <ElTag type="warning">
                    {{ formatNumber(druidInfo?.sqlStat?.slowSqlCount || 0) }}
                  </ElTag>
                </ElDescriptionsItem>
                <ElDescriptionsItem label="错误 SQL 数量">
                  <ElTag type="danger">
                    {{ formatNumber(druidInfo?.sqlStat?.errorSqlCount || 0) }}
                  </ElTag>
                </ElDescriptionsItem>
              </ElDescriptions>
            </ElCard>
          </div>

          <!-- 慢SQL列表（如果包含） -->
          <ElCard
            v-if="showSlowSqlList && druidInfo?.slowSqlList && druidInfo?.slowSqlList.length > 0"
            shadow="hover"
            class="stack-card"
          >
            <template #header>
              <div class="card-header">
                <span>慢SQL列表</span>
                <ElSwitch v-model="showSlowSqlList" active-text="显示" inactive-text="隐藏" />
              </div>
            </template>
            <ElTable :data="druidInfo?.slowSqlList" border stripe>
              <ElTableColumn prop="sqlId" label="SQL ID" width="100" />
              <ElTableColumn prop="sqlText" label="SQL语句" min-width="300" show-overflow-tooltip>
                <template #default="{ row }">
                  <ElTooltip :content="row.sqlText" placement="top" :show-after="300">
                    <div class="sql-text">{{ row?.sqlText }}</div>
                  </ElTooltip>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="executeCount" label="执行次数" width="100" />
              <ElTableColumn prop="executeTimeAvg" label="平均执行时间(ms)" width="150" sortable>
                <template #default="{ row }">
                  <span :class="getTimeClass(row.executeTimeAvg || 0)">
                    {{ formatNumber(row?.executeTimeAvg || 0, 2) }}
                  </span>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="executeTimeMax" label="最大执行时间(ms)" width="150" />
              <ElTableColumn prop="slowCount" label="慢SQL次数" width="120">
                <template #default="{ row }">
                  <ElTag type="warning">{{ row?.slowCount || 0 }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="errorCount" label="错误次数" width="100">
                <template #default="{ row }">
                  <ElTag v-if="(row.errorCount || 0) > 0" type="danger">{{
                    row?.errorCount
                  }}</ElTag>
                  <span v-else>0</span>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { Refresh, Link } from '@element-plus/icons-vue'
  import { fetchGetDruidMonitorInfo } from '@/api/monitor/druid'
  import { usePageVisibility } from '@/hooks/core/usePageVisibility'
  import type { DruidMonitorInfo } from '@/types/api/monitor'
  import { router } from '@/router'

  defineOptions({ name: 'DruidMonitor' })

  const loading = ref(false)
  const druidInfo = ref<DruidMonitorInfo | null>(null)
  const includeSlowSql = ref(false) // 是否包含慢SQL列表
  const showSlowSqlList = ref(false) // 是否显示慢SQL列表
  const slowSqlThreshold = ref(5000) // 慢SQL阈值（毫秒）
  let refreshTimer: number | null = null

  // 页面可见性检测 - 页面不可见时暂停刷新
  const { onPause, onResume } = usePageVisibility()
  onPause(() => stopAutoRefresh())
  onResume(() => startAutoRefresh())

  // 格式化百分比
  const formatPercent = (value: number) => {
    return `${value.toFixed(2)}%`
  }

  // 格式化数字
  const formatNumber = (value: number, decimals: number = 0) => {
    return value.toLocaleString('zh-CN', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    })
  }

  // 获取使用率样式类
  const getUsageClass = (usage: number) => {
    if (usage >= 90) return 'text-danger'
    if (usage >= 70) return 'text-warning'
    return 'text-success'
  }

  // 获取进度条颜色
  const getProgressColor = (usage: number) => {
    if (usage >= 90) return '#f56c6c'
    if (usage >= 70) return '#e6a23c'
    return '#67c23a'
  }

  /**
   * 获取 Druid 监控数据
   * - 成功：request 层直接返回 data，赋值给 druidInfo
   * - 失败：由 HTTP 拦截器统一 showError 提示，此处仅记录日志并确保 loading 关闭
   */
  const getData = async () => {
    loading.value = true
    try {
      const data = await fetchGetDruidMonitorInfo(includeSlowSql.value, slowSqlThreshold.value)
      druidInfo.value = data
      // 如果有慢SQL列表，自动显示
      if (data.slowSqlList && data.slowSqlList.length > 0) {
        showSlowSqlList.value = true
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error('获取 Druid 监控信息失败:', error)
      }
      // 如果获取失败，设置为不可用状态
      druidInfo.value = {
        available: false,
        message: '获取 Druid 监控信息失败'
      }
    } finally {
      loading.value = false
    }
  }

  // 获取时间样式类
  const getTimeClass = (time: number) => {
    if (time >= 10000) return 'text-danger'
    if (time >= 5000) return 'text-warning'
    return ''
  }

  // 刷新数据
  const refreshData = () => {
    getData()
  }

  // 打开 Druid 内置监控页面
  const openBuiltInPage = () => {
    router.push('/monitor/druid-iframe')
  }

  // 自动刷新（每10秒）
  const startAutoRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
    }
    refreshTimer = window.setInterval(() => {
      getData()
    }, 10000)
  }

  // 停止自动刷新
  const stopAutoRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }

  onMounted(() => {
    getData()
    startAutoRefresh()
  })

  onBeforeUnmount(() => {
    stopAutoRefresh()
  })
</script>

<style scoped lang="scss">
  .druid-monitor-page {
    height: 100%;
    padding: 20px;
    overflow-y: auto;
    box-sizing: border-box;
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

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .druid-stack {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;
  }

  .stack-card {
    margin: 0;
  }

  :deep(.el-card) {
    border-radius: 12px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgb(0 0 0 / 12%);
    }

    .el-card__header {
      padding: 16px 20px;
      font-weight: 600;
      color: var(--art-gray-800);
      border-bottom: 1px solid var(--art-card-border);
    }
  }

  :deep(.el-descriptions) {
    .el-descriptions__label {
      font-weight: 500;
      color: var(--art-gray-600);
    }

    .el-descriptions__content {
      color: var(--art-gray-800);
    }
  }

  :deep(.el-progress) {
    .el-progress-bar__outer {
      border-radius: 4px;
    }

    .el-progress-bar__inner {
      border-radius: 4px;
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

  :deep(.el-switch) {
    --el-switch-on-color: var(--el-color-primary);
  }

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }

  .text-success {
    color: var(--el-color-success);
  }

  .text-warning {
    color: var(--el-color-warning);
  }

  .text-danger {
    color: var(--el-color-danger);
  }

  .druid-empty {
    padding: 48px 0;
  }

  .sql-text {
    max-width: 300px;
    overflow: hidden;
    font-family: monospace;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>

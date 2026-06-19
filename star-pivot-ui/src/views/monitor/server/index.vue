<!-- 服务器监控页面 -->
<template>
  <div :class="{ 'is-dark': isDark }" class="server-monitor-page">
    <ElCard class="art-table-card server-monitor-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <span class="title-main">服务器监控</span>
            <span class="title-sub">实时查看主机资源与运行状态</span>
          </div>
          <div class="header-actions">
            <div :class="`is-${healthStatus.level}`" class="health-indicator">
              <span class="health-dot"></span>
              <span class="health-text">{{ healthStatus.label }}</span>
            </div>
            <div class="status-pills">
              <div
                v-for="item in statusPills"
                :key="item.label"
                :class="`is-${item.level}`"
                class="status-pill"
              >
                <span class="pill-label">{{ item.label }}</span>
                <span class="pill-value">{{ item.value }}</span>
              </div>
            </div>
            <ElRadioGroup v-model="densityMode" class="density-switch" size="small">
              <ElRadioButton value="comfortable">舒适</ElRadioButton>
              <ElRadioButton value="compact">紧凑</ElRadioButton>
            </ElRadioGroup>
            <ElButton :icon="Refresh" :loading="loading" type="primary" @click="refreshData">
              刷新
            </ElButton>
          </div>
        </div>
        <div class="header-meta">
          <span>最后更新时间：{{ lastUpdatedText }}</span>
          <span>自动刷新：{{ refreshCountdownText }}</span>
        </div>
      </template>

      <div v-loading="loading" :class="`density-${densityMode}`" class="monitor-content">
        <template v-if="serverInfo">
          <ElRow :gutter="16" class="overview-row">
            <ElCol v-for="item in overviewCards" :key="item.label" :md="6" :sm="12" :xs="24">
              <div :class="`is-${item.level}`" class="overview-card">
                <div class="overview-label">{{ item.label }}</div>
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-extra">{{ item.extra }}</div>
                <span v-if="item.level !== 'normal'" class="overview-alert">
                  {{ item.level === 'danger' ? '告警' : '预警' }}
                </span>
              </div>
            </ElCol>
          </ElRow>

          <ElRow :gutter="16">
            <ElCol :md="12" :xs="24">
              <ElCard class="section-card section-card--cpu" shadow="hover">
                <template #header>
                  <div class="section-title">
                    <span>CPU</span>
                    <ElTag effect="light" type="info">核心 {{ serverInfo.cpu?.cpuNum ?? 0 }}</ElTag>
                  </div>
                </template>
                <div class="meter-group">
                  <div
                    v-for="item in cpuUsageMeters"
                    :key="item.label"
                    :class="`is-${item.level}`"
                    class="meter-item"
                  >
                    <div class="meter-label">{{ item.label }}</div>
                    <ElProgress
                      :color="item.color"
                      :percentage="item.value"
                      :show-text="false"
                      :stroke-width="12"
                    />
                    <div class="meter-value">{{ formatPercent(item.value) }}</div>
                  </div>
                </div>
                <ElTable :data="cpuRows" border size="small">
                  <ElTableColumn label="属性" min-width="140" prop="label" />
                  <ElTableColumn label="值" min-width="120" prop="value" />
                </ElTable>
              </ElCard>
            </ElCol>
            <ElCol :md="12" :xs="24">
              <ElCard class="section-card section-card--memory" shadow="hover">
                <template #header>
                  <div class="section-title">
                    <span>内存</span>
                    <ElTag effect="light" type="success">系统 / JVM</ElTag>
                  </div>
                </template>
                <div class="meter-group">
                  <div
                    v-for="item in memoryUsageMeters"
                    :key="item.label"
                    :class="`is-${item.level}`"
                    class="meter-item"
                  >
                    <div class="meter-label">{{ item.label }}</div>
                    <ElProgress
                      :color="item.color"
                      :percentage="item.value"
                      :show-text="false"
                      :stroke-width="12"
                    />
                    <div class="meter-value">{{ formatPercent(item.value) }}</div>
                  </div>
                </div>
                <ElTable :data="memoryRows" border size="small">
                  <ElTableColumn label="属性" min-width="140" prop="label" />
                  <ElTableColumn label="内存" min-width="120" prop="memory" />
                  <ElTableColumn label="JVM" min-width="120" prop="jvm" />
                </ElTable>
              </ElCard>
            </ElCol>
          </ElRow>

          <ElCard class="section-card section-card--server" shadow="hover">
            <template #header>
              <div class="section-title">
                <span>服务器信息</span>
              </div>
            </template>
            <ElDescriptions :column="2" border size="small">
              <ElDescriptionsItem v-for="item in serverRows" :key="item.label" :label="item.label">
                {{ item.value }}
              </ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>

          <ElCard class="section-card section-card--jvm" shadow="hover">
            <template #header>
              <div class="section-title">
                <span>Java虚拟机信息</span>
              </div>
            </template>
            <ElDescriptions :column="2" border size="small">
              <ElDescriptionsItem v-for="item in jvmRows" :key="item.label" :label="item.label">
                {{ item.value }}
              </ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>

          <ElCard class="section-card section-card--disk" shadow="hover">
            <template #header>
              <div class="section-title">
                <span>磁盘状态</span>
              </div>
            </template>
            <ElTable :data="diskRows" border size="small">
              <ElTableColumn label="盘符路径" min-width="120" prop="mount" />
              <ElTableColumn label="文件系统" min-width="100" prop="fileSystem" />
              <ElTableColumn label="磁盘类型" min-width="140" prop="typeName" />
              <ElTableColumn label="总大小" min-width="90" prop="total" />
              <ElTableColumn label="可用大小" min-width="90" prop="usable" />
              <ElTableColumn label="已用大小" min-width="90" prop="used" />
              <ElTableColumn label="已用百分比" min-width="120" prop="usage">
                <template #default="{ row }">
                  <ElTag :type="row.usageType" effect="light">{{ row.usage }}</ElTag>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </template>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { Refresh } from '@element-plus/icons-vue'
  import { fetchGetServerInfo } from '@/api/monitor/server'
  import { usePageVisibility } from '@/hooks/core/usePageVisibility'
  import { useSettingStore } from '@/store/modules/setting'
  import type { ServerInfo } from '@/types/api/monitor'

  defineOptions({ name: 'ServerMonitor' })
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  const loading = ref(false)
  const serverInfo = ref<ServerInfo | null>(null)
  const densityMode = ref<'comfortable' | 'compact'>('comfortable')
  const requestInFlight = ref(false)
  const lastUpdatedAt = ref<number | null>(null)
  const nextRefreshAt = ref<number | null>(null)
  const refreshCountdown = ref(0)
  const baseRefreshInterval = 10000
  const maxRefreshInterval = 60000
  const currentRefreshInterval = ref(baseRefreshInterval)
  let refreshTimer: number | null = null
  let countdownTimer: number | null = null

  // 页面可见性检测 - 页面不可见时暂停刷新
  const { onPause, onResume } = usePageVisibility()
  onPause(() => stopAutoRefresh())
  onResume(() => startAutoRefresh())

  // 格式化百分比
  const formatPercent = (value: number) => {
    return `${value.toFixed(2)}%`
  }

  const clampPercent = (value: number) => {
    return Math.min(Math.max(value, 0), 100)
  }

  const formatNumber = (value: number) => {
    return value.toLocaleString()
  }

  const formatGb = (value: number | undefined) => {
    return `${(value ?? 0).toFixed(2)} GB`
  }

  const getUsageType = (usage: number) => {
    if (usage > 85) return 'danger'
    if (usage > 70) return 'warning'
    return 'success'
  }

  const getUsageLevel = (usage: number) => {
    if (usage >= 85) return 'danger'
    if (usage >= 70) return 'warning'
    return 'normal'
  }

  // 格式化时长
  const formatDuration = (millis: number) => {
    const seconds = Math.floor(millis / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 0) {
      return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`
    } else if (hours > 0) {
      return `${hours}小时 ${minutes % 60}分钟`
    } else if (minutes > 0) {
      return `${minutes}分钟 ${seconds % 60}秒`
    } else {
      return `${seconds}秒`
    }
  }

  // 格式化时间
  const formatTime = (timestamp: number | string) => {
    if (typeof timestamp === 'string') {
      return new Date(timestamp).toLocaleString('zh-CN')
    }
    return new Date(timestamp).toLocaleString('zh-CN')
  }

  const cpuRows = computed(() => {
    if (!serverInfo.value) return []
    const cpu = serverInfo.value.cpu
    return [
      { label: '核心数', value: cpu?.cpuNum ?? 0 },
      { label: '用户使用率', value: formatPercent(cpu?.used ?? 0) },
      { label: '系统使用率', value: formatPercent(cpu?.sys ?? 0) },
      { label: '当前空闲率', value: formatPercent(cpu?.free ?? 0) }
    ]
  })

  const memoryRows = computed(() => {
    if (!serverInfo.value) return []
    const memory = serverInfo.value.memory
    const jvm = serverInfo.value.jvm
    return [
      {
        label: '总内存',
        memory: `${formatNumber(memory?.total ?? 0)} MB`,
        jvm: `${formatNumber(jvm?.total ?? 0)} MB`
      },
      {
        label: '已用内存',
        memory: `${formatNumber(memory?.used ?? 0)} MB`,
        jvm: `${formatNumber(jvm?.used ?? 0)} MB`
      },
      {
        label: '剩余内存',
        memory: `${formatNumber(memory?.free ?? 0)} MB`,
        jvm: `${formatNumber(jvm?.free ?? 0)} MB`
      },
      {
        label: '使用率',
        memory: formatPercent(memory?.usage ?? 0),
        jvm: formatPercent(jvm?.usage ?? 0)
      }
    ]
  })

  const cpuUsageMeters = computed(() => {
    const cpu = serverInfo.value?.cpu
    return [
      {
        label: '用户使用率',
        value: clampPercent(cpu?.used ?? 0),
        color: '#6366f1',
        level: getUsageLevel(cpu?.used ?? 0)
      },
      {
        label: '系统使用率',
        value: clampPercent(cpu?.sys ?? 0),
        color: '#f59e0b',
        level: getUsageLevel(cpu?.sys ?? 0)
      },
      {
        label: '当前空闲率',
        value: clampPercent(cpu?.free ?? 0),
        color: '#10b981',
        level: 'normal'
      }
    ]
  })

  const memoryUsageMeters = computed(() => {
    const memory = serverInfo.value?.memory
    const jvm = serverInfo.value?.jvm
    return [
      {
        label: '系统内存使用率',
        value: clampPercent(memory?.usage ?? 0),
        color: '#ef4444',
        level: getUsageLevel(memory?.usage ?? 0)
      },
      {
        label: 'JVM使用率',
        value: clampPercent(jvm?.usage ?? 0),
        color: '#06b6d4',
        level: getUsageLevel(jvm?.usage ?? 0)
      }
    ]
  })

  const overviewCards = computed(() => {
    const cpu = serverInfo.value?.cpu
    const memory = serverInfo.value?.memory
    const jvm = serverInfo.value?.jvm
    const diskCount = serverInfo.value?.disk?.stores?.length ?? 0
    return [
      {
        label: 'CPU使用率',
        value: formatPercent(cpu?.used ?? 0),
        extra: `空闲 ${formatPercent(cpu?.free ?? 0)}`,
        level: getUsageLevel(cpu?.used ?? 0)
      },
      {
        label: '系统内存使用率',
        value: formatPercent(memory?.usage ?? 0),
        extra: `总内存 ${formatNumber(memory?.total ?? 0)} MB`,
        level: getUsageLevel(memory?.usage ?? 0)
      },
      {
        label: 'JVM使用率',
        value: formatPercent(jvm?.usage ?? 0),
        extra: `运行 ${formatDuration(jvm?.runTime ?? 0)}`,
        level: getUsageLevel(jvm?.usage ?? 0)
      },
      {
        label: '磁盘数量',
        value: `${diskCount} 个`,
        extra: `服务器 ${serverInfo.value?.system?.computerName || '-'}`,
        level: 'normal'
      }
    ]
  })

  const statusPills = computed(() => {
    const cpuUsage = serverInfo.value?.cpu?.used ?? 0
    const memoryUsage = serverInfo.value?.memory?.usage ?? 0
    const jvmUsage = serverInfo.value?.jvm?.usage ?? 0
    const diskMaxUsage = Math.max(
      ...(serverInfo.value?.disk?.stores?.map((item) => item.usage ?? 0) ?? [0])
    )
    return [
      { label: 'CPU', value: formatPercent(cpuUsage), level: getUsageLevel(cpuUsage) },
      { label: '内存', value: formatPercent(memoryUsage), level: getUsageLevel(memoryUsage) },
      { label: 'JVM', value: formatPercent(jvmUsage), level: getUsageLevel(jvmUsage) },
      { label: '磁盘峰值', value: formatPercent(diskMaxUsage), level: getUsageLevel(diskMaxUsage) }
    ]
  })

  const healthStatus = computed(() => {
    const levels = statusPills.value.map((item) => item.level)
    if (levels.includes('danger')) return { level: 'danger', label: '系统告警' }
    if (levels.includes('warning')) return { level: 'warning', label: '系统预警' }
    return { level: 'normal', label: '运行正常' }
  })

  const lastUpdatedText = computed(() => {
    if (!lastUpdatedAt.value) return '-'
    return new Date(lastUpdatedAt.value).toLocaleString('zh-CN')
  })

  const refreshCountdownText = computed(() => {
    if (!nextRefreshAt.value) return '暂停'
    return `${refreshCountdown.value}s`
  })

  const syncRefreshCountdown = () => {
    if (!nextRefreshAt.value) {
      refreshCountdown.value = 0
      return
    }
    const diff = Math.max(nextRefreshAt.value - Date.now(), 0)
    refreshCountdown.value = Math.ceil(diff / 1000)
  }

  const startCountdown = () => {
    stopCountdown()
    syncRefreshCountdown()
    countdownTimer = window.setInterval(() => {
      syncRefreshCountdown()
    }, 1000)
  }

  const stopCountdown = () => {
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }

  const serverRows = computed(() => {
    if (!serverInfo.value) return []
    const system = serverInfo.value.system
    return [
      { label: '服务器名称', value: system?.computerName || '-' },
      { label: '操作系统', value: system?.osName || '-' },
      { label: '服务器IP', value: system?.computerIp || '-' },
      { label: '系统架构', value: system?.osArch || '-' }
    ]
  })

  const jvmRows = computed(() => {
    if (!serverInfo.value) return []
    const jvm = serverInfo.value.jvm
    return [
      { label: 'Java名称', value: jvm?.name || '-' },
      { label: 'Java版本', value: jvm?.version || '-' },
      { label: '启动时间', value: formatTime(jvm?.startTime || 0) },
      { label: '运行时长', value: formatDuration(jvm?.runTime || 0) },
      { label: '安装路径', value: jvm?.home || '-' },
      { label: '项目路径', value: jvm?.userDir || '-' },
      { label: '运行参数', value: jvm?.inputArgs || '-' }
    ]
  })

  const diskRows = computed(() => {
    const stores = serverInfo.value?.disk?.stores || []
    return stores.map((store) => ({
      mount: store.mount || '-',
      fileSystem: store.fileSystem || '-',
      typeName: store.typeName || '-',
      total: formatGb(store.totalGb),
      usable: formatGb(store.usableGb),
      used: formatGb(store.usedGb),
      usage: formatPercent(store.usage || 0),
      usageType: getUsageType(store.usage || 0)
    }))
  })

  const scheduleNextRefresh = (delay = currentRefreshInterval.value) => {
    stopAutoRefresh()
    nextRefreshAt.value = Date.now() + delay
    startCountdown()
    refreshTimer = window.setTimeout(() => {
      stopCountdown()
      getData()
    }, delay)
  }

  const getData = async (manual = false) => {
    if (requestInFlight.value) return
    requestInFlight.value = true
    if (manual || !serverInfo.value) {
      loading.value = true
    }
    try {
      const serverData = await fetchGetServerInfo()
      serverInfo.value = serverData
      lastUpdatedAt.value = Date.now()
      currentRefreshInterval.value = baseRefreshInterval
    } catch (error) {
      currentRefreshInterval.value = Math.min(currentRefreshInterval.value * 2, maxRefreshInterval)
      if (import.meta.env.DEV) {
        console.error('获取监控数据失败:', error)
      }
    } finally {
      requestInFlight.value = false
      loading.value = false
      scheduleNextRefresh()
    }
  }

  const refreshData = () => {
    nextRefreshAt.value = null
    stopCountdown()
    getData(true)
  }

  const startAutoRefresh = () => {
    scheduleNextRefresh(baseRefreshInterval)
  }

  const stopAutoRefresh = () => {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
    nextRefreshAt.value = null
    stopCountdown()
  }

  onMounted(() => {
    getData(true)
  })

  onBeforeUnmount(() => {
    stopAutoRefresh()
    stopCountdown()
  })
</script>

<style scoped lang="scss">
  .server-monitor-page {
    --monitor-page-bg: linear-gradient(180deg, #f6f8fc 0%, #f2f5fb 100%);
    --monitor-card-bg: rgb(255 255 255 / 94%);
    --monitor-card-border: rgb(148 163 184 / 22%);
    --monitor-muted-text: var(--art-gray-600);
    --monitor-title-text: var(--art-gray-900);
    --monitor-block-bg: rgb(248 250 252 / 90%);
    --monitor-block-border: rgb(148 163 184 / 24%);
    --monitor-table-header-bg: rgb(241 245 249 / 88%);
    --monitor-table-hover-bg: rgb(59 130 246 / 9%);

    height: 100%;
    padding: 16px;
    overflow-y: auto;
    background: var(--monitor-page-bg);
  }

  :deep(.server-monitor-card) {
    border: 1px solid var(--monitor-card-border);
    border-radius: 14px;
    background: var(--monitor-card-bg);
    box-shadow:
      0 10px 28px rgb(15 23 42 / 8%),
      0 2px 8px rgb(15 23 42 / 5%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow:
        0 14px 30px rgb(15 23 42 / 10%),
        0 6px 14px rgb(15 23 42 / 6%);
    }

    .el-card__body {
      padding: 18px 20px;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 40px;
    gap: 12px;
    flex-wrap: wrap;
  }

  .header-meta {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 12px;
    color: var(--monitor-muted-text);
  }

  .header-title {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .title-main {
    font-size: 17px;
    font-weight: 600;
    color: var(--monitor-title-text);
    line-height: 1.1;
    letter-spacing: 0.2px;
  }

  .title-sub {
    font-size: 12px;
    color: var(--monitor-muted-text);
  }

  .header-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
    flex-wrap: wrap;
  }

  .health-indicator {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 10px;
    border-radius: 999px;
    border: 1px solid var(--monitor-card-border);
    background: var(--monitor-block-bg);
    color: var(--monitor-muted-text);
    font-size: 12px;
    line-height: 1;
    transition: all 0.22s ease;
  }

  .health-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #22c55e;
    box-shadow: 0 0 8px rgb(34 197 94 / 55%);
  }

  .health-text {
    font-weight: 600;
    color: var(--monitor-title-text);
  }

  .health-indicator.is-warning {
    border-color: rgb(245 158 11 / 50%);
    background: rgb(245 158 11 / 12%);
  }

  .health-indicator.is-warning .health-dot {
    background: #f59e0b;
    box-shadow: 0 0 8px rgb(245 158 11 / 55%);
  }

  .health-indicator.is-danger {
    border-color: rgb(239 68 68 / 54%);
    background: rgb(239 68 68 / 12%);
  }

  .health-indicator.is-danger .health-dot {
    background: #ef4444;
    box-shadow: 0 0 8px rgb(239 68 68 / 58%);
  }

  .status-pills {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .status-pill {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 10px;
    border-radius: 999px;
    border: 1px solid var(--monitor-card-border);
    background: var(--monitor-block-bg);
    font-size: 12px;
    line-height: 1;
    transition: all 0.22s ease;
  }

  .pill-label {
    color: var(--monitor-muted-text);
  }

  .pill-value {
    color: var(--monitor-title-text);
    font-weight: 600;
  }

  .status-pill.is-warning {
    border-color: rgb(245 158 11 / 48%);
    background: rgb(245 158 11 / 12%);
  }

  .status-pill.is-danger {
    border-color: rgb(239 68 68 / 52%);
    background: rgb(239 68 68 / 12%);
  }

  .density-switch {
    :deep(.el-radio-button__inner) {
      padding: 7px 12px;
      border-radius: 8px;
      border: 1px solid var(--monitor-card-border);
      background: var(--monitor-block-bg);
      color: var(--monitor-muted-text);
      box-shadow: none;
    }
  }

  .overview-row {
    margin-bottom: 10px;
  }

  .overview-card {
    position: relative;
    margin-bottom: 10px;
    min-height: 102px;
    padding: 14px 16px;
    border: 1px solid var(--monitor-card-border);
    border-radius: 12px;
    background: linear-gradient(135deg, rgb(255 255 255 / 95%) 0%, rgb(247 250 255 / 90%) 100%);
    box-shadow: 0 8px 18px rgb(15 23 42 / 6%);
    overflow: hidden;
    transition: all 0.24s ease;
  }

  .overview-card::after {
    content: '';
    position: absolute;
    top: -24px;
    right: -28px;
    width: 90px;
    height: 90px;
    border-radius: 50%;
    background: radial-gradient(circle, rgb(59 130 246 / 14%) 0%, rgb(59 130 246 / 0%) 70%);
  }

  .overview-alert {
    position: absolute;
    top: 10px;
    right: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 3px 8px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.2px;
  }

  .overview-card.is-warning {
    border-color: rgb(245 158 11 / 44%);
    box-shadow: 0 10px 20px rgb(245 158 11 / 10%);
  }

  .overview-card.is-warning .overview-alert {
    color: #f59e0b;
    background: rgb(245 158 11 / 16%);
    border: 1px solid rgb(245 158 11 / 44%);
  }

  .overview-card.is-danger {
    border-color: rgb(239 68 68 / 50%);
    box-shadow: 0 12px 22px rgb(239 68 68 / 12%);
  }

  .overview-card.is-danger .overview-alert {
    color: #ef4444;
    background: rgb(239 68 68 / 16%);
    border: 1px solid rgb(239 68 68 / 48%);
  }

  .overview-label {
    font-size: 12px;
    color: var(--monitor-muted-text);
  }

  .overview-value {
    margin: 6px 0;
    font-size: 22px;
    font-weight: 600;
    line-height: 1.2;
    color: var(--monitor-title-text);
  }

  .overview-extra {
    font-size: 12px;
    color: var(--monitor-muted-text);
  }

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .meter-group {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
    padding: 2px 0 12px;
  }

  .meter-item {
    padding: 10px 12px;
    border-radius: 10px;
    background: var(--monitor-block-bg);
    border: 1px solid var(--monitor-block-border);
    transition: all 0.2s ease;
  }

  .meter-item.is-warning {
    border-color: rgb(245 158 11 / 42%);
  }

  .meter-item.is-danger {
    border-color: rgb(239 68 68 / 45%);
    box-shadow: inset 0 0 0 1px rgb(239 68 68 / 14%);
  }

  .meter-label {
    margin-bottom: 6px;
    font-size: 12px;
    color: var(--monitor-muted-text);
  }

  .meter-value {
    margin-top: 6px;
    text-align: right;
    font-size: 12px;
    font-weight: 500;
    color: var(--monitor-muted-text);
  }

  :deep(.el-card) {
    border-radius: 12px;
    border: 1px solid var(--monitor-card-border);
    background: var(--monitor-card-bg);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 8px 20px rgb(15 23 42 / 8%);
    }

    .el-card__header {
      padding: 14px 18px;
      font-weight: 600;
      color: var(--monitor-title-text);
      border-bottom: 1px solid var(--monitor-card-border);
    }
  }

  .section-card {
    margin-bottom: 12px;
    position: relative;
    overflow: hidden;
    transition: transform 0.22s ease;
  }

  .section-card::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 2px;
    background: var(--section-accent, rgb(59 130 246 / 72%));
  }

  .section-card--cpu {
    --section-accent: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
  }

  .section-card--memory {
    --section-accent: linear-gradient(90deg, #06b6d4 0%, #3b82f6 100%);
  }

  .section-card--server {
    --section-accent: linear-gradient(90deg, #22c55e 0%, #14b8a6 100%);
  }

  .section-card--jvm {
    --section-accent: linear-gradient(90deg, #f59e0b 0%, #fb7185 100%);
  }

  .section-card--disk {
    --section-accent: linear-gradient(90deg, #a855f7 0%, #6366f1 100%);
  }

  .monitor-content.density-compact {
    .overview-card {
      min-height: 88px;
      padding: 10px 12px;
    }

    .overview-value {
      margin: 4px 0;
      font-size: 18px;
    }

    .section-card {
      margin-bottom: 8px;
    }

    .meter-group {
      gap: 8px;
      padding-bottom: 8px;
    }

    .meter-item {
      padding: 8px 10px;
    }

    :deep(.el-card .el-card__header) {
      padding: 10px 14px;
    }

    :deep(.el-table .cell) {
      font-size: 12px;
      padding-top: 4px;
      padding-bottom: 4px;
    }
  }

  :deep(.el-table) {
    --el-table-header-bg-color: var(--monitor-table-header-bg);
    --el-table-row-hover-bg-color: var(--monitor-table-hover-bg);
    --el-table-tr-bg-color: transparent;
    --el-table-border-color: var(--monitor-card-border);
    --el-table-header-text-color: var(--monitor-muted-text);
    --el-table-text-color: var(--monitor-title-text);
    border-radius: 10px;
    overflow: hidden;

    .cell {
      font-size: 13px;
    }
  }

  :deep(.el-descriptions__label) {
    width: 130px;
    color: var(--monitor-muted-text);
    font-weight: 500;
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: none;
    }
  }

  .server-monitor-page.is-dark {
    --monitor-page-bg: linear-gradient(180deg, #0b111a 0%, #111826 100%);
    --monitor-card-bg: rgb(17 24 39 / 82%);
    --monitor-card-border: rgb(71 85 105 / 42%);
    --monitor-muted-text: rgb(203 213 225 / 88%);
    --monitor-title-text: rgb(241 245 249 / 95%);
    --monitor-block-bg: rgb(15 23 42 / 72%);
    --monitor-block-border: rgb(100 116 139 / 34%);
    --monitor-table-header-bg: rgb(15 23 42 / 82%);
    --monitor-table-hover-bg: rgb(59 130 246 / 15%);
  }

  .server-monitor-page.is-dark .status-pill:hover {
    transform: translateY(-1px);
    border-color: rgb(148 163 184 / 56%);
    background: rgb(30 41 59 / 84%);
  }

  .server-monitor-page.is-dark .health-indicator:hover {
    transform: translateY(-1px);
    border-color: rgb(148 163 184 / 56%);
  }

  .server-monitor-page.is-dark .overview-card {
    background: linear-gradient(145deg, rgb(30 41 59 / 76%) 0%, rgb(17 24 39 / 84%) 100%);
    box-shadow:
      inset 0 0 0 1px rgb(148 163 184 / 12%),
      0 12px 20px rgb(2 6 23 / 38%);
  }

  .server-monitor-page.is-dark .overview-card:hover {
    transform: translateY(-2px);
    box-shadow:
      inset 0 0 0 1px rgb(148 163 184 / 20%),
      0 16px 28px rgb(2 6 23 / 46%);
  }

  .server-monitor-page.is-dark .overview-card::after {
    background: radial-gradient(circle, rgb(14 165 233 / 20%) 0%, rgb(14 165 233 / 0%) 72%);
  }

  .server-monitor-page.is-dark .overview-card.is-warning {
    box-shadow:
      inset 0 0 0 1px rgb(245 158 11 / 24%),
      0 12px 24px rgb(0 0 0 / 36%);
  }

  .server-monitor-page.is-dark .overview-card.is-danger {
    box-shadow:
      inset 0 0 0 1px rgb(239 68 68 / 26%),
      0 12px 24px rgb(0 0 0 / 38%);
  }

  .server-monitor-page.is-dark .meter-item:hover {
    border-color: rgb(100 116 139 / 56%);
    background: rgb(15 23 42 / 88%);
  }

  .server-monitor-page.is-dark .meter-item.is-warning:hover {
    border-color: rgb(245 158 11 / 58%);
  }

  .server-monitor-page.is-dark .meter-item.is-danger:hover {
    border-color: rgb(239 68 68 / 62%);
    box-shadow: inset 0 0 0 1px rgb(239 68 68 / 22%);
  }

  .server-monitor-page.is-dark .section-card:hover {
    transform: translateY(-2px);
  }

  .server-monitor-page.is-dark :deep(.density-switch .el-radio-button__inner:hover) {
    border-color: rgb(59 130 246 / 52%);
    color: rgb(191 219 254);
  }

  .server-monitor-page.is-dark
    :deep(.density-switch .el-radio-button__original-radio:checked + .el-radio-button__inner) {
    border-color: rgb(59 130 246 / 72%);
    background: rgb(30 58 138 / 34%);
    color: rgb(219 234 254);
  }

  .server-monitor-page.is-dark :deep(.el-table__row:hover > td.el-table__cell) {
    background: rgb(30 58 138 / 22%) !important;
  }

  .server-monitor-page.is-dark :deep(.el-button.el-button--primary) {
    border-color: rgb(59 130 246 / 72%);
    background: linear-gradient(135deg, rgb(37 99 235 / 86%) 0%, rgb(29 78 216 / 92%) 100%);
  }

  .server-monitor-page.is-dark :deep(.el-button.el-button--primary:hover) {
    filter: brightness(1.08);
    box-shadow: 0 8px 18px rgb(30 64 175 / 32%);
  }

  .server-monitor-page.is-dark :deep(.el-tag.el-tag--info),
  .server-monitor-page.is-dark :deep(.el-tag.el-tag--success) {
    color: rgb(191 219 254);
    border-color: rgb(59 130 246 / 45%);
    background: rgb(30 58 138 / 24%);
  }

  @media (max-width: 768px) {
    .server-monitor-page {
      padding: 10px;
    }

    :deep(.server-monitor-card .el-card__body) {
      padding: 14px;
    }

    .overview-card {
      min-height: 94px;
    }

    .overview-value {
      font-size: 20px;
    }

    .header-actions {
      width: 100%;
      justify-content: flex-start;
    }

    .header-meta {
      margin-top: 6px;
      width: 100%;
      justify-content: space-between;
      gap: 8px;
    }
  }
</style>

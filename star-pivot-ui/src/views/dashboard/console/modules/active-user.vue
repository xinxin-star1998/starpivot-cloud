<template>
  <div class="art-card h-105 p-4 box-border mb-5 max-sm:mb-4">
    <ArtBarChart
      class="box-border p-2"
      barWidth="50%"
      height="13.7rem"
      :showAxisLine="false"
      :data="chartData"
      :xAxisData="xAxisLabels"
    />
    <div class="ml-1">
      <h3 class="mt-5 text-lg font-medium">{{ t('dashboard.activeUser.title') }}</h3>
      <p class="mt-1 text-sm">{{ t('dashboard.activeUser.subtitle') }}</p>
      <p class="mt-1 text-sm">{{ t('dashboard.activeUser.description') }}</p>
    </div>
    <div class="flex-b mt-2">
      <div class="flex-1" v-for="(item, index) in list" :key="index">
        <p class="text-2xl text-g-900">{{ item.num }}</p>
        <p class="text-xs text-g-500">{{ item.name }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import type {DashboardTrendData} from '@/types/api/dashboard'

const { t } = useI18n()

  interface UserStatItem {
    name: string
    num: string
  }

  const props = withDefaults(
    defineProps<{
      trendData: DashboardTrendData
    }>(),
    {
      trendData: () => ({ xAxisData: [], data: [] })
    }
  )

  const xAxisLabels = computed(() => props.trendData.xAxisData)
  const chartData = computed(() => props.trendData.data)

  /**
   * 用户统计数据列表
   * 包含总用户量、总访问量、日访问量和周同比等关键指标
   */
  const list = computed<UserStatItem[]>(() => [
    { name: t('dashboard.activeUser.statDimension'), num: t('dashboard.activeUser.statNewUsers') },
    { name: t('dashboard.activeUser.statPeriod'), num: t('dashboard.activeUser.statPeriodValue') },
    { name: t('dashboard.activeUser.statSource'), num: 'sys_user' },
    { name: t('dashboard.activeUser.statStatus'), num: t('dashboard.activeUser.statRealtime') }
  ])
</script>

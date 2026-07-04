<template>
  <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ t('dashboard.salesOverview.title') }}</h4>
        <p>{{ t('dashboard.salesOverview.growth') }}<span class="text-success">+15%</span></p>
      </div>
    </div>
    <ArtLineChart
      height="calc(100% - 56px)"
      :data="data"
      :xAxisData="xAxisData"
      :showAreaColor="true"
      :showAxisLine="false"
    />
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import type {DashboardTrendData} from '@/types/api/dashboard'

const { t } = useI18n()

  const props = withDefaults(
    defineProps<{
      trendData: DashboardTrendData
    }>(),
    {
      trendData: () => ({ xAxisData: [], data: [] })
    }
  )

  const data = computed(() => props.trendData.data)
  const xAxisData = computed(() => props.trendData.xAxisData)
</script>

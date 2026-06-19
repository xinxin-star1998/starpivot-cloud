<!-- 工作台页面 -->
<template>
  <div v-loading="loading" element-loading-text="加载中...">
    <CardList :data-list="dashboardData.cardList" />

    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="10">
        <ActiveUser :trend-data="dashboardData.userTrend" />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="14">
        <SalesOverview :trend-data="dashboardData.visitTrend" />
      </ElCol>
    </ElRow>

    <ElRow :gutter="20">
      <ElCol :sm="24" :md="24" :lg="24">
        <ElRow :gutter="20">
          <ElCol :sm="24" :md="24" :lg="12">
            <NewUser :user-list="dashboardData.newUserList" />
          </ElCol>
          <ElCol :sm="24" :md="12" :lg="6">
            <Dynamic :dynamic-list="dashboardData.dynamicList" />
          </ElCol>
          <ElCol :sm="24" :md="12" :lg="6">
            <TodoList :todo-list="dashboardData.todoList" />
          </ElCol>
        </ElRow>
      </ElCol>
    </ElRow>

    <!-- <AboutProject /> -->
  </div>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchGetConsoleDashboardData } from '@/api/dashboard/console'
  import type { ConsoleDashboardData } from '@/types/api/dashboard'
  import CardList from './modules/card-list.vue'
  import ActiveUser from './modules/active-user.vue'
  import SalesOverview from './modules/sales-overview.vue'
  import NewUser from './modules/new-user.vue'
  import Dynamic from './modules/dynamic-stats.vue'
  import TodoList from './modules/todo-list.vue'
  // import AboutProject from './modules/about-project.vue'

  defineOptions({ name: 'Console' })

  const { t } = useI18n()

  const loading = ref(false)

  const createDefaultData = (): ConsoleDashboardData => ({
    cardList: [],
    visitTrend: { xAxisData: [], data: [] },
    userTrend: { xAxisData: [], data: [] },
    todoList: [],
    dynamicList: [],
    newUserList: []
  })

  const dashboardData = ref<ConsoleDashboardData>(createDefaultData())

  const loadDashboardData = async () => {
    loading.value = true
    try {
      dashboardData.value = await fetchGetConsoleDashboardData()
    } catch (error) {
      dashboardData.value = createDefaultData()
      ElMessage.error(t('dashboard.loadFailed'))
      console.error('加载首页数据失败', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadDashboardData()
  })
</script>

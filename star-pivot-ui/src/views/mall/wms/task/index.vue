<!-- 库存工作单 -->
<template>
  <div class="ware-task-page art-full-height">
    <TaskSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <TaskDetailDrawer
      v-model:visible="detailVisible"
      :task-id="currentTaskId"
      @submit="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchWareTaskList, TASK_STATUS_MAP, type WareOrderTaskVo } from '@/api/mall/ware-task'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import TaskSearch from './modules/task-search.vue'
  import TaskDetailDrawer from './modules/task-detail-drawer.vue'
  import { ElButton, ElTag } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'WareTask' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    orderSn: undefined as string | undefined,
    taskStatus: undefined as number | undefined,
    wareId: undefined as number | undefined
  })

  const detailVisible = ref(false)
  const currentTaskId = ref<number>()

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
    refreshData
  } = useTable({
    core: {
      apiFn: fetchWareTaskList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'orderSn', label: '订单号', minWidth: 140, showOverflowTooltip: true },
        { prop: 'consignee', label: '收货人', width: 100 },
        { prop: 'consigneeTel', label: '电话', width: 120 },
        { prop: 'wareId', label: '仓库', width: 80 },
        {
          prop: 'taskStatus',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const type =
              row.taskStatus === 2 ? 'success' : row.taskStatus === 3 ? 'info' : 'warning'
            return h(ElTag, { type, size: 'small' }, () =>
              TASK_STATUS_MAP[row.taskStatus ?? 0] ?? row.taskStatus
            )
          }
        },
        { prop: 'createTime', label: '创建时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          formatter: (row) => {
            if (!hasAuth('mall:task:list')) return ''
            return h(
              ElButton,
              { link: true, type: 'primary', onClick: () => openDetail(row.id) },
              () => '处理'
            )
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    getData()
  }

  function openDetail(id?: number) {
    currentTaskId.value = id
    detailVisible.value = true
  }
</script>

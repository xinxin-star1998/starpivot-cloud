<!-- 操作日志页面 -->
<template>
  <div class="oper-log-page art-full-height">
    <!-- 搜索栏 -->
    <ElCollapseTransition>
      <div v-show="showSearchBar">
        <OperLogSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />
      </div>
    </ElCollapseTransition>

    <ElCard class="art-table-card" shadow="never" :style="cardStyle">
      <!-- 表格头部 -->
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
              v-auth="'system:operlog:delete'"
            >
              批量删除
            </ElButton>
            <ElButton type="danger" @click="handleClean" v-ripple v-auth="'system:operlog:delete'">
              清空日志
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- 详情对话框 -->
      <OperLogDetail v-model:visible="detailDialogVisible" :oper-log="currentOperLog" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchCleanOperLog, fetchDeleteOperLog, fetchGetOperLogList } from '@/api/log/operlog'
  import { ElButton, ElCollapseTransition, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import type { OperLogListItem, OperLogSearchParams } from '@/types/api/operlog'
  import OperLogDetail from './modules/oper-log-detail.vue'
  import OperLogSearch from './modules/oper-log-search.vue'
  import { getOperBusinessTypeLabel } from './constants'

  defineOptions({ name: 'OperLog' })

  const { hasAuth } = useAuth()

  // 搜索栏显示状态
  const showSearchBar = ref(true)

  // 卡片样式（根据搜索栏显示状态动态调整）
  const cardStyle = computed(() => ({
    'margin-top': showSearchBar.value ? '12px' : '0'
  }))

  // 搜索表单（包含 dateRange 用于搜索组件）
  const searchForm = ref<OperLogSearchParams & { dateRange?: [string, string] | null }>({
    title: undefined,
    businessType: undefined,
    operName: undefined,
    status: undefined,
    startTime: undefined,
    endTime: undefined,
    dateRange: null
  })

  // 选中行
  const selectedRows = ref<OperLogListItem[]>([])

  // 详情对话框
  const detailDialogVisible = ref(false)
  const currentOperLog = ref<OperLogListItem | null>(null)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    // 核心配置
    core: {
      apiFn: fetchGetOperLogList,
      apiParams: {
        pageSize: 20
      },
      immediate: true, // 确保页面加载时自动获取数据
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'operTime',
          label: '操作时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'title',
          label: '模块标题',
          width: 150
        },
        {
          prop: 'businessType',
          label: '业务类型',
          width: 100,
          formatter: (row: OperLogListItem) => {
            return getOperBusinessTypeLabel(row.businessType)
          }
        },
        {
          prop: 'operName',
          label: '操作人员',
          width: 120
        },
        {
          prop: 'deptName',
          label: '部门',
          width: 120
        },
        {
          prop: 'operUrl',
          label: '请求URL',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'requestMethod',
          label: '请求方式',
          width: 100,
          formatter: (row: OperLogListItem) => {
            const method = row.requestMethod || ''
            const colorMap: Record<string, string> = {
              GET: 'success',
              POST: 'primary',
              PUT: 'warning',
              DELETE: 'danger',
              PATCH: 'info'
            }
            return h(
              ElTag,
              {
                type: (colorMap[method] || 'info') as
                  | 'primary'
                  | 'success'
                  | 'warning'
                  | 'info'
                  | 'danger',
                size: 'small'
              },
              () => method
            )
          }
        },
        {
          prop: 'operIp',
          label: '操作IP',
          width: 140
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: OperLogListItem) => {
            return h(
              ElTag,
              {
                type: row.status === 0 ? 'success' : 'danger',
                size: 'small'
              },
              () => (row.status === 0 ? '正常' : '异常')
            )
          }
        },
        {
          prop: 'costTime',
          label: '耗时(ms)',
          width: 100,
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: OperLogListItem) => {
            return h('div', { class: 'flex gap-2' }, [
              h(
                ElButton,
                {
                  type: 'primary',
                  link: true,
                  size: 'small',
                  onClick: () => showDetail(row)
                },
                () => '详情'
              ),
              hasAuth('system:operlog:delete') &&
                h(
                  ElButton,
                  {
                    type: 'danger',
                    link: true,
                    size: 'small',
                    onClick: () => handleDelete(row)
                  },
                  () => '删除'
                )
            ])
          }
        }
      ]
    }
  })

  /**
   * 搜索
   * @param params 搜索参数（已由搜索组件处理日期范围转换）
   */
  const handleSearch = (params?: OperLogSearchParams) => {
    // 如果传入了参数，使用传入的参数（搜索组件已处理日期范围）
    if (params) {
      Object.assign(searchForm.value, params)
    }
    // 使用 getData 方法（实际是 getDataByPage），会自动重置到第一页并清空当前搜索条件的缓存
    getData(searchForm.value)
  }

  /**
   * 重置搜索参数
   */
  const resetSearchParams = () => {
    // 重置搜索表单
    searchForm.value = {
      title: undefined,
      businessType: undefined,
      operName: undefined,
      status: undefined,
      startTime: undefined,
      endTime: undefined,
      dateRange: null
    }
    // 直接使用重置后的搜索表单重新获取数据（getData 内部会重置到第一页）
    getData(searchForm.value)
  }

  /**
   * 显示详情
   */
  const showDetail = (row: OperLogListItem) => {
    currentOperLog.value = row
    detailDialogVisible.value = true
  }

  /**
   * 删除
   */
  const handleDelete = async (row: OperLogListItem) => {
    try {
      await ElMessageBox.confirm('确定要删除这条操作日志吗？', '提示', {
        type: 'warning'
      })
      await fetchDeleteOperLog([row.operId!])
      ElMessage.success('删除成功')
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error?.message || '删除失败')
      }
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的日志')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 条操作日志吗？`,
        '提示',
        {
          type: 'warning'
        }
      )
      const operIds = selectedRows.value.map((row: OperLogListItem) => row.operId!).filter(Boolean)
      await fetchDeleteOperLog(operIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error?.message || '删除失败')
      }
    }
  }

  /**
   * 清空日志
   */
  const handleClean = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      })
      await fetchCleanOperLog()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error?.message || '清空失败')
      }
    }
  }

  /**
   * 选中行变化
   */
  const handleSelectionChange = (selection: OperLogListItem[]) => {
    selectedRows.value = selection
  }
</script>

<style scoped lang="scss">
  .oper-log-page {
    padding: 20px;
  }
</style>

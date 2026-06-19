<!-- 登录日志页面 -->
<template>
  <div class="logininfor-page art-full-height">
    <!-- 搜索栏 -->
    <ElCollapseTransition>
      <div v-show="showSearchBar">
        <LogininforSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />
      </div>
    </ElCollapseTransition>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <!-- 表格头部 -->
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              type="danger"
              :disabled="!hasSelectedRows"
              @click="handleBatchDelete"
              v-ripple
              v-auth="AUTH_DELETE"
            >
              批量删除
            </ElButton>
            <ElButton type="danger" @click="handleClean" v-ripple v-auth="AUTH_DELETE">
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
      />

      <!-- 详情对话框 -->
      <LogininforDetail v-model:visible="detailDialogVisible" :logininfor="currentLogininfor" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetLogininforList,
    fetchDeleteLogininfor,
    fetchCleanLogininfor
  } from '@/api/log/logininfor'
  import { ElMessageBox, ElMessage, ElTag, ElButton, ElCollapseTransition } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import type { LogininforListItem, LogininforSearchParams } from '@/types/api/logininfor'
  import LogininforDetail from './modules/logininfor-detail.vue'
  import LogininforSearch from './modules/logininfor-search.vue'

  defineOptions({ name: 'Logininfor' })

  // ==================== 常量定义 ====================
  /** 权限标识 */
  const AUTH_DELETE = 'system:logininfor:delete'

  /** 登录状态值 */
  const LOGIN_STATUS = {
    SUCCESS: '0',
    FAILED: '1'
  } as const

  /** 默认分页大小 */
  const DEFAULT_PAGE_SIZE = 20

  // ==================== 组合式函数 ====================
  const { hasAuth } = useAuth()

  // ==================== 响应式数据 ====================
  /** 搜索栏显示状态 */
  const showSearchBar = ref(true)

  /** 搜索表单（包含 dateRange 用于搜索组件） */
  const searchForm = ref<LogininforSearchParams & { dateRange?: [string, string] | null }>({
    userName: undefined,
    ipaddr: undefined,
    status: undefined,
    startTime: undefined,
    endTime: undefined,
    dateRange: null
  })

  /** 选中的行数据 */
  const selectedRows = ref<LogininforListItem[]>([])

  /** 详情对话框显示状态 */
  const detailDialogVisible = ref(false)

  /** 当前查看的登录日志 */
  const currentLogininfor = ref<LogininforListItem | null>(null)

  // ==================== 计算属性 ====================
  /** 是否有选中的行 */
  const hasSelectedRows = computed(() => selectedRows.value.length > 0)

  // ==================== 表格配置 ====================
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
    core: {
      apiFn: fetchGetLogininforList,
      apiParams: {
        pageSize: DEFAULT_PAGE_SIZE
      },
      immediate: true,
      columnsFactory: () => createTableColumns()
    }
  })

  /**
   * 创建表格列配置
   */
  function createTableColumns() {
    return [
      { type: 'selection' as const },
      { type: 'index' as const, width: 60, label: '序号' },
      {
        prop: 'loginTime',
        label: '登录时间',
        width: 180,
        sortable: true
      },
      {
        prop: 'userName',
        label: '用户账号',
        width: 120
      },
      {
        prop: 'ipaddr',
        label: '登录IP',
        width: 140
      },
      {
        prop: 'loginLocation',
        label: '登录地点',
        width: 150
      },
      {
        prop: 'browser',
        label: '浏览器',
        width: 180,
        showOverflowTooltip: true
      },
      {
        prop: 'os',
        label: '操作系统',
        width: 150,
        showOverflowTooltip: true
      },
      {
        prop: 'status',
        label: '登录状态',
        width: 100,
        formatter: (row: LogininforListItem) => {
          const isSuccess = row.status === LOGIN_STATUS.SUCCESS
          return h(
            ElTag,
            {
              type: isSuccess ? 'success' : 'danger',
              size: 'small'
            },
            () => (isSuccess ? '成功' : '失败')
          )
        }
      },
      {
        prop: 'msg',
        label: '提示消息',
        minWidth: 200,
        showOverflowTooltip: true
      },
      {
        prop: 'operation',
        label: '操作',
        width: 120,
        fixed: 'right' as const,
        formatter: (row: LogininforListItem) => {
          return h('div', { class: 'flex gap-2' }, [
            h(
              ElButton,
              {
                type: 'primary',
                link: true,
                size: 'small',
                onClick: () => handleShowDetail(row)
              },
              () => '详情'
            ),
            hasAuth(AUTH_DELETE) &&
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

  // ==================== 搜索相关方法 ====================
  /**
   * 处理搜索
   * @param params 搜索参数（已由搜索组件处理日期范围转换）
   */
  const handleSearch = (params?: LogininforSearchParams) => {
    // 如果传入了参数，使用传入的参数（搜索组件已处理日期范围）
    if (params) {
      Object.assign(searchForm.value, params)
    }
    // 执行搜索，会自动重置到第一页
    getData(searchForm.value)
  }

  /**
   * 处理重置
   */
  const handleReset = () => {
    // 重置搜索表单
    searchForm.value = {
      userName: undefined,
      ipaddr: undefined,
      status: undefined,
      startTime: undefined,
      endTime: undefined,
      dateRange: null
    }
    // 直接使用重置后的搜索表单重新获取数据（getData 内部会重置到第一页）
    getData(searchForm.value)
  }

  // ==================== 详情相关方法 ====================
  /**
   * 显示详情
   */
  const handleShowDetail = (row: LogininforListItem) => {
    currentLogininfor.value = row
    detailDialogVisible.value = true
  }

  // ==================== 删除相关方法 ====================
  /**
   * 删除单条日志
   */
  const handleDelete = async (row: LogininforListItem) => {
    if (!row.infoId) {
      ElMessage.warning('日志ID不存在')
      return
    }

    try {
      await ElMessageBox.confirm('确定要删除这条登录日志吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })

      await fetchDeleteLogininfor([row.infoId])
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
    if (!hasSelectedRows.value) {
      ElMessage.warning('请选择要删除的日志')
      return
    }

    const infoIds = selectedRows.value
      .map((row: LogininforListItem) => row.infoId)
      .filter((id: number | undefined): id is number => Boolean(id))

    if (infoIds.length === 0) {
      ElMessage.warning('所选日志中没有有效的ID')
      return
    }

    try {
      await ElMessageBox.confirm(`确定要删除选中的 ${infoIds.length} 条登录日志吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })

      await fetchDeleteLogininfor(infoIds)
      ElMessage.success('删除成功')
      // 清空选中项
      selectedRows.value = []
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error?.message || '删除失败')
      }
    }
  }

  /**
   * 清空所有日志
   */
  const handleClean = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有登录日志吗？此操作不可恢复！', '警告', {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      })

      await fetchCleanLogininfor()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error?.message || '清空失败')
      }
    }
  }

  // ==================== 表格事件处理 ====================
  /**
   * 处理选中行变化
   */
  const handleSelectionChange = (selection: LogininforListItem[]) => {
    selectedRows.value = selection
  }

  /**
   * 处理刷新
   */
  const handleRefresh = () => {
    refreshData()
  }
</script>

<style scoped lang="scss">
  .logininfor-page {
    padding: 20px;
  }
</style>

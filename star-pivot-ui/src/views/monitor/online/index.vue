<!--
  在线用户页面
  
  说明：
  - 当前为简化实现，基于 JWT 无状态认证
  - 通过 Redis 中存储的刷新令牌（jwt:refresh:user:{userId}）来判断用户是否在线
  - 强制下线时删除对应的刷新令牌，使 JWT Token 无法刷新而失效
  - 后续可按需优化为更完善的会话管理方案（如：引入 Session 机制、记录登录 IP/设备信息等）
-->
<template>
  <div class="online-user-page art-full-height">
    <!-- 搜索栏 -->
    <ElCollapseTransition>
      <div v-show="showSearchBar">
        <ElCard shadow="never" style="margin-bottom: 12px">
          <ElForm :model="searchForm" :inline="true">
            <ElFormItem label="用户名">
              <ElInput
                v-model="searchForm.userName"
                placeholder="请输入用户名"
                clearable
                style="width: 200px"
              />
            </ElFormItem>
            <ElFormItem label="IP地址">
              <ElInput
                v-model="searchForm.ipaddr"
                placeholder="请输入IP地址"
                clearable
                style="width: 200px"
              />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" :icon="Search" @click="handleSearch">查询</ElButton>
              <ElButton :icon="Refresh" @click="resetSearchParams">重置</ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>
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
              @click="handleBatchForceLogout"
              v-ripple
              v-auth="'monitor:online:force-logout'"
            >
              批量强制下线
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        @selection-change="handleSelectionChange"
      >
        <template #operation="{ row }">
          <ElButton
            type="danger"
            link
            size="small"
            @click="handleForceLogout(row)"
            v-auth="'monitor:online:force-logout'"
          >
            强制下线
          </ElButton>
        </template>
      </ArtTable>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { Refresh, Search } from '@element-plus/icons-vue'
  import { fetchForceLogout, fetchGetOnlineUserList } from '@/api/monitor/online'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { usePageVisibility } from '@/hooks/core/usePageVisibility'
  import type { OnlineUser, OnlineUserQueryParams } from '@/types/api/monitor'
  import type { ColumnOption } from '@/types'

  defineOptions({ name: 'OnlineUser' })

  // 页面可见性检测 - 页面不可见时暂停刷新
  const { onPause, onResume } = usePageVisibility()
  onPause(() => stopAutoRefresh())
  onResume(() => startAutoRefresh())

  // 搜索栏显示状态
  const showSearchBar = ref(true)

  // 卡片样式
  const cardStyle = computed(() => ({
    'margin-top': showSearchBar.value ? '12px' : '0'
  }))

  // 搜索表单
  const searchForm = ref<OnlineUserQueryParams>({
    userName: undefined,
    ipaddr: undefined
  })

  // 数据
  const loading = ref(false)
  const data = ref<OnlineUser[]>([])

  // 选中行
  const selectedRows = ref<OnlineUser[]>([])

  // 表格列
  const columns = ref<ColumnOption[]>([
    { type: 'selection' },
    { type: 'index', width: 60, label: '序号' },
    { prop: 'userName', label: '用户名', width: 120 },
    { prop: 'nickName', label: '用户昵称', width: 120 },
    { prop: 'deptName', label: '部门', width: 120 },
    { prop: 'ipaddr', label: 'IP地址', width: 150 },
    { prop: 'loginLocation', label: '登录地点', width: 150 },
    { prop: 'browser', label: '浏览器', width: 120 },
    { prop: 'os', label: '操作系统', width: 120 },
    { prop: 'loginTime', label: '登录时间', width: 180 },
    { prop: 'lastAccessTime', label: '最后访问时间', width: 180 }
    // { prop: 'operation', label: '操作', width: 120, fixed: 'right', slot: 'operation' }
  ])

  const columnChecks = ref<ColumnOption[]>([...columns.value])

  /**
   * 获取在线用户数据
   * - 成功：request 层直接返回 data，赋值给 data
   * - 失败：由 HTTP 拦截器统一 showError 提示，此处仅记录日志并确保 loading 关闭
   */
  const getData = async () => {
    loading.value = true
    try {
      const result = await fetchGetOnlineUserList(searchForm.value)
      data.value = result || []
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error('获取在线用户列表失败:', error)
      }
      data.value = []
    } finally {
      loading.value = false
    }
  }

  // 刷新数据
  const refreshData = () => {
    getData()
  }

  // 搜索
  const handleSearch = () => {
    getData()
  }

  // 重置搜索
  const resetSearchParams = () => {
    searchForm.value = {
      userName: undefined,
      ipaddr: undefined
    }
    getData()
  }

  // 选中行变化
  const handleSelectionChange = (rows: OnlineUser[]) => {
    selectedRows.value = rows
  }

  // 强制下线
  const handleForceLogout = async (row: OnlineUser) => {
    try {
      await ElMessageBox.confirm(`确定要强制用户 "${row.userName}" 下线吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchForceLogout(row.sessionId)
      ElMessage.success('强制下线成功')
      getData()
    } catch (error) {
      // 取消操作不处理，其他错误由 HTTP 拦截器统一处理
      if (error !== 'cancel' && import.meta.env.DEV) {
        console.error('强制下线失败:', error)
      }
    }
  }

  // 批量强制下线
  const handleBatchForceLogout = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要强制下线的用户')
      return
    }

    try {
      await ElMessageBox.confirm(`确定要强制 ${selectedRows.value.length} 个用户下线吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const promises = selectedRows.value.map((row) => fetchForceLogout(row.sessionId))
      await Promise.all(promises)

      ElMessage.success(`已成功强制 ${selectedRows.value.length} 个用户下线`)
      selectedRows.value = []
      getData()
    } catch (error) {
      // 取消操作不处理，其他错误由 HTTP 拦截器统一处理
      if (error !== 'cancel' && import.meta.env.DEV) {
        console.error('批量强制下线失败:', error)
      }
    }
  }

  // 自动刷新（每10秒）
  let refreshTimer: number | null = null
  const startAutoRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
    }
    refreshTimer = window.setInterval(() => {
      getData()
    }, 10000)
  }

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
  .online-user-page {
    padding: 20px;
    background-color: var(--default-bg-color);
  }

  :deep(.el-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgb(0 0 0 / 8%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgb(0 0 0 / 12%);
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

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
    }
  }
</style>

<!-- 参数配置管理页面 -->
<template>
  <div class="config-page">
    <!-- 搜索栏 -->
    <ConfigSearch
      v-model="searchForm"
      v-auth="'system:config:list'"
      @reset="resetSearchParams"
      @search="handleSearch"
    />

    <ElCard class="art-table-card config-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace class="table-actions" wrap>
            <ElButton
              v-auth="'system:config:add'"
              v-ripple
              class="action-btn"
              @click="showDialog('add')"
            >
              新增参数配置
            </ElButton>
            <ElButton
              v-auth="'system:config:export'"
              v-ripple
              :loading="exporting"
              plain
              type="primary"
              @click="handleExport"
            >
              参数导出
            </ElButton>
            <ElButton
              v-auth="'system:config:delete'"
              v-ripple
              :disabled="selectedRows.length === 0"
              class="action-btn"
              type="danger"
              @click="handleBatchDelete"
            >
              批量删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        class="config-table"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- 参数配置弹窗 -->
      <ConfigDialog
        v-model:visible="dialogVisible"
        :config-data="currentConfigData"
        :type="dialogType"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script lang="ts" setup>
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {type Config, fetchDeleteConfig, fetchExportConfig, fetchGetConfigList} from '@/api/system/config/config'
import ConfigSearch from './modules/config-search.vue'
import ConfigDialog from './modules/config-dialog.vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DialogType} from '@/types'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {useAuth} from '@/hooks/core/useAuth'
import {h, nextTick, ref} from 'vue'

defineOptions({ name: 'Config' })

  const { hasAuth } = useAuth()

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentConfigData = ref<Partial<Config>>({})

  // 选中行
  const selectedRows = ref<Config[]>([])
  const exporting = ref(false)

  // 搜索表单
  const searchForm = ref({
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: undefined
  })

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
    // 核心配置
    core: {
      apiFn: fetchGetConfigList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'configName',
          label: '参数名称',
          width: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'configKey',
          label: '参数键名',
          width: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'configValue',
          label: '参数键值',
          minWidth: 100,
          showOverflowTooltip: true
        },
        {
          prop: 'configType',
          label: '系统内置（Y是 N否）',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right', // 固定列
          formatter: (row) => {
            const actions: any[] = []

            // 编辑参数配置按钮权限：system:config:edit
            if (hasAuth('system:config:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }

            // 删除参数配置按钮权限：system:config:remove
            if (hasAuth('system:config:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => deleteConfig(row)
                })
              )
            }

            if (actions.length === 0) {
              // 无任何操作权限时返回空占位
              return h('span', { style: 'color: var(--art-gray-500)' }, '')
            }

            return h('div', actions)
          }
        }
      ]
    },
    // 数据处理
    transform: {
      // 数据转换器
      dataTransformer: (records) => {
        // 类型守卫检查
        if (!Array.isArray(records)) {
          return []
        }

        return records
      }
    }
  })

  /**
   * 搜索处理
   * @param params 参数
   */
  const handleSearch = (params: Record<string, any>) => {
    // 搜索参数赋值
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 显示参数配置弹窗
   */
  const showDialog = (type: DialogType, row?: Config): void => {
    dialogType.value = type
    currentConfigData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除参数配置
   */
  const deleteConfig = async (row: Config): Promise<void> => {
    if (!row.configId) {
      ElMessage.warning('参数ID不存在，无法删除')
      return
    }
    try {
      await ElMessageBox.confirm(`确定要删除该参数配置吗？`, '删除参数配置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      })
      await fetchDeleteConfig([row.configId])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除参数配置失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 批量删除参数配置
   */
  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的参数配置')
      return
    }
    const names = selectedRows.value.map((row) => row.configId).join('、')
    ElMessageBox.confirm(`确定要删除以下参数配置吗？\n${names}`, '批量删除参数配置', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
      .then(async () => {
        const ids = selectedRows.value
          .map((row) => row.configId)
          .filter((id): id is number => typeof id === 'number')
        if (ids.length === 0) {
          ElMessage.warning('未找到可删除的参数ID')
          return
        }
        await fetchDeleteConfig(ids)
        selectedRows.value = []
        refreshData()
        ElMessage.success('删除成功')
      })
      .catch(() => {
        // 用户取消删除
      })
  }

  /**
   * 导出参数配置
   */
  const handleExport = async (): Promise<void> => {
    try {
      exporting.value = true
      await fetchExportConfig(searchParams as Record<string, any>)
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出参数配置失败:', error)
      ElMessage.error('导出失败')
    } finally {
      exporting.value = false
    }
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentConfigData.value = {}
      // 刷新列表数据
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: Config[]): void => {
    selectedRows.value = selection
  }
</script>

<style lang="scss" scoped>
  .config-page {
    padding: 0 var(--art-page-padding) var(--art-page-padding);
  }

  .search-panel {
    padding: 14px 16px 2px;
    margin-bottom: 12px;
    background-color: var(--default-box-color);
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  .config-table-card {
    overflow: hidden;
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  .table-actions {
    :deep(.el-button + .el-button) {
      margin-left: 8px;
    }
  }

  .action-btn {
    font-weight: 500;
    border-radius: 8px;
    box-shadow: var(--art-shadow-sm);
  }

  .config-table {
    :deep(.el-table) {
      --el-table-header-bg-color: var(--art-gray-100);
      --el-table-row-hover-bg-color: var(--art-gray-50);
    }

    :deep(.el-table th.el-table__cell) {
      font-weight: 600;
      color: var(--art-gray-800);
    }

    :deep(.el-table td.el-table__cell) {
      padding: 10px 0;
    }
  }
</style>

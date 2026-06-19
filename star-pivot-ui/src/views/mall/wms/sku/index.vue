<!-- 商品库存管理页面 -->
<template>
  <div class="sku-page art-full-height">
    <!-- 搜索栏 -->
    <SkuSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></SkuSearch>

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple v-auth="'mall:sku:add'"
              >新增商品库存</ElButton
            >
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
              v-auth="'mall:sku:delete'"
            >
              批量删除
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

      <!-- 商品库存弹窗 -->
      <SkuDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :sku-data="currentSkuData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchDeleteSku, fetchGetSkuList, type WareSku } from '@/api/mall/ware-sku'
  import SkuSearch from './modules/sku-search.vue'
  import SkuDialog from './modules/sku-dialog.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { DialogType } from '@/types'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'WareSku' })

  const { hasAuth } = useAuth()

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentSkuData = ref<Partial<WareSku>>({})

  // 选中行
  const selectedRows = ref<WareSku[]>([])

  // 搜索表单
  const searchForm = ref({
    skuId: undefined,
    wareId: undefined,
    stock: undefined,
    skuName: undefined,
    stockLocked: undefined
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
      apiFn: fetchGetSkuList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'id',
          label: 'id',
          width: 100,
          sortable: true
        },
        {
          prop: 'skuId',
          label: 'sku_id',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'wareId',
          label: '仓库id',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'stock',
          label: '库存数',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'skuName',
          label: 'sku_name',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'stockLocked',
          label: '锁定库存',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right', // 固定列
          formatter: (row) => {
            const actions: any[] = []

            // 编辑商品库存按钮权限：mall:sku:edit
            if (hasAuth('mall:sku:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }

            // 删除商品库存按钮权限：mall:sku:remove
            if (hasAuth('mall:sku:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => deleteSku(row)
                })
              )
            }

            if (actions.length === 0) {
              // 无任何操作权限时返回空占位
              return h('span', { style: 'color: #999' }, '')
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
   * 显示商品库存弹窗
   */
  const showDialog = (type: DialogType, row?: WareSku): void => {
    dialogType.value = type
    currentSkuData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除商品库存
   */
  const deleteSku = async (row: WareSku): Promise<void> => {
    if (row.id == null) {
      ElMessage.warning('无效的记录')
      return
    }
    try {
      await ElMessageBox.confirm(`确定要删除该商品库存吗？`, '删除商品库存', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      })
      await fetchDeleteSku([row.id])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除商品库存失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 批量删除商品库存
   */
  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的商品库存')
      return
    }
    const names = selectedRows.value.map((row) => row.id).join('、')
    ElMessageBox.confirm(`确定要删除以下商品库存吗？\n${names}`, '批量删除商品库存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
      .then(() => {
        const ids = selectedRows.value.map((row) => row.id).filter((id): id is number => id != null)
        if (!ids.length) {
          ElMessage.warning('所选记录无效')
          return
        }
        fetchDeleteSku(ids)
        selectedRows.value = []
        refreshData()
        ElMessage.success('删除成功')
      })
      .catch(() => {
        // 用户取消删除
      })
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentSkuData.value = {}
      // 刷新列表数据
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: WareSku[]): void => {
    selectedRows.value = selection
  }
</script>

<style scoped lang="scss">
  .sku-page {
    padding: 20px;
  }
</style>

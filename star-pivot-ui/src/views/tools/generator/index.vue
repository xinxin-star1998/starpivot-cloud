<!-- 代码生成页面 -->
<template>
  <div class="generator-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :span="6"
      label-width="84px"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton
              @click="handleBatchGenerateCode(selectedRows)"
              v-ripple
              v-auth="'tool:gen:create'"
            >
              <ArtSvgIcon icon="ri:download-line" class="mr-1" />
              生成
            </ElButton>
            <ElButton @click="handleCreateTable" v-ripple v-auth="'tool:gen:add'">
              <ArtSvgIcon icon="ri:add-line" class="mr-1" />
              创建表
            </ElButton>
            <ElButton @click="handleImportTable" v-ripple v-auth="'tool:gen:import'">
              <ArtSvgIcon icon="ri:file-upload-line" class="mr-1" />
              导入表
            </ElButton>
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleDeleteTable"
              v-ripple
              v-auth="'tool:gen:delete'"
            >
              <ArtSvgIcon icon="ri:delete-bin-line" class="mr-1" />
              删除
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
      <!-- 创建表弹窗 -->
      <genAddDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        @submit="handleDialogSubmit"
      />
      <!-- 导入表弹窗 -->
      <ImportDialog v-model:visible="importDialogVisible" @success="handleImportSuccess" />
      <!-- 代码预览弹窗 -->
      <PreviewDialog v-model:visible="previewVisible" :table-id="previewTableId" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { ElButton, ElMessage, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import {
    fetchBatchGenerateCode,
    fetchDeleteTable,
    fetchGenerateCode,
    fetchGetGenTableList,
    fetchSyncDatabase
  } from '@/api/generator/gen-table'
  import FileSaver from 'file-saver'
  import { DialogType } from '@/types'

  const genAddDialog = defineAsyncComponent(
    () => import('@views/tools/generator/modules/gen-add-dialog.vue')
  )
  const ImportDialog = defineAsyncComponent(
    () => import('@views/tools/generator/modules/ImportDialog.vue')
  )
  const PreviewDialog = defineAsyncComponent(
    () => import('@views/tools/generator/modules/PreviewDialog.vue')
  )

  defineOptions({ name: 'Generator' })

  // 权限检查
  const { hasAuth } = useAuth()
  const router = useRouter()
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  // 导入表弹窗可见性
  const importDialogVisible = ref(false)
  // 代码预览弹窗可见性
  const previewVisible = ref(false)
  // 当前预览的表ID
  const previewTableId = ref<number>()
  type GenTableListItem = Api.Generator.GenTableListItem

  // 搜索表单
  const searchForm = ref({
    tableName: undefined,
    tableComment: undefined,
    className: undefined
  })

  // 选中行
  const selectedRows = ref<GenTableListItem[]>([])

  // 搜索表单配置
  const formItems = computed(() => [
    {
      label: '表名称',
      key: 'tableName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入表名称' }
    },
    {
      label: '表描述',
      key: 'tableComment',
      type: 'input',
      props: { clearable: true, placeholder: '请输入表描述' }
    },
    {
      label: '实体类名称',
      key: 'className',
      type: 'input',
      props: { clearable: true, placeholder: '请输入实体类名称' }
    }
  ])

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
      apiFn: fetchGetGenTableList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'tableName',
          label: '表名称',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'tableComment',
          label: '表描述',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'className',
          label: '实体类名称',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'tplCategory',
          label: '模板类型',
          width: 120,
          formatter: (row: GenTableListItem) => {
            const tplMap: Record<string, { text: string; type: 'success' | 'info' | 'warning' }> = {
              crud: { text: '单表操作', type: 'success' },
              tree: { text: '树表操作', type: 'info' }
            }
            const config = tplMap[row.tplCategory || ''] || {
              text: row.tplCategory || '-',
              type: 'info' as const
            }
            return h(ElTag, { type: config.type }, () => config.text)
          }
        },
        {
          prop: 'tplWebType',
          label: '前端模板',
          width: 120,
          formatter: (row: GenTableListItem) => {
            const webTypeMap: Record<
              string,
              { text: string; type: 'success' | 'info' | 'warning' }
            > = {
              'element-ui': { text: 'Element UI', type: 'info' },
              'element-plus': { text: 'Element Plus', type: 'success' },
              'art-design-pro': { text: 'Art Design Pro', type: 'warning' }
            }
            const config = webTypeMap[row.tplWebType || ''] || {
              text: row.tplWebType || '-',
              type: 'info' as const
            }
            return h(ElTag, { type: config.type }, () => config.text)
          }
        },
        {
          prop: 'functionName',
          label: '功能名称',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 200,
          fixed: 'right',
          align: 'center',
          formatter: (row: GenTableListItem) => {
            const actions: any[] = []

            // 预览按钮权限：tool:gen:preview
            if (hasAuth('tool:gen:preview')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'view',
                  onClick: () => handlePreview(row.tableId as number)
                })
              )
            }

            // 编辑按钮权限：tool:gen:edit
            if (hasAuth('tool:gen:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => handleEdit(row.tableId as number)
                })
              )
            }

            // 删除按钮权限：tool:gen:remove
            if (hasAuth('tool:gen:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => handleDelete(row.tableId as number)
                })
              )
            }

            // 同步按钮权限：tool:gen:sync
            if (hasAuth('tool:gen:sync')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'sync',
                  onClick: () =>
                    handleSync(row.tableName as string, row.tableComment || row.tableName)
                })
              )
            }

            // 生成代码按钮权限：tool:gen:code
            if (hasAuth('tool:gen:create')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'generate',
                  onClick: () => handleGenerateCode(row)
                })
              )
            }

            if (actions.length === 0) {
              // 无任何操作权限时返回空占位
              return h('span', { style: 'color: #999' }, '')
            }

            return h('div', { class: 'flex items-center justify-center' }, actions)
          }
        }
      ]
    }
  })

  /**
   * 搜索处理
   * ArtSearchBar 的 search 事件不携带参数，这里直接使用 v-model 的 searchForm
   */
  const handleSearch = () => {
    Object.assign(searchParams, searchForm.value)
    getData()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    searchForm.value = {
      tableName: undefined,
      tableComment: undefined,
      className: undefined
    }
    resetSearchParams()
    getData()
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: GenTableListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 创建表
   */
  const handleCreateTable = () => {
    dialogVisible.value = true
    dialogType.value = 'add'
  }

  /**
   * 打开导入表弹窗
   */
  const handleImportTable = () => {
    importDialogVisible.value = true
  }

  /**
   * 导入表成功后刷新列表
   */
  const handleImportSuccess = () => {
    refreshData()
  }
  /**
   * 跳转到代码生成编辑页面
   * @param tableId 表ID
   */
  const handleEdit = (tableId: number) => {
    // 跳转到编辑页，由编辑页去加载表的详细配置信息
    router.push(`/tool/gen/edit/${tableId}`)
  }
  /**
   * 处理弹窗提交（创建表成功后的回调）
   * 子组件已经处理了创建逻辑，这里只需要刷新列表
   */
  const handleDialogSubmit = () => {
    // 子组件已经调用了创建表接口并显示了成功消息
    // 这里只需要刷新表格数据
    refreshData()
  }

  /**
   * 删除单个表（单删）
   * @param tableId 表ID
   */
  const handleDelete = async (tableId: number): Promise<void> => {
    try {
      // 查找对应的表信息用于提示
      const tableList = (data.value ?? []) as GenTableListItem[]
      const table = tableList.find((item) => item.tableId === tableId)
      const tableName = table?.tableName || '该表'

      await ElMessageBox.confirm(`确定要删除表"${tableName}"吗？此操作不可恢复！`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchDeleteTable([tableId])
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      // 用户取消删除时不显示错误
      if (error !== 'cancel') {
        console.error('删除表失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 预览代码
   * @param tableId 表ID
   */
  const handlePreview = (tableId: number): void => {
    previewTableId.value = tableId
    previewVisible.value = true
  }
  /**
   * 同步数据库
   * @param tableName 表名称
   * @param tableComment 表描述（用于提示）
   */
  const handleSync = async (tableName: string, tableComment?: string): Promise<void> => {
    try {
      const displayName = tableComment || tableName
      await ElMessageBox.confirm(
        `确定要同步表"${displayName}"的数据库结构吗？\n同步操作将更新表的字段信息。`,
        '同步确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchSyncDatabase(tableName)
      ElMessage.success('同步成功')
      // 同步成功后刷新数据
      refreshData()
    } catch (error) {
      // 用户取消同步时不显示错误
      if (error !== 'cancel') {
        console.error('同步数据库失败:', error)
        ElMessage.error('同步失败，请稍后重试')
      }
    }
  }
  /**
   * 批量生成代码（下载方式）
   * @param selectedRows 选中的表数据
   */
  const handleBatchGenerateCode = async (selectedRows: GenTableListItem[]): Promise<void> => {
    if (selectedRows.length === 0) {
      ElMessage.warning('请选择要生成代码的表')
      return
    }

    const tableNames = selectedRows.map((row: GenTableListItem) => row.tableName)
    const tableNamesStr = tableNames.join('、')
    try {
      await ElMessageBox.confirm(
        `确定要批量生成以下 ${selectedRows.length} 个表的代码吗？\n${tableNamesStr}\n代码将以 zip 压缩包形式下载。`,
        '批量生成代码确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )

      // 调用批量生成代码接口
      const blob = await fetchBatchGenerateCode(tableNames)
      // 下载 zip 文件
      const fileName = `starPivot_${new Date().getTime()}.zip`
      FileSaver.saveAs(blob, fileName)
      ElMessage.success('代码生成成功，文件已下载')
    } catch (error) {
      // 用户取消时不显示错误
      if (error !== 'cancel') {
        console.error('批量生成代码失败:', error)
        ElMessage.error('批量生成代码失败，请稍后重试')
      }
    }
  }
  /**
   * 生成代码
   * @param row 表数据行
   */
  const handleGenerateCode = async (row: GenTableListItem): Promise<void> => {
    const tableName = row.tableName
    try {
      await ElMessageBox.confirm(
        `确定要生成表"${tableName}"的代码吗？\n代码将以 zip 压缩包形式下载。`,
        '生成代码确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )

      const blob = await fetchGenerateCode(tableName)
      const fileName = `${tableName}_${new Date().getTime()}.zip`
      FileSaver.saveAs(blob, fileName)
      ElMessage.success('代码生成成功，文件已下载')
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('生成代码失败:', error)
        const errorMessage = error?.message || error?.msg || ''
        ElMessage.error(errorMessage || '生成代码失败，请稍后重试')
      }
    }
  }

  /**
   * 批量删除表
   */
  const handleDeleteTable = async (): Promise<void> => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的表')
      return
    }

    try {
      const tableNames = selectedRows.value.map((row: GenTableListItem) => row.tableName).join(',')
      await ElMessageBox.confirm(
        `确定要删除以下 ${selectedRows.value.length} 个表吗？\n${tableNames}\n此操作不可恢复！`,
        '批量删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      const tableIds = selectedRows.value.map((row: GenTableListItem) => row.tableId as number)
      await fetchDeleteTable(tableIds)
      ElMessage.success('删除成功')
      selectedRows.value = []
      refreshData()
    } catch (error) {
      // 用户取消删除时不显示错误
      if (error !== 'cancel') {
        console.error('批量删除表失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }
</script>

<style scoped lang="scss">
  .generator-page {
    padding: 16px;
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
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

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }

  /* 统一搜索输入框宽度，并让操作按钮与输入项基线对齐 */
  :deep(.art-search-bar .el-form-item__content > .el-input) {
    width: 100%;
  }

  :deep(.art-search-bar .action-column .action-buttons-wrapper) {
    min-height: 32px;
    margin-bottom: 0;
    align-items: center;
  }
</style>

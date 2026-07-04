<template>
  <ElDialog
    v-model="dialogVisible"
    title="导入表"
    width="70%"
    align-center
    :close-on-click-modal="false"
  >
    <!-- 查询区域 -->
    <div class="search-bar">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="表名称">
          <ElInput v-model="searchForm.tableName" placeholder="请输入表名称" clearable />
        </ElFormItem>
        <ElFormItem label="表描述">
          <ElInput v-model="searchForm.tableComment" placeholder="请输入表描述" clearable />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch" :loading="loading">
            <ArtSvgIcon icon="ri:file-search-line" class="mr-1" />
            搜索
          </ElButton>
          <ElButton @click="handleReset" class="ml-2">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </div>

    <!-- 表格区域：使用 ArtTable 统一风格 -->
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
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleCancel">取消</ElButton>
        <ElButton type="primary" @click="handleConfirm" :disabled="selectedRows.length === 0">
          确定
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
/**
 * 导入表弹窗组件
 * 仅负责展示查询表单与待导入表格列表，真正的导入逻辑由父组件触发
 */
import {ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage} from 'element-plus'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import {fetchGetDbList, fetchImportTable} from '@/api/generator/gen-table'
import type {ColumnOption} from '@/types'

interface ImportTableItem {
    /** 表名称 */
    tableName: string
    /** 表描述 */
    tableComment?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
  }

  interface Props {
    /** 弹窗可见性，由父组件控制 */
    visible: boolean
  }

  interface Emits {
    /** 更新弹窗显示状态 */
    (e: 'update:visible', value: boolean): void
    /** 导入成功后通知父组件刷新列表 */
    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 弹窗显示控制，与父组件形成 v-model:visible
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  // 查询表单
  const searchForm = reactive({
    tableName: '',
    tableComment: ''
  })

  // 表格数据与加载状态
  const data = ref<ImportTableItem[]>([])
  const loading = ref(false)

  // 表格列配置
  const columns = ref<ColumnOption[]>([
    {
      type: 'selection',
      width: 55
    },
    {
      prop: 'tableName',
      label: '表名称',
      minWidth: 160,
      showOverflowTooltip: true
    },
    {
      prop: 'tableComment',
      label: '表描述',
      minWidth: 200,
      showOverflowTooltip: true
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180
    },
    {
      prop: 'updateTime',
      label: '更新时间',
      width: 180
    }
  ])

  // 分页信息
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  // 选中的行
  const selectedRows = ref<ImportTableItem[]>([])

  // 监听弹窗打开，自动加载数据
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        // 弹窗打开时重置分页并查询
        pagination.current = 1
        handleSearch()
      }
    }
  )

  /**
   * 执行查询
   * 从后端加载可导入的数据表列表
   */
  const handleSearch = async () => {
    try {
      loading.value = true
      // 调用后端接口获取数据库表列表（带分页参数）
      const pageResponse = await fetchGetDbList({
        ...searchForm,
        pageNum: pagination.current,
        pageSize: pagination.size
      })
      // fetchGetDbList 返回的已经是 PageResponse<GenTableVO>，直接使用
      const rows = Array.isArray(pageResponse?.rows) ? pageResponse.rows : []
      data.value = rows
      // 更新分页信息
      pagination.total = pageResponse?.total ?? rows.length ?? 0
      if (typeof pageResponse?.pageNum === 'number') {
        pagination.current = pageResponse.pageNum
      }
      if (typeof pageResponse?.pageSize === 'number') {
        pagination.size = pageResponse.pageSize
      }
      if (!rows.length) {
        ElMessage.info('未查询到符合条件的表')
      }
    } catch (error) {
      // 统一拦截器已处理错误提示，这里仅在开发环境打印日志
      if (import.meta.env.DEV) {
        console.error('获取数据库表列表失败：', error)
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置查询条件并重新查询（重置到第一页）
   */
  const handleReset = () => {
    searchForm.tableName = ''
    searchForm.tableComment = ''
    pagination.current = 1
    handleSearch()
  }

  /**
   * 表格多选变化
   */
  const handleSelectionChange = (rows: ImportTableItem[]) => {
    selectedRows.value = rows
  }

  /**
   * 分页尺寸变化
   * @param size 新的每页条数
   */
  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.current = 1
    handleSearch()
  }

  /**
   * 当前页变化
   * @param current 新的页码
   */
  const handleCurrentChange = (current: number) => {
    pagination.current = current
    handleSearch()
  }

  /**
   * 点击取消按钮
   */
  const handleCancel = () => {
    dialogVisible.value = false
  }

  /**
   * 点击确定按钮，导入选中的表结构
   */
  const handleConfirm = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请至少选择一个表')
      return
    }
    const tables = selectedRows.value.map((item: ImportTableItem) => item.tableName).join(',')
    try {
      await fetchImportTable(tables)
      ElMessage.success('导入成功')
      // 通知父组件刷新列表
      emit('success')
      dialogVisible.value = false
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error('导入失败：', error)
      }
      ElMessage.error('导入失败')
    }
  }
</script>

<style scoped lang="scss">
  :deep(.el-dialog) {
    overflow: hidden;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px;
      margin: 0;
      background: linear-gradient(
        135deg,
        var(--el-color-primary-light-9) 0%,
        var(--el-color-primary-light-8) 100%
      );
      border-bottom: 1px solid var(--art-card-border);

      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: var(--art-gray-900);
      }
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      background-color: var(--art-gray-50);
      border-top: 1px solid var(--art-card-border);
    }
  }

  .search-bar {
    padding: 16px;
    margin-bottom: 16px;
    background-color: var(--art-gray-50);
    border-radius: 8px;
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

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
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

  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }

  .empty-text {
    margin-top: 16px;
    font-size: 14px;
    color: #999;
    text-align: center;
  }
</style>

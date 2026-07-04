<!-- 通知公告管理页面 -->
<template>
  <div class="notice-page art-full-height">
    <!-- 搜索栏 -->
    <NoticeSearch
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></NoticeSearch>

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple v-auth="'system:notice:add'">
              <!-- <ArtSvgIcon icon="ri:add-line" class="mr-1" /> -->
              新增通知公告
            </ElButton>
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
              v-auth="'system:notice:delete'"
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

      <!-- 通知公告弹窗 -->
      <NoticeDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :notice-data="currentNoticeData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {h, nextTick, onMounted, ref} from 'vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {useDict} from '@/hooks/core/useDict'
import {fetchDeleteNotice, fetchGetNoticeList, type Notice} from '@/api/system/notice/notice'
import NoticeSearch from './modules/notice-search.vue'
import NoticeDialog from './modules/notice-dialog.vue'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {DialogType} from '@/types'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'Notice' })

  const { hasAuth } = useAuth()
  const { getDictItem, getTagType, loadDicts } = useDict()

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentNoticeData = ref<Partial<Notice>>({})

  // 选中行
  const selectedRows = ref<Notice[]>([])

  // 搜索表单
  const searchForm = ref({
    noticeTitle: undefined,
    noticeType: undefined,
    noticeContent: undefined,
    status: undefined
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
      apiFn: fetchGetNoticeList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'noticeTitle',
          label: '公告标题',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'noticeType',
          label: '公告类型',
          formatter: (row: Notice) => {
            const dictItem = getDictItem('sys_notice_type', row.noticeType)
            if (dictItem) {
              return h(
                ElTag,
                { type: getTagType(dictItem.cssClass) as any },
                () => dictItem.dictLabel
              )
            }
            return row.noticeType || '-'
          }
        },
        {
          prop: 'noticeContent',
          label: '公告内容',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'status',
          label: '公告状态',
          formatter: (row: Notice) => {
            const dictItem = getDictItem('sys_notice_status', row.status)
            if (dictItem) {
              return h(
                ElTag,
                { type: getTagType(dictItem.cssClass) as any },
                () => dictItem.dictLabel
              )
            }
            return row.status || '-'
          }
        },
        {
          prop: 'createBy',
          label: '创建人',
          width: 100,
          showOverflowTooltip: true
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right', // 固定列
          formatter: (row) => {
            const actions: any[] = []

            // 编辑通知公告按钮权限：system:notice:edit
            if (hasAuth('system:notice:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }

            // 删除通知公告按钮权限：system:notice:remove
            if (hasAuth('system:notice:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => deleteNotice(row)
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
   * 显示通知公告弹窗
   */
  const showDialog = (type: DialogType, row?: Notice): void => {
    dialogType.value = type
    currentNoticeData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除通知公告（单条）
   */
  const deleteNotice = async (row: Notice): Promise<void> => {
    try {
      await ElMessageBox.confirm(`确定要删除该通知公告吗？`, '删除通知公告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      })
      const id = row.noticeId
      if (id == null) return
      await fetchDeleteNotice([id])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除通知公告失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 批量删除通知公告
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的通知公告')
      return
    }
    const ids = selectedRows.value
      .map((row) => row.noticeId)
      .filter((id): id is number => id != null)
    if (ids.length === 0) return
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${ids.length} 条通知公告吗？`,
        '批量删除通知公告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
      await fetchDeleteNotice(ids)
      selectedRows.value = []
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除通知公告失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentNoticeData.value = {}
      // 刷新列表数据
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: Notice[]): void => {
    selectedRows.value = selection
  }

  /**
   * 初始化加载字典数据
   */
  const initDictData = async () => {
    await loadDicts(['sys_notice_type', 'sys_notice_status'])
  }

  // 组件挂载时加载字典数据
  onMounted(() => {
    initDictData()
  })
</script>

<style scoped lang="scss">
  .notice-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-card-hover);
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
</style>

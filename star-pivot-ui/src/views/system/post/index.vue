<!-- 岗位管理页面 -->
<template>
  <div class="post-page art-full-height">
    <!-- 搜索栏 -->
    <PostSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></PostSearch>

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple v-auth="'system:post:add'">
              新增岗位
            </ElButton>
            <ElButton
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
              v-auth="'system:post:delete'"
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

      <!-- 岗位弹窗 -->
      <PostDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :post-data="currentPostData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {useAuth} from '@/hooks/core/useAuth'
import {fetchDeletePost, fetchGetPostList, fetchUpdatePost} from '@/api/post/post'
import PostSearch from './modules/post-search.vue'
import PostDialog from './modules/post-dialog.vue'
import {ElMessage, ElMessageBox, ElSwitch} from 'element-plus'
import {type ColumnOption, DialogType} from '@/types'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'

defineOptions({ name: 'Post' })

  // 权限检查
  const { hasAuth } = useAuth()

  type PostListItem = Api.Post.PostListItem

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentPostData = ref<Partial<PostListItem>>({})

  // 选中行
  const selectedRows = ref<PostListItem[]>([])

  // 搜索表单
  const searchForm = ref({
    postName: undefined,
    postCode: undefined,
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
      apiFn: fetchGetPostList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: (): ColumnOption<PostListItem>[] => [
        { type: 'selection' }, // 勾选列
        // { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'postId',
          label: '岗位ID',
          width: 100,
          sortable: true
        },
        {
          prop: 'postCode',
          label: '岗位编码',
          width: 150,
          sortable: true
        },
        {
          prop: 'postName',
          label: '岗位名称',
          width: 150,
          sortable: true
        },
        {
          prop: 'postSort',
          label: '显示顺序',
          width: 120,
          sortable: true
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: Api.Post.PostListItem) => {
            return h(ElSwitch, {
              modelValue: Number(row.status) === 0 || String(row.status) === '0',
              activeValue: true,
              inactiveValue: false,
              onChange: (value: string | number | boolean) => {
                handleStatusChange(row, value === true)
              }
            })
          }
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right', // 固定列
          formatter: (row) => {
            const actions: any[] = []

            // 编辑岗位按钮权限：system:post:edit
            if (hasAuth('system:post:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }

            // 删除岗位按钮权限：system:post:remove
            if (hasAuth('system:post:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => deletePost(row)
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
   * 显示岗位弹窗
   */
  const showDialog = (type: DialogType, row?: PostListItem): void => {
    dialogType.value = type
    currentPostData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除岗位
   */
  const deletePost = (row: PostListItem): void => {
    ElMessageBox.confirm(`确定要删除岗位"${row.postName}"吗？`, '删除岗位', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
      .then(() => {
        fetchDeletePost([row.postId])
        refreshData()
        ElMessage.success('删除成功')
      })
      .catch(() => {
        // 用户取消删除
      })
  }

  /**
   * 批量删除岗位
   */
  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的岗位')
      return
    }
    const postNames = selectedRows.value.map((row) => row.postName).join('、')
    ElMessageBox.confirm(`确定要删除以下岗位吗？\n${postNames}`, '批量删除岗位', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
      .then(() => {
        const postIds = selectedRows.value.map((row) => row.postId)
        fetchDeletePost(postIds)
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
      currentPostData.value = {}
      // 刷新列表数据
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: PostListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 处理岗位状态切换
   */
  const handleStatusChange = async (row: PostListItem, value: boolean) => {
    try {
      const newStatus = value ? '0' : '1'
      // 调用更新岗位API来更新状态
      await fetchUpdatePost({
        postId: row.postId,
        postCode: row.postCode,
        postName: row.postName,
        postSort: row.postSort,
        status: newStatus,
        remark: row.remark || ''
      })
      ElMessage.success(value ? '启用成功' : '停用成功')
      // 更新本地数据
      row.status = value ? 0 : 1
      // 刷新列表
      refreshData()
    } catch (error) {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
      // 刷新列表以恢复原状态
      refreshData()
    }
  }
</script>

<style scoped lang="scss">
  .post-page {
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

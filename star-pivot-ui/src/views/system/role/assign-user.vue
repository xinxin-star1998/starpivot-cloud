<!-- 分配用户页面 -->
<template>
  <div class="art-full-height">
    <!-- 搜索栏 -->
    <AssignUserSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></AssignUserSearch>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="goBack" v-ripple>返回</ElButton>
          </ElSpace>
          <ElSpace wrap>
            <ElButton @click="openDialog" v-ripple>添加用户</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
      <AddUser
        v-model="showAddUserDialog"
        :role-id="roleId"
        @close="showAddUserDialog = false"
        @confirm="handleAddUserConfirm"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {useTable} from '@/hooks/core/useTable'
import {fetchCancelUser, fetchGetUserListByRoleId} from '@/api/role/role'
import AssignUserSearch from './modules/assign-user-search.vue'
import {ElButton, ElMessage, ElSpace, ElTag} from 'element-plus'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import {useRoute, useRouter} from 'vue-router'

const AddUser = defineAsyncComponent(() => import('@views/system/role/modules/add-user.vue'))

  defineOptions({ name: 'AssignUser' })

  // type UserListItem = Api.SystemManage.UserListItem

  const route = useRoute()
  const router = useRouter()

  // 从路由参数获取角色ID
  const roleId = computed(() => route.params.roleId as string)

  // 搜索表单
  const searchForm = ref({
    userName: undefined,
    phonenumber: undefined
  })

  const showSearchBar = ref(true)

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
      apiFn: (params: any) => {
        // 合并角色ID到请求参数中
        return fetchGetUserListByRoleId({
          ...params,
          roleId: roleId.value
        })
      },
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        {
          type: 'index',
          label: '序号',
          width: 100,
          index: (index: number) => {
            return (pagination.current - 1) * pagination.size + index + 1
          }
        },
        {
          prop: 'userName',
          label: '用户名称',
          width: 120
        },
        {
          prop: 'nickName',
          label: '用户昵称',
          width: 120
        },
        {
          prop: 'email',
          label: '邮箱',
          width: 120
        },
        {
          prop: 'phonenumber',
          label: '手机号',
          formatter: (row) => {
            return row.phonenumber || '未知'
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const status = Number(row.status)
            const isEnabled = status === 0
            const tagType = isEnabled ? 'success' : 'danger'
            const text = isEnabled ? '启用' : '禁用'
            return h(ElTag, { type: tagType }, () => text)
          }
        },
        {
          prop: 'createTime',
          label: '创建日期',
          width: 180,
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 200,
          formatter: (row) => {
            return h('div', { class: 'flex gap-2' }, [
              h(
                ElButton,
                {
                  type: 'danger',
                  size: 'small',
                  onClick: async () => {
                    try {
                      await fetchCancelUser({
                        userId: row.userId,
                        roleId: roleId.value
                      })
                      ElMessage.success('取消授权成功')
                      refreshData()
                    } catch (error) {
                      console.error('取消授权失败:', error)
                      ElMessage.error('取消授权失败')
                    }
                  }
                },
                () => '取消授权'
              )
            ])
          }
        }
      ]
    }
  })

  /**
   * 搜索处理
   * @param params 搜索参数
   */
  const handleSearch = (params: Record<string, any>) => {
    // 搜索参数赋值
    Object.assign(searchParams, params)
    getData()
  }
  // 控制添加用户对话框显示
  const showAddUserDialog = ref(false)

  /**
   * 添加用户跳转dialog
   */
  const openDialog = () => {
    // 验证 roleId 是否存在
    if (!roleId.value) {
      ElMessage.warning('角色ID不存在，无法添加用户')
      return
    }
    showAddUserDialog.value = true
  }

  /**
   * 处理添加用户确认
   */
  const handleAddUserConfirm = async () => {
    // 刷新列表
    refreshData()
  }
  /**
   * 返回上一页
   */
  const goBack = () => {
    router.back()
  }
</script>

<style lang="scss" scoped>
  .user-info {
    display: flex;
    align-items: center;
  }

  .avatar-wrapper {
    flex-shrink: 0;
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
</style>

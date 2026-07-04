<template>
  <ElDialog v-model="visible" title="选择用户" width="60%" align-center @close="handleClose">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="用户名称">
          <ElInput
            v-model="searchForm.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem label="手机号码">
          <ElInput
            v-model="searchForm.phonenumber"
            placeholder="请输入手机号码"
            clearable
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :icon="Search" @click="handleSearch">搜索</ElButton>
          <ElButton :icon="Refresh" @click="handleReset">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </div>

    <!-- 用户列表表格 -->
    <ElTable
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      @selection-change="handleSelectionChange"
      style="margin-top: 20px"
    >
      <ElTableColumn type="selection" width="55" />
      <ElTableColumn prop="userName" label="用户名称" width="120" />
      <ElTableColumn prop="nickName" label="用户昵称" width="120" />
      <ElTableColumn prop="email" label="邮箱" width="180" />
      <ElTableColumn prop="phonenumber" label="手机" width="150" />
      <ElTableColumn prop="status" label="状态" width="100">
        <template #default="{ row }">
          <ElTag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createTime" label="创建时间" width="180" />
    </ElTable>
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <ElPagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" @click="handleConfirm">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {ElMessage} from 'element-plus'
import {Refresh, Search} from '@element-plus/icons-vue'
import {fetchAssignUser, fetchGetUserListNotInByRoleId} from '@/api/role/role'

interface Props {
    modelValue: boolean
    roleId?: string | number
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'close'): void
    (e: 'confirm', userIds: number[]): void
  }

  const props = withDefaults(defineProps<Props>(), {
    roleId: undefined
  })

  const emit = defineEmits<Emits>()

  // 弹窗显示状态双向绑定
  const visible = computed({
    get: () => props.modelValue,
    set: (val: boolean) => emit('update:modelValue', val)
  })

  // 表格引用
  const tableRef = ref()

  // 加载状态
  const loading = ref(false)

  // 搜索表单
  const searchForm = ref({
    roleId: props.roleId,
    userName: undefined as string | undefined,
    phonenumber: undefined as string | undefined,
    pageNum: 1,
    pageSize: 10
  })

  // 表格数据
  const tableData = ref<Api.SystemManage.UserListItem[]>([])

  // 选中的用户
  const selectedUsers = ref<Api.SystemManage.UserListItem[]>([])

  // 分页信息
  const pagination = ref({
    pageNum: 1,
    pageSize: 10,
    total: 0
  })

  /**
   * 获取用户列表
   */
  const fetchUserList = async () => {
    if (props.roleId == null) {
      tableData.value = []
      return
    }
    try {
      loading.value = true
      // 同步分页参数到 searchForm
      searchForm.value.pageNum = pagination.value.pageNum
      searchForm.value.pageSize = pagination.value.pageSize
      const response = await fetchGetUserListNotInByRoleId({
        ...searchForm.value,
        roleId: props.roleId
      })
      // 后端返回的数据结构使用 rows 字段
      tableData.value = (response as any)?.rows || ([] as Api.SystemManage.UserListItem[])
      // 更新分页信息
      if ((response as any)?.total !== undefined) {
        pagination.value.total = (response as any).total
      }
      if ((response as any)?.pageNum !== undefined) {
        pagination.value.pageNum = (response as any).pageNum
      }
      if ((response as any)?.pageSize !== undefined) {
        pagination.value.pageSize = (response as any).pageSize
      }
      ElMessage.success('获取用户列表成功')
    } catch (error) {
      console.error('获取用户列表失败:', error)
      ElMessage.error('获取用户列表失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索处理
   */
  const handleSearch = () => {
    pagination.value.pageNum = 1
    fetchUserList()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    searchForm.value.userName = undefined
    searchForm.value.phonenumber = undefined
    pagination.value.pageNum = 1
    fetchUserList()
  }

  /**
   * 分页大小变化
   */
  const handleSizeChange = (size: number) => {
    pagination.value.pageSize = size
    pagination.value.pageNum = 1
    fetchUserList()
  }

  /**
   * 当前页变化
   */
  const handleCurrentChange = (page: number) => {
    pagination.value.pageNum = page
    fetchUserList()
  }

  /**
   * 选择变化
   */
  const handleSelectionChange = (selection: Api.SystemManage.UserListItem[]) => {
    selectedUsers.value = selection
  }

  /**
   * 确定按钮处理
   */
  const handleConfirm = async () => {
    if (selectedUsers.value.length === 0) {
      ElMessage.warning('请至少选择一个用户')
      return
    }
    if (props.roleId == null) {
      ElMessage.warning('角色ID无效')
      return
    }
    const userIds = selectedUsers.value.map((user: Api.SystemManage.UserListItem) => user.userId)
    const UserRoleDTO = {
      roleId: props.roleId,
      userIds: userIds
    }
    await fetchAssignUser(UserRoleDTO)
    ElMessage.success('分配成功')
    emit('confirm', userIds)
    handleClose()
  }

  /**
   * 关闭弹窗并重置表单
   */
  const handleClose = () => {
    visible.value = false
    emit('close')
    // 重置状态
    searchForm.value.userName = undefined
    searchForm.value.phonenumber = undefined
    pagination.value.pageNum = 1
    pagination.value.pageSize = 10
    selectedUsers.value = []
    tableData.value = []
    // 清空表格选择
    if (tableRef.value) {
      tableRef.value.clearSelection()
    }
  }

  // 监听弹窗显示状态，打开时加载数据
  watch(
    () => visible.value,
    (newVal: boolean) => {
      if (newVal) {
        fetchUserList()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .search-bar {
    padding: 20px 0;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .dialog-footer {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
</style>

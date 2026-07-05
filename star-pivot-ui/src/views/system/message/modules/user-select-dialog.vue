<template>
  <ElDialog v-model="visible" destroy-on-close title="选择用户" width="720px" @open="handleOpen">
    <ElForm :model="searchForm" inline class="search-form">
      <ElFormItem label="用户名称">
        <ElInput
          v-model="searchForm.userName"
          clearable
          placeholder="请输入用户名称"
          style="width: 180px"
          @keyup.enter="handleSearch"
        />
      </ElFormItem>
      <ElFormItem label="手机号码">
        <ElInput
          v-model="searchForm.phonenumber"
          clearable
          placeholder="请输入手机号码"
          style="width: 180px"
          @keyup.enter="handleSearch"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton type="primary" @click="handleSearch">搜索</ElButton>
        <ElButton @click="handleReset">重置</ElButton>
      </ElFormItem>
    </ElForm>

    <ElTable
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      max-height="360"
      row-key="userId"
      @selection-change="handleSelectionChange"
    >
      <ElTableColumn reserve-selection type="selection" width="48" />
      <ElTableColumn label="用户名称" min-width="110" prop="userName" show-overflow-tooltip />
      <ElTableColumn label="用户昵称" min-width="110" prop="nickName" show-overflow-tooltip />
      <ElTableColumn label="手机" min-width="120" prop="phonenumber" show-overflow-tooltip />
      <ElTableColumn label="部门" min-width="120" prop="deptName" show-overflow-tooltip />
      <ElTableColumn label="状态" width="80">
        <template #default="{ row }">
          <ElTag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </ElTag>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="pagination-wrap">
      <ElPagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadUsers"
        @size-change="handleSizeChange"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span class="selected-hint">已选 {{ selectedRows.length }} 人</span>
        <div>
          <ElButton @click="visible = false">取消</ElButton>
          <ElButton type="primary" @click="handleConfirm">确定</ElButton>
        </div>
      </div>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
import { fetchGetUserList } from '@/api/user/user'

export interface SelectedUserItem {
  userId: number
  userName?: string
  nickName?: string
}

const visible = defineModel<boolean>('visible', { default: false })

const props = defineProps<{
  selected?: SelectedUserItem[]
}>()

const emit = defineEmits<{
  confirm: [users: SelectedUserItem[]]
}>()

const tableRef = ref()
const loading = ref(false)
const tableData = ref<Api.SystemManage.UserListItem[]>([])
const selectedRows = ref<SelectedUserItem[]>([])

const searchForm = reactive({
  userName: undefined as string | undefined,
  phonenumber: undefined as string | undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

function mapUser(row: Api.SystemManage.UserListItem): SelectedUserItem {
  return {
    userId: Number(row.userId),
    userName: row.userName,
    nickName: row.nickName
  }
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await fetchGetUserList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      userName: searchForm.userName,
      phonenumber: searchForm.phonenumber,
      status: '0'
    })
    tableData.value = res.rows || []
    pagination.total = Number(res.total) || 0
    await nextTick()
    restoreSelection()
  } finally {
    loading.value = false
  }
}

function restoreSelection() {
  const table = tableRef.value
  if (!table) return
  table.clearSelection()
  const selectedIds = new Set(selectedRows.value.map((item) => item.userId))
  tableData.value.forEach((row) => {
    if (selectedIds.has(Number(row.userId))) {
      table.toggleRowSelection(row, true)
    }
  })
}

function handleSelectionChange(rows: Api.SystemManage.UserListItem[]) {
  const currentPageIds = new Set(tableData.value.map((row) => Number(row.userId)))
  const kept = selectedRows.value.filter((item) => !currentPageIds.has(item.userId))
  const merged = [...kept, ...rows.map(mapUser)]
  const unique = new Map<number, SelectedUserItem>()
  merged.forEach((item) => unique.set(item.userId, item))
  selectedRows.value = Array.from(unique.values())
}

function handleSearch() {
  pagination.pageNum = 1
  loadUsers()
}

function handleReset() {
  searchForm.userName = undefined
  searchForm.phonenumber = undefined
  pagination.pageNum = 1
  loadUsers()
}

function handleSizeChange() {
  pagination.pageNum = 1
  loadUsers()
}

function handleOpen() {
  selectedRows.value = [...(props.selected || [])]
  searchForm.userName = undefined
  searchForm.phonenumber = undefined
  pagination.pageNum = 1
  pagination.pageSize = 10
  loadUsers()
}

function handleConfirm() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请至少选择一名用户')
    return
  }
  emit('confirm', selectedRows.value)
  visible.value = false
}
</script>

<style scoped>
.search-form {
  margin-bottom: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.selected-hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>

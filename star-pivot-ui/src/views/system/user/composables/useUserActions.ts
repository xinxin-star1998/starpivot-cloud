import type { Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchDeleteUser,
  fetchExportUser,
  fetchImportUserExcel,
  fetchResetUserPassword,
  fetchUnlockUser,
  fetchUpdateUserStatus
} from '@/api/user/user'
import { runSingleDelete, useBatchDelete } from '@/hooks/core/useBatchDelete'
import { useResetPasswordPrompt } from '@/hooks/core/useResetPasswordPrompt'
import { isUserCancel } from '@/utils/sys/confirm-action'

type UserListItem = Api.SystemManage.UserListItem

export function useUserActions(options: {
  selectedRows: Ref<UserListItem[]>
  searchForm: Ref<Record<string, unknown>>
  refreshData: () => Promise<void>
}) {
  const { selectedRows, searchForm, refreshData } = options

  const onRefreshed = () => refreshData()

  const deleteUser = (row: UserListItem) =>
    runSingleDelete(
      {
        title: '注销用户',
        message: '确定要注销该用户吗？',
        successMessage: '注销成功',
        errorMessage: '注销失败'
      },
      () => fetchDeleteUser([row.userId]),
      onRefreshed
    )

  const { handleBatchDelete } = useBatchDelete<UserListItem>({
    selectedRows,
    getId: (row) => row.userId,
    getLabel: (row) => row.userName || '未知用户',
    deleteFn: fetchDeleteUser,
    onSuccess: onRefreshed,
    emptyMessage: '请选择要删除的用户',
    invalidIdsMessage: '所选用户中没有有效的用户ID',
    title: '批量注销用户',
    buildMessage: (count, labels) => `确定要注销以下 ${count} 个用户吗？\n${labels}`,
    successMessage: '注销成功',
    errorMessage: '注销失败'
  })

  const handleStatusChange = async (row: UserListItem, value: boolean) => {
    try {
      const newStatus = value ? '0' : '1'
      await fetchUpdateUserStatus(row.userId, Number(newStatus))
      ElMessage.success(value ? '启用成功' : '禁用成功')
      row.status = newStatus
      await refreshData()
    } catch (error) {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
      await refreshData()
    }
  }

  const unlockUser = async (row: UserListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(`确定要解锁用户 "${row.userName}" 的账户吗？`, '解锁账户', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchUnlockUser(row.userId)
      ElMessage.success('账户解锁成功')
      await refreshData()
    } catch (error: unknown) {
      if (isUserCancel(error)) return
      console.error('解锁账户失败:', error)
      const err = error as { message?: string; response?: { data?: { msg?: string } } }
      ElMessage.error(err?.message || err?.response?.data?.msg || '解锁失败')
    }
  }

  const { resetPassword: resetPwd } = useResetPasswordPrompt(fetchResetUserPassword, refreshData)

  const doImportUser = (file: File, updateSupport: boolean) =>
    fetchImportUserExcel(file, updateSupport)

  const handleExportUsers = async () => {
    try {
      await fetchExportUser({ ...searchForm.value })
    } catch (error) {
      console.error('导出用户失败:', error)
      ElMessage.error('导出用户失败')
    }
  }

  return {
    deleteUser,
    handleBatchDelete,
    handleStatusChange,
    unlockUser,
    resetPwd: (row: UserListItem) => resetPwd(row.userId, row.userName ?? ''),
    doImportUser,
    handleExportUsers
  }
}

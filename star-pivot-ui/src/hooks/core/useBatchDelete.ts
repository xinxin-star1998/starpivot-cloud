import type {Ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {isUserCancel} from '@/utils/sys/confirm-action'

type ConfirmType = 'success' | 'warning' | 'info' | 'error'

export interface BatchDeleteOptions<T> {
  selectedRows: Ref<T[]>
  getId: (row: T) => number | undefined | null
  getLabel: (row: T) => string
  deleteFn: (ids: number[]) => Promise<unknown>
  onSuccess?: () => void | Promise<void>
  emptyMessage?: string
  invalidIdsMessage?: string
  title?: string
  buildMessage?: (count: number, labels: string) => string
  successMessage?: string
  errorMessage?: string
  confirmType?: ConfirmType
  onError?: (error: unknown) => void
}

export function useBatchDelete<T>(options: BatchDeleteOptions<T>) {
  const {
    selectedRows,
    getId,
    getLabel,
    deleteFn,
    onSuccess,
    emptyMessage = '请选择要删除的数据',
    invalidIdsMessage = '所选数据中没有有效的 ID',
    title = '批量删除',
    buildMessage = (count, labels) => `确定要删除以下 ${count} 项吗？\n${labels}`,
    successMessage = '删除成功',
    errorMessage = '删除失败',
    confirmType = 'error',
    onError
  } = options

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(emptyMessage)
      return
    }
    try {
      const labels = selectedRows.value.map((row) => getLabel(row)).join('、')
      await ElMessageBox.confirm(buildMessage(selectedRows.value.length, labels), title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: confirmType
      })
      const ids = selectedRows.value
        .map((row) => getId(row))
        .filter((id): id is number => id !== undefined && id !== null)
      if (ids.length === 0) {
        ElMessage.warning(invalidIdsMessage)
        return
      }
      await deleteFn(ids)
      selectedRows.value = []
      await onSuccess?.()
      ElMessage.success(successMessage)
    } catch (error) {
      if (isUserCancel(error)) return
      console.error('批量删除失败:', error)
      if (onError) {
        onError(error)
        return
      }
      ElMessage.error(errorMessage)
    }
  }

  return { handleBatchDelete }
}

export interface SingleDeleteOptions {
  title?: string
  message: string
  confirmType?: ConfirmType
  successMessage?: string
  errorMessage?: string
  onError?: (error: unknown) => void
}

export async function runSingleDelete(
  options: SingleDeleteOptions,
  deleteFn: () => Promise<unknown>,
  onSuccess?: () => void | Promise<void>
): Promise<void> {
  const {
    title = '删除确认',
    message,
    confirmType = 'error',
    successMessage = '删除成功',
    errorMessage = '删除失败',
    onError
  } = options
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: confirmType
    })
    await deleteFn()
    await onSuccess?.()
    ElMessage.success(successMessage)
  } catch (error) {
    if (isUserCancel(error)) return
    console.error('删除失败:', error)
    if (onError) {
      onError(error)
      return
    }
    ElMessage.error(errorMessage)
  }
}

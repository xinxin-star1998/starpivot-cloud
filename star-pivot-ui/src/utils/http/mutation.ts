/**
 * 变更类操作（增删改、确认框提交等）的统一错误处理
 *
 * - 忽略用户取消确认框
 * - HttpError 默认由 HTTP 拦截器 showError，此处避免重复 toast
 * - 非 HTTP 错误展示 fallback 提示
 */
import { ElMessage } from 'element-plus'
import { isHttpError } from './error'
import { safeError } from '@/utils/sys/console'

/** 是否为 MessageBox / 用户主动取消 */
export function isUserCancelError(error: unknown): boolean {
  if (error === 'cancel' || error === 'close') return true
  if (typeof error === 'object' && error !== null && 'action' in error) {
    const action = (error as { action?: string }).action
    return action === 'cancel' || action === 'close'
  }
  return false
}

export interface HandleMutationErrorOptions {
  /** 是否在 HttpError 时仍弹出 fallback（默认 false，避免与拦截器重复） */
  showHttpError?: boolean
}

/**
 * 处理 mutation / 确认操作失败
 * @param error catch 到的错误
 * @param fallbackMessage 用户可见的默认失败文案
 */
export function handleMutationError(
  error: unknown,
  fallbackMessage = '操作失败',
  options?: HandleMutationErrorOptions
): void {
  if (isUserCancelError(error)) return

  if (isHttpError(error)) {
    if (import.meta.env.DEV) {
      safeError(fallbackMessage, error)
    }
    if (!options?.showHttpError) return
  }

  const message =
    error instanceof Error && !isHttpError(error) ? error.message : fallbackMessage
  ElMessage.error(message)
}

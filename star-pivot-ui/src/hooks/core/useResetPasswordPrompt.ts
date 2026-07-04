import {ElMessage, ElMessageBox} from 'element-plus'
import {isUserCancel} from '@/utils/sys/confirm-action'
import {
    ADMIN_RESET_PASSWORD_PATTERN,
    clearSavedLoginPassword,
    createPasswordPromptAutofillGuard
} from '@/utils/sys/password-prompt-guard'

export interface ResetPasswordPromptOptions {
  userId: number
  userName: string
  resetFn: (userId: number, password: string) => Promise<unknown>
  onSuccess?: () => void | Promise<void>
}

export async function promptResetPassword(options: ResetPasswordPromptOptions): Promise<void> {
  const stopGuard = createPasswordPromptAutofillGuard()
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入用户 "${options.userName}" 的新密码`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: ADMIN_RESET_PASSWORD_PATTERN,
        inputErrorMessage: '密码长度 6-20 位，必须包含字母和数字',
        inputPlaceholder: '请输入新密码',
        inputType: 'password'
      }
    )
    if (!value) return
    await options.resetFn(options.userId, value)
    ElMessage.success('重置密码成功')
    clearSavedLoginPassword(options.userName)
    await options.onSuccess?.()
  } catch (error: unknown) {
    if (isUserCancel(error)) return
    console.error('重置密码失败:', error)
    const err = error as { message?: string }
    ElMessage.error(err?.message || '重置密码失败')
  } finally {
    stopGuard()
  }
}

export function useResetPasswordPrompt(
  resetFn: (userId: number, password: string) => Promise<unknown>,
  onSuccess?: () => void | Promise<void>
) {
  const resetPassword = (userId: number, userName: string) =>
    promptResetPassword({ userId, userName, resetFn, onSuccess })

  return { resetPassword, promptResetPassword }
}

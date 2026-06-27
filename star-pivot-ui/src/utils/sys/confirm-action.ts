/** 用户主动取消 Element Plus 确认/输入框 */
export function isUserCancel(error: unknown): boolean {
  return error === 'cancel' || error === 'close'
}

/**
 * 执行需用户确认的操作：取消时不抛错，其它错误交给 onError 或重新抛出
 */
export async function runConfirmedAction(
  confirm: () => Promise<void>,
  action: () => Promise<void>,
  onError?: (error: unknown) => void
): Promise<void> {
  try {
    await confirm()
    await action()
  } catch (error) {
    if (isUserCancel(error)) return
    if (onError) {
      onError(error)
      return
    }
    throw error
  }
}

/** 管理端密码统一规则：6-20 位，必须包含字母和数字（新增用户 / 重置 / 个人改密） */
export const ADMIN_PASSWORD_PATTERN = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/

/** @deprecated 请使用 {@link ADMIN_PASSWORD_PATTERN} */
export const ADMIN_RESET_PASSWORD_PATTERN = ADMIN_PASSWORD_PATTERN

export const ADMIN_PASSWORD_RULE_MESSAGE = '密码长度 6-20 位，必须包含字母和数字'

/**
 * 阻止浏览器在 MessageBox 密码框中自动填充已保存密码。
 * 返回 disconnect 函数，调用方应在 finally 中执行。
 */
export function createPasswordPromptAutofillGuard(): () => void {
  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      for (const node of Array.from(mutation.addedNodes)) {
        if (!(node instanceof HTMLElement)) continue
        const input =
          node instanceof HTMLInputElement
            ? node
            : (node.querySelector('input[type="password"]') as HTMLInputElement | null)
        if (input?.type === 'password') {
          input.setAttribute('autocomplete', 'new-password')
          if (input.value) {
            input.value = ''
            input.dispatchEvent(new Event('input', { bubbles: true }))
          }
          observer.disconnect()
          return
        }
      }
    }
  })
  observer.observe(document.body, { childList: true, subtree: true })
  return () => observer.disconnect()
}

/** 清除登录页「记住密码」中对应用户的旧密码 */
export function clearSavedLoginPassword(username: string) {
  try {
    const saved = localStorage.getItem('login-info')
    if (!saved) return
    const info = JSON.parse(saved)
    if (info.username === username) {
      delete info.password
      localStorage.setItem('login-info', JSON.stringify(info))
    }
  } catch {
    /* ignore */
  }
}

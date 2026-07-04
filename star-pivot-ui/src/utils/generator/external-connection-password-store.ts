/**
 * 外部库连接密码仅保存在当前浏览器标签页的内存中，关闭标签页即清除。
 * 禁止写入 localStorage / sessionStorage。
 */
const presetPasswords = new Map<string, string>()
let lastConnectionPassword = ''

export function setPresetPassword(name: string, password: string) {
  const trimmed = password.trim()
  if (!name || !trimmed) {
    presetPasswords.delete(name)
    return
  }
  presetPasswords.set(name, password)
}

export function getPresetPassword(name: string): string | undefined {
  return presetPasswords.get(name)
}

export function clearPresetPassword(name: string) {
  presetPasswords.delete(name)
}

export function setLastConnectionPassword(password: string) {
  lastConnectionPassword = password
}

export function getLastConnectionPassword(): string {
  return lastConnectionPassword
}

export function clearLastConnectionPassword() {
  lastConnectionPassword = ''
}

/** 将支付宝返回的 HTML form 写入新窗口并自动提交 */
export function submitAlipayPayForm(payForm: string) {
  const win = window.open('', '_blank')
  if (!win) {
    throw new Error('请允许浏览器弹出窗口以跳转支付宝')
  }
  win.document.open()
  win.document.write(payForm)
  win.document.close()
}

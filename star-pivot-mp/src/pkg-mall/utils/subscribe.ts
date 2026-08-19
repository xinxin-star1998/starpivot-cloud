import {fetchSubscribeTemplates} from '@/pkg-mall/api/subscribe'
import {isLogin} from '@/stores/member'

/**
 * 支付成功等用户手势后申请订阅消息授权。
 * 失败/拒绝静默忽略，不影响主流程。
 */
export async function requestOrderSubscribeMessage(): Promise<void> {
  if (!isLogin()) return
  try {
    const conf = await fetchSubscribeTemplates()
    const tmplIds = (conf.templateIds || []).filter(Boolean).slice(0, 3)
    if (!conf.enabled || !tmplIds.length) return

    await new Promise<void>((resolve) => {
      uni.requestSubscribeMessage({
        tmplIds,
        complete: () => resolve(),
        fail: () => resolve()
      })
    })
  } catch {
    // 忽略
  }
}

import type {PortalOrder} from '@/api/types'

export function buildLogisticsTrackUrl(deliverySn: string, deliveryCompany?: string) {
  const nu = encodeURIComponent(deliverySn.trim())
  if (deliveryCompany?.trim()) {
    return `https://www.kuaidi100.com/chaxun?com=${encodeURIComponent(deliveryCompany.trim())}&nu=${nu}`
  }
  return `https://www.kuaidi100.com/chaxun?nu=${nu}`
}

export function canShowLogistics(order?: PortalOrder | null) {
  if (!order?.deliverySn?.trim()) return false
  return order.status === 2 || order.status === 3
}

export function canApplyReturn(order?: PortalOrder | null) {
  return order?.status === 2 || order?.status === 3
}

export function openLogisticsTrack(order: PortalOrder) {
  const sn = order.deliverySn?.trim()
  if (!sn) {
    uni.showToast({ title: '暂无运单号', icon: 'none' })
    return
  }
  const url = buildLogisticsTrackUrl(sn, order.deliveryCompany)
  uni.setClipboardData({
    data: sn,
    success: () => {
      uni.showActionSheet({
        itemList: ['查看物流轨迹', '仅复制运单号'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.navigateTo({ url: `/pages/common/webview/index?url=${encodeURIComponent(url)}` })
          } else {
            uni.showToast({ title: '运单号已复制' })
          }
        },
        fail: () => {
          uni.showToast({ title: '运单号已复制' })
        }
      })
    }
  })
}

/** 生成快递 100 查询链接（通用单号查询） */
export function buildLogisticsTrackUrl(deliverySn: string, deliveryCompany?: string) {
  const nu = encodeURIComponent(deliverySn.trim())
  if (deliveryCompany?.trim()) {
    return `https://www.kuaidi100.com/chaxun?com=${encodeURIComponent(deliveryCompany.trim())}&nu=${nu}`
  }
  return `https://www.kuaidi100.com/chaxun?nu=${nu}`
}

export function formatOperLogParam(param?: string): string {
  if (!param || param === '参数解析失败') return param || '无'
  try {
    const obj = JSON.parse(param)
    return JSON.stringify(obj, null, 2)
  } catch {
    return param
  }
}

export function getHttpMethodClass(method?: string): string {
  if (!method) return ''
  const methodUpper = method.toUpperCase()
  const classMap: Record<string, string> = {
    GET: 'method-get',
    POST: 'method-post',
    PUT: 'method-put',
    DELETE: 'method-delete'
  }
  return classMap[methodUpper] || ''
}

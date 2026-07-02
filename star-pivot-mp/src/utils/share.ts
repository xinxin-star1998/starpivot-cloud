/** 构建小程序分享参数（配合 onShareAppMessage 使用） */
export function buildSharePayload(options: {
  title: string
  path: string
  imageUrl?: string
}) {
  return {
    title: options.title,
    path: options.path,
    imageUrl: options.imageUrl || ''
  }
}

export function productSharePath(spuId: number) {
  return `/pages/product/detail?id=${spuId}`
}

export function subjectSharePath(subjectId: number) {
  return `/pages/subject/index?id=${subjectId}`
}

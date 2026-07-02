import {ref} from 'vue'
import {imageSrc as resolveImageSrc, prefetchImages as prefetchImageUrls} from '@/utils/goods-image-url'

/** 页面内图片预解析：prefetch 后自增 tick，驱动 imageSrc 重渲染 */
export function useGoodsImages() {
  const imageTick = ref(0)

  async function prefetchImages(...groups: Array<Array<string | undefined | null>>) {
    await prefetchImageUrls(...groups)
    imageTick.value++
  }

  function imageSrc(raw?: string | null) {
    return resolveImageSrc(raw, imageTick.value)
  }

  return { imageTick, prefetchImages, imageSrc }
}

/**
 * 图片懒加载指令
 *
 * 使用 IntersectionObserver API 实现图片懒加载，提升页面加载性能
 *
 * 使用方式：
 * <img v-lazy="imageUrl" alt="图片" />
 * <img v-lazy="{ src: imageUrl, placeholder: '/placeholder.png' }" alt="图片" />
 *
 * @module directives/business/lazy
 */

import type { App, DirectiveBinding } from 'vue'

interface LazyOptions {
  src?: string
  placeholder?: string
  error?: string
}

/**
 * 默认占位图（可选，可通过配置覆盖）
 */
const DEFAULT_PLACEHOLDER =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

/**
 * 默认错误图（可选，可通过配置覆盖）
 */
const DEFAULT_ERROR =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjZjU0NDQ0IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5aSx6LSlPC90ZXh0Pjwvc3ZnPg=='

/**
 * IntersectionObserver 实例（单例，所有图片共享）
 */
let observer: IntersectionObserver | null = null

/**
 * 创建 IntersectionObserver 实例
 */
function createObserver(): IntersectionObserver {
  if (observer) {
    return observer
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement
          const binding = (img as any).__lazyBinding as DirectiveBinding

          if (binding) {
            loadImage(img, binding)
            observer?.unobserve(img)
          }
        }
      })
    },
    {
      // 提前 50px 开始加载（提升用户体验）
      rootMargin: '50px',
      threshold: 0.01
    }
  )

  return observer
}

/**
 * 加载图片
 */
function loadImage(img: HTMLImageElement, binding: DirectiveBinding): void {
  const value = binding.value
  let imageSrc: string
  let placeholder: string | undefined
  let errorImage: string | undefined

  // 解析指令值
  if (typeof value === 'string') {
    imageSrc = value
  } else if (value && typeof value === 'object') {
    const options = value as LazyOptions
    imageSrc = options.src || ''
    placeholder = options.placeholder
    errorImage = options.error
  } else {
    console.warn('[v-lazy] 无效的指令值:', value)
    return
  }

  if (!imageSrc) {
    console.warn('[v-lazy] 图片地址为空')
    return
  }

  // 设置占位图
  if (placeholder && img.src !== imageSrc) {
    img.src = placeholder
  } else if (!img.src && DEFAULT_PLACEHOLDER) {
    img.src = DEFAULT_PLACEHOLDER
  }

  // 创建新的 Image 对象预加载
  const image = new Image()
  image.onload = () => {
    img.src = imageSrc
    img.classList.add('lazy-loaded')
  }
  image.onerror = () => {
    if (errorImage) {
      img.src = errorImage
    } else if (DEFAULT_ERROR) {
      img.src = DEFAULT_ERROR
    }
    img.classList.add('lazy-error')
    console.error('[v-lazy] 图片加载失败:', imageSrc)
  }
  image.src = imageSrc
}

/**
 * 设置图片懒加载指令
 */
export function setupLazyDirective(app: App): void {
  app.directive('lazy', {
    mounted(el: HTMLImageElement, binding: DirectiveBinding) {
      // 保存 binding 引用，供 IntersectionObserver 回调使用
      ;(el as any).__lazyBinding = binding

      // 如果浏览器不支持 IntersectionObserver，直接加载图片
      if (!window.IntersectionObserver) {
        loadImage(el, binding)
        return
      }

      // 获取或创建 IntersectionObserver 实例
      const obs = createObserver()

      // 开始观察元素
      obs.observe(el)
    },
    updated(el: HTMLImageElement, binding: DirectiveBinding) {
      // 更新 binding 引用
      ;(el as any).__lazyBinding = binding

      // 如果图片已经加载过，直接更新
      if (el.classList.contains('lazy-loaded')) {
        const value = binding.value
        const imageSrc = typeof value === 'string' ? value : (value as LazyOptions)?.src
        if (imageSrc && el.src !== imageSrc) {
          el.src = imageSrc
        }
      }
    },
    unmounted(el: HTMLImageElement) {
      // 取消观察
      if (observer) {
        observer.unobserve(el)
      }
      // 清理引用
      delete (el as any).__lazyBinding
    }
  })
}

/**
 * 页面可见性检测 Hook
 *
 * 用于检测页面是否可见，优化性能：
 * - 页面不可见时暂停定时刷新、动画等操作
 * - 页面可见时恢复操作
 *
 * @example
 * ```ts
 * const { isVisible, onPause, onResume } = usePageVisibility()
 *
 * onPause(() => {
 *   stopAutoRefresh()
 * })
 *
 * onResume(() => {
 *   startAutoRefresh()
 * })
 * ```
 */

import {onBeforeUnmount, onMounted, ref} from 'vue'

export function usePageVisibility() {
  const isVisible = ref(true)
  const pauseCallbacks: (() => void)[] = []
  const resumeCallbacks: (() => void)[] = []

  const handleVisibilityChange = () => {
    const wasVisible = isVisible.value
    isVisible.value = !document.hidden

    if (wasVisible && !isVisible.value) {
      // 页面变为不可见
      pauseCallbacks.forEach((cb) => cb())
    } else if (!wasVisible && isVisible.value) {
      // 页面变为可见
      resumeCallbacks.forEach((cb) => cb())
    }
  }

  const onPause = (callback: () => void) => {
    pauseCallbacks.push(callback)
  }

  const onResume = (callback: () => void) => {
    resumeCallbacks.push(callback)
  }

  const clearCallbacks = () => {
    pauseCallbacks.length = 0
    resumeCallbacks.length = 0
  }

  onMounted(() => {
    isVisible.value = !document.hidden
    document.addEventListener('visibilitychange', handleVisibilityChange)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    clearCallbacks()
  })

  return {
    isVisible,
    onPause,
    onResume
  }
}

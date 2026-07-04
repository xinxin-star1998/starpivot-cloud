/** C 端移动端断点（与 --portal-bp-tablet 一致） */
export function usePortalMobile() {
  const isMobile = ref(false)
  let mq: MediaQueryList | null = null

  function sync() {
    isMobile.value = mq?.matches ?? false
  }

  onMounted(() => {
    mq = window.matchMedia('(width <= 900px)')
    sync()
    mq.addEventListener('change', sync)
  })

  onUnmounted(() => {
    mq?.removeEventListener('change', sync)
  })

  return { isMobile }
}

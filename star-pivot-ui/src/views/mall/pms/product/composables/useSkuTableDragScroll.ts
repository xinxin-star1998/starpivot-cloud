export function useSkuTableDragScroll() {
  const scrollRef = ref<HTMLElement | null>(null)
  let dragActive = false
  let dragStartX = 0
  let dragScrollLeft = 0

  function onMouseDown(e: MouseEvent) {
    const el = scrollRef.value
    if (!el || e.button !== 0) return
    const target = e.target as HTMLElement
    if (
      target.closest(
        'input, textarea, .el-input, .el-input-number, .el-checkbox, .el-radio, button, a'
      )
    ) {
      return
    }
    dragActive = true
    dragStartX = e.clientX
    dragScrollLeft = el.scrollLeft
    el.classList.add('is-dragging')
  }

  function onMouseMove(e: MouseEvent) {
    if (!dragActive || !scrollRef.value) return
    e.preventDefault()
    scrollRef.value.scrollLeft = dragScrollLeft - (e.clientX - dragStartX)
  }

  function stopDrag() {
    if (!dragActive) return
    dragActive = false
    scrollRef.value?.classList.remove('is-dragging')
  }

  return { scrollRef, onMouseDown, onMouseMove, stopDrag }
}

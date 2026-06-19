<!-- 虚拟滚动列表组件 -->
<!-- 用于渲染大量数据，只渲染可见区域，提升性能 -->
<template>
  <div ref="containerRef" class="art-virtual-list" :style="containerStyle" @scroll="handleScroll">
    <div :style="{ height: virtualResult.totalHeight + 'px', position: 'relative' }">
      <div :style="{ transform: `translateY(${virtualResult.offsetY}px)` }">
        <div
          v-for="(item, index) in visibleData"
          :key="getItemKey(item, virtualResult.startIndex + index)"
          :style="{ height: itemHeight + 'px' }"
          class="virtual-list-item"
        >
          <slot name="default" :item="item" :index="virtualResult.startIndex + index">
            {{ item }}
          </slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useVirtualScroll } from '@/utils/virtual-scroll'
  import { useThrottleFn } from '@vueuse/core'

  defineOptions({ name: 'ArtVirtualList' })

  interface Props {
    /** 数据列表 */
    data: Record<string, unknown>[]
    /** 每项高度（px），固定高度 */
    itemHeight: number
    /** 容器高度（px），如果不设置则自动计算 */
    height?: number | string
    /** 缓冲区大小（上下各保留多少项），默认 5 */
    bufferSize?: number
    /** 获取列表项的唯一 key，默认使用索引 */
    itemKey?: string | ((item: Record<string, unknown>, index: number) => string | number)
  }

  const props = withDefaults(defineProps<Props>(), {
    data: () => [],
    itemHeight: 50,
    bufferSize: 5,
    itemKey: undefined
  })

  const containerRef = ref<HTMLElement>()
  const scrollTop = ref(0)
  const containerHeight = ref(0)

  /**
   * 容器样式
   */
  const containerStyle = computed(() => {
    const style: Record<string, string> = {
      overflowY: 'auto',
      overflowX: 'hidden'
    }

    if (props.height) {
      if (typeof props.height === 'number') {
        style.height = `${props.height}px`
      } else {
        style.height = props.height
      }
    } else {
      style.height = '100%'
    }

    return style
  })

  /**
   * 使用虚拟滚动计算
   */
  const virtualResult = useVirtualScroll({
    scrollTop: () => scrollTop.value,
    containerHeight: () => containerHeight.value || 400,
    itemHeight: () => props.itemHeight,
    bufferSize: props.bufferSize,
    total: () => props.data.length
  })

  /**
   * 可见区域的数据
   */
  const visibleData = computed(() => {
    const { startIndex, endIndex } = virtualResult.value
    return props.data.slice(startIndex, endIndex + 1)
  })

  /**
   * 获取列表项的唯一 key
   */
  const getItemKey = (item: Record<string, unknown>, index: number): string | number => {
    if (typeof props.itemKey === 'function') {
      return props.itemKey(item, index)
    }
    if (typeof props.itemKey === 'string') {
      return item[props.itemKey] as string | number
    }
    return index
  }

  /**
   * 处理滚动事件
   */
  const handleScroll = useThrottleFn((event: Event) => {
    const target = event.target as HTMLElement
    scrollTop.value = target.scrollTop
  }, 16)

  /**
   * 更新容器高度
   */
  const updateContainerHeight = () => {
    nextTick(() => {
      if (containerRef.value) {
        containerHeight.value = containerRef.value.clientHeight
      }
    })
  }

  /**
   * 滚动到指定位置
   */
  const scrollTo = (scrollTopValue: number) => {
    if (containerRef.value) {
      containerRef.value.scrollTop = scrollTopValue
    }
  }

  /**
   * 滚动到指定索引
   */
  const scrollToIndex = (index: number) => {
    if (index < 0 || index >= props.data.length) {
      return
    }
    const targetScrollTop = index * props.itemHeight
    scrollTo(targetScrollTop)
  }

  /**
   * 滚动到顶部
   */
  const scrollToTop = () => {
    scrollTo(0)
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    if (containerRef.value) {
      scrollTo(containerRef.value.scrollHeight)
    }
  }

  let resizeObserver: ResizeObserver | null = null
  let observedEl: HTMLElement | null = null

  onMounted(() => {
    updateContainerHeight()
    // 使用 ResizeObserver 监听容器尺寸变化
    const el = containerRef.value
    if (el && typeof ResizeObserver !== 'undefined') {
      observedEl = el
      resizeObserver = new ResizeObserver(updateContainerHeight)
      resizeObserver.observe(el)
    }
    window.addEventListener('resize', updateContainerHeight)
  })

  onUnmounted(() => {
    if (resizeObserver && observedEl) {
      resizeObserver.unobserve(observedEl)
      resizeObserver = null
      observedEl = null
    }
    window.removeEventListener('resize', updateContainerHeight)
  })

  // 暴露方法供外部调用
  defineExpose({
    scrollTo,
    scrollToIndex,
    scrollToTop,
    scrollToBottom,
    containerRef
  })
</script>

<style scoped lang="scss">
  .art-virtual-list {
    position: relative;
    width: 100%;

    .virtual-list-item {
      box-sizing: border-box;
    }
  }
</style>

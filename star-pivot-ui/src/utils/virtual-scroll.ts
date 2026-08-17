/**
 * 虚拟滚动工具函数
 *
 * 用于处理大数据列表的虚拟滚动，提升渲染性能
 * 适用于表格、列表等需要展示大量数据的场景
 *
 * @module utils/virtual-scroll
 */

import { computed, isRef as vueIsRef, type Ref } from 'vue'

/**
 * 虚拟滚动配置选项
 */
export interface VirtualScrollOptions {
  /** 容器高度（px） */
  containerHeight: number
  /** 每项高度（px），固定高度时使用 */
  itemHeight?: number
  /** 每项高度函数（动态高度时使用） */
  itemHeightFn?: (index: number) => number
  /** 缓冲区大小（上下各保留多少项），默认 5 */
  bufferSize?: number
  /** 总数据量 */
  total: number
}

/**
 * 虚拟滚动计算结果
 */
export interface VirtualScrollResult {
  /** 可见区域的起始索引 */
  startIndex: number
  /** 可见区域的结束索引 */
  endIndex: number
  /** 渲染的数据列表（已截取） */
  visibleData: Record<string, unknown>[]
  /** 上方占位高度（px） */
  offsetY: number
  /** 总高度（px） */
  totalHeight: number
}

/**
 * 计算虚拟滚动范围
 *
 * @param scrollTop 滚动位置（px）
 * @param options 配置选项
 * @returns 虚拟滚动计算结果
 */
export function calculateVirtualScroll(
  scrollTop: number,
  options: VirtualScrollOptions
): VirtualScrollResult {
  const { containerHeight, itemHeight, itemHeightFn, bufferSize = 5, total } = options

  if (total === 0) {
    return {
      startIndex: 0,
      endIndex: 0,
      visibleData: [],
      offsetY: 0,
      totalHeight: 0
    }
  }

  // 固定高度模式
  if (itemHeight) {
    return calculateFixedHeight(scrollTop, {
      containerHeight,
      itemHeight,
      bufferSize,
      total
    })
  }

  // 动态高度模式
  if (itemHeightFn) {
    return calculateDynamicHeight(scrollTop, {
      containerHeight,
      itemHeightFn,
      bufferSize,
      total
    })
  }

  throw new Error('[virtual-scroll] 必须提供 itemHeight 或 itemHeightFn')
}

/**
 * 固定高度模式计算
 */
function calculateFixedHeight(
  scrollTop: number,
  options: {
    containerHeight: number
    itemHeight: number
    bufferSize: number
    total: number
  }
): VirtualScrollResult {
  const { containerHeight, itemHeight, bufferSize, total } = options

  // 计算可见区域
  const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - bufferSize)
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  const endIndex = Math.min(total - 1, startIndex + visibleCount + bufferSize * 2)

  // 计算占位高度
  const offsetY = startIndex * itemHeight
  const totalHeight = total * itemHeight

  return {
    startIndex,
    endIndex,
    visibleData: [], // 由调用方根据索引填充
    offsetY,
    totalHeight
  }
}

/**
 * 动态高度模式计算（简化版，实际应用中可能需要更复杂的算法）
 */
function calculateDynamicHeight(
  scrollTop: number,
  options: {
    containerHeight: number
    itemHeightFn: (index: number) => number
    bufferSize: number
    total: number
  }
): VirtualScrollResult {
  const { containerHeight, itemHeightFn, bufferSize, total } = options

  // 估算平均高度（使用前10项的平均值）
  const sampleSize = Math.min(10, total)
  let avgHeight = 0
  if (sampleSize > 0) {
    let sum = 0
    for (let i = 0; i < sampleSize; i++) {
      sum += itemHeightFn(i)
    }
    avgHeight = sum / sampleSize
  }

  // 使用平均高度估算起始索引
  const estimatedStartIndex = Math.max(0, Math.floor(scrollTop / avgHeight) - bufferSize)

  // 精确计算起始索引（从估算位置向上查找）
  let startIndex = estimatedStartIndex
  let accumulatedHeight = 0

  // 向上查找实际起始位置
  for (let i = estimatedStartIndex - 1; i >= 0; i--) {
    const height = itemHeightFn(i)
    if (accumulatedHeight + height > scrollTop) {
      break
    }
    accumulatedHeight += height
    startIndex = i
  }

  // 向下计算可见区域
  let currentHeight = accumulatedHeight
  let endIndex = startIndex

  for (let i = startIndex; i < total; i++) {
    const height = itemHeightFn(i)
    currentHeight += height

    if (currentHeight > scrollTop + containerHeight) {
      endIndex = i + bufferSize
      break
    }

    endIndex = i
  }

  endIndex = Math.min(total - 1, endIndex + bufferSize)

  // 计算总高度
  let totalHeight = 0
  for (let i = 0; i < total; i++) {
    totalHeight += itemHeightFn(i)
  }

  return {
    startIndex,
    endIndex,
    visibleData: [], // 由调用方根据索引填充
    offsetY: accumulatedHeight,
    totalHeight
  }
}

/**
 * 使用虚拟滚动的组合式函数（Vue 3 Composition API）
 * 返回响应式的计算结果，自动响应 scrollTop、containerHeight、total 等变化
 *
 * @example
 * ```vue
 * <script setup lang="ts">
 * import { useVirtualScroll } from '@/utils/virtual-scroll'
 * import { ref, computed } from 'vue'
 *
 * const containerRef = ref<HTMLElement>()
 * const scrollTop = ref(0)
 * const dataList = ref([...]) // 大量数据
 *
 * const virtualResult = useVirtualScroll({
 *   scrollTop: () => scrollTop.value,
 *   containerHeight: 600,
 *   itemHeight: 50,
 *   total: () => dataList.value.length
 * })
 *
 * const visibleData = computed(() => {
 *   return dataList.value.slice(
 *     virtualResult.value.startIndex,
 *     virtualResult.value.endIndex + 1
 *   )
 * })
 * </script>
 *
 * <template>
 *   <div
 *     ref="containerRef"
 *     @scroll="scrollTop = $event.target.scrollTop"
 *     style="height: 600px; overflow-y: auto;"
 *   >
 *     <div :style="{ height: virtualResult.totalHeight + 'px', position: 'relative' }">
 *       <div :style="{ transform: `translateY(${virtualResult.offsetY}px)` }">
 *         <div v-for="(item, index) in visibleData" :key="virtualResult.startIndex + index">
 *           {{ item }}
 *         </div>
 *       </div>
 *     </div>
 *   </div>
 * </template>
 * ```
 */
export function useVirtualScroll(options: {
  scrollTop: Ref<number> | (() => number)
  containerHeight: number | Ref<number> | (() => number)
  itemHeight?: number | Ref<number> | (() => number)
  itemHeightFn?: (index: number) => number
  bufferSize?: number
  total: number | Ref<number> | (() => number)
}) {
  // 将各种输入转换为 getter 函数
  const getScrollTop = vueIsRef(options.scrollTop)
    ? () => (options.scrollTop as Ref<number>).value
    : typeof options.scrollTop === 'function'
      ? options.scrollTop
      : () => (typeof options.scrollTop === 'number' ? options.scrollTop : 0)

  const getContainerHeight = vueIsRef(options.containerHeight)
    ? () => (options.containerHeight as Ref<number>).value
    : typeof options.containerHeight === 'function'
      ? options.containerHeight
      : () => options.containerHeight as number

  const getItemHeight = options.itemHeight
    ? vueIsRef(options.itemHeight)
      ? () => (options.itemHeight as Ref<number>).value
      : typeof options.itemHeight === 'function'
        ? options.itemHeight
        : () => options.itemHeight as number
    : undefined

  const getTotal = vueIsRef(options.total)
    ? () => (options.total as Ref<number>).value
    : typeof options.total === 'function'
      ? options.total
      : () => options.total as number

  // 使用 computed 创建响应式的计算结果
  const result = computed<VirtualScrollResult>(() => {
    return calculateVirtualScroll(getScrollTop(), {
      containerHeight: getContainerHeight(),
      itemHeight: getItemHeight?.(),
      itemHeightFn: options.itemHeightFn,
      bufferSize: options.bufferSize,
      total: getTotal()
    })
  })

  return result
}

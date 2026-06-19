// Vue API 通过 unplugin-auto-import 自动导入，无需显式导入

export interface UseCheckableTreeOptions {
  keyField: string
  childrenField?: string
  /**
   * 高度自适应上限（px）
   * - 仅用于容器高度自动调整
   */
  maxHeight?: number
  /**
   * 树展开/收起动画时长（ms），用于延迟计算高度
   */
  expandAnimationMs?: number
}

type AnyRecord = Record<string, any>

/**
 * Element Plus ElTree 的通用控制逻辑封装：
 * - 展开/收起
 * - 全选/全不选 + 半选状态
 * - 父子联动开关（check-strictly）
 * - 回显选中（支持“先关闭联动 -> 设值 -> 恢复联动”的稳定流程）
 * - 容器高度自适应
 */
export function useCheckableTree<T extends AnyRecord>(options: UseCheckableTreeOptions) {
  const childrenField = options.childrenField ?? 'children'
  const maxHeight = options.maxHeight ?? 600
  const expandAnimationMs = options.expandAnimationMs ?? 300

  const treeRef = ref<any>()
  const containerRef = ref<HTMLElement>()
  const data = ref<T[]>([])

  const expandAll = ref(false)
  const selectAll = ref(false)
  const indeterminate = ref(false)

  // 业务语义：true=父子联动；Element Plus 语义：check-strictly=true=不联动
  const parentChildLinked = ref(true)
  const checkStrictly = computed(() => !parentChildLinked.value)

  const getAllKeys = (nodes: T[]): number[] => {
    const keys: number[] = []
    const traverse = (list: T[]) => {
      list.forEach((node) => {
        const key = node?.[options.keyField]
        if (key) keys.push(key)
        const children = node?.[childrenField] as T[] | undefined
        if (children?.length) traverse(children)
      })
    }
    traverse(nodes)
    return keys
  }

  const syncSelectState = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys: any[] = tree.getCheckedKeys?.() || []
    const halfCheckedKeys: any[] = tree.getHalfCheckedKeys?.() || []
    const allKeys = getAllKeys(data.value as T[])

    selectAll.value = checkedKeys.length === allKeys.length && allKeys.length > 0
    indeterminate.value = (checkedKeys.length > 0 || halfCheckedKeys.length > 0) && !selectAll.value
  }

  const toggleSelectAll = () => {
    const tree = treeRef.value
    if (!tree) return

    if (selectAll.value) {
      tree.setCheckedKeys(getAllKeys(data.value as T[]))
      indeterminate.value = false
    } else {
      tree.setCheckedKeys([])
      indeterminate.value = false
    }
    // setCheckedKeys 不会触发 @check，手动同步
    syncSelectState()
  }

  const toggleExpandAll = () => {
    const tree = treeRef.value
    if (!tree) return

    const nodesMap = tree.store?.nodesMap
    if (!nodesMap) return

    Object.values(nodesMap).forEach((node: any) => {
      node.expanded = expandAll.value
    })

    setTimeout(() => adjustContainerHeight(), expandAnimationMs + 50)
  }

  const handleNodeExpand = () => {
    setTimeout(() => adjustContainerHeight(), expandAnimationMs + 50)
  }

  const handleNodeCollapse = () => {
    setTimeout(() => adjustContainerHeight(), expandAnimationMs + 50)
  }

  const adjustContainerHeight = () => {
    if (!containerRef.value) return
    const treeEl = containerRef.value.querySelector('.el-tree') as HTMLElement | null
    if (!treeEl) return

    containerRef.value.style.height = 'auto'
    const height = treeEl.scrollHeight
    containerRef.value.style.height = `${Math.min(height + 24, maxHeight)}px`
  }

  const handleCheckStrictlyChange = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys = tree.getCheckedKeys?.() || []
    const halfCheckedKeys = tree.getHalfCheckedKeys?.() || []
    tree.setCheckedKeys([])
    nextTick(() => {
      tree.setCheckedKeys([...checkedKeys, ...halfCheckedKeys])
      syncSelectState()
    })
  }

  /**
   * 回显勾选：为了“只回显指定 key”，默认会临时关闭父子联动（checkStrictly=true），设值后恢复。
   * 同时多等一个 tick，保证 halfCheckedKeys 稳定，再同步全选/半选状态。
   */
  const applyCheckedKeys = async (keys: number[]) => {
    const tree = treeRef.value
    if (!tree) return

    const originalLink = parentChildLinked.value
    parentChildLinked.value = false
    await nextTick()

    tree.setCheckedKeys(keys)

    parentChildLinked.value = originalLink
    await nextTick()
    await nextTick()
    syncSelectState()
  }

  return {
    treeRef,
    containerRef,
    data,
    expandAll,
    selectAll,
    indeterminate,
    parentChildLinked,
    checkStrictly,

    getAllKeys,
    syncSelectState,
    toggleSelectAll,
    toggleExpandAll,
    handleNodeExpand,
    handleNodeCollapse,
    adjustContainerHeight,
    handleCheckStrictlyChange,
    applyCheckedKeys
  }
}

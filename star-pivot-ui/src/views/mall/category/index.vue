<!-- 商城-商品分类：懒加载树 + 拖拽排序 + 批量删除 + CRUD -->
<template>
  <div class="mall-category-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader
        v-model:columns="headerColumns"
        :loading="loading"
        layout="refresh"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'mall:category:add'" type="primary" @click="openAddRoot" v-ripple>
              新增一级分类
            </ElButton>
            <ElButton
              v-auth="'mall:category:delete'"
              type="danger"
              :disabled="checkedIds.length === 0"
              @click="handleBatchRemove"
              v-ripple
            >
              批量删除
            </ElButton>
            <ElInput
              v-model="keyword"
              clearable
              placeholder="按名称筛选（已加载节点）"
              style="width: 260px"
              maxlength="64"
              @clear="clearKeyword"
            />
          </ElSpace>
        </template>
      </ArtTableHeader>

      <div class="category-tree-wrap" v-loading="loading">
        <ElTree
          :key="treeBootKey"
          ref="treeRef"
          class="category-tree"
          lazy
          :load="loadNode"
          node-key="catId"
          :props="treeProps"
          show-checkbox
          draggable
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          :expand-on-click-node="true"
          :render-after-expand="false"
          :filter-node-method="filterNode"
          highlight-current
          @node-drop="handleNodeDrop"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <div class="tree-node__title">
                <span class="tree-node__name">{{ data.name || '（未命名）' }}</span>
                <ElTag
                  v-if="levelTag(data.catLevel)"
                  :type="levelTag(data.catLevel)!.type"
                  size="small"
                >
                  {{ levelTag(data.catLevel)!.text }}
                </ElTag>
                <ElTag :type="showStatusType(data.showStatus)" size="small">
                  {{ showStatusLabel(data.showStatus) }}
                </ElTag>
                <span v-if="data.sort != null" class="tree-node__sort">排序 {{ data.sort }}</span>
              </div>
              <ElSpace class="tree-node__actions" :size="4" @click.stop>
                <ElButton
                  v-if="canAddChild(data)"
                  v-auth="'mall:category:add'"
                  link
                  type="primary"
                  @click="openAddChild(data)"
                >
                  新增子类
                </ElButton>
                <ElButton v-auth="'mall:category:edit'" link type="primary" @click="openEdit(data)">
                  编辑
                </ElButton>
                <ElButton
                  v-auth="'mall:category:delete'"
                  link
                  type="danger"
                  @click="handleRemove(data)"
                >
                  删除
                </ElButton>
              </ElSpace>
            </div>
          </template>
        </ElTree>
      </div>
      <p class="category-tree-hint">
        懒加载：展开时加载子级；筛选仅作用于已展开节点。拖拽仅限同级排序。
      </p>
    </ElCard>

    <CategoryDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :category-data="currentCategory"
      :parent-for-add="parentForAdd"
      @submit="handleDialogSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import { nextTick } from 'vue'
  import { watchDebounced } from '@vueuse/core'
  import type { ColumnOption } from '@/types/component'
  import {
    fetchMallCategoryById,
    fetchMallCategoryChildren,
    fetchMallCategoryRemove,
    fetchMallCategorySortBatch,
    type MallCategoryTreeNode
  } from '@/api/mall/category'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import CategoryDialog, { type CategoryDialogSubmitPayload } from './modules/category-dialog.vue'
  import { ElButton, ElMessage, ElMessageBox, ElSpace, ElTag, ElTree } from 'element-plus'
  import type { DialogType } from '@/types'
  import type Node from 'element-plus/es/components/tree/src/model/node'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'MallCategory' })

  const { hasAuth } = useAuth()

  type LazyCatNode = MallCategoryTreeNode & { leaf?: boolean }

  const treeProps = {
    label: 'name',
    children: 'children',
    isLeaf: 'leaf'
  }

  const loading = ref(false)
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const keyword = ref('')
  const treeBootKey = ref(0)
  const headerColumns = ref<ColumnOption[]>([])

  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentCategory = ref<Partial<MallCategoryTreeNode>>({})
  const parentForAdd = ref<{ catId: number; name?: string } | null>(null)

  const checkedIds = computed(() => {
    const keys = treeRef.value?.getCheckedKeys(false) as Array<string | number> | undefined
    if (!keys?.length) return []
    return keys.map((k) => Number(k)).filter((id) => Number.isFinite(id))
  })

  function toLazyNode(row: MallCategoryTreeNode): LazyCatNode {
    const lv = row.catLevel != null ? Number(row.catLevel) : NaN
    const leaf = Number.isFinite(lv) ? lv >= 3 : false
    return { ...row, leaf }
  }

  function canAddChild(data: LazyCatNode) {
    const lv = data.catLevel != null ? Number(data.catLevel) : 1
    return Number.isFinite(lv) ? lv < 3 : true
  }

  /** 懒加载下：非叶子且未到三级，视为可能有子分类 */
  function nodeMayHaveChildren(data: LazyCatNode) {
    if (data.leaf === true) return false
    const lv = data.catLevel != null ? Number(data.catLevel) : NaN
    return !Number.isFinite(lv) || lv < 3
  }

  const filterNode = (value: string, data: LazyCatNode) => {
    if (!value) return true
    const q = value.toLowerCase()
    return (data.name || '').toLowerCase().includes(q)
  }

  function applyTreeFilter() {
    treeRef.value?.filter(keyword.value.trim())
  }

  watchDebounced(keyword, () => applyTreeFilter(), { debounce: 220 })

  const clearKeyword = () => {
    keyword.value = ''
    nextTick(() => applyTreeFilter())
  }

  function levelTag(
    lv: number | undefined | null
  ): { type: 'primary' | 'success' | 'warning'; text: string } | null {
    if (lv === undefined || lv === null) return null
    const n = Number(lv)
    const map: Record<number, { type: 'primary' | 'success' | 'warning'; text: string }> = {
      1: { type: 'primary', text: '一级' },
      2: { type: 'success', text: '二级' },
      3: { type: 'warning', text: '三级' }
    }
    return map[n] ?? null
  }

  function showStatusType(s: number | undefined | null): 'success' | 'info' {
    if (s === undefined || s === null) return 'info'
    return Number(s) === 0 ? 'info' : 'success'
  }

  function showStatusLabel(s: number | undefined | null): string {
    if (s === undefined || s === null) return '-'
    return Number(s) === 0 ? '隐藏' : '显示'
  }

  function normalizeParentId(data: MallCategoryTreeNode | undefined | null): number {
    const pid = data?.parentCid
    return pid == null || Number(pid) === 0 ? 0 : Number(pid)
  }

  const allowDrag = () => hasAuth('mall:category:edit')

  const allowDrop = (draggingNode: Node, dropNode: Node, type: 'prev' | 'inner' | 'next') => {
    if (type === 'inner') return false
    return normalizeParentId(draggingNode.data) === normalizeParentId(dropNode.data)
  }

  const loadNode = async (
    node: { level: number; data: LazyCatNode },
    resolve: (data: LazyCatNode[]) => void
  ) => {
    if (node.level === 0) {
      loading.value = true
      try {
        const res = await fetchMallCategoryChildren(0)
        resolve((res || []).map(toLazyNode))
      } catch {
        resolve([])
      } finally {
        loading.value = false
      }
      await nextTick()
      applyTreeFilter()
      return
    }
    const pid = Number(node.data?.catId)
    if (!Number.isFinite(pid)) {
      resolve([])
      return
    }
    try {
      const res = await fetchMallCategoryChildren(pid)
      resolve((res || []).map(toLazyNode))
    } catch {
      resolve([])
    }
    await nextTick()
    applyTreeFilter()
  }

  const refreshData = () => {
    treeBootKey.value++
    nextTick(() => applyTreeFilter())
  }

  /** 从当前节点向上查父级，得到根 → 目标 的 catId 路径 */
  async function buildPathToRoot(catId: number): Promise<number[]> {
    const path: number[] = []
    let currentId: number | undefined = catId
    for (let depth = 0; depth < 4 && currentId != null; depth++) {
      path.unshift(currentId)
      const detail = await fetchMallCategoryById(currentId)
      const pid = detail.parentCid
      if (pid == null || Number(pid) === 0) break
      currentId = Number(pid)
    }
    return path
  }

  async function reloadChildrenOf(parentCid: number) {
    const tree = treeRef.value
    if (!tree) return
    const children = (await fetchMallCategoryChildren(parentCid)).map(toLazyNode)
    if (parentCid === 0) {
      await new Promise<void>((resolve) => {
        tree.store.root.loadData(() => resolve())
      })
      return
    }
    tree.updateKeyChildren(parentCid, children)
  }

  /** 展开祖先链，保证目标节点在 DOM 中可见 */
  async function expandAncestors(catId: number) {
    const tree = treeRef.value
    if (!tree) return
    const path = await buildPathToRoot(catId)
    if (path.length <= 1) return

    for (let i = 0; i < path.length - 1; i++) {
      const id = path[i]!
      let node = tree.getNode(id)
      if (!node) {
        const parentCid = i === 0 ? 0 : path[i - 1]!
        await reloadChildrenOf(parentCid)
        await nextTick()
        node = tree.getNode(id)
      }
      if (!node) continue
      if (!node.expanded) node.expand()
      if (!node.loaded && !node.isLeaf) {
        await new Promise<void>((resolve) => {
          node!.loadData(() => resolve())
        })
      }
    }
  }

  function patchTreeNodeData(catId: number, detail: LazyCatNode) {
    const tree = treeRef.value
    if (!tree) return
    const node = tree.getNode(catId)
    if (!node) return
    Object.assign(node.data, detail)
    if (detail.leaf !== undefined) {
      node.isLeaf = detail.leaf
    }
  }

  function focusTreeNode(catId: number) {
    const tree = treeRef.value
    if (!tree) return
    tree.setCurrentKey(catId)
    nextTick(() => {
      const el = tree.$el?.querySelector('.el-tree-node.is-current .el-tree-node__content') as
        | HTMLElement
        | undefined
      el?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    })
  }

  /** 编辑后：更新节点数据、展开路径、高亮，不重建整树 */
  async function afterEditCategory(catId: number) {
    const detail = toLazyNode(await fetchMallCategoryById(catId))
    const tree = treeRef.value
    if (!tree) return

    if (!tree.getNode(catId)) {
      const path = await buildPathToRoot(catId)
      if (path.length > 1) {
        const parentId = path[path.length - 2]!
        await reloadChildrenOf(parentId)
        await nextTick()
      }
    }

    patchTreeNodeData(catId, detail)
    await expandAncestors(catId)
    await nextTick()
    focusTreeNode(catId)
    applyTreeFilter()
  }

  /** 新增后：仅刷新父级子列表并展开到父节点 */
  async function afterAddCategory(parentCid: number) {
    await reloadChildrenOf(parentCid)
    await nextTick()
    if (parentCid > 0) {
      const tree = treeRef.value
      const parentNode = tree?.getNode(parentCid)
      if (parentNode && !parentNode.expanded) {
        parentNode.expand()
      }
      if (parentNode && !parentNode.loaded && !parentNode.isLeaf) {
        await new Promise<void>((resolve) => {
          parentNode.loadData(() => resolve())
        })
      }
      focusTreeNode(parentCid)
    }
    applyTreeFilter()
  }

  const collectSiblingSortItems = (parentNode: Node) => {
    return parentNode.childNodes.map((n, index) => ({
      catId: Number(n.data.catId),
      sort: index
    }))
  }

  const handleNodeDrop = async (_draggingNode: Node, dropNode: Node) => {
    const parentNode = dropNode.parent
    if (!parentNode) return
    const items = collectSiblingSortItems(parentNode)
    if (!items.length || items.some((i) => !Number.isFinite(i.catId))) return
    try {
      await fetchMallCategorySortBatch(items)
    } catch {
      refreshData()
    }
  }

  const openAddRoot = () => {
    dialogType.value = 'add'
    parentForAdd.value = null
    currentCategory.value = {}
    dialogVisible.value = true
  }

  const openAddChild = (data: LazyCatNode) => {
    dialogType.value = 'add'
    parentForAdd.value = { catId: Number(data.catId), name: data.name }
    currentCategory.value = {}
    dialogVisible.value = true
  }

  const openEdit = (data: LazyCatNode) => {
    dialogType.value = 'edit'
    parentForAdd.value = null
    currentCategory.value = { ...data }
    dialogVisible.value = true
  }

  const sortIdsForDelete = (ids: number[]) => {
    const checkedNodes = (treeRef.value?.getCheckedNodes(false) ?? []) as LazyCatNode[]
    const levelById = new Map<number, number>()
    for (const n of checkedNodes) {
      if (n.catId != null) {
        levelById.set(Number(n.catId), Number(n.catLevel ?? 0))
      }
    }
    return [...ids].sort((a, b) => (levelById.get(b) ?? 0) - (levelById.get(a) ?? 0))
  }

  const handleRemove = async (data: LazyCatNode) => {
    const id = data.catId
    if (id == null) return
    if (nodeMayHaveChildren(data)) {
      ElMessage.warning('请先删除子分类，或展开确认无子节点后再删')
      return
    }
    try {
      await ElMessageBox.confirm(`确定删除分类「${data.name || id}」吗？`, '删除确认', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    try {
      await fetchMallCategoryRemove([id])
      refreshData()
    } catch {
      // 拦截器已提示
    }
  }

  const handleBatchRemove = async () => {
    const ids = checkedIds.value
    if (!ids.length) return

    const checkedNodes = (treeRef.value?.getCheckedNodes(false) ?? []) as LazyCatNode[]
    const withChildren = checkedNodes.filter((n) => nodeMayHaveChildren(n))
    if (withChildren.length > 0) {
      ElMessage.warning('所选含可能仍有子分类的节点，请只勾选三级类目或已确认无子级的节点')
      return
    }

    try {
      await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 个分类吗？`, '批量删除', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    try {
      await fetchMallCategoryRemove(sortIdsForDelete(ids))
      treeRef.value?.setCheckedKeys([])
      refreshData()
    } catch {
      // 拦截器已提示
    }
  }

  const handleDialogSubmit = async (payload: CategoryDialogSubmitPayload) => {
    if (payload.mode === 'edit' && payload.catId != null) {
      try {
        await afterEditCategory(payload.catId)
      } catch {
        refreshData()
      }
      return
    }
    if (payload.mode === 'add') {
      try {
        await afterAddCategory(payload.parentCid ?? 0)
      } catch {
        refreshData()
      }
    }
  }
</script>

<style scoped lang="scss">
  .mall-category-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  .category-tree-wrap {
    min-height: 320px;
    max-height: calc(100vh - 240px);
    padding: 8px 4px 12px;
    overflow: auto;
  }

  .category-tree {
    min-width: min(100%, 760px);
  }

  .category-tree-hint {
    margin: 0 8px 12px;
    font-size: 12px;
    color: var(--art-gray-500);
  }

  :deep(.el-tree-node__content) {
    align-items: center;
    min-height: 40px;
    padding: 4px 8px;
  }

  .tree-node {
    display: flex;
    flex: 1;
    align-items: center;
    gap: 12px;
    min-width: 0;
    padding-right: 4px;
  }

  .tree-node__title {
    display: flex;
    flex: 1;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .tree-node__actions {
    flex-shrink: 0;
  }

  .tree-node__name {
    font-weight: 500;
    color: var(--art-gray-900);
  }

  .tree-node__sort {
    font-size: 12px;
    color: var(--art-gray-500);
  }
</style>

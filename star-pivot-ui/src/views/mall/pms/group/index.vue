<!-- 属性分组管理：左侧三级分类树，右侧分组列表 -->
<template>
  <div class="group-page art-full-height">
    <div class="group-layout">
      <!-- 左侧分类树 -->
      <ElCard shadow="never" class="left-panel category-tree-card">
        <div class="category-tree-header">
          <span class="category-tree-title">属性分组</span>
          <ElButton
            text
            :loading="treeLoading"
            aria-label="刷新分类树"
            @click="refreshCategoryTree"
          >
            <ElIcon><RefreshRight /></ElIcon>
          </ElButton>
        </div>
        <ElInput
          v-model="treeKeyword"
          placeholder="输入关键字进行过滤"
          size="small"
          clearable
          class="category-tree-filter"
        />
        <div v-loading="treeLoading" class="category-tree-wrap">
          <ElTree
            :key="treeBootKey"
            ref="treeRef"
            class="category-tree"
            lazy
            :load="loadTreeNode"
            node-key="catId"
            :props="treeProps"
            highlight-current
            :expand-on-click-node="true"
            :filter-node-method="filterTreeNode"
            @node-click="handleCategoryClick"
          >
            <template #default="{ data }">
              <span class="tree-node-label" :class="{ 'is-selectable': isLevel3Node(data) }">
                {{ data.name || '（未命名）' }}
              </span>
            </template>
          </ElTree>
        </div>
        <div class="category-tree-footer">
          <p
            class="category-tree-tip"
            :class="{ 'is-placeholder': !selectedCategoryName && !browsingCategoryName }"
          >
            <template v-if="selectedCategoryName">当前：{{ selectedCategoryName }}</template>
            <template v-else-if="browsingCategoryName"
              >浏览：{{ browsingCategoryName }}，请选择三级分类</template
            >
            <template v-else>请点击三级分类筛选列表</template>
          </p>
        </div>
      </ElCard>

      <!-- 右侧列表 -->
      <div class="right-panel">
        <GroupSearch v-model="searchForm" @search="handleSearch" @reset="handleResetSearch" />

        <ElCard class="art-table-card" shadow="never">
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton @click="handleSearchAll">查询全部</ElButton>
                <ElButton v-auth="'mall:group:add'" v-ripple @click="showDialog('add')">
                  新增
                </ElButton>
                <ElButton
                  v-auth="'mall:group:import'"
                  type="primary"
                  plain
                  v-ripple
                  @click="importDialogVisible = true"
                >
                  导入
                </ElButton>
                <ElButton
                  v-auth="'mall:group:export'"
                  type="primary"
                  plain
                  :loading="exporting"
                  v-ripple
                  @click="handleExport"
                >
                  导出
                </ElButton>
                <ElButton
                  v-auth="'mall:group:delete'"
                  type="danger"
                  :disabled="selectedRows.length === 0"
                  v-ripple
                  @click="handleBatchDelete"
                >
                  批量删除
                </ElButton>
              </ElSpace>
            </template>
          </ArtTableHeader>

          <ArtTable
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            @selection-change="handleSelectionChange"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />

          <GroupDialog
            v-model:visible="dialogVisible"
            :type="dialogType"
            :group-data="currentGroupData"
            :default-catelog-id="defaultCatelogForDialog"
            @submit="handleDialogSubmit"
          />

          <GroupAttrRelationDialog
            v-model:visible="relationDialogVisible"
            :attr-group-id="relationGroup?.attrGroupId"
            :group-name="relationGroup?.attrGroupName"
            @submit="handleRelationSubmit"
          />

          <ExcelImportDialog
            v-model="importDialogVisible"
            title="属性分组导入"
            :import-fn="doImportGroup"
            :download-template-fn="fetchDownloadGroupImportTemplate"
            @success="refreshData"
          />
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { nextTick } from 'vue'
  import { watchDebounced } from '@vueuse/core'
  import { RefreshRight } from '@element-plus/icons-vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchDeleteGroup,
    fetchDownloadGroupImportTemplate,
    fetchExportGroup,
    fetchGetGroupList,
    fetchImportGroup,
    type Group
  } from '@/api/mall/group'
  import { fetchMallCategoryChildren, type MallCategoryTreeNode } from '@/api/mall/category'
  import { fetchCategoryNameMap, getCategoryDisplayName } from '@/utils/mall/category-tree'
  import GroupSearch from './modules/group-search.vue'
  import GroupDialog from './modules/group-dialog.vue'
  import GroupAttrRelationDialog from './modules/group-attr-relation-dialog.vue'
  import ExcelImportDialog from '@/components/core/forms/excel-import-dialog/index.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { DialogType } from '@/types'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { handleMutationError } from '@/utils/http/mutation'
  import { formatTableIconCell } from '@/utils/ui/table-icon-cell'

  defineOptions({ name: 'Group' })

  type LazyCatNode = MallCategoryTreeNode & { leaf?: boolean }

  const { hasAuth } = useAuth()

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentGroupData = ref<Partial<Group>>({})
  const defaultCatelogForDialog = ref<number | undefined>()
  const relationDialogVisible = ref(false)
  const relationGroup = ref<Partial<Group>>({})
  const importDialogVisible = ref(false)
  const exporting = ref(false)

  const selectedRows = ref<Group[]>([])
  const selectedCatelogId = ref<number | undefined>()
  const selectedCategoryName = ref('')
  const browsingCategoryName = ref('')

  const treeRef = ref()
  const treeKeyword = ref('')
  const treeBootKey = ref(0)
  const treeLoading = ref(false)

  const treeProps = {
    label: 'name',
    children: 'children',
    isLeaf: 'leaf'
  }

  const searchForm = ref({
    attrGroupName: undefined as string | undefined,
    catelogId: undefined as number | undefined
  })

  const categoryNameMap = ref<Record<number, string>>({})

  onMounted(async () => {
    categoryNameMap.value = await fetchCategoryNameMap()
  })

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchGetGroupList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'attrGroupName',
          label: '组名',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'sort',
          label: '排序',
          width: 80
        },
        {
          prop: 'descript',
          label: '描述',
          minWidth: 140,
          showOverflowTooltip: true
        },
        {
          prop: 'icon',
          label: '组图标',
          width: 72,
          align: 'center',
          formatter: (row: Group) => formatTableIconCell(row.icon)
        },
        {
          prop: 'catelogId',
          label: '所属分类',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: Group) => getCategoryDisplayName(categoryNameMap.value, row.catelogId)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 230,
          fixed: 'right',
          formatter: (row) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:group:query')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '关联属性',
                  icon: 'ri:links-line',
                  iconClass: 'bg-primary/12 text-primary',
                  onClick: () => openAttrRelation(row)
                })
              )
            }
            if (hasAuth('mall:group:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '编辑',
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }
            if (hasAuth('mall:group:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '删除',
                  type: 'delete',
                  onClick: () => deleteGroup(row)
                })
              )
            }
            if (actions.length === 0) {
              return h('span', { style: 'color: #999' }, '')
            }
            return h('div', actions)
          }
        }
      ]
    },
    transform: {
      dataTransformer: (records) => (Array.isArray(records) ? records : [])
    }
  })

  watch(
    categoryNameMap,
    () => {
      if (data.value?.length) {
        data.value = [...data.value]
      }
    },
    { deep: true }
  )

  function toLazyNode(row: MallCategoryTreeNode): LazyCatNode {
    const lv = row.catLevel != null ? Number(row.catLevel) : NaN
    const leaf = Number.isFinite(lv) ? lv >= 3 : false
    return { ...row, leaf }
  }

  function isLevel3Node(data: LazyCatNode) {
    const lv = data.catLevel != null ? Number(data.catLevel) : NaN
    return Number.isFinite(lv) && lv === 3
  }

  const filterTreeNode = (value: string, data: LazyCatNode) => {
    if (!value) return true
    return (data.name || '').toLowerCase().includes(value.toLowerCase())
  }

  function applyTreeFilter() {
    treeRef.value?.filter(treeKeyword.value.trim())
  }

  watchDebounced(treeKeyword, () => applyTreeFilter(), { debounce: 220 })

  const loadTreeNode = async (
    node: { level: number; data: LazyCatNode },
    resolve: (data: LazyCatNode[]) => void
  ) => {
    if (node.level === 0) {
      treeLoading.value = true
      try {
        const res = await fetchMallCategoryChildren(0)
        resolve((res || []).map(toLazyNode))
      } catch {
        resolve([])
      } finally {
        treeLoading.value = false
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

  const refreshCategoryTree = () => {
    treeBootKey.value++
    nextTick(() => applyTreeFilter())
  }

  function applyCategoryFilter(catId: number, name?: string) {
    browsingCategoryName.value = ''
    selectedCatelogId.value = catId
    selectedCategoryName.value = name || getCategoryDisplayName(categoryNameMap.value, catId)
    searchForm.value.catelogId = catId
    Object.assign(searchParams, {
      catelogId: catId,
      attrGroupName: searchForm.value.attrGroupName,
      pageNum: 1
    })
    getData()
  }

  const handleCategoryClick = (data: LazyCatNode) => {
    if (isLevel3Node(data) && data.catId != null) {
      treeRef.value?.setCurrentKey(data.catId)
      applyCategoryFilter(Number(data.catId), data.name)
      return
    }
    browsingCategoryName.value = data.name || '（未命名）'
    if (data.catId != null) {
      treeRef.value?.setCurrentKey(data.catId)
    }
    const hadFilter = selectedCatelogId.value != null
    selectedCatelogId.value = undefined
    selectedCategoryName.value = ''
    searchForm.value.catelogId = undefined
    if (hadFilter) {
      Object.assign(searchParams, {
        catelogId: undefined,
        attrGroupName: searchForm.value.attrGroupName,
        pageNum: 1
      })
      getData()
    }
  }

  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, {
      ...params,
      catelogId: selectedCatelogId.value,
      pageNum: 1
    })
    getData()
  }

  const handleResetSearch = () => {
    resetSearchParams()
    searchForm.value.attrGroupName = undefined
    searchForm.value.catelogId = selectedCatelogId.value
    if (selectedCatelogId.value != null) {
      Object.assign(searchParams, { catelogId: selectedCatelogId.value, pageNum: 1 })
    }
    getData()
  }

  const handleSearchAll = () => {
    browsingCategoryName.value = ''
    selectedCatelogId.value = undefined
    selectedCategoryName.value = ''
    searchForm.value.catelogId = undefined
    treeRef.value?.setCurrentKey(undefined)
    Object.assign(searchParams, {
      catelogId: undefined,
      attrGroupName: searchForm.value.attrGroupName,
      pageNum: 1
    })
    getData()
  }

  const showDialog = (type: DialogType, row?: Group): void => {
    dialogType.value = type
    currentGroupData.value = row || {}
    defaultCatelogForDialog.value = type === 'add' ? selectedCatelogId.value : undefined
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const openAttrRelation = (row: Group) => {
    if (row.attrGroupId == null) return
    relationGroup.value = { ...row }
    nextTick(() => {
      relationDialogVisible.value = true
    })
  }

  const handleRelationSubmit = () => {
    relationGroup.value = {}
  }

  const deleteGroup = async (row: Group): Promise<void> => {
    try {
      await ElMessageBox.confirm('确定要删除该属性分组吗？', '删除属性分组', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteGroup([row.attrGroupId!])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的属性分组')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定删除选中的 ${selectedRows.value.length} 个属性分组吗？`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      const ids = selectedRows.value.map((row) => row.attrGroupId!).filter(Boolean)
      await fetchDeleteGroup(ids)
      selectedRows.value = []
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      handleMutationError(error, '批量删除失败')
    }
  }

  const handleDialogSubmit = () => {
    dialogVisible.value = false
    currentGroupData.value = {}
    refreshData()
  }

  const handleSelectionChange = (selection: Group[]): void => {
    selectedRows.value = selection
  }

  const buildExportParams = () => ({
    attrGroupName: searchForm.value.attrGroupName,
    catelogId: selectedCatelogId.value ?? searchForm.value.catelogId
  })

  const handleExport = async () => {
    exporting.value = true
    try {
      await fetchExportGroup(buildExportParams())
    } catch {
      ElMessage.error('导出失败')
    } finally {
      exporting.value = false
    }
  }

  const doImportGroup = (file: File, updateSupport: boolean) =>
    fetchImportGroup(file, updateSupport)
</script>

<style scoped lang="scss">
  .group-page {
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 12px;
  }

  .group-layout {
    display: flex;
    flex: 1;
    gap: 12px;
    min-height: 0;
  }

  .left-panel {
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    width: 280px;
    min-height: 0;

    :deep(.el-card__body) {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-height: 0;
      padding: 12px;
    }
  }

  .category-tree-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .category-tree-title {
    font-size: 14px;
    font-weight: 600;
  }

  .category-tree-filter {
    margin-bottom: 8px;
  }

  .category-tree-wrap {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  .category-tree {
    min-width: 100%;
  }

  .tree-node-label.is-selectable {
    cursor: pointer;
  }

  .category-tree-footer {
    flex-shrink: 0;
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .category-tree-tip {
    margin: 0;
    font-size: 12px;
    line-height: 1.4;
    color: var(--el-text-color-secondary);

    &.is-placeholder {
      color: var(--el-text-color-placeholder);
    }
  }

  .right-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
    min-height: 0;

    .art-table-card {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-height: 0;
    }
  }
</style>

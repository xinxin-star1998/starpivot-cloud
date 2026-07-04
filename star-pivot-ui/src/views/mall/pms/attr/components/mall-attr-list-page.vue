<!-- 商品属性列表：左侧三级分类树 + 右侧属性表格（按 attrType 区分） -->
<template>
  <div class="mall-attr-page art-full-height">
    <div class="mall-attr-layout">
      <ElCard shadow="never" class="left-panel category-tree-card">
        <div class="category-tree-header">
          <span class="category-tree-title">{{ pageTitle }}</span>
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

      <div class="right-panel">
        <AttrSearch v-model="searchForm" @search="handleSearch" @reset="handleResetSearch" />

        <ElCard class="art-table-card" shadow="never">
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton @click="handleSearchAll">查询全部</ElButton>
                <ElButton v-auth="perm.add" v-ripple @click="showDialog('add')">新增</ElButton>
                <ElButton
                  v-auth="perm.import"
                  type="primary"
                  plain
                  v-ripple
                  @click="importDialogVisible = true"
                >
                  导入
                </ElButton>
                <ElButton
                  v-auth="perm.export"
                  type="primary"
                  plain
                  :loading="exporting"
                  v-ripple
                  @click="handleExport"
                >
                  导出
                </ElButton>
                <ElButton
                  v-auth="perm.delete"
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

          <AttrDialog
            v-model:visible="dialogVisible"
            :type="dialogType"
            :attr-type="props.attrType"
            :attr-data="currentAttrData"
            :default-catelog-id="defaultCatelogForDialog"
            @submit="handleDialogSubmit"
          />

          <ExcelImportDialog
            v-model="importDialogVisible"
            :title="importDialogTitle"
            :import-fn="doImportAttr"
            :download-template-fn="downloadAttrTemplate"
            @success="refreshData"
          />
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {nextTick} from 'vue'
import {watchDebounced} from '@vueuse/core'
import {RefreshRight} from '@element-plus/icons-vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {
  fetchDeleteAttr,
  fetchDownloadAttrImportTemplate,
  fetchExportAttr,
  fetchGetAttrList,
  fetchImportAttr,
  type MallAttr
} from '@/api/mall/attr'
import {fetchMallCategoryChildren, type MallCategoryTreeNode} from '@/api/mall/category'
import {fetchCategoryNameMap, getCategoryDisplayName} from '@/utils/mall/category-tree'
import {formatTableIconCell} from '@/utils/ui/table-icon-cell'
import AttrSearch from '../modules/attr-search.vue'
import AttrDialog from '../modules/attr-dialog.vue'
import ExcelImportDialog from '@/components/core/forms/excel-import-dialog/index.vue'
import {ElMessage, ElMessageBox, ElTag, ElTooltip} from 'element-plus'
import {formatValueSelectBrief, getAttrValueSelect} from '@/utils/mall/attr-value-select'
import type {DialogType} from '@/types'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {useAuth} from '@/hooks/core/useAuth'
import {handleMutationError} from '@/utils/http/mutation'

const props = withDefaults(
    defineProps<{
      /** 0-销售属性 1-基本属性 */
      attrType: 0 | 1
      pageTitle?: string
    }>(),
    {
      pageTitle: '商品属性'
    }
  )

  /** 与后端 PmsAttrController、菜单 authMark 一致：基本属性 mall:base:*，销售属性 mall:sale:* */
  const perm = computed(() => {
    const scope = props.attrType === 1 ? 'mall:base' : 'mall:sale'
    return {
      query: `${scope}:query`,
      add: `${scope}:add`,
      edit: `${scope}:edit`,
      delete: `${scope}:delete`,
      import: `${scope}:import`,
      export: `${scope}:export`
    }
  })

  const importDialogTitle = computed(() => (props.attrType === 1 ? '基本属性导入' : '销售属性导入'))

  type LazyCatNode = MallCategoryTreeNode & { leaf?: boolean }

  const { hasAuth } = useAuth()

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentAttrData = ref<Partial<MallAttr>>({})
  const defaultCatelogForDialog = ref<number | undefined>()
  const selectedRows = ref<MallAttr[]>([])
  const importDialogVisible = ref(false)
  const exporting = ref(false)

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
    attrName: undefined as string | undefined,
    catelogId: undefined as number | undefined
  })

  const categoryNameMap = ref<Record<number, string>>({})

  onMounted(async () => {
    categoryNameMap.value = await fetchCategoryNameMap()
  })

  const labelYesNo = (v: number | undefined | null, yes = '是', no = '否') =>
    Number(v) === 1 ? yes : no

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
      apiFn: fetchGetAttrList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        attrType: props.attrType,
        ...searchForm.value
      },
      columnsFactory: () => {
        const p = props.attrType === 1 ? 'mall:base' : 'mall:sale'
        return [
          { type: 'selection' },
          { type: 'index', width: 60, label: '序号' },
          { prop: 'attrName', label: '属性名', minWidth: 120, showOverflowTooltip: true },
          {
            prop: 'searchType',
            label: '检索',
            width: 80,
            formatter: (row: MallAttr) => labelYesNo(row.searchType, '需要', '不需要')
          },
          {
            prop: 'valueType',
            label: '值类型',
            width: 90,
            formatter: (row: MallAttr) => labelYesNo(row.valueType, '多选', '单选')
          },
          {
            prop: 'icon',
            label: '图标',
            width: 72,
            align: 'center',
            formatter: (row: MallAttr) => formatTableIconCell(row.icon)
          },
          {
            prop: 'valueSelect',
            label: '可选值',
            minWidth: 160,
            formatter: (row: MallAttr) => {
              const { full, tags, restCount } = formatValueSelectBrief(getAttrValueSelect(row))
              if (!tags.length) {
                return h('span', { class: 'value-select-empty' }, '—')
              }
              const tagNodes = [
                h(ElTag, { type: 'success', size: 'small' }, () => tags[0]),
                ...(restCount > 0
                  ? [h(ElTag, { type: 'success', size: 'small' }, () => `+${restCount}`)]
                  : [])
              ]
              const tagsEl = h('span', { class: 'value-select-tags' }, tagNodes)
              if (restCount > 0) {
                return h(
                  ElTooltip,
                  { content: full, placement: 'top', showAfter: 300 },
                  { default: () => tagsEl }
                )
              }
              return tagsEl
            }
          },
          {
            prop: 'enable',
            label: '启用',
            width: 72,
            formatter: (row: MallAttr) => labelYesNo(row.enable, '启用', '禁用')
          },
          {
            prop: 'catelogId',
            label: '所属分类',
            minWidth: 120,
            showOverflowTooltip: true,
            formatter: (row: MallAttr) =>
              getCategoryDisplayName(categoryNameMap.value, row.catelogId)
          },
          {
            prop: 'showDesc',
            label: '快速展示',
            width: 90,
            formatter: (row: MallAttr) => labelYesNo(row.showDesc)
          },
          {
            prop: 'operation',
            label: '操作',
            width: 150,
            fixed: 'right',
            formatter: (row: MallAttr) => {
              const actions: ReturnType<typeof h>[] = []
              if (hasAuth(`${p}:edit`)) {
                actions.push(
                  h(ArtButtonTable, {
                    label: '编辑',
                    type: 'edit',
                    onClick: () => showDialog('edit', row)
                  })
                )
              }
              if (hasAuth(`${p}:delete`)) {
                actions.push(
                  h(ArtButtonTable, {
                    label: '删除',
                    type: 'delete',
                    onClick: () => deleteAttr(row)
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
      }
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

  function mergeListParams(extra: Record<string, unknown> = {}) {
    return {
      attrType: props.attrType,
      attrName: searchForm.value.attrName,
      catelogId: selectedCatelogId.value,
      pageNum: 1,
      ...extra
    }
  }

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
    Object.assign(searchParams, mergeListParams())
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
      Object.assign(searchParams, mergeListParams({ catelogId: undefined }))
      getData()
    }
  }

  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, mergeListParams(params))
    getData()
  }

  const handleResetSearch = () => {
    resetSearchParams()
    searchForm.value.attrName = undefined
    Object.assign(searchParams, mergeListParams())
    getData()
  }

  const handleSearchAll = () => {
    browsingCategoryName.value = ''
    selectedCatelogId.value = undefined
    selectedCategoryName.value = ''
    searchForm.value.catelogId = undefined
    treeRef.value?.setCurrentKey(undefined)
    Object.assign(searchParams, mergeListParams({ catelogId: undefined }))
    getData()
  }

  const showDialog = (type: DialogType, row?: MallAttr) => {
    dialogType.value = type
    currentAttrData.value = row || {}
    // 左侧已选分类时预填弹窗，未选则在弹窗内选三级分类
    defaultCatelogForDialog.value = type === 'add' ? selectedCatelogId.value : undefined
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const deleteAttr = async (row: MallAttr) => {
    try {
      await ElMessageBox.confirm('确定要删除该属性吗？', '删除属性', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteAttr([row.attrId!])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的属性')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定删除选中的 ${selectedRows.value.length} 个属性吗？`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      const ids = selectedRows.value.map((r) => r.attrId!).filter(Boolean)
      await fetchDeleteAttr(ids)
      selectedRows.value = []
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      handleMutationError(error, '批量删除失败')
    }
  }

  const handleDialogSubmit = () => {
    dialogVisible.value = false
    currentAttrData.value = {}
    refreshData()
  }

  const handleSelectionChange = (selection: MallAttr[]) => {
    selectedRows.value = selection
  }

  const buildExportParams = () => ({
    attrType: props.attrType,
    attrName: searchForm.value.attrName,
    catelogId: selectedCatelogId.value ?? searchForm.value.catelogId
  })

  const handleExport = async () => {
    exporting.value = true
    try {
      await fetchExportAttr(buildExportParams())
    } catch {
      ElMessage.error('导出失败')
    } finally {
      exporting.value = false
    }
  }

  const doImportAttr = (file: File, updateSupport: boolean) =>
    fetchImportAttr(file, props.attrType, updateSupport)

  const downloadAttrTemplate = () => fetchDownloadAttrImportTemplate(props.attrType)
</script>

<style scoped lang="scss">
  .mall-attr-page {
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 12px;
  }

  .mall-attr-layout {
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

  .value-select-empty {
    color: var(--el-text-color-placeholder);
  }

  .value-select-tags {
    display: inline-flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;
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

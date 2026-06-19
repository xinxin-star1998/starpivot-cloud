<!-- 商城-SPU 管理：左侧三级分类树，右侧商品列表 -->
<template>
  <div class="mall-product-page art-full-height">
    <div class="product-layout">
      <!-- 左侧分类树 -->
      <ElCard shadow="never" class="left-panel category-tree-card">
        <div class="category-tree-header">
          <span class="category-tree-title">商品分类</span>
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

      <!-- 右侧商品列表 -->
      <div class="right-panel">
        <ProductSearch v-model="searchForm" @search="handleSearch" @reset="handleResetSearch" />

        <ElCard class="art-table-card" shadow="never">
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton @click="handleSearchAll">查询全部</ElButton>
                <ElButton v-auth="'mall:product:add'" type="primary" @click="goAddSpu" v-ripple>
                  新增 SPU
                </ElButton>
                <ElButton
                  v-auth="'mall:product:delete'"
                  type="danger"
                  :disabled="selectedRows.length === 0"
                  @click="handleBatchDelete"
                  v-ripple
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
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { h, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import { watchDebounced } from '@vueuse/core'
  import { RefreshRight } from '@element-plus/icons-vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchMallProductList,
    fetchMallProductPublishStatus,
    fetchMallProductRemove,
    type MallProductVo
  } from '@/api/mall/product'
  import { fetchMallCategoryChildren, type MallCategoryTreeNode } from '@/api/mall/category'
  import { fetchCategoryNameMap, getCategoryDisplayName } from '@/utils/mall/category-tree'
  import { fetchBrandNameMap, getBrandDisplayName } from '@/utils/mall/brand-map'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ProductSearch from './modules/product-search.vue'
  import { ElImage, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'
  import { getCoverDisplayUrl, resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'MallProduct' })

  const coverImgDisplayUrls = ref<Map<string, string>>(new Map())
  const coverImgVersion = ref(0)

  const preloadCoverImages = async (rows: MallProductVo[]) => {
    if (!rows?.length) return
    const coverImgs = rows
      .map((row) => row.coverImg?.trim() || row.images?.[0]?.trim())
      .filter((url): url is string => !!url)
    if (!coverImgs.length) return
    const resolved = await resolveGoodsImageDisplayUrls(coverImgs)
    resolved.forEach((url, key) => coverImgDisplayUrls.value.set(key, url))
    coverImgVersion.value++
  }

  type LazyCatNode = MallCategoryTreeNode & { leaf?: boolean }

  const { hasAuth } = useAuth()
  const router = useRouter()

  const searchForm = ref({
    spuName: undefined as string | undefined,
    catalogId: undefined as number | undefined,
    brandId: undefined as number | undefined,
    publishStatus: undefined as number | undefined
  })

  const selectedRows = ref<MallProductVo[]>([])

  const selectedCatalogId = ref<number | undefined>()
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

  const categoryNameMap = ref<Record<number, string>>({})
  const brandNameMap = ref<Record<number, string>>({})

  onMounted(async () => {
    const [catMap, brandMap] = await Promise.all([fetchCategoryNameMap(), fetchBrandNameMap()])
    categoryNameMap.value = catMap
    brandNameMap.value = brandMap
  })

  const formatWeight = (row: MallProductVo) => {
    const w = row.weight
    if (w === null || w === undefined || w === '') return '-'
    const n = Number(w)
    return Number.isFinite(n) ? String(n) : String(w)
  }

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
      apiFn: fetchMallProductList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          type: 'index',
          label: '序号',
          width: 70,
          index: (index: number) => (pagination.current - 1) * pagination.size + index + 1
        },
        {
          prop: 'coverImg',
          label: '图片',
          width: 88,
          formatter: (row: MallProductVo) => {
            void coverImgVersion.value
            const raw = row.coverImg?.trim() || row.images?.[0]?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, coverImgDisplayUrls.value)
            if (!displayUrl) return '-'
            return h(ElImage, {
              src: displayUrl,
              fit: 'cover',
              previewSrcList: [displayUrl],
              previewTeleported: true,
              style: 'width:56px;height:56px;border-radius:4px'
            })
          }
        },
        { prop: 'spuName', label: 'SPU 名称', minWidth: 160, showOverflowTooltip: true },
        {
          prop: 'spuDescription',
          label: '描述',
          minWidth: 180,
          showOverflowTooltip: true
        },
        {
          prop: 'catalogId',
          label: '所属分类',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: MallProductVo) =>
            getCategoryDisplayName(categoryNameMap.value, row.catalogId)
        },
        {
          prop: 'brandId',
          label: '品牌',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: MallProductVo) => getBrandDisplayName(brandNameMap.value, row.brandId)
        },
        {
          prop: 'weight',
          label: '重量',
          width: 100,
          formatter: (row: MallProductVo) => formatWeight(row)
        },
        {
          prop: 'publishStatus',
          label: '上架状态',
          width: 110,
          formatter: (row: MallProductVo) => {
            const s = row.publishStatus
            if (s === undefined || s === null) {
              return '-'
            }
            const on = s === 1
            return h(ElTag, { type: on ? 'success' : 'info' }, () => (on ? '上架' : '下架'))
          }
        },
        { prop: 'createTime', label: '创建时间', width: 170 },
        { prop: 'updateTime', label: '更新时间', width: 170 },
        {
          prop: 'operation',
          label: '操作',
          width: 240,
          fixed: 'right',
          formatter: (row: MallProductVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:product:edit')) {
              const onShelf = row.publishStatus === 1
              actions.push(
                h(ArtButtonTable, {
                  label: onShelf ? '下架' : '上架',
                  icon: onShelf ? 'ri:arrow-down-circle-line' : 'ri:arrow-up-circle-line',
                  iconClass: onShelf ? 'bg-warning/12 text-warning' : 'bg-success/12 text-success',
                  onClick: () => togglePublishStatus(row)
                })
              )
              actions.push(
                h(ArtButtonTable, {
                  label: '编辑',
                  type: 'edit',
                  onClick: () => goEditSpu(row)
                })
              )
            }
            if (hasAuth('mall:product:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '删除',
                  type: 'delete',
                  onClick: () => deleteOne(row)
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
    hooks: {
      onSuccess: (rows) => {
        preloadCoverImages(rows)
      },
      onCacheHit: (rows) => {
        preloadCoverImages(rows)
      }
    }
  })

  watch(
    [categoryNameMap, brandNameMap],
    () => {
      if (data.value?.length) {
        data.value = [...data.value]
      }
    },
    { deep: true }
  )

  onActivated(() => {
    refreshData()
  })

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
    selectedCatalogId.value = catId
    selectedCategoryName.value = name || getCategoryDisplayName(categoryNameMap.value, catId)
    searchForm.value.catalogId = catId
    Object.assign(searchParams, {
      catalogId: catId,
      spuName: searchForm.value.spuName,
      publishStatus: searchForm.value.publishStatus,
      brandId: searchForm.value.brandId,
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
    const hadFilter = selectedCatalogId.value != null
    selectedCatalogId.value = undefined
    selectedCategoryName.value = ''
    searchForm.value.catalogId = undefined
    if (hadFilter) {
      Object.assign(searchParams, {
        catalogId: undefined,
        spuName: searchForm.value.spuName,
        publishStatus: searchForm.value.publishStatus,
        brandId: searchForm.value.brandId,
        pageNum: 1
      })
      getData()
    }
  }

  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, {
      ...params,
      catalogId: selectedCatalogId.value,
      pageNum: 1
    })
    getData()
  }

  const handleResetSearch = () => {
    resetSearchParams()
    searchForm.value.spuName = undefined
    searchForm.value.publishStatus = undefined
    searchForm.value.brandId = undefined
    searchForm.value.catalogId = selectedCatalogId.value
    if (selectedCatalogId.value != null) {
      Object.assign(searchParams, { catalogId: selectedCatalogId.value, pageNum: 1 })
    }
    getData()
  }

  const handleSearchAll = () => {
    browsingCategoryName.value = ''
    selectedCatalogId.value = undefined
    selectedCategoryName.value = ''
    searchForm.value.catalogId = undefined
    treeRef.value?.setCurrentKey(undefined)
    Object.assign(searchParams, {
      catalogId: undefined,
      spuName: searchForm.value.spuName,
      publishStatus: searchForm.value.publishStatus,
      brandId: searchForm.value.brandId,
      pageNum: 1
    })
    getData()
  }

  const goAddSpu = () => {
    const query =
      selectedCatalogId.value != null ? { catalogId: String(selectedCatalogId.value) } : undefined
    router.push({ path: '/mall/product/add', query })
  }

  const goEditSpu = (row: MallProductVo) => {
    if (!row.id) return
    router.push({ path: `/mall/product/edit/${row.id}` })
  }

  const togglePublishStatus = (row: MallProductVo) => {
    if (row.id == null) return
    const onShelf = row.publishStatus === 1
    const nextStatus = (onShelf ? 0 : 1) as 0 | 1
    const action = onShelf ? '下架' : '上架'
    ElMessageBox.confirm(
      `确定${action} SPU「${row.spuName || row.id}」吗？${onShelf ? '下架后其 SKU 将不再出现在 SKU 列表中。' : ''}`,
      `${action}商品`,
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
      .then(async () => {
        await fetchMallProductPublishStatus(row.id!, nextStatus)
        refreshData()
      })
      .catch(() => {})
  }

  const handleSelectionChange = (selection: MallProductVo[]) => {
    selectedRows.value = selection
  }

  const deleteOne = (row: MallProductVo) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除 SPU「${row.spuName || row.id}」吗？`, '删除 SPU', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchMallProductRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }

  const handleBatchDelete = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的 SPU')
      return
    }
    const names = selectedRows.value.map((r) => r.spuName || r.id).join('、')
    ElMessageBox.confirm(`确定删除以下 SPU 吗？\n${names}`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        const ids = selectedRows.value.map((r) => r.id!).filter(Boolean)
        await fetchMallProductRemove(ids)
        selectedRows.value = []
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .mall-product-page {
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 12px;
  }

  .product-layout {
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
      margin-top: 12px;
    }
  }
</style>

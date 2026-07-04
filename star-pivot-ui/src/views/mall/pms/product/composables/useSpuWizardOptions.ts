import {fetchMallBrandList, type MallBrandVo} from '@/api/mall/brand'
import {fetchMallCategoryTree, type MallCategoryTreeNode} from '@/api/mall/category'
import {filterVisibleCategoryTree, findCategoryPath, mapCategoryCascaderOptions} from '@/utils/mall/category-tree'
import {pageRows} from '@/utils/mall/spu-wizard-attr'
import {fetchWmsWareInfoList, type WmsWareInfoVo} from '@/api/mall/wareinfo'

export function useSpuWizardOptions() {
  const categoryOptions = ref<MallCategoryTreeNode[]>([])
  const categoryTreeFull = ref<MallCategoryTreeNode[]>([])
  const catalogPath = ref<number[]>([])
  const brandOptions = ref<MallBrandVo[]>([])
  const wareOptions = ref<WmsWareInfoVo[]>([])

  const cascaderProps = {
    value: 'catId',
    label: 'name',
    children: 'children',
    leaf: 'leaf',
    emitPath: true,
    expandTrigger: 'click' as const
  }

  const loadCategoryOptions = async () => {
    try {
      const tree = await fetchMallCategoryTree()
      categoryTreeFull.value = tree || []
      categoryOptions.value = mapCategoryCascaderOptions(filterVisibleCategoryTree(tree || []))
    } catch {
      categoryTreeFull.value = []
      categoryOptions.value = []
    }
  }

  const loadBrands = async () => {
    try {
      const res = await fetchMallBrandList({ pageNum: 1, pageSize: 500, showStatus: 1 })
      brandOptions.value = pageRows<MallBrandVo>(res)
    } catch {
      brandOptions.value = []
    }
  }

  const loadWarehouses = async () => {
    try {
      const res = await fetchWmsWareInfoList({ pageNum: 1, pageSize: 200 })
      wareOptions.value = res.rows || []
    } catch {
      wareOptions.value = []
    }
  }

  const syncCatalogPath = (id?: number) => {
    if (id == null) {
      catalogPath.value = []
      return
    }
    catalogPath.value =
      findCategoryPath(categoryTreeFull.value, id) ??
      findCategoryPath(categoryOptions.value, id) ??
      []
  }

  const loadAllOptions = () => Promise.all([loadCategoryOptions(), loadBrands(), loadWarehouses()])

  return {
    categoryOptions,
    categoryTreeFull,
    catalogPath,
    brandOptions,
    wareOptions,
    cascaderProps,
    syncCatalogPath,
    loadAllOptions
  }
}

import request from '@/utils/http'

/** 分类树节点（与后端 CategoryTreeVo / pms_category 一致） */
export interface MallCategoryTreeNode {
  catId?: number
  name?: string
  parentCid?: number
  catLevel?: number
  showStatus?: number /** 0-隐藏；1-显示（与品牌 showStatus 一致） */
  sort?: number
  icon?: string
  productUnit?: string
  productCount?: number
  children?: MallCategoryTreeNode[]
  /** 级联：是否为叶子（仅三级应为 true） */
  leaf?: boolean
  disabled?: boolean
}

/** 三级（或多级）类目树 */
export function fetchMallCategoryTree() {
  return request.get<MallCategoryTreeNode[]>({
    url: '/api/mall/category/tree'
  })
}

/** 按父 id 懒加载直接子节点；parentCid 为 0 或未传表示一级类目 */
export function fetchMallCategoryChildren(parentCid: number) {
  return request.get<MallCategoryTreeNode[]>({
    url: '/api/mall/category/children',
    params: { parentCid }
  })
}

/** 新增/修改提交体（与后端 CategorySaveBo 一致） */
export interface MallCategorySavePayload {
  catId?: number
  name: string
  /** 新增时有效：0 表示一级类目；编辑时后端忽略 */
  parentCid?: number
  sort?: number
  showStatus: number
  icon?: string
  productUnit?: string
}

export function fetchMallCategoryById(id: number) {
  return request.get<MallCategoryTreeNode>({
    url: `/api/mall/category/${id}`
  })
}

export function fetchMallCategoryAdd(data: MallCategorySavePayload) {
  return request.post<void>({
    url: '/api/mall/category',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallCategoryUpdate(data: MallCategorySavePayload) {
  return request.put<void>({
    url: '/api/mall/category',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallCategoryRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/category/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

/** 同级拖拽排序（items 为同一 parent 下的 catId + sort） */
export interface MallCategorySortItem {
  catId: number
  sort: number
}

export function fetchMallCategorySortBatch(items: MallCategorySortItem[]) {
  return request.put<void>({
    url: '/api/mall/category/sort',
    data: { items },
    showSuccessMessage: true
  })
}

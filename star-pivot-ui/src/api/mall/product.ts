import request from '@/utils/http'
import { canSubmitMallAudit } from '@/utils/mall/audit-status'

/** SPU VO（pms_spu_info + 关联数据） */
export interface MallProductVo {
  id?: number
  spuName?: string
  spuDescription?: string
  catalogId?: number
  brandId?: number
  weight?: number | string
  publishStatus?: number
  approvalInstanceId?: number
  auditStatus?: string
  createTime?: string
  updateTime?: string
  /** 列表封面（图集默认或首张） */
  coverImg?: string
  decript?: string[]
  images?: string[]
  baseAttrs?: MallProductBaseAttr[]
  skus?: MallProductSku[]
}

export interface MallProductBaseAttr {
  attrId: number
  attrName?: string
  attrValues: string
  showDesc: number
}

export interface MallProductSkuAttr {
  attrId: number
  attrName: string
  attrValue: string
}

export interface MallProductSkuImage {
  imgUrl: string
  defaultImg: number
}

export interface MallProductSku {
  attr: MallProductSkuAttr[]
  skuName: string
  price: number
  skuTitle?: string
  skuSubtitle?: string
  images?: MallProductSkuImage[]
  descar?: string[]
  fullCount?: number
  discount?: number
  countStatus?: number
  fullPrice?: number
  reducePrice?: number
  priceStatus?: number
  initialStock?: number
  stockWarning?: number
}

export interface MallProductListParams extends Api.Common.CommonSearchParams {
  spuName?: string
  catalogId?: number
  brandId?: number
  publishStatus?: number
}

export interface MallProductSavePayload {
  id?: number
  spuName: string
  spuDescription?: string
  catalogId: number
  brandId?: number | null
  weight: number
  publishStatus: number
  decript?: string[]
  images?: string[]
  baseAttrs?: MallProductBaseAttr[]
  skus?: MallProductSku[]
  /** 发布时默认入库仓库（仅新增商品时与 initialStock 配合） */
  defaultWareId?: number
}

export function fetchMallProductList(params: MallProductListParams) {
  return request.post<Api.Common.PaginatedResponse<MallProductVo>>({
    url: '/api/mall/product/productPageList',
    data: params
  })
}

export function fetchMallProductById(id: number) {
  return request.get<MallProductVo>({
    url: `/api/mall/product/${id}`
  })
}

export function fetchMallProductAdd(data: MallProductSavePayload) {
  return request.post<void>({
    url: '/api/mall/product',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallProductUpdate(data: MallProductSavePayload) {
  return request.put<void>({
    url: '/api/mall/product',
    data,
    showSuccessMessage: true
  })
}

export function fetchMallProductRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/product/removeProduct',
    data: { ids },
    showSuccessMessage: true
  })
}

/** SPU 上架/下架：publishStatus 0-下架 1-上架（上架需先走审批） */
export function fetchMallProductPublishStatus(id: number, publishStatus: 0 | 1) {
  return request.put<void>({
    url: '/api/mall/product/publish-status',
    data: { id, publishStatus },
    showSuccessMessage: true
  })
}

export function fetchMallProductSubmitApproval(id: number) {
  return request.post<void>({
    url: `/api/mall/product/${id}/submit-approval`,
    showSuccessMessage: true
  })
}

export function canSubmitProductAudit(row: Pick<MallProductVo, 'publishStatus' | 'auditStatus'>) {
  return row.publishStatus !== 1 && canSubmitMallAudit(row.auditStatus)
}

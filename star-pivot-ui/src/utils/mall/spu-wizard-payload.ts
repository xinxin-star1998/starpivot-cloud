import type {MallProductBaseAttr, MallProductSavePayload, MallProductSku, MallProductSkuImage} from '@/api/mall/product'
import {joinValueSelect} from '@/utils/mall/attr-value-select'

export interface SpuWizardBaseForm {
  id?: number
  spuName: string
  spuDescription: string
  catalogId?: number
  brandId?: number
  weight: number
  publishStatus: number
  decript: string[]
  images: string[]
  /** 仅向导页展示（对应 pms_spu_bounds，不落库） */
  bounds: { buyBounds: number; growBounds: number }
  defaultWareId?: number
}

export interface SpuWizardBaseAttrCell {
  attrId: number
  attrName?: string
  attrValues: string | string[]
  showDesc: number
}

export interface SpuWizardSaleDraft {
  attrId: number
  attrName: string
  attrValues: string[]
}

export interface SpuWizardSkuRow {
  attr: { attrId: number; attrName: string; attrValue: string }[]
  skuName: string
  skuTitle: string
  skuSubtitle: string
  price: number
  images: { imgUrl: string; defaultImg: number }[]
  descar: string[]
  descarKey: string
  fullCount: number
  discount: number
  fullPrice: number
  reducePrice: number
  initialStock: number
  stockWarning: number
}

export function collectBaseAttrs(cells: SpuWizardBaseAttrCell[][]): MallProductBaseAttr[] {
  const list: MallProductBaseAttr[] = []
  cells.forEach((group) => {
    group.forEach((cell) => {
      let val = cell.attrValues
      if (val === '' || (Array.isArray(val) && !val.length)) return
      if (Array.isArray(val)) val = joinValueSelect(val) ?? ''
      list.push({
        attrId: cell.attrId,
        attrName: cell.attrName,
        attrValues: String(val),
        showDesc: cell.showDesc
      })
    })
  })
  return list
}

export function buildSkuImagePayload(row: SpuWizardSkuRow): MallProductSkuImage[] {
  return row.images
    .filter((img) => img.imgUrl?.trim())
    .map((img) => ({
      imgUrl: img.imgUrl.trim(),
      defaultImg: img.defaultImg ?? 0
    }))
}

export function buildSpuSavePayload(
  base: SpuWizardBaseForm,
  baseAttrCells: SpuWizardBaseAttrCell[][],
  skuRows: SpuWizardSkuRow[],
  isEdit: boolean
): MallProductSavePayload {
  const payload: MallProductSavePayload = {
    catalogId: base.catalogId!,
    spuName: base.spuName,
    spuDescription: base.spuDescription || undefined,
    weight: Number(base.weight),
    publishStatus: base.publishStatus,
    decript: base.decript.length ? base.decript : undefined,
    images: base.images.length ? base.images : undefined,
    baseAttrs: collectBaseAttrs(baseAttrCells),
    skus: skuRows.map((row) => toSkuPayload(row))
  }
  if (base.brandId != null) payload.brandId = base.brandId
  else payload.brandId = null
  if (!isEdit && base.defaultWareId != null) payload.defaultWareId = base.defaultWareId
  if (isEdit && base.id != null) payload.id = base.id
  return payload
}

function toSkuPayload(row: SpuWizardSkuRow): MallProductSku {
  return {
    attr: row.attr.map((a) => ({ ...a })),
    skuName: row.skuName,
    skuTitle: row.skuTitle,
    skuSubtitle: row.skuSubtitle,
    price: Number(row.price) || 0,
    images: buildSkuImagePayload(row),
    descar: row.descar,
    fullCount: row.fullCount,
    discount: row.discount,
    fullPrice: row.fullPrice,
    reducePrice: row.reducePrice,
    initialStock: row.initialStock > 0 ? row.initialStock : undefined,
    stockWarning: row.stockWarning > 0 ? row.stockWarning : undefined
  }
}

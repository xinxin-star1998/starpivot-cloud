import type { Ref } from 'vue'
import type { MallAttr } from '@/api/mall/attr'
import type { MallProductSku } from '@/api/mall/product'
import { parseValueSelect } from '@/utils/mall/attr-value-select'
import type { SpuAttrGroupWithAttrs } from '@/utils/mall/spu-wizard-attr'
import type {
  SpuWizardBaseAttrCell,
  SpuWizardSaleDraft,
  SpuWizardSkuRow
} from '@/utils/mall/spu-wizard-payload'

export function applyBaseAttrsFromDetail(
  baseAttrValues: SpuWizardBaseAttrCell[][],
  attrGroups: SpuAttrGroupWithAttrs[],
  saved: { attrId?: number; attrValues?: string; showDesc?: number }[]
) {
  if (!saved?.length) return
  const map = new Map(saved.filter((a) => a.attrId != null).map((a) => [a.attrId!, a]))
  baseAttrValues.forEach((group, gidx) => {
    group.forEach((cell, aidx) => {
      const hit = map.get(cell.attrId)
      if (hit?.attrValues == null || String(hit.attrValues).trim() === '') return
      const attr = attrGroups[gidx]?.attrs[aidx]
      if (attr?.valueType === 1) {
        cell.attrValues = parseValueSelect(hit.attrValues)
      } else {
        cell.attrValues = hit.attrValues
      }
      cell.showDesc = hit.showDesc ?? cell.showDesc
    })
  })
}

export function restoreSaleAndSkusFromDetail(
  saleAttrs: Ref<MallAttr[]>,
  saleAttrDraft: Ref<SpuWizardSaleDraft[]>,
  saleTableColumns: Ref<SpuWizardSaleDraft[]>,
  saleInputVisible: Ref<boolean[]>,
  saleInputValue: Ref<string[]>,
  skuRows: Ref<SpuWizardSkuRow[]>,
  spuImages: string[],
  skus: MallProductSku[]
) {
  if (!skus?.length) return

  const buildSkuImages = (): SpuWizardSkuRow['images'] =>
    spuImages.map(() => ({ imgUrl: '', defaultImg: 0 }))

  const colMap = new Map<number, SpuWizardSaleDraft>()
  skus.forEach((sku) => {
    sku.attr?.forEach((a) => {
      if (a.attrId == null) return
      let col = colMap.get(a.attrId)
      if (!col) {
        col = { attrId: a.attrId, attrName: a.attrName ?? '', attrValues: [] }
        colMap.set(a.attrId, col)
      }
      if (a.attrValue && !col.attrValues.includes(a.attrValue)) {
        col.attrValues.push(a.attrValue)
      }
    })
  })
  saleTableColumns.value = Array.from(colMap.values())

  const existingSaleIds = new Set(saleAttrs.value.map((a) => a.attrId))
  colMap.forEach((col, attrId) => {
    if (existingSaleIds.has(attrId)) return
    saleAttrs.value.push({
      attrId,
      attrName: col.attrName,
      attrType: 0,
      valueType: 1,
      valueSelect: col.attrValues.join(';')
    })
  })

  saleAttrDraft.value = saleAttrs.value.map((a) => {
    const col = colMap.get(a.attrId!)
    return {
      attrId: a.attrId!,
      attrName: a.attrName ?? '',
      attrValues: col ? [...col.attrValues] : []
    }
  })
  saleInputVisible.value = saleAttrs.value.map(() => false)
  saleInputValue.value = saleAttrs.value.map(() => '')

  skuRows.value = skus.map((sku) => {
    const descar = sku.descar ?? sku.attr?.map((a) => a.attrValue) ?? []
    const images = sku.images?.length
      ? sku.images.map((img) => ({
          imgUrl: img.imgUrl ?? '',
          defaultImg: img.defaultImg ?? 0
        }))
      : buildSkuImages()
    return {
      attr: (sku.attr ?? []).map((a) => ({
        attrId: a.attrId!,
        attrName: a.attrName ?? '',
        attrValue: a.attrValue ?? ''
      })),
      skuName: sku.skuName ?? '',
      skuTitle: sku.skuTitle ?? '',
      skuSubtitle: sku.skuSubtitle ?? '',
      price: Number(sku.price ?? 0),
      images,
      descar,
      descarKey: descar.join(' ') || '__default__',
      fullCount: sku.fullCount ?? 0,
      discount: Number(sku.discount ?? 0),
      fullPrice: Number(sku.fullPrice ?? 0),
      reducePrice: Number(sku.reducePrice ?? 0),
      initialStock: 0,
      stockWarning: sku.stockWarning ?? 0
    }
  })
}

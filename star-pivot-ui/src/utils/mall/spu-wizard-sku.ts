import {cartesianProduct} from '@/utils/mall/spu-descartes'
import type {SpuWizardSaleDraft, SpuWizardSkuRow} from '@/utils/mall/spu-wizard-payload'

export function buildSkuImages(spuImages: string[]): SpuWizardSkuRow['images'] {
  return spuImages.map(() => ({ imgUrl: '', defaultImg: 0 }))
}

export function createDefaultSkuRow(spuName: string, spuImages: string[]): SpuWizardSkuRow {
  const title = spuName || '默认 SKU'
  return {
    attr: [],
    skuName: title,
    skuTitle: title,
    skuSubtitle: '',
    price: 0,
    images: buildSkuImages(spuImages),
    descar: [],
    descarKey: '__default__',
    fullCount: 0,
    discount: 0,
    fullPrice: 0,
    reducePrice: 0,
    initialStock: 0,
    stockWarning: 0
  }
}

export function createSkuRowFromCombo(
  descar: string[],
  columns: SpuWizardSaleDraft[],
  spuName: string,
  spuImages: string[]
): SpuWizardSkuRow {
  const key = descar.join(' ')
  const attr = descar.map((val, idx) => ({
    attrId: columns[idx]!.attrId,
    attrName: columns[idx]!.attrName,
    attrValue: val
  }))
  const title = `${spuName} ${descar.join(' ')}`
  return {
    attr,
    skuName: title,
    skuTitle: title,
    skuSubtitle: '',
    price: 0,
    images: buildSkuImages(spuImages),
    descar,
    descarKey: key,
    fullCount: 0,
    discount: 0,
    fullPrice: 0,
    reducePrice: 0,
    initialStock: 0,
    stockWarning: 0
  }
}

export function generateSkuRowsFromSaleDraft(
  saleAttrDraft: SpuWizardSaleDraft[],
  spuName: string,
  spuImages: string[],
  existingRows: SpuWizardSkuRow[]
): { columns: SpuWizardSaleDraft[]; rows: SpuWizardSkuRow[] } | null {
  const columns: SpuWizardSaleDraft[] = []
  const valueLists: string[][] = []
  saleAttrDraft.forEach((item) => {
    if (item.attrValues.length > 0) {
      columns.push(item)
      valueLists.push(item.attrValues)
    }
  })

  if (!valueLists.length) return null

  const combos = cartesianProduct(valueLists)
  const rows: SpuWizardSkuRow[] = []

  combos.forEach((descar) => {
    const key = descar.join(' ')
    const existing = existingRows.find((s) => s.descarKey === key)
    if (existing) {
      rows.push(existing)
      return
    }
    rows.push(createSkuRowFromCombo(descar, columns, spuName, spuImages))
  })

  return { columns, rows }
}

export function syncSkuImageSlots(rows: SpuWizardSkuRow[], imageCount: number) {
  rows.forEach((row) => {
    while (row.images.length < imageCount) {
      row.images.push({ imgUrl: '', defaultImg: 0 })
    }
    if (row.images.length > imageCount) {
      row.images.splice(imageCount)
    }
  })
}

export function validateSkuRows(
  catalogId: number | undefined,
  rows: SpuWizardSkuRow[]
): string | null {
  if (catalogId == null) return '请选择商品分类'
  if (!rows.length) return '请至少配置一个 SKU'
  for (const row of rows) {
    if (!row.skuName?.trim()) return '请填写 SKU 名称'
    if (row.price == null || Number(row.price) < 0) return '请填写有效的 SKU 价格'
  }
  return null
}

export function toggleSkuImage(
  row: SpuWizardSkuRow,
  imgIdx: number,
  img: string,
  checked: boolean
) {
  if (!row.images[imgIdx]) return
  row.images[imgIdx].imgUrl = checked ? img : ''
}

export function setDefaultSkuImage(row: SpuWizardSkuRow, imgIdx: number, img: string) {
  row.images.forEach((cell, idx) => {
    cell.defaultImg = idx === imgIdx ? 1 : 0
    if (idx === imgIdx) cell.imgUrl = img
  })
}

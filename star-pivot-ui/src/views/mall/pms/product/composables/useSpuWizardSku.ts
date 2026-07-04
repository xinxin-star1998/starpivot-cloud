import type {Ref} from 'vue'
import {ElMessage} from 'element-plus'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {
    createDefaultSkuRow,
    generateSkuRowsFromSaleDraft,
    setDefaultSkuImage as setDefaultSkuImageFn,
    syncSkuImageSlots as syncSkuImageSlotsFn,
    toggleSkuImage as toggleSkuImageFn
} from '@/utils/mall/spu-wizard-sku'
import type {SpuWizardBaseForm, SpuWizardSaleDraft, SpuWizardSkuRow} from '@/utils/mall/spu-wizard-payload'

export function useSpuWizardSku(
  baseForm: SpuWizardBaseForm,
  saleAttrDraft: Ref<SpuWizardSaleDraft[]>,
  step: Ref<number>
) {
  const saleTableColumns = ref<SpuWizardSaleDraft[]>([])
  const skuRows = ref<SpuWizardSkuRow[]>([])
  const imageDisplayUrls = ref<Map<string, string>>(new Map())

  const getImageDisplayUrl = (objectName: string): string =>
    imageDisplayUrls.value.get(objectName) || ''

  const syncSkuImageSlots = () => syncSkuImageSlotsFn(skuRows.value, baseForm.images.length)

  watch(
    () => baseForm.images,
    async (images) => {
      syncSkuImageSlots()
      imageDisplayUrls.value = await resolveGoodsImageDisplayUrls(images)
    },
    { deep: true }
  )

  const generateDefaultSku = () => {
    saleTableColumns.value = []
    skuRows.value = [createDefaultSkuRow(baseForm.spuName, baseForm.images)]
    step.value = 3
  }

  const generateSkus = () => {
    const result = generateSkuRowsFromSaleDraft(
      saleAttrDraft.value,
      baseForm.spuName,
      baseForm.images,
      skuRows.value
    )
    if (!result) {
      ElMessage.warning('请至少选择一个销售属性值')
      return
    }
    saleTableColumns.value = result.columns
    skuRows.value = result.rows
    step.value = 3
  }

  const toggleSkuImage = (row: SpuWizardSkuRow, imgIdx: number, img: string, checked: boolean) => {
    toggleSkuImageFn(row, imgIdx, img, checked)
  }

  const setDefaultSkuImage = (row: SpuWizardSkuRow, imgIdx: number, img: string) => {
    setDefaultSkuImageFn(row, imgIdx, img)
  }

  const resetSku = () => {
    saleTableColumns.value = []
    skuRows.value = []
  }

  return {
    saleTableColumns,
    skuRows,
    getImageDisplayUrl,
    generateDefaultSku,
    generateSkus,
    toggleSkuImage,
    setDefaultSkuImage,
    syncSkuImageSlots,
    resetSku
  }
}

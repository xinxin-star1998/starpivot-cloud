import type {Ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {applyBaseAttrsFromDetail, restoreSaleAndSkusFromDetail} from '@/utils/mall/spu-wizard-detail'
import {
    buildSpuSavePayload,
    type SpuWizardBaseAttrCell,
    type SpuWizardBaseForm,
    type SpuWizardSaleDraft,
    type SpuWizardSkuRow
} from '@/utils/mall/spu-wizard-payload'
import {validateSkuRows} from '@/utils/mall/spu-wizard-sku'
import type {MallAttr} from '@/api/mall/attr'
import type {SpuAttrGroupWithAttrs} from '@/utils/mall/spu-wizard-attr'
import {RouteLocationNormalizedLoaded, Router} from 'vue-router'
import {fetchMallProductAdd, fetchMallProductById, fetchMallProductUpdate} from '@/api/mall/product'

interface SubmitDeps {
  route: RouteLocationNormalizedLoaded
  router: Router
  spuId: Ref<number | undefined>
  isEdit: Ref<boolean>
  step: Ref<number>
  submitting: Ref<boolean>
  baseForm: SpuWizardBaseForm
  baseAttrValues: Ref<SpuWizardBaseAttrCell[][]>
  attrGroups: Ref<SpuAttrGroupWithAttrs[]>
  saleAttrs: Ref<MallAttr[]>
  saleAttrDraft: Ref<SpuWizardSaleDraft[]>
  saleTableColumns: Ref<SpuWizardSaleDraft[]>
  saleInputVisible: Ref<boolean[]>
  saleInputValue: Ref<string[]>
  skuRows: Ref<SpuWizardSkuRow[]>
  catalogPath: Ref<number[]>
  syncCatalogPath: (id?: number) => void
  loadBaseAttrStep: (opts?: { catalogId?: number; force?: boolean }) => Promise<void>
  loadSaleAttrStep: (opts?: { catalogId?: number; force?: boolean }) => Promise<void>
  syncSkuImageSlots: () => void
  resetAttrs: () => void
  resetSku: () => void
}

export function useSpuWizardSubmit(deps: SubmitDeps) {
  const {
    route,
    router,
    spuId,
    isEdit,
    step,
    submitting,
    baseForm,
    baseAttrValues,
    attrGroups,
    saleAttrs,
    saleAttrDraft,
    saleTableColumns,
    saleInputVisible,
    saleInputValue,
    skuRows,
    catalogPath,
    syncCatalogPath,
    loadBaseAttrStep,
    loadSaleAttrStep,
    syncSkuImageSlots,
    resetAttrs,
    resetSku
  } = deps

  const submitSpu = async () => {
    const errorMsg = validateSkuRows(baseForm.catalogId, skuRows.value)
    if (errorMsg) {
      ElMessage.warning(errorMsg)
      return
    }
    const payload = buildSpuSavePayload(baseForm, baseAttrValues.value, skuRows.value, isEdit.value)
    try {
      await ElMessageBox.confirm('将提交商品及 SKU 信息，是否继续？', '提示', { type: 'warning' })
    } catch {
      return
    }
    submitting.value = true
    try {
      if (isEdit.value) {
        await fetchMallProductUpdate(payload)
      } else {
        await fetchMallProductAdd(payload)
      }
      step.value = 4
    } finally {
      submitting.value = false
    }
  }

  const goBack = () => {
    if (window.history.length > 1) {
      router.back()
      return
    }
    router.push({ path: '/mall/product/index' })
  }

  const applyCatalogFromQuery = () => {
    const q = route.query.catalogId
    if (q == null) return
    const cid = Number(q)
    if (Number.isFinite(cid)) {
      baseForm.catalogId = cid
      syncCatalogPath(cid)
    }
  }

  const resetWizard = () => {
    step.value = 0
    Object.assign(baseForm, {
      id: undefined,
      spuName: '',
      spuDescription: '',
      catalogId: undefined,
      brandId: undefined,
      weight: 0,
      publishStatus: 0,
      decript: [],
      images: [],
      bounds: { buyBounds: 0, growBounds: 0 },
      defaultWareId: undefined
    })
    catalogPath.value = []
    resetAttrs()
    resetSku()
    applyCatalogFromQuery()
  }

  const loadProductForEdit = async () => {
    if (!spuId.value) return
    const detail = await fetchMallProductById(spuId.value)
    const cid = detail.catalogId != null ? Number(detail.catalogId) : undefined
    Object.assign(baseForm, {
      id: detail.id,
      spuName: detail.spuName ?? '',
      spuDescription: detail.spuDescription ?? '',
      catalogId: cid,
      brandId: detail.brandId != null ? Number(detail.brandId) : undefined,
      weight: Number(detail.weight ?? 0),
      publishStatus: detail.publishStatus ?? 0,
      decript: detail.decript ? [...detail.decript] : [],
      images: detail.images ? [...detail.images] : [],
      bounds: { buyBounds: 0, growBounds: 0 }
    })
    syncCatalogPath(cid)
    if (cid == null) return

    await loadBaseAttrStep({ catalogId: cid, force: true })
    applyBaseAttrsFromDetail(baseAttrValues.value, attrGroups.value, detail.baseAttrs ?? [])
    await loadSaleAttrStep({ catalogId: cid, force: true })
    if (detail.skus?.length) {
      restoreSaleAndSkusFromDetail(
        saleAttrs,
        saleAttrDraft,
        saleTableColumns,
        saleInputVisible,
        saleInputValue,
        skuRows,
        baseForm.images,
        detail.skus
      )
    }
    syncSkuImageSlots()
  }

  return { submitSpu, goBack, resetWizard, loadProductForEdit, applyCatalogFromQuery }
}

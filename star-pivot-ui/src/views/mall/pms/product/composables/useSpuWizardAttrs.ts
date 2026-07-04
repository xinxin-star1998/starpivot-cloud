import type {Ref} from 'vue'
import {ElMessage} from 'element-plus'
import type {MallAttr} from '@/api/mall/attr'
import {parseValueSelect} from '@/utils/mall/attr-value-select'
import {
    fetchCatalogBaseAttrGroups,
    fetchCatalogSaleAttrs,
    type SpuAttrGroupWithAttrs
} from '@/utils/mall/spu-wizard-attr'
import type {SpuWizardBaseAttrCell, SpuWizardSaleDraft} from '@/utils/mall/spu-wizard-payload'

export function useSpuWizardAttrs(catalogId: Ref<number | undefined>) {
  const loadingAttrs = ref(false)
  const loadingSale = ref(false)
  const attrGroups = ref<SpuAttrGroupWithAttrs[]>([])
  const baseAttrValues = ref<SpuWizardBaseAttrCell[][]>([])
  const saleAttrs = ref<MallAttr[]>([])
  const saleAttrDraft = ref<SpuWizardSaleDraft[]>([])
  const saleInputVisible = ref<boolean[]>([])
  const saleInputValue = ref<string[]>([])
  const attrsLoadedForCatalog = ref<number | undefined>()
  const saleAttrsLoadedForCatalog = ref<number | undefined>()

  watch(catalogId, () => {
    attrsLoadedForCatalog.value = undefined
    saleAttrsLoadedForCatalog.value = undefined
  })

  const initBaseAttrMatrix = (groups: SpuAttrGroupWithAttrs[]) => {
    baseAttrValues.value = groups.map((g) =>
      g.attrs.map((a) => ({
        attrId: a.attrId!,
        attrName: a.attrName,
        attrValues: a.valueType === 1 ? [] : '',
        showDesc: a.showDesc ?? 0
      }))
    )
  }

  const loadBaseAttrStep = async (opts?: { catalogId?: number; force?: boolean }) => {
    const cid = opts?.catalogId ?? catalogId.value
    if (cid == null) return
    if (!opts?.force && attrsLoadedForCatalog.value === cid) return
    loadingAttrs.value = true
    try {
      attrGroups.value = await fetchCatalogBaseAttrGroups(cid)
      initBaseAttrMatrix(attrGroups.value)
      attrsLoadedForCatalog.value = cid
    } catch {
      attrGroups.value = []
      ElMessage.error('加载规格参数失败，请检查分类或权限后重试')
    } finally {
      loadingAttrs.value = false
    }
  }

  const loadSaleAttrStep = async (opts?: { catalogId?: number; force?: boolean }) => {
    const cid = opts?.catalogId ?? catalogId.value
    if (cid == null) return
    if (!opts?.force && saleAttrsLoadedForCatalog.value === cid) return
    loadingSale.value = true
    try {
      saleAttrs.value = await fetchCatalogSaleAttrs(cid)
      saleAttrDraft.value = saleAttrs.value.map((a) => ({
        attrId: a.attrId!,
        attrName: a.attrName ?? '',
        attrValues: []
      }))
      saleInputVisible.value = saleAttrs.value.map(() => false)
      saleInputValue.value = saleAttrs.value.map(() => '')
      saleAttrsLoadedForCatalog.value = cid
    } catch {
      saleAttrs.value = []
      ElMessage.error('加载销售属性失败，请检查分类或权限后重试')
    } finally {
      loadingSale.value = false
    }
  }

  const showSaleInput = (aidx: number) => {
    saleInputVisible.value[aidx] = true
  }

  const confirmSaleInput = (aidx: number) => {
    const val = saleInputValue.value[aidx]?.trim()
    if (val) {
      const draft = saleAttrDraft.value[aidx]
      if (draft && !draft.attrValues.includes(val)) {
        draft.attrValues.push(val)
      }
      const attr = saleAttrs.value[aidx]
      if (attr) {
        const opts = parseValueSelect(attr.valueSelect)
        if (!opts.includes(val)) {
          attr.valueSelect = attr.valueSelect ? `${attr.valueSelect};${val}` : val
        }
      }
    }
    saleInputVisible.value[aidx] = false
    saleInputValue.value[aidx] = ''
  }

  const resetAttrs = () => {
    attrGroups.value = []
    baseAttrValues.value = []
    saleAttrs.value = []
    saleAttrDraft.value = []
    saleInputVisible.value = []
    saleInputValue.value = []
    attrsLoadedForCatalog.value = undefined
    saleAttrsLoadedForCatalog.value = undefined
  }

  return {
    loadingAttrs,
    loadingSale,
    attrGroups,
    baseAttrValues,
    saleAttrs,
    saleAttrDraft,
    saleInputVisible,
    saleInputValue,
    attrsLoadedForCatalog,
    saleAttrsLoadedForCatalog,
    loadBaseAttrStep,
    loadSaleAttrStep,
    showSaleInput,
    confirmSaleInput,
    resetAttrs
  }
}

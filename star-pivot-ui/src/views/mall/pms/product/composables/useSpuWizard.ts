import type {InjectionKey} from 'vue'
import type {FormInstance, FormRules} from 'element-plus'
import {useRoute, useRouter} from 'vue-router'
import {parseValueSelect} from '@/utils/mall/attr-value-select'
import {useSpuWizardOptions} from './useSpuWizardOptions'
import {useSpuWizardAttrs} from './useSpuWizardAttrs'
import {useSpuWizardSku} from './useSpuWizardSku'
import {useSpuWizardSubmit} from './useSpuWizardSubmit'

export const SPU_WIZARD_KEY: InjectionKey<ReturnType<typeof useSpuWizard>> = Symbol('spuWizard')

export function useSpuWizardInject() {
  const ctx = inject(SPU_WIZARD_KEY)
  if (!ctx) throw new Error('useSpuWizardInject must be used within addSpu page')
  return ctx
}

export function useSpuWizard() {
  const route = useRoute()
  const router = useRouter()

  const spuId = computed(() => {
    const raw = route.params.id
    if (raw == null || raw === '') return undefined
    const n = Number(raw)
    return Number.isFinite(n) ? n : undefined
  })
  const isEdit = computed(() => spuId.value != null)
  const pageTitle = computed(() => (isEdit.value ? '编辑商品' : '发布商品'))

  const step = ref(0)
  const submitting = ref(false)
  const baseFormRef = ref<FormInstance>()

  const baseForm = reactive({
    id: undefined as number | undefined,
    spuName: '',
    spuDescription: '',
    catalogId: undefined as number | undefined,
    brandId: undefined as number | undefined,
    weight: 0,
    publishStatus: 0,
    decript: [] as string[],
    images: [] as string[],
    bounds: { buyBounds: 0, growBounds: 0 },
    defaultWareId: undefined as number | undefined
  })

  const catalogId = computed(() => baseForm.catalogId)

  const options = useSpuWizardOptions()
  const attrs = useSpuWizardAttrs(catalogId)
  const sku = useSpuWizardSku(baseForm, attrs.saleAttrDraft, step)

  const baseRules: FormRules = {
    spuName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    catalogPath: [
      {
        validator: (_r, _v, cb) => {
          if (!options.catalogPath.value?.length) cb(new Error('请选择三级分类'))
          else cb()
        },
        trigger: 'change'
      }
    ],
    weight: [{ required: true, message: '请填写重量', trigger: 'blur' }],
    publishStatus: [{ required: true, message: '请选择上架状态', trigger: 'change' }]
  }

  watch(options.catalogPath, (path, oldPath) => {
    if (path?.length) {
      baseForm.catalogId = path[path.length - 1]
    } else if (oldPath?.length) {
      baseForm.catalogId = undefined
    }
  })

  const lifecycle = useSpuWizardSubmit({
    route,
    router,
    spuId,
    isEdit,
    step,
    submitting,
    baseForm,
    baseAttrValues: attrs.baseAttrValues,
    attrGroups: attrs.attrGroups,
    saleAttrs: attrs.saleAttrs,
    saleAttrDraft: attrs.saleAttrDraft,
    saleTableColumns: sku.saleTableColumns,
    saleInputVisible: attrs.saleInputVisible,
    saleInputValue: attrs.saleInputValue,
    skuRows: sku.skuRows,
    catalogPath: options.catalogPath,
    syncCatalogPath: options.syncCatalogPath,
    loadBaseAttrStep: attrs.loadBaseAttrStep,
    loadSaleAttrStep: attrs.loadSaleAttrStep,
    syncSkuImageSlots: sku.syncSkuImageSlots,
    resetAttrs: attrs.resetAttrs,
    resetSku: sku.resetSku
  })

  const onBaseNext = async () => {
    if (!baseFormRef.value) return
    try {
      await baseFormRef.value.validate()
    } catch {
      return
    }
    if (attrs.attrsLoadedForCatalog.value !== catalogId.value) {
      await attrs.loadBaseAttrStep()
    }
    step.value = 1
  }

  const onBaseAttrsNext = async () => {
    if (attrs.saleAttrsLoadedForCatalog.value !== catalogId.value) {
      await attrs.loadSaleAttrStep()
    }
    step.value = 2
  }

  onMounted(async () => {
    await options.loadAllOptions()
    if (isEdit.value) {
      await lifecycle.loadProductForEdit()
    } else {
      lifecycle.applyCatalogFromQuery()
    }
  })

  return reactive({
    spuId,
    isEdit,
    pageTitle,
    step,
    submitting,
    baseFormRef,
    categoryOptions: options.categoryOptions,
    catalogPath: options.catalogPath,
    brandOptions: options.brandOptions,
    wareOptions: options.wareOptions,
    cascaderProps: options.cascaderProps,
    baseForm,
    baseRules,
    loadingAttrs: attrs.loadingAttrs,
    loadingSale: attrs.loadingSale,
    attrGroups: attrs.attrGroups,
    baseAttrValues: attrs.baseAttrValues,
    saleAttrs: attrs.saleAttrs,
    saleAttrDraft: attrs.saleAttrDraft,
    saleTableColumns: sku.saleTableColumns,
    saleInputVisible: attrs.saleInputVisible,
    saleInputValue: attrs.saleInputValue,
    skuRows: sku.skuRows,
    getImageDisplayUrl: sku.getImageDisplayUrl,
    parseValueSelect,
    onBaseNext,
    onBaseAttrsNext,
    showSaleInput: attrs.showSaleInput,
    confirmSaleInput: attrs.confirmSaleInput,
    generateDefaultSku: sku.generateDefaultSku,
    generateSkus: sku.generateSkus,
    toggleSkuImage: sku.toggleSkuImage,
    setDefaultSkuImage: sku.setDefaultSkuImage,
    submitSpu: lifecycle.submitSpu,
    goBack: lifecycle.goBack,
    resetWizard: lifecycle.resetWizard
  })
}

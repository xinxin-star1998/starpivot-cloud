<!-- SPU 发布向导：新增 / 编辑（独立页） -->
<template>
  <div class="mall-add-spu-page art-full-height">
    <ElCard shadow="never" class="wizard-toolbar">
      <div class="wizard-toolbar__row">
        <ElButton @click="goBack">返回列表</ElButton>
        <span class="wizard-toolbar__title">{{ pageTitle }}</span>
      </div>
      <ElSteps :active="step" finish-status="success" align-center class="wizard-steps">
        <ElStep title="基本信息" />
        <ElStep title="规格参数" />
        <ElStep title="销售属性" />
        <ElStep title="SKU 信息" />
        <ElStep title="完成" />
      </ElSteps>
    </ElCard>

    <!-- 步骤 0：基本信息 -->
    <ElCard v-show="step === 0" shadow="never" class="wizard-panel">
      <ElForm
        ref="baseFormRef"
        :model="baseForm"
        :rules="baseRules"
        label-width="120px"
        style="max-width: 720px; margin: 0 auto"
      >
        <ElFormItem label="商品名称" prop="spuName">
          <ElInput v-model="baseForm.spuName" maxlength="200" show-word-limit />
        </ElFormItem>
        <ElFormItem label="商品描述" prop="spuDescription">
          <ElInput v-model="baseForm.spuDescription" type="textarea" :rows="2" />
        </ElFormItem>
        <ElFormItem label="商品分类" prop="catalogPath">
          <ElCascader
            v-model="catalogPath"
            :options="categoryOptions"
            :props="cascaderProps"
            clearable
            filterable
            placeholder="请选择三级类目"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem label="品牌" prop="brandId">
          <ElSelect
            v-model="baseForm.brandId"
            clearable
            filterable
            placeholder="选填"
            style="width: 100%"
          >
            <ElOption
              v-for="b in brandOptions"
              :key="b.brandId"
              :label="b.name"
              :value="b.brandId!"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="重量 (Kg)" prop="weight">
          <ElInputNumber
            v-model="baseForm.weight"
            :min="0"
            :precision="3"
            :step="0.1"
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem label="积分设置（仅展示，不保存）">
          <ElSpace wrap>
            <span>金币</span>
            <ElInputNumber
              v-model="baseForm.bounds.buyBounds"
              :min="0"
              controls-position="right"
              style="width: 140px"
            />
            <span>成长值</span>
            <ElInputNumber
              v-model="baseForm.bounds.growBounds"
              :min="0"
              controls-position="right"
              style="width: 140px"
            />
          </ElSpace>
        </ElFormItem>
        <ElFormItem label="上架状态" prop="publishStatus">
          <ElRadioGroup v-model="baseForm.publishStatus">
            <ElRadio :value="1">上架</ElRadio>
            <ElRadio :value="0">下架</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="商品介绍图" prop="decript">
          <SpuImageUpload
            v-model="baseForm.decript"
            :goods-id="spuId"
            :limit="20"
            hint="详情页展示图（可选）"
          />
        </ElFormItem>
        <ElFormItem label="商品图集" prop="images">
          <SpuImageUpload
            v-model="baseForm.images"
            :goods-id="spuId"
            :limit="10"
            hint="SPU 主图，SKU 可从中勾选"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loadingAttrs" @click="onBaseNext">
            下一步：规格参数
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 步骤 1：基本属性 -->
    <ElCard v-show="step === 1" v-loading="loadingAttrs" shadow="never" class="wizard-panel">
      <template v-if="attrGroups.length === 0 && !loadingAttrs">
        <ElEmpty description="该分类下暂无基本属性，可直接下一步" />
      </template>
      <ElTabs v-else tab-position="left" style="min-height: 280px">
        <ElTabPane
          v-for="(group, gidx) in attrGroups"
          :key="group.attrGroupId"
          :label="group.attrGroupName"
        >
          <ElForm label-width="140px">
            <ElFormItem
              v-for="(attr, aidx) in group.attrs"
              :key="attr.attrId"
              :label="attr.attrName"
            >
              <ElSelect
                v-model="baseAttrValues[gidx]![aidx]!.attrValues"
                :multiple="attr.valueType === 1"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="请选择或输入"
                style="min-width: 240px"
              >
                <ElOption
                  v-for="(val, vidx) in parseValueSelect(attr.valueSelect)"
                  :key="vidx"
                  :label="val"
                  :value="val"
                />
              </ElSelect>
              <ElCheckbox
                v-model="baseAttrValues[gidx]![aidx]!.showDesc"
                :true-value="1"
                :false-value="0"
                style="margin-left: 12px"
              >
                快速展示
              </ElCheckbox>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
      </ElTabs>
      <div class="wizard-actions">
        <ElButton @click="step = 0">上一步</ElButton>
        <ElButton type="primary" :loading="loadingSale" @click="onBaseAttrsNext">
          下一步：销售属性
        </ElButton>
      </div>
    </ElCard>

    <!-- 步骤 2：销售属性 -->
    <ElCard v-show="step === 2" v-loading="loadingSale" shadow="never" class="wizard-panel">
      <template v-if="saleAttrs.length === 0 && !loadingSale">
        <ElEmpty description="该分类下暂无销售属性">
          <ElButton type="primary" @click="generateDefaultSku">下一步：SKU 信息</ElButton>
        </ElEmpty>
      </template>
      <ElForm v-else label-width="120px" style="max-width: 800px; margin: 0 auto">
        <ElFormItem v-for="(attr, aidx) in saleAttrs" :key="attr.attrId" :label="attr.attrName">
          <ElCheckboxGroup v-model="saleAttrDraft[aidx]!.attrValues">
            <ElCheckbox
              v-for="val in parseValueSelect(attr.valueSelect)"
              :key="val"
              :label="val"
              :value="val"
            />
          </ElCheckboxGroup>
          <ElButton
            v-if="!saleInputVisible[aidx]"
            size="small"
            style="margin-left: 8px"
            @click="showSaleInput(aidx)"
          >
            + 自定义
          </ElButton>
          <ElInput
            v-else
            v-model="saleInputValue[aidx]"
            size="small"
            style="width: 140px; margin-left: 8px"
            placeholder="回车确认"
            @keyup.enter="confirmSaleInput(aidx)"
            @blur="confirmSaleInput(aidx)"
          />
        </ElFormItem>
      </ElForm>
      <div class="wizard-actions">
        <ElButton @click="step = 1">上一步</ElButton>
        <ElButton type="primary" @click="generateSkus">下一步：SKU 信息</ElButton>
      </div>
    </ElCard>

    <!-- 步骤 3：SKU -->
    <ElCard v-show="step === 3" shadow="never" class="wizard-panel wizard-panel--sku">
      <div
        ref="skuTableScrollRef"
        class="sku-table-scroll"
        @mousedown="onSkuTableMouseDown"
        @mousemove="onSkuTableMouseMove"
        @mouseup="stopSkuTableDrag"
        @mouseleave="stopSkuTableDrag"
      >
        <ElTable
          :data="skuRows"
          border
          stripe
          class="sku-table"
          :header-cell-class-name="() => 'sku-table__header-cell'"
          :cell-class-name="() => 'sku-table__body-cell'"
        >
          <ElTableColumn label="属性组合" fixed="left" align="center">
            <ElTableColumn
              v-for="(col, colIdx) in saleTableColumns"
              :key="col.attrId"
              :label="col.attrName"
              min-width="112"
              align="center"
            >
              <template #default="{ row }">
                <span class="sku-attr-val">{{ row.attr[colIdx]?.attrValue }}</span>
              </template>
            </ElTableColumn>
          </ElTableColumn>
          <ElTableColumn label="SKU 名称" min-width="260" fixed="left">
            <template #default="{ row }">
              <ElInput
                v-model="row.skuName"
                class="sku-cell-input"
                placeholder="SKU 名称"
                clearable
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="价格" width="140" align="center">
            <template #default="{ row }">
              <ElInputNumber
                v-model="row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                class="sku-cell-num"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="标题" min-width="240">
            <template #default="{ row }">
              <ElInput
                v-model="row.skuTitle"
                class="sku-cell-input"
                placeholder="展示标题"
                clearable
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="副标题" min-width="240">
            <template #default="{ row }">
              <ElInput
                v-model="row.skuSubtitle"
                class="sku-cell-input"
                placeholder="副标题"
                clearable
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="满件折扣" width="232" class-name="sku-table__promo-cell">
            <template #default="{ row }">
              <div class="sku-promo-block">
                <div class="sku-promo-row">
                  <span class="sku-promo-label">满</span>
                  <ElInputNumber
                    v-model="row.fullCount"
                    :min="0"
                    controls-position="right"
                    class="sku-cell-num sku-cell-num--sm"
                  />
                  <span class="sku-promo-label">件</span>
                </div>
                <div class="sku-promo-row">
                  <span class="sku-promo-label">打</span>
                  <ElInputNumber
                    v-model="row.discount"
                    :min="0"
                    :max="1"
                    :step="0.01"
                    :precision="2"
                    controls-position="right"
                    class="sku-cell-num sku-cell-num--sm"
                  />
                  <span class="sku-promo-label">折</span>
                </div>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn label="满减" width="232" class-name="sku-table__promo-cell">
            <template #default="{ row }">
              <div class="sku-promo-block">
                <div class="sku-promo-row">
                  <span class="sku-promo-label">满</span>
                  <ElInputNumber
                    v-model="row.fullPrice"
                    :min="0"
                    :step="10"
                    controls-position="right"
                    class="sku-cell-num sku-cell-num--sm"
                  />
                  <span class="sku-promo-label">元</span>
                </div>
                <div class="sku-promo-row">
                  <span class="sku-promo-label">减</span>
                  <ElInputNumber
                    v-model="row.reducePrice"
                    :min="0"
                    :step="10"
                    controls-position="right"
                    class="sku-cell-num sku-cell-num--sm"
                  />
                  <span class="sku-promo-label">元</span>
                </div>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn v-if="baseForm.images.length" label="SKU 图" min-width="320">
            <template #default="{ row }">
              <div class="sku-image-pick">
                <div
                  v-for="(img, imgIdx) in baseForm.images"
                  :key="imgIdx"
                  class="sku-image-pick__item"
                >
                  <img :src="getImageDisplayUrl(img)" alt="" />
                  <div class="sku-image-pick__actions">
                    <ElCheckbox
                      :model-value="row.images[imgIdx]?.imgUrl === img"
                      @change="(val) => toggleSkuImage(row, imgIdx, img, val === true)"
                    >
                      选用
                    </ElCheckbox>
                    <ElRadio
                      :model-value="row.images[imgIdx]?.defaultImg === 1"
                      :name="`default-${row.descarKey}`"
                      @change="setDefaultSkuImage(row, imgIdx, img)"
                    >
                      默认
                    </ElRadio>
                  </div>
                </div>
              </div>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
      <div class="wizard-actions">
        <ElButton @click="step = 2">上一步</ElButton>
        <ElButton type="primary" :loading="submitting" @click="submitSpu">保存商品</ElButton>
      </div>
    </ElCard>
    <!-- 步骤 4：完成 -->
    <ElCard v-show="step === 4" shadow="never" class="wizard-panel wizard-done">
      <ElResult icon="success" title="保存成功" sub-title="商品及 SKU 信息已提交">
        <template #extra>
          <ElSpace>
            <ElButton type="primary" @click="goBack">返回列表</ElButton>
            <ElButton v-if="!isEdit" @click="resetWizard">继续发布</ElButton>
          </ElSpace>
        </template>
      </ElResult>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import {
    fetchMallProductAdd,
    fetchMallProductById,
    fetchMallProductUpdate,
    type MallProductSku
  } from '@/api/mall/product'
  import { fetchMallBrandList, type MallBrandVo } from '@/api/mall/brand'
  import { fetchMallCategoryTree, type MallCategoryTreeNode } from '@/api/mall/category'
  import type { MallAttr } from '@/api/mall/attr'
  import {
    filterVisibleCategoryTree,
    findCategoryPath,
    mapCategoryCascaderOptions
  } from '@/utils/mall/category-tree'
  import { parseValueSelect } from '@/utils/mall/attr-value-select'
  import { cartesianProduct } from '@/utils/mall/spu-descartes'
  import {
    fetchCatalogBaseAttrGroups,
    fetchCatalogSaleAttrs,
    pageRows,
    type SpuAttrGroupWithAttrs
  } from '@/utils/mall/spu-wizard-attr'
  import {
    buildSpuSavePayload,
    type SpuWizardBaseAttrCell,
    type SpuWizardSaleDraft,
    type SpuWizardSkuRow
  } from '@/utils/mall/spu-wizard-payload'
  import SpuImageUpload from './spu-image-upload.vue'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'MallProductAddSpu' })

  const imageDisplayUrls = ref<Map<string, string>>(new Map())

  const getImageDisplayUrl = (objectName: string): string =>
    imageDisplayUrls.value.get(objectName) || ''

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
  const loadingAttrs = ref(false)
  const loadingSale = ref(false)

  const baseFormRef = ref<FormInstance>()
  const categoryOptions = ref<MallCategoryTreeNode[]>([])
  /** 完整类目树，用于编辑回显路径（含 showStatus=0 的节点） */
  const categoryTreeFull = ref<MallCategoryTreeNode[]>([])
  const catalogPath = ref<number[]>([])
  const brandOptions = ref<MallBrandVo[]>([])

  const cascaderProps = {
    value: 'catId',
    label: 'name',
    children: 'children',
    leaf: 'leaf',
    emitPath: true,
    expandTrigger: 'click' as const
  }

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
    bounds: { buyBounds: 0, growBounds: 0 }
  })

  const baseRules: FormRules = {
    spuName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    catalogPath: [
      {
        validator: (_r, _v, cb) => {
          if (!catalogPath.value?.length) cb(new Error('请选择三级分类'))
          else cb()
        },
        trigger: 'change'
      }
    ],
    weight: [{ required: true, message: '请填写重量', trigger: 'blur' }],
    publishStatus: [{ required: true, message: '请选择上架状态', trigger: 'change' }]
  }

  watch(catalogPath, (path, oldPath) => {
    if (path?.length) {
      baseForm.catalogId = path[path.length - 1]
    } else if (oldPath?.length) {
      // 仅用户清空级联时清除 catalogId，避免回显失败误清空导致规格/销售属性不加载
      baseForm.catalogId = undefined
    }
  })

  const attrGroups = ref<SpuAttrGroupWithAttrs[]>([])
  const baseAttrValues = ref<SpuWizardBaseAttrCell[][]>([])
  const saleAttrs = ref<MallAttr[]>([])
  const saleAttrDraft = ref<SpuWizardSaleDraft[]>([])
  const saleTableColumns = ref<SpuWizardSaleDraft[]>([])
  const saleInputVisible = ref<boolean[]>([])
  const saleInputValue = ref<string[]>([])
  const skuRows = ref<SpuWizardSkuRow[]>([])
  const skuTableScrollRef = ref<HTMLElement | null>(null)
  let skuDragActive = false
  let skuDragStartX = 0
  let skuDragScrollLeft = 0

  function onSkuTableMouseDown(e: MouseEvent) {
    const el = skuTableScrollRef.value
    if (!el || e.button !== 0) return
    const target = e.target as HTMLElement
    if (
      target.closest(
        'input, textarea, .el-input, .el-input-number, .el-checkbox, .el-radio, button, a'
      )
    ) {
      return
    }
    skuDragActive = true
    skuDragStartX = e.clientX
    skuDragScrollLeft = el.scrollLeft
    el.classList.add('is-dragging')
  }

  function onSkuTableMouseMove(e: MouseEvent) {
    if (!skuDragActive || !skuTableScrollRef.value) return
    e.preventDefault()
    skuTableScrollRef.value.scrollLeft = skuDragScrollLeft - (e.clientX - skuDragStartX)
  }

  function stopSkuTableDrag() {
    if (!skuDragActive) return
    skuDragActive = false
    skuTableScrollRef.value?.classList.remove('is-dragging')
  }

  const attrsLoadedForCatalog = ref<number | undefined>()
  const saleAttrsLoadedForCatalog = ref<number | undefined>()

  const catalogId = computed(() => baseForm.catalogId)

  watch(catalogId, () => {
    attrsLoadedForCatalog.value = undefined
    saleAttrsLoadedForCatalog.value = undefined
  })

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

  const onBaseNext = async () => {
    if (!baseFormRef.value) return
    try {
      await baseFormRef.value.validate()
    } catch {
      return
    }
    if (attrsLoadedForCatalog.value !== catalogId.value) {
      await loadBaseAttrStep()
    }
    step.value = 1
  }

  const onBaseAttrsNext = async () => {
    if (saleAttrsLoadedForCatalog.value !== catalogId.value) {
      await loadSaleAttrStep()
    }
    step.value = 2
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

  const buildSkuImages = (): SpuWizardSkuRow['images'] =>
    baseForm.images.map(() => ({ imgUrl: '', defaultImg: 0 }))

  const generateDefaultSku = () => {
    saleTableColumns.value = []
    const title = baseForm.spuName || '默认 SKU'
    skuRows.value = [
      {
        attr: [],
        skuName: title,
        skuTitle: title,
        skuSubtitle: '',
        price: 0,
        images: buildSkuImages(),
        descar: [],
        descarKey: '__default__',
        fullCount: 0,
        discount: 0,
        fullPrice: 0,
        reducePrice: 0
      }
    ]
    step.value = 3
  }

  const generateSkus = () => {
    const columns: SpuWizardSaleDraft[] = []
    const valueLists: string[][] = []
    saleAttrDraft.value.forEach((item) => {
      if (item.attrValues.length > 0) {
        columns.push(item)
        valueLists.push(item.attrValues)
      }
    })

    if (!valueLists.length) {
      ElMessage.warning('请至少选择一个销售属性值')
      return
    }

    saleTableColumns.value = columns
    const combos = cartesianProduct(valueLists)
    const prev = skuRows.value
    skuRows.value = []

    combos.forEach((descar) => {
      const key = descar.join(' ')
      const existing = prev.find((s) => s.descarKey === key)
      if (existing) {
        skuRows.value.push(existing)
        return
      }
      const attr = descar.map((val, idx) => ({
        attrId: columns[idx]!.attrId,
        attrName: columns[idx]!.attrName,
        attrValue: val
      }))
      const title = `${baseForm.spuName} ${descar.join(' ')}`
      skuRows.value.push({
        attr,
        skuName: title,
        skuTitle: title,
        skuSubtitle: '',
        price: 0,
        images: buildSkuImages(),
        descar,
        descarKey: key,
        fullCount: 0,
        discount: 0,
        fullPrice: 0,
        reducePrice: 0
      })
    })
    step.value = 3
  }

  const syncSkuImageSlots = () => {
    const len = baseForm.images.length
    skuRows.value.forEach((row) => {
      while (row.images.length < len) {
        row.images.push({ imgUrl: '', defaultImg: 0 })
      }
      if (row.images.length > len) {
        row.images.splice(len)
      }
    })
  }

  watch(
    () => baseForm.images,
    async (images) => {
      syncSkuImageSlots()
      imageDisplayUrls.value = await resolveGoodsImageDisplayUrls(images)
    },
    { deep: true }
  )

  const toggleSkuImage = (row: SpuWizardSkuRow, imgIdx: number, img: string, checked: boolean) => {
    if (!row.images[imgIdx]) return
    row.images[imgIdx].imgUrl = checked ? img : ''
  }

  const setDefaultSkuImage = (row: SpuWizardSkuRow, imgIdx: number, img: string) => {
    row.images.forEach((cell, idx) => {
      cell.defaultImg = idx === imgIdx ? 1 : 0
      if (idx === imgIdx) cell.imgUrl = img
    })
  }

  const validateBeforeSubmit = (): boolean => {
    if (baseForm.catalogId == null) {
      ElMessage.warning('请选择商品分类')
      return false
    }
    if (!skuRows.value.length) {
      ElMessage.warning('请至少配置一个 SKU')
      return false
    }
    for (const row of skuRows.value) {
      if (!row.skuName?.trim()) {
        ElMessage.warning('请填写 SKU 名称')
        return false
      }
      if (row.price == null || Number(row.price) < 0) {
        ElMessage.warning('请填写有效的 SKU 价格')
        return false
      }
    }
    return true
  }

  const submitSpu = async () => {
    if (!validateBeforeSubmit()) return
    const payload = buildSpuSavePayload(baseForm, baseAttrValues.value, skuRows.value, isEdit.value)
    try {
      await ElMessageBox.confirm('将提交商品及 SKU 信息，是否继续？', '提示', {
        type: 'warning'
      })
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
      bounds: { buyBounds: 0, growBounds: 0 }
    })
    catalogPath.value = []
    attrGroups.value = []
    baseAttrValues.value = []
    saleAttrs.value = []
    saleAttrDraft.value = []
    saleTableColumns.value = []
    saleAttrsLoadedForCatalog.value = undefined
    skuRows.value = []
    const q = route.query.catalogId
    if (q != null) {
      const cid = Number(q)
      if (Number.isFinite(cid)) {
        baseForm.catalogId = cid
        syncCatalogPath(cid)
      }
    }
  }

  const applyBaseAttrsFromDetail = (
    saved: { attrId?: number; attrValues?: string; showDesc?: number }[]
  ) => {
    if (!saved?.length) return
    const map = new Map(saved.filter((a) => a.attrId != null).map((a) => [a.attrId!, a]))
    baseAttrValues.value.forEach((group, gidx) => {
      group.forEach((cell, aidx) => {
        const hit = map.get(cell.attrId)
        if (hit?.attrValues == null || String(hit.attrValues).trim() === '') return
        const attr = attrGroups.value[gidx]?.attrs[aidx]
        if (attr?.valueType === 1) {
          cell.attrValues = parseValueSelect(hit.attrValues)
        } else {
          cell.attrValues = hit.attrValues
        }
        cell.showDesc = hit.showDesc ?? cell.showDesc
      })
    })
  }

  const restoreSaleAndSkusFromDetail = (skus: MallProductSku[]) => {
    if (!skus?.length) return
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
        reducePrice: Number(sku.reducePrice ?? 0)
      }
    })
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
    applyBaseAttrsFromDetail(detail.baseAttrs ?? [])
    await loadSaleAttrStep({ catalogId: cid, force: true })
    if (detail.skus?.length) {
      restoreSaleAndSkusFromDetail(detail.skus)
    }
    syncSkuImageSlots()
  }

  onMounted(async () => {
    await Promise.all([loadCategoryOptions(), loadBrands()])
    if (isEdit.value) {
      await loadProductForEdit()
    } else {
      const q = route.query.catalogId
      if (q != null) {
        const cid = Number(q)
        if (Number.isFinite(cid)) {
          baseForm.catalogId = cid
          syncCatalogPath(cid)
        }
      }
    }
  })
</script>

<style scoped lang="scss">
  .mall-add-spu-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px;
    min-height: 0;
  }

  .wizard-toolbar__row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .wizard-toolbar__title {
    font-size: 16px;
    font-weight: 600;
  }

  .wizard-steps {
    margin-top: 8px;
  }

  .wizard-panel {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  .wizard-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 24px;
  }

  .wizard-tip {
    margin: 12px 0 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  .wizard-done {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
  }

  .wizard-panel--sku {
    :deep(.el-card__body) {
      padding-top: 8px;
    }
  }

  .sku-table-scroll {
    width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 4px 2px 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    background: var(--el-fill-color-blank);
    scrollbar-width: thin;
    scrollbar-color: var(--el-border-color) transparent;

    &.is-dragging {
      user-select: none;
    }

    &::-webkit-scrollbar {
      height: 8px;
    }

    &::-webkit-scrollbar-thumb {
      border-radius: 4px;
      background: var(--el-border-color-darker);
    }
  }

  .sku-table {
    width: max-content;
    min-width: 100%;

    :deep(.sku-table__header-cell) {
      background: var(--el-fill-color-light) !important;
      font-weight: 600;
    }

    :deep(.sku-table__body-cell) {
      padding: 12px 14px;
      vertical-align: middle;
    }

    :deep(.sku-table__promo-cell) {
      background: var(--el-fill-color-extra-light);

      .cell {
        overflow: visible;
      }
    }

    :deep(.el-input__wrapper) {
      width: 100%;
    }

    :deep(.sku-cell-num:not(.sku-cell-num--sm)) {
      width: 100%;
    }
  }

  .sku-attr-val {
    display: inline-block;
    padding: 4px 10px;
    font-size: 13px;
    background: var(--el-color-primary-light-9);
    border: 1px solid var(--el-color-primary-light-7);
    border-radius: 4px;
    white-space: nowrap;
  }

  .sku-cell-input {
    width: 100%;
    min-width: 200px;
  }

  .sku-cell-num {
    width: 100%;
  }

  .sku-cell-num--sm {
    width: 100px !important;
    min-width: 100px;
    flex-shrink: 0;
  }

  .sku-promo-block {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: max-content;
  }

  .sku-promo-row {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    gap: 6px;
  }

  .sku-promo-label {
    flex-shrink: 0;
    width: 20px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  .sku-image-pick {
    display: flex;
    flex-wrap: nowrap;
    gap: 10px;
  }

  .sku-image-pick__item {
    flex-shrink: 0;
    width: 96px;
    padding: 6px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    text-align: center;

    img {
      width: 100%;
      height: 72px;
      object-fit: cover;
      border-radius: 4px;
    }
  }

  .sku-image-pick__actions {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
    margin-top: 6px;

    :deep(.el-checkbox),
    :deep(.el-radio) {
      height: auto;
      margin-right: 0;
    }

    :deep(.el-checkbox__label),
    :deep(.el-radio__label) {
      font-size: 12px;
      padding-left: 6px;
    }
  }
</style>

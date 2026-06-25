<!-- C 端商品详情 -->
<template>
  <div v-loading="loading" class="portal-product">
    <ElButton class="back-btn" link @click="router.back()">← 返回</ElButton>

    <template v-if="product">
      <div class="portal-product__main">
        <div class="gallery">
          <img :src="mainImage" :alt="product.spuName" class="gallery__main" />
          <div v-if="thumbImages.length > 1" class="gallery__thumbs">
            <img
              v-for="(img, idx) in thumbImages"
              :key="idx"
              :src="img"
              class="gallery__thumb"
              :class="{ active: mainImage === img }"
              @click="mainImage = img"
            />
          </div>
        </div>

        <div class="info">
          <h1 class="info__title">{{ product.spuName }}</h1>
          <p v-if="product.brandName" class="info__brand">{{ product.brandName }}</p>
          <p class="info__price">
            <span class="currency">¥</span>{{ formatPrice(currentPrice) }}
          </p>

          <div v-for="dim in attrDimensions" :key="dim.name" class="sku-row">
            <span class="sku-row__label">{{ dim.name }}</span>
            <div class="sku-row__values">
              <span
                v-for="val in dim.values"
                :key="val"
                class="sku-tag"
                :class="{
                  active: selectedAttrs[dim.name] === val,
                  disabled: !isAttrAvailable(dim.name, val)
                }"
                @click="selectAttr(dim.name, val)"
              >
                {{ val }}
              </span>
            </div>
          </div>

          <div class="sku-row">
            <span class="sku-row__label">数量</span>
            <ElInputNumber v-model="quantity" :max="maxPurchaseQty" :min="1" />
            <span v-if="selectedSku?.availableStock != null" class="stock-hint">
              可售 {{ selectedSku.availableStock }} 件
            </span>
          </div>

          <div class="actions">
            <ElButton type="danger" size="large" :disabled="!selectedSkuId" @click="handleAddCart">
              加入购物车
            </ElButton>
            <ElButton
              type="warning"
              size="large"
              plain
              :disabled="!selectedSkuId"
              @click="handleBuyNow"
            >
              立即购买
            </ElButton>
          </div>
        </div>
      </div>

      <div v-if="product.decript?.length" class="portal-product__desc">
        <h3>商品详情</h3>
        <div class="desc-images">
          <img v-for="(img, i) in descImages" :key="i" :src="img" alt="详情图" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { fetchPortalProductDetail } from '@/api/portal/product'
  import { fetchPortalCartAdd } from '@/api/portal/cart'
  import type { PortalProductDetail } from '@/api/portal/types'
  import { usePortalAuth } from '@/hooks/portal/usePortalAuth'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import { notifyPortalCartChanged } from '@/utils/portal/cart-event'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'PortalProductDetail' })

  const route = useRoute()
  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const product = ref<PortalProductDetail | null>(null)
  const imageUrlMap = ref(new Map<string, string>())
  const mainImage = ref('')
  const thumbImages = ref<string[]>([])
  const descImages = ref<string[]>([])
  const selectedAttrs = reactive<Record<string, string>>({})
  const quantity = ref(1)

  type SkuItem = NonNullable<PortalProductDetail['skus']>[number]

  const attrDimensions = computed(() => {
    const map = new Map<string, Set<string>>()
    for (const sku of product.value?.skus || []) {
      for (const attr of sku.attr || []) {
        if (!attr.attrName || !attr.attrValue) continue
        if (!map.has(attr.attrName)) map.set(attr.attrName, new Set())
        map.get(attr.attrName)!.add(attr.attrValue)
      }
    }
    return Array.from(map.entries()).map(([name, set]) => ({
      name,
      values: Array.from(set)
    }))
  })

  const selectedSku = computed<SkuItem | undefined>(() => {
    const skus = product.value?.skus || []
    if (!skus.length) return undefined
    if (!attrDimensions.value.length) return skus[0]
    return skus.find((sku) =>
      (sku.attr || []).every((a) => selectedAttrs[a.attrName!] === a.attrValue)
    )
  })

  const selectedSkuId = computed(() => selectedSku.value?.skuId)
  const currentPrice = computed(() => selectedSku.value?.price ?? product.value?.price)
  const maxPurchaseQty = computed(() => {
    const stock = selectedSku.value?.availableStock
    if (stock == null || stock <= 0) return 1
    return Math.min(stock, 99)
  })

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  function isAttrAvailable(attrName: string, attrValue: string) {
    const trial = { ...selectedAttrs, [attrName]: attrValue }
    return (product.value?.skus || []).some((sku) =>
      (sku.attr || []).every((a) => !trial[a.attrName!] || trial[a.attrName!] === a.attrValue)
    )
  }

  function selectAttr(name: string, val: string) {
    if (!isAttrAvailable(name, val)) return
    selectedAttrs[name] = val
  }

  function initDefaultSku() {
    const skus = product.value?.skus || []
    if (!skus.length) return
    const first = skus[0]
    for (const attr of first.attr || []) {
      if (attr.attrName && attr.attrValue) {
        selectedAttrs[attr.attrName] = attr.attrValue
      }
    }
  }

  async function loadProduct() {
    const id = Number(route.params.id)
    if (!Number.isFinite(id)) return
    product.value = await fetchPortalProductDetail(id)

    const rawImages = [
      product.value.coverImg,
      ...(product.value.images || []),
      ...(product.value.skus?.flatMap((s) => s.images?.map((i) => i.imgUrl) || []) || [])
    ].filter(Boolean) as string[]

    const map = await resolveGoodsImageDisplayUrls(rawImages)
    imageUrlMap.value = map

    const resolve = (key?: string) => (key ? map.get(key) || map.get(key) || '' : '')
    const thumbs = [...new Set(rawImages.map((k) => resolve(k)).filter(Boolean))]
    thumbImages.value = thumbs
    mainImage.value = thumbs[0] || ''

    if (product.value.decript?.length) {
      const descMap = await resolveGoodsImageDisplayUrls(product.value.decript)
      descImages.value = product.value.decript.map((k) => descMap.get(k) || k).filter(Boolean)
    }

    initDefaultSku()
  }

  async function handleAddCart() {
    if (!requireLogin() || !selectedSkuId.value) return
    await fetchPortalCartAdd({ skuId: selectedSkuId.value, quantity: quantity.value })
    notifyPortalCartChanged()
    ElMessage.success('已加入购物车')
  }

  async function handleBuyNow() {
    if (!requireLogin() || !selectedSkuId.value) return
    await fetchPortalCartAdd({ skuId: selectedSkuId.value, quantity: quantity.value })
    notifyPortalCartChanged()
    router.push('/portal/checkout?direct=1')
  }

  onMounted(async () => {
    try {
      await loadProduct()
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .back-btn {
    margin-bottom: 12px;
  }

  .portal-product__main {
    display: grid;
    grid-template-columns: 420px 1fr;
    gap: 32px;
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 16px;

    @media (width <= 768px) {
      grid-template-columns: 1fr;
    }
  }

  .gallery__main {
    width: 100%;
    aspect-ratio: 1;
    object-fit: contain;
    background: #fafafa;
    border-radius: 8px;
  }

  .gallery__thumbs {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    flex-wrap: wrap;
  }

  .gallery__thumb {
    width: 64px;
    height: 64px;
    object-fit: cover;
    border: 2px solid transparent;
    border-radius: 4px;
    cursor: pointer;

    &.active {
      border-color: #e1251b;
    }
  }

  .info__title {
    margin: 0 0 8px;
    font-size: 22px;
    line-height: 1.4;
  }

  .info__brand {
    color: #999;
    margin: 0 0 16px;
  }

  .info__price {
    color: #e1251b;
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 24px;

    .currency {
      font-size: 16px;
    }
  }

  .sku-row {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;

    &__label {
      width: 48px;
      flex-shrink: 0;
      color: #999;
      line-height: 32px;
      font-size: 14px;
    }

    &__values {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .sku-tag {
    padding: 6px 14px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    user-select: none;

    &.active {
      border-color: #e1251b;
      color: #e1251b;
      background: #fff5f5;
    }

    &.disabled {
      color: #ccc;
      cursor: not-allowed;
      text-decoration: line-through;
    }
  }

  .actions {
    margin-top: 24px;
    display: flex;
    gap: 12px;
  }

  .portal-product__desc {
    background: #fff;
    border-radius: 8px;
    padding: 24px;

    h3 {
      margin: 0 0 16px;
    }

    .desc-images img {
      max-width: 100%;
      display: block;
      margin: 0 auto 8px;
    }
  }
</style>

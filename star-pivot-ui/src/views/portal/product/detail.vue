<!-- C 端商品详情 -->
<template>
  <div v-loading="loading" class="portal-product">
    <button type="button" class="back-btn" @click="router.back()">
      <ArtSvgIcon icon="ri:arrow-left-line" />
      返回
    </button>

    <template v-if="product">
      <div class="portal-product__main">
        <div class="gallery">
          <div class="gallery__main-wrap">
            <img :src="mainImage" :alt="product.spuName" class="gallery__main" />
          </div>
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
          <p v-if="product.brandName" class="info__brand">{{ product.brandName }}</p>
          <div class="info__title-row">
            <h1 class="info__title">{{ product.spuName }}</h1>
            <PortalSharePopover
              :title="product.spuName"
              :promo-text="sharePromoText"
            />
          </div>
          <div v-if="commentSummary?.total" class="info__rating">
            <ElRate :model-value="summaryAvgStar" disabled show-score score-template="{value} 分" />
            <span class="info__rating-count">{{ commentSummary.total }} 条评价</span>
          </div>
          <div class="info__price-box">
            <p class="info__price">
              <span class="currency">¥</span>{{ formatPrice(currentPrice) }}
            </p>
            <span v-if="selectedSku?.availableStock != null" class="info__stock">
              库存 {{ selectedSku.availableStock }} 件
            </span>
          </div>

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
            <ElButton
              size="large"
              class="btn-collect"
              :type="collected ? 'danger' : 'default'"
              :loading="collectLoading"
              @click="handleToggleCollect"
            >
              <ArtSvgIcon :icon="collected ? 'ri:heart-fill' : 'ri:heart-line'" />
              {{ collected ? '已收藏' : '收藏' }}
            </ElButton>
            <ElButton type="primary" size="large" class="btn-cart" :disabled="!selectedSkuId" @click="handleAddCart">
              <ArtSvgIcon icon="ri:shopping-cart-2-line" />
              加入购物车
            </ElButton>
            <ElButton
              size="large"
              class="btn-buy"
              :disabled="!selectedSkuId"
              @click="handleBuyNow"
            >
              立即购买
            </ElButton>
          </div>
        </div>
      </div>

      <div v-if="product.decript?.length" class="portal-product__desc">
        <h3>
          <ArtSvgIcon icon="ri:file-text-line" />
          商品详情
        </h3>
        <div class="desc-images">
          <img v-for="(img, i) in descImages" :key="i" :src="img" alt="详情图" />
        </div>
      </div>

      <section class="portal-product__comments">
        <div class="comments-head">
          <h3>
            <ArtSvgIcon icon="ri:chat-smile-2-line" />
            商品评价
            <span v-if="commentTotal">({{ commentTotal }})</span>
          </h3>
        </div>

        <div v-if="canComment" class="comment-form">
          <p class="comment-form__hint">您已购买该商品，欢迎分享使用感受</p>
          <div class="comment-form__star">
            <span>评分</span>
            <ElRate v-model="commentForm.star" />
          </div>
          <ElInput
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="说说商品怎么样吧"
          />
          <div class="comment-form__upload">
            <span class="comment-form__upload-label">晒图（最多3张）</span>
            <div class="comment-form__images">
              <div v-for="(url, idx) in commentPreviewUrls" :key="idx" class="comment-form__thumb">
                <img :src="url" alt="" />
                <button type="button" class="remove-btn" @click="removeCommentImage(idx)">×</button>
              </div>
              <label v-if="commentImageKeys.length < 3" class="comment-form__add">
                <input type="file" accept="image/*" hidden @change="handleCommentImageSelect" />
                <ArtSvgIcon icon="ri:image-add-line" />
                <span>上传</span>
              </label>
            </div>
          </div>
          <ElButton type="primary" :loading="submittingComment" @click="handleSubmitComment">提交评价</ElButton>
        </div>

        <div v-if="comments.length" class="comment-list">
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <div class="comment-item__head">
              <span class="nick">{{ item.memberNickName || '匿名用户' }}</span>
              <ElRate :model-value="item.star" disabled />
              <span class="time">{{ item.createTime }}</span>
            </div>
            <p v-if="item.spuAttributes" class="comment-item__sku">{{ item.spuAttributes }}</p>
            <p class="comment-item__content">{{ item.content }}</p>
            <div v-if="item.replies?.length" class="comment-item__replies">
              <div v-for="reply in item.replies" :key="reply.id" class="comment-reply">
                <span class="comment-reply__author">{{ reply.memberNickName || '官方' }}：</span>
                <span>{{ reply.content }}</span>
              </div>
            </div>
            <div v-if="commentImageMap.get(item.id!)" class="comment-item__images">
              <img
                v-for="(url, idx) in commentImageMap.get(item.id!) || []"
                :key="idx"
                :src="url"
                alt="评价图"
              />
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无评价，快来抢沙发" />

        <div v-if="commentTotal > comments.length" class="load-more">
          <ElButton :loading="loadingComments" @click="loadMoreComments">查看更多评价</ElButton>
        </div>
      </section>

      <section v-if="relatedProducts.length" class="portal-product__related">
        <h3>
          <ArtSvgIcon icon="ri:sparkling-2-line" />
          相关推荐
        </h3>
        <div class="related-grid">
          <div
            v-for="item in relatedProducts"
            :key="item.id"
            class="related-card"
            @click="goRelatedProduct(item.id!)"
          >
            <img
              :src="relatedCoverUrls.get(item.coverImg || '') || placeholderRelatedImg"
              :alt="item.spuName"
            />
            <div class="related-card__body">
              <p class="related-card__name">{{ item.spuName }}</p>
              <PortalProductRating :avg-star="item.avgStar" :comment-count="item.commentCount" />
              <p class="related-card__price">
                <span>¥</span>{{ formatPrice(item.price) }}
              </p>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalProductDetail, fetchPortalProductRelated} from '@/api/portal/product'
import {fetchPortalCartAdd} from '@/api/portal/cart'
import {fetchPortalCollectAdd, fetchPortalCollectRemove, fetchPortalCollectStatus} from '@/api/portal/collect'
import {
  fetchPortalCanComment,
  fetchPortalCommentPageList,
  fetchPortalCommentSubmit,
  fetchPortalCommentSummary
} from '@/api/portal/comment'
import {uploadPortalCommentImage} from '@/api/portal/image'
import type {PortalComment, PortalCommentSummary, PortalProductDetail, PortalProductListItem} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {resolveCommentResourceUrls} from '@/utils/portal/comment-resources'
import {notifyPortalCartChanged} from '@/utils/portal/cart-event'
import {notifyPortalReviewChanged} from '@/utils/portal/review-event'
import {addPortalBrowseRecord} from '@/utils/portal/browse-history'
import {ElMessage} from 'element-plus'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import PortalProductRating from '@/views/portal/components/portal-product-rating.vue'
import PortalSharePopover from '@/views/portal/components/portal-share-popover.vue'

defineOptions({ name: 'PortalProductDetail' })

  const route = useRoute()
  const router = useRouter()
  const { requireLogin, portalStore } = usePortalAuth()

  const loading = ref(true)
  const product = ref<PortalProductDetail | null>(null)
  const collected = ref(false)
  const collectLoading = ref(false)
  const canComment = ref(false)
  const comments = ref<PortalComment[]>([])
  const commentTotal = ref(0)
  const commentSummary = ref<PortalCommentSummary | null>(null)
  const commentPage = ref(1)
  const loadingComments = ref(false)
  const submittingComment = ref(false)
  const uploadingCommentImage = ref(false)
  const commentForm = reactive({ star: 5, content: '' })
  const commentImageKeys = ref<string[]>([])
  const commentPreviewUrls = ref<string[]>([])
  const commentImageMap = ref(new Map<number, string[]>())
  const imageUrlMap = ref(new Map<string, string>())
  const mainImage = ref('')
  const thumbImages = ref<string[]>([])
  const descImages = ref<string[]>([])
  const selectedAttrs = reactive<Record<string, string>>({})
  const quantity = ref(1)
  const relatedProducts = ref<PortalProductListItem[]>([])
  const relatedCoverUrls = ref(new Map<string, string>())

  const placeholderRelatedImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160"><rect fill="#f0f0f0" width="160" height="160"/></svg>'
    )

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

  const sharePromoText = computed(() => {
    const name = product.value?.spuName || 'StarPivot 商品'
    const price = currentPrice.value
    const priceText = price != null ? `¥${Number(price).toFixed(2)}` : ''
    const url = typeof window !== 'undefined' ? window.location.href : ''
    return priceText ? `${name} ${priceText}\n${url}` : `${name}\n${url}`
  })
  const maxPurchaseQty = computed(() => {
    const stock = selectedSku.value?.availableStock
    if (stock == null || stock <= 0) return 1
    return Math.min(stock, 99)
  })

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  const summaryAvgStar = computed(() => {
    const avg = commentSummary.value?.avgStar
    return avg != null ? Number(avg) : 0
  })

  async function loadCommentSummary() {
    if (!product.value?.id) return
    try {
      commentSummary.value = await fetchPortalCommentSummary(product.value.id)
    } catch {
      commentSummary.value = null
    }
  }

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

    const querySkuId = Number(route.query.skuId)
    const matchedSku = Number.isFinite(querySkuId)
      ? skus.find((sku) => sku.skuId === querySkuId)
      : undefined
    const target = matchedSku || skus[0]

    for (const key of Object.keys(selectedAttrs)) {
      delete selectedAttrs[key]
    }
    for (const attr of target.attr || []) {
      if (attr.attrName && attr.attrValue) {
        selectedAttrs[attr.attrName] = attr.attrValue
      }
    }
  }

  async function loadProduct() {
    const id = Number(route.params.id)
    if (!Number.isFinite(id)) return
    product.value = await fetchPortalProductDetail(id)

    addPortalBrowseRecord({
      spuId: id,
      spuName: product.value.spuName,
      coverImg: product.value.coverImg,
      price: product.value.price != null ? Number(product.value.price) : undefined
    })

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
    await Promise.all([loadCollectStatus(), loadCanComment(), loadCommentSummary(), loadComments(true)])
    await loadRelatedProducts()
    scrollToReviewIfNeeded()
  }

  async function loadRelatedProducts() {
    if (!product.value?.id) return
    try {
      relatedProducts.value = await fetchPortalProductRelated(product.value.id, 8)
      const keys = relatedProducts.value.map((item) => item.coverImg).filter(Boolean) as string[]
      if (keys.length) {
        const map = await resolveGoodsImageDisplayUrls(keys)
        map.forEach((url, key) => relatedCoverUrls.value.set(key, url))
      }
    } catch {
      relatedProducts.value = []
    }
  }

  function goRelatedProduct(id: number) {
    router.push(`/portal/product/${id}`)
  }

  function scrollToReviewIfNeeded() {
    if (route.query.review !== '1') return
    nextTick(() => {
      document.querySelector('.portal-product__comments')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
    })
  }

  async function loadCollectStatus() {
    if (!portalStore.isLogin || !product.value?.id) {
      collected.value = false
      return
    }
    try {
      const res = await fetchPortalCollectStatus(product.value.id)
      collected.value = res.collected
    } catch {
      collected.value = false
    }
  }

  async function loadCanComment() {
    if (!portalStore.isLogin || !product.value?.id) {
      canComment.value = false
      return
    }
    try {
      const res = await fetchPortalCanComment(product.value.id)
      canComment.value = res.canComment
    } catch {
      canComment.value = false
    }
  }

  async function resolveCommentImages(rows: PortalComment[]) {
    for (const row of rows) {
      if (!row.id || !row.resources) continue
      const urls = await resolveCommentResourceUrls(row.resources)
      if (urls.length) commentImageMap.value.set(row.id, urls)
    }
  }

  async function loadComments(reset = true) {
    if (!product.value?.id) return
    if (reset) {
      commentPage.value = 1
      comments.value = []
      commentImageMap.value = new Map()
    }
    const res = await fetchPortalCommentPageList({
      spuId: product.value.id,
      pageNum: commentPage.value,
      pageSize: 5
    })
    commentTotal.value = res.total || 0
    const rows = res.rows || []
    comments.value = reset ? rows : [...comments.value, ...rows]
    await resolveCommentImages(rows)
  }

  async function loadMoreComments() {
    loadingComments.value = true
    commentPage.value += 1
    try {
      await loadComments(false)
    } finally {
      loadingComments.value = false
    }
  }

  async function handleToggleCollect() {
    if (!requireLogin() || !product.value?.id) return
    collectLoading.value = true
    try {
      if (collected.value) {
        await fetchPortalCollectRemove(product.value.id)
        collected.value = false
      } else {
        await fetchPortalCollectAdd(product.value.id)
        collected.value = true
      }
    } finally {
      collectLoading.value = false
    }
  }

  function resetCommentImages() {
    commentImageKeys.value = []
    commentPreviewUrls.value = []
  }

  function removeCommentImage(index: number) {
    commentImageKeys.value.splice(index, 1)
    commentPreviewUrls.value.splice(index, 1)
  }

  async function handleCommentImageSelect(e: Event) {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file || commentImageKeys.value.length >= 3) return
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('请选择图片文件')
      return
    }
    uploadingCommentImage.value = true
    try {
      const res = await uploadPortalCommentImage(file)
      commentImageKeys.value.push(res.objectName)
      commentPreviewUrls.value.push(res.displayUrl || res.presignedUrl || res.objectName)
    } finally {
      uploadingCommentImage.value = false
    }
  }

  async function handleSubmitComment() {
    if (!requireLogin() || !product.value?.id || !selectedSkuId.value) return
    if (!commentForm.content.trim()) {
      ElMessage.warning('请填写评价内容')
      return
    }
    submittingComment.value = true
    try {
      await fetchPortalCommentSubmit({
        spuId: product.value.id,
        skuId: selectedSkuId.value,
        star: commentForm.star,
        content: commentForm.content.trim(),
        resources: commentImageKeys.value.length ? commentImageKeys.value.join(',') : undefined
      })
      commentForm.content = ''
      resetCommentImages()
      canComment.value = false
      notifyPortalReviewChanged()
      await Promise.all([loadComments(true), loadCommentSummary()])
    } finally {
      submittingComment.value = false
    }
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

  watch(
    () => route.query.review,
    () => scrollToReviewIfNeeded()
  )

  watch(
    () => route.params.id,
    async () => {
      loading.value = true
      relatedProducts.value = []
      relatedCoverUrls.value = new Map()
      try {
        await loadProduct()
      } finally {
        loading.value = false
      }
    }
  )
</script>

<style scoped lang="scss">

  .back-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 16px;
    padding: 6px 12px;
    border: 1px solid var(--portal-border);
    border-radius: 20px;
    background: var(--portal-bg-elevated);
    color: var(--portal-text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all var(--portal-transition);

    &:hover {
      color: var(--portal-primary);
      border-color: var(--portal-primary);
    }
  }

  .portal-product__main {
    display: grid;
    grid-template-columns: 440px 1fr;
    gap: 40px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 32px;
    margin-bottom: 20px;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);

    @media (width <= 768px) {
      grid-template-columns: 1fr;
      padding: 20px;
    }
  }

  .gallery__main-wrap {
    border-radius: var(--portal-radius);
    overflow: hidden;
    background: #fafbfc;
    border: 1px solid var(--portal-border);
  }

  .gallery__main {
    width: 100%;
    aspect-ratio: 1;
    object-fit: contain;
  }

  .gallery__thumbs {
    display: flex;
    gap: 10px;
    margin-top: 14px;
    flex-wrap: wrap;
  }

  .gallery__thumb {
    width: 68px;
    height: 68px;
    object-fit: cover;
    border: 2px solid transparent;
    border-radius: var(--portal-radius-sm);
    cursor: pointer;
    transition: border-color var(--portal-transition);

    &.active {
      border-color: var(--portal-primary);
    }

    &:hover:not(.active) {
      border-color: var(--portal-border-strong);
    }
  }

  .info__brand {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--portal-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .info__title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .info__title {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    line-height: 1.4;
    color: var(--portal-text);
    letter-spacing: -0.02em;
    flex: 1;
    min-width: 0;
  }

  .info__rating {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
  }

  .info__rating-count {
    font-size: 13px;
    color: var(--portal-text-muted);
  }

  .info__price-box {
    display: flex;
    align-items: baseline;
    gap: 16px;
    padding: 16px 20px;
    margin-bottom: 24px;
    background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
    border-radius: var(--portal-radius);
    border: 1px solid #ffe0e0;
  }

  .info__price {
    color: var(--portal-primary);
    font-size: 32px;
    font-weight: 800;
    margin: 0;
    letter-spacing: -0.03em;

    .currency {
      font-size: 18px;
    }
  }

  .info__stock {
    font-size: 13px;
    color: var(--portal-text-muted);
  }

  .sku-row {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 18px;

    &__label {
      width: 52px;
      flex-shrink: 0;
      color: var(--portal-text-secondary);
      line-height: 34px;
      font-size: 14px;
    }

    &__values {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .sku-tag {
    padding: 7px 16px;
    border: 1px solid var(--portal-border-strong);
    border-radius: var(--portal-radius-sm);
    font-size: 13px;
    cursor: pointer;
    user-select: none;
    transition: all var(--portal-transition);
    background: #fff;

    &.active {
      border-color: var(--portal-primary);
      color: var(--portal-primary);
      background: var(--portal-primary-light);
      font-weight: 600;
    }

    &.disabled {
      color: var(--portal-text-muted);
      cursor: not-allowed;
      text-decoration: line-through;
      background: #fafbfc;
    }

    &:not(.disabled):not(.active):hover {
      border-color: #ffb8b8;
    }
  }

  .stock-hint {
    margin-left: 8px;
    font-size: 13px;
    color: var(--portal-text-muted);
    line-height: 32px;
  }

  .actions {
    margin-top: 28px;
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    :deep(.el-button) {
      min-width: 140px;
      border-radius: 24px;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }

    .btn-collect {
      min-width: 110px;
    }

    .btn-cart {
      background: var(--portal-primary-gradient);
      border: none;
    }

    .btn-buy {
      background: var(--portal-accent-orange);
      border: none;
      color: #fff;

      &:hover {
        opacity: 0.9;
        color: #fff;
      }
    }
  }

  .portal-product__desc {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 28px;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);

    h3 {
      margin: 0 0 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 700;
      color: var(--portal-text);

      svg {
        color: var(--portal-primary);
      }
    }

    .desc-images img {
      max-width: 100%;
      display: block;
      margin: 0 auto 8px;
      border-radius: var(--portal-radius-sm);
    }
  }

  .portal-product__comments {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 28px;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);

    .comments-head h3 {
      margin: 0 0 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 700;

      span {
        font-size: 14px;
        color: var(--portal-text-muted);
        font-weight: 400;
      }
    }
  }

  .comment-form {
    padding: 16px;
    margin-bottom: 20px;
    border-radius: var(--portal-radius);
    background: var(--portal-primary-light);

    &__hint {
      margin: 0 0 12px;
      font-size: 13px;
      color: var(--portal-text-secondary);
    }

    &__star {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      font-size: 14px;
    }

    &__upload {
      margin: 12px 0;

      &-label {
        display: block;
        margin-bottom: 8px;
        font-size: 13px;
        color: var(--portal-text-secondary);
      }
    }

    &__images {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    &__thumb {
      position: relative;
      width: 72px;
      height: 72px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: var(--portal-radius-sm);
        border: 1px solid var(--portal-border);
      }

      .remove-btn {
        position: absolute;
        top: -6px;
        right: -6px;
        width: 20px;
        height: 20px;
        border: none;
        border-radius: 50%;
        background: rgb(0 0 0 / 55%);
        color: #fff;
        cursor: pointer;
        font-size: 14px;
        line-height: 1;
      }
    }

    &__add {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 72px;
      height: 72px;
      border: 1px dashed var(--portal-border-strong);
      border-radius: var(--portal-radius-sm);
      cursor: pointer;
      color: var(--portal-text-muted);
      font-size: 11px;
      gap: 2px;

      svg {
        font-size: 22px;
      }

      &:hover {
        border-color: var(--portal-primary);
        color: var(--portal-primary);
      }
    }
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .comment-item {
    padding-bottom: 16px;
    border-bottom: 1px solid var(--portal-border);

    &:last-child {
      border-bottom: none;
    }

    &__head {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .nick {
        font-weight: 600;
        color: var(--portal-text);
      }

      .time {
        margin-left: auto;
        font-size: 12px;
        color: var(--portal-text-muted);
      }
    }

    &__sku {
      margin: 0 0 6px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__content {
      margin: 0;
      font-size: 14px;
      line-height: 1.6;
      color: var(--portal-text-secondary);
    }

    &__replies {
      margin-top: 10px;
      padding: 10px 12px;
      border-radius: var(--portal-radius-sm);
      background: #f8f9fb;
    }

    &__images {
      display: flex;
      gap: 8px;
      margin-top: 10px;
      flex-wrap: wrap;

      img {
        width: 64px;
        height: 64px;
        object-fit: cover;
        border-radius: var(--portal-radius-sm);
        border: 1px solid var(--portal-border);
      }
    }
  }

  .comment-reply {
    font-size: 13px;
    line-height: 1.5;
    color: var(--portal-text-secondary);

    & + & {
      margin-top: 6px;
    }

    &__author {
      color: var(--portal-primary);
      font-weight: 600;
    }
  }

  .load-more {
    text-align: center;
    margin-top: 16px;
  }

  .portal-product__related {
    margin-top: 24px;
    padding: 24px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    border: 1px solid var(--portal-border);

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 20px;
      font-size: 18px;
    }
  }

  .related-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 16px;
  }

  .related-card {
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--portal-transition);
    background: #fff;

    &:hover {
      border-color: var(--portal-primary);
      box-shadow: var(--portal-shadow-sm);
    }

    img {
      width: 100%;
      aspect-ratio: 1;
      object-fit: cover;
      background: #fafbfc;
    }

    &__body {
      padding: 12px;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 13px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    &__price {
      margin: 8px 0 0;
      color: var(--portal-primary);
      font-size: 16px;
      font-weight: 700;

      span {
        font-size: 12px;
      }
    }
  }
</style>

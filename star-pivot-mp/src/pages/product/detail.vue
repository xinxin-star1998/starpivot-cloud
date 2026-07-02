<template>

  <view class="page">

    <view v-if="loading" class="hint">加载中...</view>

    <template v-else-if="detail">

      <swiper v-if="images.length" class="gallery" circular indicator-dots>

        <swiper-item v-for="(img, idx) in images" :key="idx">

          <image class="gallery-img" :src="imageSrc(img)" mode="aspectFill" />

        </swiper-item>

      </swiper>

      <view class="info card">
        <view class="title-row">
          <text class="self-tag">自营</text>
          <text class="name">{{ detail.spuName }}</text>
        </view>
        <view class="price-row">
          <text class="price"><text class="yen">¥</text>{{ selectedSku?.price ?? detail.price }}</text>
          <text v-if="commentSummary?.total" class="rating">★ {{ commentSummary.avgStar }} · {{ commentSummary.total }}评</text>
        </view>
        <text class="desc">{{ detail.spuDescription }}</text>
      </view>
      <view v-if="detail.skus?.length" class="sku-select-row card" @click="openSkuPopup">
        <text class="panel-label">已选</text>
        <text class="sku-selected">{{ selectedSkuLabel }}</text>
        <text class="arrow">›</text>
      </view>

      <view v-if="relatedProducts.length" class="related panel card">
        <view class="panel-head">
          <text class="panel-title">看了又看</text>
        </view>
        <scroll-view scroll-x class="related-scroll" :show-scrollbar="false">
          <view class="related-row">
            <view
              v-for="item in relatedProducts"
              :key="item.id"
              class="related-item"
              @click="goProduct(item.id)"
            >
              <image class="related-img" :src="imageSrc(productCover(item))" mode="aspectFill" />
              <text class="related-name">{{ item.spuName }}</text>
              <text class="related-price">¥{{ item.price }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="comments panel card">

        <view class="panel-head">

          <text class="panel-title">商品评价</text>

          <text v-if="comments.length" class="more" @click="loadMoreComments">更多</text>

        </view>

        <view v-for="c in comments" :key="c.id" class="comment-item">

          <view class="comment-stars">

            <text v-for="n in 5" :key="n" class="star" :class="{ on: (c.star || 0) >= n }">★</text>

          </view>

          <text class="comment-user">{{ c.memberNickName || '用户' }}</text>

          <text class="comment-content">{{ c.content }}</text>

        </view>

        <view v-if="!comments.length" class="hint-inline">暂无评价</view>

      </view>



      <view class="footer">

        <view class="icon-btn" @click="toggleCollect">

          <text>{{ collected ? '♥' : '♡' }}</text>

          <text class="icon-label">收藏</text>

        </view>

        <button class="btn-cart" @click="handleAddCart">加入购物车</button>

        <button class="btn-buy" @click="handleBuyNow">立即购买</button>

      </view>

      <view v-if="skuPopupVisible" class="sku-mask" @click="closeSkuPopup">
        <view class="sku-popup" @click.stop>
          <view class="popup-head">
            <image v-if="images.length" class="popup-thumb" :src="imageSrc(images[0])" mode="aspectFill" />
            <view class="popup-meta">
              <text class="popup-price">¥{{ selectedSku?.price ?? detail.price }}</text>
              <text class="popup-selected">已选：{{ selectedSkuLabel }}</text>
            </view>
            <text class="popup-close" @click="closeSkuPopup">✕</text>
          </view>
          <text class="popup-label">选择规格</text>
          <scroll-view scroll-y class="sku-scroll" :show-scrollbar="false">
            <view class="sku-list">
              <view
                v-for="sku in detail.skus"
                :key="sku.skuId"
                class="sku-item"
                :class="{ active: selectedSku?.skuId === sku.skuId }"
                @click="selectedSku = sku"
              >
                {{ sku.saleAttrs || sku.skuName }}
              </view>
            </view>
          </scroll-view>
          <view class="popup-footer">
            <button class="popup-confirm" @click="confirmSkuPopup">确定</button>
          </view>
        </view>
      </view>

    </template>

  </view>

</template>



<script setup lang="ts">

import {onLoad, onShareAppMessage} from '@dcloudio/uni-app'

import {computed, ref} from 'vue'

import {addCollect, fetchCollectStatus, removeCollect} from '@/api/collect'

import {fetchCommentList, fetchCommentSummary} from '@/api/comment'

import {addToCart} from '@/api/cart'

import {fetchProductDetail, fetchRelatedProducts, productCover} from '@/api/product'

import type {PortalComment, PortalCommentSummary, PortalProductDetail, PortalProductListItem} from '@/api/types'

import {isLogin} from '@/stores/member'
import {buildSharePayload, productSharePath} from '@/utils/share'
import {addBrowseRecord} from '@/utils/browse-history'
import {refreshCartBadge} from '@/utils/tabbar-cart'
import {useGoodsImages} from '@/composables/use-goods-images'


const detail = ref<PortalProductDetail | null>(null)

const selectedSku = ref<{

  skuId?: number

  price?: number

  skuName?: string

  saleAttrs?: string

} | null>(null)

const loading = ref(false)

const collected = ref(false)

const commentSummary = ref<PortalCommentSummary | null>(null)

const comments = ref<PortalComment[]>([])

const commentPage = ref(1)

const relatedProducts = ref<PortalProductListItem[]>([])

const skuPopupVisible = ref(false)

const pendingSkuAction = ref<'cart' | 'buy' | null>(null)

let productId = 0

const { imageSrc, prefetchImages } = useGoodsImages()



const images = computed(() => {

  if (!detail.value) return []

  if (detail.value.images?.length) return detail.value.images

  return detail.value.pic ? [detail.value.pic] : []

})

const selectedSkuLabel = computed(() => {
  if (!selectedSku.value) return '请选择规格'
  return selectedSku.value.saleAttrs || selectedSku.value.skuName || '已选规格'
})

const hasMultipleSkus = computed(() => (detail.value?.skus?.length || 0) > 1)



async function loadDetail() {

  if (!productId) return

  loading.value = true

  try {

    const product = await fetchProductDetail(productId)

    detail.value = product

    selectedSku.value = product.skus?.length === 1 ? product.skus[0] : null

    addBrowseRecord({
      spuId: productId,
      spuName: product.spuName,
      coverImg: product.coverImg || product.pic,
      price: product.price != null ? Number(product.price) : undefined
    })

    await prefetchImages(images.value)

    try {
      const [summary, commentPageData, related] = await Promise.all([
        fetchCommentSummary(productId),
        fetchCommentList(productId, 1, 3),
        fetchRelatedProducts(productId, 8)
      ])
      commentSummary.value = summary
      comments.value = commentPageData.rows || []
      commentPage.value = 1
      relatedProducts.value = related || []
      await prefetchImages(relatedProducts.value.map((item) => productCover(item)))
    } catch {
      commentSummary.value = null
      comments.value = []
      relatedProducts.value = []
    }

    if (isLogin()) {
      try {
        const status = await fetchCollectStatus(productId)
        collected.value = status.collected
      } catch {
        collected.value = false
      }
    }

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  } finally {

    loading.value = false

  }

}



async function loadMoreComments() {

  commentPage.value += 1

  try {

    const page = await fetchCommentList(productId, commentPage.value, 10)

    comments.value = [...comments.value, ...(page.rows || [])]

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

}



async function toggleCollect() {

  if (!ensureLogin()) return

  try {

    if (collected.value) {

      await removeCollect(productId)

      collected.value = false

      uni.showToast({ title: '已取消收藏' })

    } else {

      await addCollect(productId)

      collected.value = true

      uni.showToast({ title: '收藏成功' })

    }

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

}



async function handleAddCart() {

  if (!ensureLogin()) return

  if (hasMultipleSkus.value && !selectedSku.value?.skuId) {
    openSkuPopup('cart')
    return
  }

  const skuId = selectedSku.value?.skuId

  if (!skuId) {

    uni.showToast({ title: '请选择规格', icon: 'none' })

    return

  }

  try {

    await addToCart(skuId, 1)

    refreshCartBadge()
    uni.showToast({ title: '已加入购物车' })

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

}



function handleBuyNow() {

  if (!ensureLogin()) return

  if (hasMultipleSkus.value && !selectedSku.value?.skuId) {
    openSkuPopup('buy')
    return
  }

  const skuId = selectedSku.value?.skuId

  if (!skuId) {

    uni.showToast({ title: '请选择规格', icon: 'none' })

    return

  }

  uni.navigateTo({ url: `/pages/checkout/index?mode=buy&skuId=${skuId}&quantity=1` })

}

function openSkuPopup(action: 'cart' | 'buy' | null = null) {
  pendingSkuAction.value = action
  skuPopupVisible.value = true
}

function closeSkuPopup() {
  skuPopupVisible.value = false
  pendingSkuAction.value = null
}

function confirmSkuPopup() {
  if (!selectedSku.value?.skuId) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  const action = pendingSkuAction.value
  closeSkuPopup()
  if (action === 'cart') {
    handleAddCart()
  } else if (action === 'buy') {
    handleBuyNow()
  }
}

function goProduct(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

function ensureLogin() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return false
  }
  return true
}

onLoad((query) => {

  productId = Number(query?.id || 0)

  loadDetail()

})



onShareAppMessage(() =>

  buildSharePayload({

    title: detail.value?.spuName || 'StarPivot 好物推荐',

    path: productSharePath(productId),

    imageUrl: detail.value?.pic || images.value[0]

  })

)

</script>



<style scoped lang="scss">

.page {
  min-height: 100vh;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  background: $sp-bg-page;
}

.gallery {
  height: 750rpx;
  background: #fff;
}

.gallery-img {
  width: 100%;
  height: 750rpx;
}

.card {
  margin: 16rpx 24rpx 0;
  padding: 28rpx;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
}

.info {
  margin-top: 16rpx;
}

.title-row {
  line-height: 1.45;
}

.self-tag {
  display: inline-block;
  padding: 2rpx 10rpx;
  margin-right: 10rpx;
  font-size: 22rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
  vertical-align: top;
}

.name {
  display: inline;
  font-size: 34rpx;
  font-weight: 700;
  color: $sp-text;
}

.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 16rpx;
}

.price {
  font-size: 44rpx;
  font-weight: 800;
  color: $sp-accent;
}

.yen {
  font-size: 28rpx;
}

.rating {
  font-size: 24rpx;
  color: #ffb400;
}

.desc {
  display: block;
  margin-top: 20rpx;
  padding-top: 20rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
  line-height: 1.6;
  border-top: 1rpx solid $sp-border;
}

.sku-select-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.sku-selected {
  flex: 1;
  font-size: 26rpx;
  color: $sp-text;
}

.arrow {
  font-size: 32rpx;
  color: $sp-text-muted;
}

.related-scroll {
  white-space: nowrap;
}

.related-row {
  display: inline-flex;
  gap: 16rpx;
}

.related-item {
  display: inline-flex;
  flex-direction: column;
  width: 200rpx;
}

.related-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
}

.related-name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.4;
  color: $sp-text;
  white-space: normal;
}

.related-price {
  margin-top: 6rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: $sp-accent;
}

.sku-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
}

.sku-popup {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-height: 75vh;
  padding: 28rpx 24rpx 0;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  box-sizing: border-box;
  overflow: hidden;
}

.popup-head {
  display: flex;
  gap: 16rpx;
  align-items: flex-start;
  flex-shrink: 0;
}

.popup-thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
  flex-shrink: 0;
}

.popup-meta {
  flex: 1;
  min-width: 0;
}

.popup-price {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: $sp-accent;
}

.popup-selected {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $sp-text-secondary;
}

.popup-close {
  font-size: 36rpx;
  color: $sp-text-muted;
  padding: 8rpx;
}

.popup-label {
  display: block;
  flex-shrink: 0;
  margin: 28rpx 0 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;
}

.sku-scroll {
  flex: 1;
  min-height: 200rpx;
  max-height: 50vh;
  height: 50vh;
}

.popup-footer {
  flex-shrink: 0;
  padding: 24rpx 0 calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
}

.popup-confirm {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}

.sku-panel {
  margin-top: 16rpx;
}

.panel-label {
  display: block;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding-bottom: 16rpx;
}

.sku-item {
  padding: 14rpx 28rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
  border: 2rpx solid $sp-border;
  border-radius: $sp-radius-sm;
  background: $sp-bg-page;
}

.sku-item.active {
  color: $sp-primary;
  border-color: $sp-primary;
  background: $sp-primary-light;
  font-weight: 600;
}

.panel {
  margin-top: 16rpx;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.panel-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $sp-text;
}

.more {
  font-size: 26rpx;
  color: $sp-primary;
}

.comment-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid $sp-border;

  &:last-child {
    border-bottom: none;
  }
}

.star {
  color: #ddd;
  font-size: 24rpx;
}

.star.on {
  color: #ffb400;
}

.comment-user {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}

.comment-content {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: $sp-text;
  line-height: 1.5;
}

.footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  gap: 12rpx;
  align-items: center;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 80rpx;
  font-size: 36rpx;
  color: $sp-accent;
}

.icon-label {
  font-size: 20rpx;
  color: $sp-text-muted;
}

.btn-cart,
.btn-buy {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: $sp-radius-pill;
  font-size: 28rpx;
  font-weight: 600;
  border: none;

  &::after {
    border: none;
  }
}

.btn-cart {
  background: linear-gradient(135deg, #ff9500 0%, #ffb800 100%);
  color: #fff;
}

.btn-buy {
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(225, 37, 27, 0.25);
}

.hint,
.hint-inline {
  padding: 24rpx;
  text-align: center;
  color: $sp-text-muted;
  font-size: 26rpx;
}
</style>


<template>

  <view class="page">

    <view v-if="loading" class="hint">加载中...</view>

    <template v-else-if="detail">

      <swiper v-if="images.length" class="gallery" circular indicator-dots>

        <swiper-item v-for="(img, idx) in images" :key="idx">

          <image class="gallery-img" :src="imageSrc(img)" mode="aspectFill" />

        </swiper-item>

      </swiper>

      <view class="info">

        <text class="name">{{ detail.spuName }}</text>

        <text class="price">¥{{ selectedSku?.price ?? detail.price }}</text>

        <text v-if="commentSummary?.total" class="rating">

          {{ commentSummary.avgStar }}分 · {{ commentSummary.total }}条评价

        </text>

        <text class="desc">{{ detail.spuDescription }}</text>

      </view>

      <view v-if="detail.skus?.length" class="sku-list">

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



      <view class="comments panel">

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

    </template>

  </view>

</template>



<script setup lang="ts">

import {onLoad, onShareAppMessage} from '@dcloudio/uni-app'

import {computed, ref} from 'vue'

import {addCollect, fetchCollectStatus, removeCollect} from '@/api/collect'

import {fetchCommentList, fetchCommentSummary} from '@/api/comment'

import {addToCart} from '@/api/cart'

import {fetchProductDetail} from '@/api/product'

import type {PortalComment, PortalCommentSummary, PortalProductDetail} from '@/api/types'

import {isLogin} from '@/stores/member'
import {buildSharePayload, productSharePath} from '@/utils/share'
import {addBrowseRecord} from '@/utils/browse-history'
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

let productId = 0

const { imageSrc, prefetchImages } = useGoodsImages()



const images = computed(() => {

  if (!detail.value) return []

  if (detail.value.images?.length) return detail.value.images

  return detail.value.pic ? [detail.value.pic] : []

})



async function loadDetail() {

  if (!productId) return

  loading.value = true

  try {

    const product = await fetchProductDetail(productId)

    detail.value = product

    selectedSku.value = product.skus?.[0] || null

    addBrowseRecord({
      spuId: productId,
      spuName: product.spuName,
      coverImg: product.coverImg || product.pic,
      price: product.price != null ? Number(product.price) : undefined
    })

    await prefetchImages(images.value)

    try {
      const [summary, commentPageData] = await Promise.all([
        fetchCommentSummary(productId),
        fetchCommentList(productId, 1, 3)
      ])
      commentSummary.value = summary
      comments.value = commentPageData.rows || []
      commentPage.value = 1
    } catch {
      commentSummary.value = null
      comments.value = []
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

  const skuId = selectedSku.value?.skuId

  if (!skuId) {

    uni.showToast({ title: '请选择规格', icon: 'none' })

    return

  }

  try {

    await addToCart(skuId, 1)

    uni.showToast({ title: '已加入购物车' })

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

}



function ensureLogin() {

  if (!isLogin()) {

    uni.navigateTo({ url: '/pages/login/index' })

    return false

  }

  return true

}



function handleBuyNow() {

  if (!ensureLogin()) return

  const skuId = selectedSku.value?.skuId

  if (!skuId) {

    uni.showToast({ title: '请选择规格', icon: 'none' })

    return

  }

  uni.navigateTo({ url: `/pages/checkout/index?mode=buy&skuId=${skuId}&quantity=1` })

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



<style scoped>

.page {

  min-height: 100vh;

  padding-bottom: 140rpx;

  background: #f5f5f5;

}

.gallery {

  height: 750rpx;

  background: #fff;

}

.gallery-img {

  width: 100%;

  height: 750rpx;

}

.info {

  margin-top: 16rpx;

  padding: 24rpx;

  background: #fff;

}

.name {

  display: block;

  font-size: 34rpx;

  font-weight: 600;

}

.price {

  display: block;

  margin-top: 12rpx;

  font-size: 40rpx;

  color: #e64545;

  font-weight: 700;

}

.rating {

  display: block;

  margin-top: 8rpx;

  font-size: 24rpx;

  color: #ffb400;

}

.desc {

  display: block;

  margin-top: 16rpx;

  font-size: 26rpx;

  color: #666;

}

.sku-list {

  display: flex;

  flex-wrap: wrap;

  gap: 16rpx;

  margin-top: 16rpx;

  padding: 24rpx;

  background: #fff;

}

.sku-item {

  padding: 12rpx 24rpx;

  font-size: 26rpx;

  border: 1rpx solid #ddd;

  border-radius: 8rpx;

}

.sku-item.active {

  color: #1677ff;

  border-color: #1677ff;

  background: #e6f4ff;

}

.panel {

  margin-top: 16rpx;

  padding: 24rpx;

  background: #fff;

}

.panel-head {

  display: flex;

  justify-content: space-between;

  margin-bottom: 16rpx;

}

.panel-title {

  font-size: 30rpx;

  font-weight: 600;

}

.more {

  font-size: 26rpx;

  color: #1677ff;

}

.comment-item {

  padding: 16rpx 0;

  border-bottom: 1rpx solid #f0f0f0;

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

  color: #999;

}

.comment-content {

  display: block;

  margin-top: 8rpx;

  font-size: 26rpx;

  color: #333;

}

.footer {

  position: fixed;

  right: 0;

  bottom: 0;

  left: 0;

  display: flex;

  gap: 12rpx;

  align-items: center;

  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));

  background: #fff;

  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);

}

.icon-btn {

  display: flex;

  flex-direction: column;

  align-items: center;

  width: 80rpx;

  font-size: 32rpx;

  color: #e64545;

}

.icon-label {

  font-size: 20rpx;

  color: #666;

}

.btn-cart,

.btn-buy {

  flex: 1;

  border-radius: 999rpx;

  font-size: 28rpx;

}

.btn-cart {

  background: #fff;

  color: #1677ff;

  border: 2rpx solid #1677ff;

}

.btn-buy {

  background: #1677ff;

  color: #fff;

}

.hint,

.hint-inline {

  padding: 24rpx;

  text-align: center;

  color: #999;

  font-size: 26rpx;

}

</style>


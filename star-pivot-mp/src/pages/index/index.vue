<template>

  <view class="page">

    <view class="search-entry" @click="goSearch('')">

      <text class="search-placeholder">搜索商品</text>

    </view>



    <swiper v-if="banners.length" class="banner" circular autoplay indicator-dots>

      <swiper-item v-for="item in banners" :key="item.id">

        <image class="banner-img" :src="imageSrc(item.pic)" mode="aspectFill" @click="onBannerTap(item)" />

      </swiper-item>

    </swiper>



    <view class="quick-nav">

      <view class="nav-item" @click="goSeckill">

        <text class="nav-icon">⚡</text>

        <text>限时秒杀</text>

      </view>

      <view class="nav-item" @click="goCoupons">

        <text class="nav-icon">🎫</text>

        <text>领券中心</text>

      </view>

      <view class="nav-item" @click="goSearch('')">

        <text class="nav-icon">🔍</text>

        <text>全部商品</text>

      </view>

    </view>



    <view v-if="homeBlocks.length" class="section">

      <view class="section-title">精选活动</view>

      <view class="block-grid">

        <view

          v-for="(block, idx) in homeBlocks"

          :key="idx"

          class="block-item"

          @click="onBlockTap(block)"

        >

          <image v-if="block.coverImg" class="block-img" :src="imageSrc(block.coverImg)" mode="aspectFill" />

          <view class="block-text">

            <text class="block-title">{{ block.title }}</text>

            <text class="block-sub">{{ block.subTitle }}</text>

          </view>

        </view>

      </view>

    </view>



    <view class="section">

      <view class="section-title">热门分类</view>

      <view class="category-grid">

        <view

          v-for="cat in topCategories"

          :key="cat.catId"

          class="category-item"

          @click="goSearch('', cat.catId)"

        >

          <text>{{ cat.name }}</text>

        </view>

      </view>

    </view>



    <view class="section">

      <view class="section-title">推荐商品</view>

      <view v-if="loading" class="hint">加载中...</view>

      <view v-for="item in products" :key="item.id" class="product-card" @click="goDetail(item.id)">

        <image class="product-pic" :src="imageSrc(cover(item))" mode="aspectFill" />

        <view class="product-info">

          <text class="product-name">{{ item.spuName }}</text>

          <text class="product-price">¥{{ item.price }}</text>

        </view>

      </view>

    </view>

  </view>

</template>



<script setup lang="ts">

import {onShow} from '@dcloudio/uni-app'

import {ref} from 'vue'

import {fetchHome} from '@/api/home'

import {productCover, searchProducts} from '@/api/product'
import {useGoodsImages} from '@/composables/use-goods-images'

import type {PortalBanner, PortalCategory, PortalHomeBlock, PortalProductListItem} from '@/api/types'

import {isLogin} from '@/stores/member'


const banners = ref<PortalBanner[]>([])

const topCategories = ref<PortalCategory[]>([])

const homeBlocks = ref<PortalHomeBlock[]>([])

const products = ref<PortalProductListItem[]>([])

const loading = ref(false)

const { imageSrc, prefetchImages } = useGoodsImages()



function cover(item: PortalProductListItem) {

  return productCover(item)

}



async function loadData() {

  loading.value = true

  try {

    const home = await fetchHome()

    banners.value = home.banners || []

    topCategories.value = (home.categories || []).slice(0, 8)

    homeBlocks.value = (home.homeBlocks || []).slice(0, 4)

    const page = await searchProducts({ pageNum: 1, pageSize: 10 })

    products.value = page.rows || []
    await prefetchImages(
      banners.value.map((b) => b.pic),
      homeBlocks.value.map((b) => b.coverImg),
      products.value.map((p) => cover(p))
    )

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  } finally {

    loading.value = false

  }

}



function goDetail(id?: number) {

  if (!id) return

  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })

}



function goSearch(keyword: string, catalogId?: number) {

  let url = `/pages/search/index?keyword=${encodeURIComponent(keyword)}`

  if (catalogId) url += `&catalogId=${catalogId}`

  uni.navigateTo({ url })

}



function goSeckill() {

  uni.navigateTo({ url: '/pages/seckill/index' })

}



function goCoupons() {

  if (!isLogin()) {

    uni.navigateTo({ url: '/pages/login/index' })

    return

  }

  uni.navigateTo({ url: '/pages/coupons/index' })

}



function onBlockTap(block: PortalHomeBlock) {

  if (block.code === 'seckill') {

    goSeckill()

    return

  }

  if (block.code === 'subject' && block.refId) {

    uni.navigateTo({ url: `/pages/subject/index?id=${block.refId}` })

    return

  }

  if (block.refId) {

    goDetail(block.refId)

    return

  }

  goSearch('')

}



function onBannerTap(item: PortalBanner) {

  if (item.url?.includes('/subject/')) {

    const id = item.url.split('/').pop()

    if (id) {

      uni.navigateTo({ url: `/pages/subject/index?id=${id}` })

      return

    }

  }

  if (item.url) {

    goSearch('')

  }

}



onShow(loadData)

</script>



<style scoped>

.page {

  padding-bottom: 24rpx;

}

.search-entry {

  margin: 16rpx 24rpx 0;

  padding: 20rpx 24rpx;

  background: #fff;

  border-radius: 999rpx;

}

.search-placeholder {

  color: #999;

  font-size: 28rpx;

}

.banner {

  height: 320rpx;

  margin-top: 16rpx;

}

.banner-img {

  width: 100%;

  height: 320rpx;

}

.quick-nav {

  display: flex;

  justify-content: space-around;

  margin: 16rpx 24rpx;

  padding: 24rpx;

  background: #fff;

  border-radius: 16rpx;

}

.nav-item {

  display: flex;

  flex-direction: column;

  align-items: center;

  gap: 8rpx;

  font-size: 24rpx;

  color: #333;

}

.nav-icon {

  font-size: 40rpx;

}

.section {

  margin: 24rpx;

  padding: 24rpx;

  background: #fff;

  border-radius: 16rpx;

}

.section-title {

  margin-bottom: 16rpx;

  font-size: 32rpx;

  font-weight: 600;

}

.block-grid {

  display: flex;

  flex-direction: column;

  gap: 16rpx;

}

.block-item {

  display: flex;

  gap: 16rpx;

  padding: 16rpx;

  background: #f8f9fb;

  border-radius: 12rpx;

}

.block-img {

  width: 120rpx;

  height: 120rpx;

  border-radius: 8rpx;

  background: #eee;

}

.block-text {

  flex: 1;

  display: flex;

  flex-direction: column;

  justify-content: center;

}

.block-title {

  font-size: 28rpx;

  font-weight: 600;

}

.block-sub {

  margin-top: 8rpx;

  font-size: 24rpx;

  color: #999;

}

.category-grid {

  display: flex;

  flex-wrap: wrap;

  gap: 16rpx;

}

.category-item {

  padding: 12rpx 24rpx;

  font-size: 26rpx;

  background: #f5f7fa;

  border-radius: 999rpx;

}

.product-card {

  display: flex;

  gap: 20rpx;

  padding: 20rpx 0;

  border-bottom: 1rpx solid #f0f0f0;

}

.product-pic {

  width: 160rpx;

  height: 160rpx;

  border-radius: 12rpx;

  background: #f5f5f5;

}

.product-info {

  flex: 1;

  display: flex;

  flex-direction: column;

  justify-content: space-between;

}

.product-name {

  font-size: 28rpx;

  color: #333;

}

.product-price {

  font-size: 32rpx;

  font-weight: 600;

  color: #e64545;

}

.hint {

  color: #999;

  font-size: 26rpx;

}

</style>


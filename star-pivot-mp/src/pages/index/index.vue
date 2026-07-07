<template>
  <view class="page">
    <view class="jd-header">
      <view class="header-top">
        <text class="logo">StarPivot</text>
        <text class="slogan">又好又便宜</text>
      </view>
      <view class="search-bar" @click="goSearch('')">
        <text class="search-icon">🔍</text>
        <text class="search-text">搜索商品、品牌</text>
        <view class="search-btn">搜索</view>
      </view>
    </view>

    <view class="main-body">
      <swiper v-if="banners.length" class="banner" circular autoplay indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
        <swiper-item v-for="item in banners" :key="item.id">
          <image lazy-load class="banner-img" :src="imageSrc(item.pic)" mode="aspectFill" @click="onBannerTap(item)" />
        </swiper-item>
      </swiper>

      <view class="quick-nav">
        <view class="nav-item" @click="goSeckill">
          <view class="nav-icon seckill">⚡</view>
          <text class="nav-label">秒杀</text>
        </view>
        <view class="nav-item" @click="goCoupons">
          <view class="nav-icon coupon">券</view>
          <text class="nav-label">领券</text>
        </view>
        <view class="nav-item" @click="goSearch('')">
          <view class="nav-icon mall">店</view>
          <text class="nav-label">全部</text>
        </view>
        <view class="nav-item" @click="goSearch('')">
          <view class="nav-icon new">新</view>
          <text class="nav-label">新品</text>
        </view>
        <view class="nav-item" @click="goCoupons">
          <view class="nav-icon rank">热</view>
          <text class="nav-label">热卖</text>
        </view>
      </view>

      <view v-if="topCategories.length" class="panel">
        <view class="category-grid">
          <view
            v-for="(cat, idx) in topCategories"
            :key="cat.catId"
            class="category-item"
            @click="goSearch('', cat.catId)"
          >
            <view class="cat-icon" :class="'cat-' + (idx % 5)">{{ cat.name?.slice(0, 1) }}</view>
            <text class="cat-name">{{ cat.name }}</text>
          </view>
        </view>
      </view>

      <view v-if="seckillProducts.length || homeBlocks.length" class="panel seckill-panel">
        <view class="panel-bar" @click="goSeckill">
          <text class="bar-title">京东秒杀</text>
          <view class="countdown">
            <text class="cd-label">距结束</text>
            <text class="cd-block">{{ countdown.h }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-block">{{ countdown.m }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-block">{{ countdown.s }}</text>
          </view>
          <text class="bar-more">更多 ›</text>
        </view>
        <scroll-view scroll-x class="seckill-scroll" :show-scrollbar="false">
          <view class="seckill-row">
            <view
              v-for="item in seckillProducts"
              :key="item.skuId"
              class="seckill-item"
              @click="goSeckillProduct(item)"
            >
              <image lazy-load v-if="item.coverImg" class="seckill-img" :src="imageSrc(item.coverImg)" mode="aspectFill" />
              <view v-else class="seckill-img seckill-img-empty">抢</view>
              <text class="seckill-price"><text class="yen">¥</text>{{ item.promoPrice ?? item.price }}</text>
              <text class="seckill-name">{{ item.spuName }}</text>
            </view>
            <template v-if="!seckillProducts.length">
              <view
                v-for="(block, idx) in homeBlocks"
                :key="idx"
                class="seckill-item"
                @click="onBlockTap(block)"
              >
                <image lazy-load v-if="block.coverImg" class="seckill-img" :src="imageSrc(block.coverImg)" mode="aspectFill" />
                <view v-else class="seckill-img seckill-img-empty">抢</view>
                <text class="seckill-name">{{ block.title }}</text>
              </view>
            </template>
          </view>
        </scroll-view>
      </view>

      <view class="panel guess-panel">
        <view class="guess-head">
          <text class="guess-title">猜你喜欢</text>
          <text class="guess-tag">精选好物</text>
        </view>
        <view v-if="loading" class="hint">加载中...</view>
        <view v-else-if="!products.length" class="hint">暂无推荐商品</view>
        <view v-else class="product-grid">
          <view v-for="item in products" :key="item.id" class="product-card" @click="goDetail(item.id)">
            <image lazy-load class="product-pic" :src="imageSrc(cover(item))" mode="aspectFill" />
            <view class="product-info">
              <view class="product-name-wrap">
                <text class="self-tag">自营</text>
                <text class="product-name">{{ item.spuName }}</text>
              </view>
              <view class="product-price-row">
                <text class="product-price"><text class="yen">¥</text>{{ item.price }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onLoad, onPullDownRefresh, onShow, onUnload} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchHome} from '@/api/home'
import {productCover, searchProducts} from '@/api/product'
import {fetchSeckillPage} from '@/api/seckill'
import {useGoodsImages} from '@/composables/use-goods-images'
import type {PortalBanner, PortalCategory, PortalHomeBlock, PortalHomeProduct, PortalProductListItem} from '@/api/types'
import {isLogin} from '@/stores/member'

const HOME_CACHE_TTL_MS = 5 * 60 * 1000

const banners = ref<PortalBanner[]>([])
const topCategories = ref<PortalCategory[]>([])
const homeBlocks = ref<PortalHomeBlock[]>([])
const seckillProducts = ref<PortalHomeProduct[]>([])
const products = ref<PortalProductListItem[]>([])
const loading = ref(false)
const { imageSrc, prefetchImages } = useGoodsImages()
const countdown = ref({ h: '00', m: '00', s: '00' })
let countdownTimer: ReturnType<typeof setInterval> | null = null
let lastFetchAt = 0
let hasLoadedOnce = false

function padTime(n: number) {
  return String(n).padStart(2, '0')
}

function tickCountdown() {
  const now = new Date()
  const next = new Date(now)
  next.setMinutes(0, 0, 0)
  next.setHours(now.getHours() + 1)
  const diff = Math.max(0, next.getTime() - Date.now())
  const totalSeconds = Math.floor(diff / 1000)
  countdown.value = {
    h: padTime(Math.floor(totalSeconds / 3600)),
    m: padTime(Math.floor((totalSeconds % 3600) / 60)),
    s: padTime(totalSeconds % 60)
  }
}

function startCountdown() {
  tickCountdown()
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(tickCountdown, 1000)
}

function cover(item: PortalProductListItem) {
  return productCover(item)
}

async function loadData(force = false) {
  if (!force && hasLoadedOnce && Date.now() - lastFetchAt < HOME_CACHE_TTL_MS) {
    return
  }
  const showLoading = !hasLoadedOnce
  if (showLoading) loading.value = true
  try {
    const [home, seckillResult, page] = await Promise.all([
      fetchHome(),
      fetchSeckillPage().catch(() => null),
      searchProducts({ pageNum: 1, pageSize: 10 })
    ])
    banners.value = home.banners || []
    topCategories.value = (home.categories || []).slice(0, 10)
    homeBlocks.value = (home.homeBlocks || []).slice(0, 6)
    seckillProducts.value = (seckillResult?.products || []).slice(0, 6)
    products.value = page.rows || []
    await prefetchImages(
      banners.value.map((b) => b.pic),
      homeBlocks.value.map((b) => b.coverImg),
      seckillProducts.value.map((p) => p.coverImg),
      products.value.map((p) => cover(p))
    )
    lastFetchAt = Date.now()
    hasLoadedOnce = true
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

function goSeckillProduct(item: PortalHomeProduct) {
  if (item.spuId) {
    uni.navigateTo({ url: `/pages/product/detail?id=${item.spuId}` })
    return
  }
  goSeckill()
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

onLoad(() => {
  loadData(true)
})

onShow(() => {
  startCountdown()
  if (hasLoadedOnce) loadData(false)
})

onPullDownRefresh(async () => {
  await loadData(true)
  uni.stopPullDownRefresh()
})

onUnload(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $sp-bg-page;
}

.jd-header {
  padding: calc(env(safe-area-inset-top) + 16rpx) 24rpx 28rpx;
  background: linear-gradient(180deg, $sp-primary 0%, $sp-primary-dark 100%);
}

.header-top {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.logo {
  font-size: 40rpx;
  font-weight: 800;
  color: #fff;
  font-style: italic;
}

.slogan {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}

.search-bar {
  display: flex;
  align-items: center;
  height: 64rpx;
  padding: 0 8rpx 0 24rpx;
  background: #fff;
  border-radius: $sp-radius-pill;
}

.search-icon {
  font-size: 26rpx;
  margin-right: 8rpx;
}

.search-text {
  flex: 1;
  font-size: 26rpx;
  color: $sp-text-muted;
}

.search-btn {
  padding: 0 28rpx;
  height: 52rpx;
  line-height: 52rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #fff;
  background: $sp-primary;
  border-radius: $sp-radius-pill;
}

.main-body {
  margin-top: -12rpx;
  padding-bottom: 24rpx;
}

.banner {
  height: 280rpx;
  margin: 0 16rpx;
  border-radius: $sp-radius-md;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 280rpx;
}

.quick-nav {
  display: flex;
  justify-content: space-around;
  margin: 16rpx;
  padding: 24rpx 8rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.nav-icon {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 700;
  border-radius: 50%;
  color: #fff;

  &.seckill {
    background: linear-gradient(135deg, #ff6034, $sp-accent);
    font-size: 40rpx;
  }

  &.coupon {
    background: linear-gradient(135deg, #ff9500, #ffb800);
    font-size: 32rpx;
  }

  &.mall {
    background: linear-gradient(135deg, $sp-primary, $sp-primary-dark);
    font-size: 32rpx;
  }

  &.new {
    background: linear-gradient(135deg, #52c41a, #73d13d);
    font-size: 32rpx;
  }

  &.rank {
    background: linear-gradient(135deg, #722ed1, #9254de);
    font-size: 32rpx;
  }
}

.nav-label {
  font-size: 24rpx;
  color: $sp-text-secondary;
}

.panel {
  margin: 16rpx;
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx 8rpx 8rpx;
}

.category-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 0;
}

.cat-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  border-radius: 50%;

  &.cat-0 { background: #ff7875; }
  &.cat-1 { background: #ffc069; }
  &.cat-2 { background: #95de64; }
  &.cat-3 { background: #69c0ff; }
  &.cat-4 { background: #b37feb; }
}

.cat-name {
  font-size: 22rpx;
  color: $sp-text-secondary;
  max-width: 100rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.seckill-panel {
  padding-bottom: 20rpx;
}

.panel-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 24rpx 16rpx;
  background: linear-gradient(90deg, $sp-primary-light 0%, #fff 100%);
}

.bar-title {
  font-size: 32rpx;
  font-weight: 800;
  color: $sp-primary;
  font-style: italic;
}

.bar-sub {
  flex: 1;
  font-size: 22rpx;
  color: $sp-text-muted;
}

.bar-more {
  font-size: 24rpx;
  color: $sp-text-muted;
}

.countdown {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-left: 8rpx;
}

.cd-label {
  font-size: 22rpx;
  color: $sp-text-secondary;
  margin-right: 4rpx;
}

.cd-block {
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  padding: 0 6rpx;
  text-align: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #fff;
  background: $sp-primary;
  border-radius: 6rpx;
}

.cd-sep {
  font-size: 22rpx;
  font-weight: 700;
  color: $sp-primary;
}

.seckill-scroll {
  white-space: nowrap;
}

.seckill-row {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 16rpx;
}

.seckill-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 160rpx;
}

.seckill-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
}

.seckill-img-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 700;
  color: $sp-primary;
  background: $sp-primary-light;
}

.seckill-name {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $sp-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 160rpx;
}

.seckill-price {
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: $sp-accent;
}

.seckill-price .yen {
  font-size: 20rpx;
}

.guess-panel {
  padding-bottom: 16rpx;
}

.guess-head {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 24rpx 0 16rpx;
}

.guess-title {
  font-size: 32rpx;
  font-weight: 800;
  color: $sp-text;
}

.guess-tag {
  padding: 4rpx 12rpx;
  font-size: 20rpx;
  color: $sp-primary;
  border: 1rpx solid $sp-primary;
  border-radius: 4rpx;
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 0 8rpx;
}

.product-card {
  width: 50%;
  padding: 8rpx;
  box-sizing: border-box;
}

.product-pic {
  width: 100%;
  height: 340rpx;
  background: #fafafa;
  border-radius: $sp-radius-sm $sp-radius-sm 0 0;
}

.product-info {
  padding: 12rpx 16rpx 16rpx;
  background: #fff;
}

.product-name-wrap {
  min-height: 72rpx;
}

.self-tag {
  display: inline-block;
  padding: 2rpx 8rpx;
  margin-right: 8rpx;
  font-size: 20rpx;
  line-height: 1.4;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
  vertical-align: top;
}

.product-name {
  display: inline;
  font-size: 26rpx;
  line-height: 1.4;
  color: $sp-text;
}

.product-price-row {
  margin-top: 8rpx;
}

.product-price {
  font-size: 36rpx;
  font-weight: 800;
  color: $sp-accent;
  font-family: 'DIN Alternate', sans-serif;
}

.yen {
  font-size: 24rpx;
  margin-right: 2rpx;
}

.hint {
  padding: 48rpx;
  text-align: center;
  color: $sp-text-muted;
  font-size: 26rpx;
}
</style>

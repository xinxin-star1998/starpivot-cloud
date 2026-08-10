<template>
  <view class="page">
    <view class="top-bar" :style="topBarStyle">
      <view class="top-row" :style="topRowStyle">
        <text class="brand">StarPivot</text>
        <view class="search-bar" @click="goSearch('')">
          <text class="search-text">搜索商品、品牌</text>
          <view class="search-btn">搜索</view>
        </view>
      </view>
    </view>

    <view class="main-body">
      <swiper
        v-if="banners.length"
        class="banner"
        circular
        autoplay
        indicator-dots
        indicator-color="rgba(255,255,255,0.45)"
        indicator-active-color="#fff"
      >
        <swiper-item v-for="item in banners" :key="item.id">
          <image
            lazy-load
            class="banner-img"
            :src="imageSrc(item.pic)"
            mode="aspectFill"
            @click="onBannerTap(item)"
          />
        </swiper-item>
      </swiper>

      <view v-if="categories.length" class="category-panel">
        <view class="category-tabs-wrap">
          <scroll-view scroll-x class="category-tabs" :show-scrollbar="false" enable-flex>
            <view class="category-tabs-inner">
              <view
                v-for="cat in categories"
                :key="cat.catId"
                class="category-tab"
                :class="{ active: cat.catId === activeCatId }"
                @click="selectCategory(cat)"
              >
                <text class="category-tab-text">{{ shortName(cat.name) }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
        <view class="category-grid">
          <view
            v-for="(item, idx) in gridItems"
            :key="item.key"
            class="category-item"
            @click="onGridItemTap(item)"
          >
            <view
              class="category-icon-wrap"
              :class="[`tone-${idx % 5}`, { 'is-brand': item.brandId != null }]"
            >
              <image
                v-if="item.icon"
                lazy-load
                class="category-icon"
                :src="imageSrc(item.icon)"
                mode="aspectFit"
              />
              <text v-else class="category-icon-fallback">{{ item.name.slice(0, 1) }}</text>
            </view>
            <text class="category-item-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <view v-if="seckillProducts.length" class="panel seckill-panel">
        <view class="panel-head" @click="goSeckill">
          <text class="panel-title">限时秒杀</text>
          <view class="countdown">
            <text class="cd-label">距结束</text>
            <text class="cd-block">{{ countdown.h }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-block">{{ countdown.m }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-block">{{ countdown.s }}</text>
          </view>
          <text class="panel-more">更多 ›</text>
        </view>
        <scroll-view scroll-x class="seckill-scroll" :show-scrollbar="false">
          <view class="seckill-row">
            <view
              v-for="item in seckillProducts"
              :key="item.skuId"
              class="seckill-item"
              @click="goSeckillProduct(item)"
            >
              <image
                lazy-load
                v-if="item.coverImg"
                class="seckill-img"
                :src="imageSrc(item.coverImg)"
                mode="aspectFill"
              />
              <view v-else class="seckill-img seckill-img-empty">秒</view>
              <text class="seckill-price">
                <text class="yen">¥</text>{{ formatMoney(item.promoPrice ?? item.price, '0.00') }}
              </text>
              <text class="seckill-name">{{ item.spuName }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="guess-block">
        <view class="guess-head">
          <text class="guess-title">猜你喜欢</text>
          <text class="guess-sub">精选推荐</text>
        </view>
        <view v-if="loading" class="hint">加载中...</view>
        <view v-else-if="!products.length" class="hint">暂无推荐商品</view>
        <view v-else class="product-grid">
          <view
            v-for="item in products"
            :key="item.id"
            class="product-card"
            @click="goDetail(item.id)"
          >
            <image lazy-load class="product-pic" :src="imageSrc(cover(item))" mode="aspectFill" />
            <view class="product-info">
              <text class="product-name">
                <text class="self-tag">自营</text>{{ item.spuName }}
              </text>
              <view class="price-row">
                <text class="product-price">
                  <text class="yen">¥</text>{{ formatMoney(item.price, '0.00') }}
                </text>
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
import {computed, ref} from 'vue'
import {fetchHome} from '@/api/home'
import {productCover, searchProducts} from '@/api/product'
import {fetchSeckillPage} from '@/api/seckill'
import {useGoodsImages} from '@/composables/use-goods-images'
import type {
  PortalBanner,
  PortalBrandBrief,
  PortalCategory,
  PortalHomeProduct,
  PortalProductListItem
} from '@/api/types'
import {formatMoney} from '@/utils/money'
import {getCustomNavMetrics} from '@/utils/nav-bar'

const HOME_CACHE_TTL_MS = 5 * 60 * 1000

const SHORT_NAME_MAP: Record<string, string> = {
  '图书、音像、电子书': '图书',
  家用电器: '电器',
  家居家装: '家居',
  电脑办公: '电脑办公',
  个护化妆: '美妆',
  母婴玩具: '母婴',
  食品饮料: '食品饮料',
  运动户外: '运动',
  汽车用品: '汽车'
}

interface CategoryGridItem {
  key: string
  name: string
  icon?: string
  catId?: number
  brandId?: number
}

const banners = ref<PortalBanner[]>([])
const categories = ref<PortalCategory[]>([])
const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
const activeCatId = ref<number>()
const seckillProducts = ref<PortalHomeProduct[]>([])
const products = ref<PortalProductListItem[]>([])
const loading = ref(false)
const { imageSrc, prefetchImages } = useGoodsImages()
const countdown = ref({ h: '00', m: '00', s: '00' })
const navMetrics = ref(getCustomNavMetrics())
const topBarStyle = computed(() => ({
  paddingTop: `${navMetrics.value.statusBarHeight + navMetrics.value.contentTopGap}px`,
  paddingBottom: `${Math.max(8, navMetrics.value.contentTopGap)}px`,
  paddingRight: `${navMetrics.value.menuRightGap}px`
}))
const topRowStyle = computed(() => ({
  height: `${navMetrics.value.contentHeight}px`
}))

const activeCategory = computed(() =>
  categories.value.find((c) => c.catId === activeCatId.value)
)

const gridItems = computed((): CategoryGridItem[] => {
  const cat = activeCategory.value
  if (!cat) return []
  const max = 10
  const items: CategoryGridItem[] = (cat.children || []).slice(0, max).map((c) => ({
    key: `c-${c.catId}`,
    name: shortName(c.name),
    icon: c.icon,
    catId: c.catId
  }))
  if (items.length < max) {
    const brands = cat.catId != null ? categoryBrands.value[cat.catId] || [] : []
    for (const b of brands) {
      if (items.length >= max) break
      items.push({
        key: `b-${b.brandId}`,
        name: (b.name || '品牌').slice(0, 4),
        icon: b.logo,
        brandId: b.brandId,
        catId: cat.catId
      })
    }
  }
  if (items.length) return items
  return [
    {
      key: `c-${cat.catId}`,
      name: shortName(cat.name),
      icon: cat.icon,
      catId: cat.catId
    }
  ]
})

let countdownTimer: ReturnType<typeof setInterval> | null = null
let lastFetchAt = 0
let hasLoadedOnce = false

function shortName(name?: string) {
  if (!name) return '分类'
  if (SHORT_NAME_MAP[name]) return SHORT_NAME_MAP[name]
  const cleaned = name.replace(/[、，,]/g, '').trim()
  return cleaned.length > 5 ? cleaned.slice(0, 5) : cleaned
}

function selectCategory(cat: PortalCategory) {
  if (cat.catId == null) return
  activeCatId.value = cat.catId
}

function onGridItemTap(item: CategoryGridItem) {
  goSearch('', item.catId, item.brandId)
}

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
    categories.value = home.categories || []
    categoryBrands.value = home.categoryBrands || {}
    if (
      activeCatId.value == null ||
      !categories.value.some((c) => c.catId === activeCatId.value)
    ) {
      activeCatId.value = categories.value[0]?.catId
    }
    seckillProducts.value = (seckillResult?.products || []).slice(0, 6)
    products.value = page.rows || []
    const categoryIcons = categories.value.flatMap((c) => [
      c.icon,
      ...(c.children || []).map((child) => child.icon)
    ])
    const brandLogos = Object.values(categoryBrands.value)
      .flat()
      .map((b) => b.logo)
    await prefetchImages(
      banners.value.map((b) => b.pic),
      categoryIcons,
      brandLogos,
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

function goSearch(keyword: string, catalogId?: number, brandId?: number) {
  let url = `/pages/search/index?keyword=${encodeURIComponent(keyword)}`
  if (catalogId) url += `&catalogId=${catalogId}`
  if (brandId) url += `&brandId=${brandId}`
  uni.navigateTo({ url })
}

function goSeckill() {
  uni.navigateTo({ url: '/pages/seckill/index' })
}

function goSeckillProduct(item: PortalHomeProduct) {
  if (item.spuId) {
    uni.navigateTo({ url: `/pages/product/detail?id=${item.spuId}` })
    return
  }
  goSeckill()
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
  navMetrics.value = getCustomNavMetrics()
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

.top-bar {
  padding-left: 24rpx;
  background: linear-gradient(180deg, $sp-primary 0%, $sp-primary-dark 92%);
  box-sizing: border-box;
}

.top-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.brand {
  flex-shrink: 0;
  font-size: 28rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5rpx;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
  height: 100%;
  padding: 0 6rpx 0 20rpx;
  background: #fff;
  border-radius: $sp-radius-pill;
  box-sizing: border-box;
}

.search-text {
  flex: 1;
  min-width: 0;
  font-size: 24rpx;
  color: $sp-text-muted;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-btn {
  flex-shrink: 0;
  align-self: center;
  padding: 0 22rpx;
  height: 80%;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  font-weight: 600;
  color: #fff;
  background: $sp-primary;
  border-radius: $sp-radius-pill;
}

.main-body {
  margin-top: -8rpx;
  padding: 0 0 32rpx;
}

.banner {
  height: 320rpx;
  margin: 0 24rpx;
  border-radius: $sp-radius-lg;
  overflow: hidden;
  background: #e8e8e8;
  box-shadow: $sp-shadow-sm;
}

.banner-img {
  width: 100%;
  height: 320rpx;
}

.category-panel {
  margin: 20rpx 24rpx 0;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
  overflow: hidden;
}

.category-tabs-wrap {
  background: linear-gradient(180deg, #fff8f7 0%, #fff 100%);
  border-bottom: 1rpx solid #f5f5f5;
}

.category-tabs {
  width: 100%;
  white-space: nowrap;
}

.category-tabs-inner {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 16rpx 0;
  min-width: 100%;
}

.category-tab {
  position: relative;
  flex-shrink: 0;
  padding: 18rpx 20rpx 22rpx;
}

.category-tab-text {
  font-size: 26rpx;
  font-weight: 500;
  line-height: 1.2;
  color: $sp-text-secondary;
  transition: color 0.15s ease;
}

.category-tab.active .category-tab-text {
  font-size: 28rpx;
  font-weight: 700;
  color: $sp-primary;
}

.category-tab.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 10rpx;
  width: 36rpx;
  height: 6rpx;
  margin-left: -18rpx;
  background: linear-gradient(90deg, $sp-accent 0%, $sp-primary 100%);
  border-radius: $sp-radius-pill;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx 8rpx 24rpx;
}

.category-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 0 10rpx;
}

.category-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.7);
}

.category-icon-wrap.tone-0 {
  background: linear-gradient(145deg, #fff0f0 0%, #ffe4e4 100%);
}

.category-icon-wrap.tone-1 {
  background: linear-gradient(145deg, #fff7eb 0%, #ffefd6 100%);
}

.category-icon-wrap.tone-2 {
  background: linear-gradient(145deg, #eef7ff 0%, #ddefff 100%);
}

.category-icon-wrap.tone-3 {
  background: linear-gradient(145deg, #f0faf4 0%, #dff3e8 100%);
}

.category-icon-wrap.tone-4 {
  background: linear-gradient(145deg, #f6f0ff 0%, #ebe0ff 100%);
}

.category-icon-wrap.is-brand {
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #f0f0f0;
}

.category-icon {
  width: 64rpx;
  height: 64rpx;
}

.category-icon-fallback {
  font-size: 32rpx;
  font-weight: 700;
  color: $sp-primary;
  opacity: 0.78;
}

.category-icon-wrap.tone-1 .category-icon-fallback {
  color: #d48806;
}

.category-icon-wrap.tone-2 .category-icon-fallback {
  color: #2b6de5;
}

.category-icon-wrap.tone-3 .category-icon-fallback {
  color: #0f9d7a;
}

.category-icon-wrap.tone-4 .category-icon-fallback {
  color: #7a5af8;
}

.category-icon-wrap.is-brand .category-icon-fallback {
  color: $sp-text-secondary;
  opacity: 1;
}

.category-item-name {
  max-width: 128rpx;
  font-size: 22rpx;
  line-height: 1.25;
  color: $sp-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.panel {
  margin: 20rpx 24rpx 0;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 24rpx 16rpx;
}

.panel-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $sp-text;
}

.panel-more {
  font-size: 24rpx;
  color: $sp-text-muted;
}

.countdown {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.cd-label {
  margin-right: 6rpx;
  font-size: 22rpx;
  color: $sp-text-muted;
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
  background: $sp-text;
  border-radius: 6rpx;
}

.cd-sep {
  font-size: 22rpx;
  font-weight: 700;
  color: $sp-text;
}

.seckill-panel {
  padding-bottom: 20rpx;
}

.seckill-scroll {
  white-space: nowrap;
}

.seckill-row {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 24rpx;
}

.seckill-item {
  display: inline-flex;
  flex-direction: column;
  width: 168rpx;
}

.seckill-img {
  width: 168rpx;
  height: 168rpx;
  border-radius: $sp-radius-sm;
  background: #f7f7f7;
}

.seckill-img-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: $sp-primary;
  background: $sp-primary-light;
}

.seckill-price {
  margin-top: 10rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $sp-accent;
}

.seckill-price .yen {
  font-size: 20rpx;
  margin-right: 2rpx;
}

.seckill-name {
  margin-top: 4rpx;
  max-width: 168rpx;
  font-size: 22rpx;
  color: $sp-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.guess-block {
  margin-top: 8rpx;
  padding: 16rpx 24rpx 0;
}

.guess-head {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  padding: 16rpx 0 20rpx;
}

.guess-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $sp-text;
}

.guess-sub {
  font-size: 22rpx;
  color: $sp-text-muted;
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.product-card {
  width: calc(50% - 8rpx);
  overflow: hidden;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
  box-sizing: border-box;
}

.product-pic {
  width: 100%;
  height: 330rpx;
  background: #f7f7f7;
}

.product-info {
  padding: 16rpx 16rpx 20rpx;
}

.product-name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  min-height: 72rpx;
  font-size: 26rpx;
  line-height: 1.4;
  color: $sp-text;
}

.self-tag {
  display: inline-block;
  margin-right: 8rpx;
  padding: 0 8rpx;
  font-size: 18rpx;
  line-height: 1.5;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: 4rpx;
  vertical-align: middle;
}

.price-row {
  margin-top: 12rpx;
}

.product-price {
  font-size: 34rpx;
  font-weight: 700;
  color: $sp-accent;
  font-family: 'DIN Alternate', sans-serif;
}

.yen {
  font-size: 22rpx;
  margin-right: 2rpx;
}

.hint {
  padding: 48rpx;
  text-align: center;
  color: $sp-text-muted;
  font-size: 26rpx;
}
</style>

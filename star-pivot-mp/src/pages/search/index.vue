<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">搜</text>
        <input
          v-model="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索商品"
          @confirm="doSearch"
          @focus="showHints = true"
        />
        <text v-if="keyword" class="clear-btn" @click="clearKeyword">✕</text>
      </view>
      <button size="mini" class="search-btn" @click="doSearch">搜索</button>
    </view>

    <view v-if="showHints && !hasActiveFilter" class="hints">
      <view v-if="history.length" class="hint-block">
        <view class="hint-head">
          <text class="hint-title">搜索历史</text>
          <text class="hint-action" @click="clearHistory">清空</text>
        </view>
        <view class="hint-tags">
          <text
            v-for="item in history"
            :key="item"
            class="hint-tag"
            @click="applyKeyword(item)"
          >{{ item }}</text>
        </view>
      </view>
      <view class="hint-block">
        <view class="hint-head">
          <text class="hint-title">热门搜索</text>
        </view>
        <view class="hint-tags">
          <text
            v-for="item in hotKeywords"
            :key="item"
            class="hint-tag hot"
            @click="applyKeyword(item)"
          >{{ item }}</text>
        </view>
      </view>
    </view>

    <view class="toolbar">
      <picker :range="sortLabels" @change="onSortChange">
        <view class="sort">{{ sortLabels[sortIndex] }} ▾</view>
      </picker>
      <view class="filters">
        <text v-if="catalogId" class="filter-tag" @click="clearCatalog">✕ 清除分类</text>
        <text v-if="brandId" class="filter-tag" @click="clearBrand">✕ {{ brandLabel }}</text>
      </view>
    </view>

    <scroll-view v-if="brandOptions.length" scroll-x class="brand-bar" :show-scrollbar="false">
      <view
        class="brand-chip"
        :class="{ active: !brandId }"
        @click="selectBrand(undefined)"
      >全部品牌</view>
      <view
        v-for="brand in brandOptions"
        :key="brand.brandId"
        class="brand-chip"
        :class="{ active: brandId === brand.brandId }"
        @click="selectBrand(brand.brandId)"
      >{{ brand.name }}</view>
    </scroll-view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!products.length" class="empty-wrap">
      <view class="empty-state">
        <view class="empty-icon">空</view>
        <text class="empty-title">未找到相关商品</text>
        <text class="empty-desc">换个关键词或筛选条件试试</text>
      </view>
    </view>
    <view v-else class="list">
      <view class="product-grid">
        <view v-for="item in products" :key="item.id" class="product-card" @click="goDetail(item.id)">
          <image class="pic" :src="imageSrc(cover(item))" mode="aspectFill" />
          <view class="info">
            <view class="name-wrap">
              <text class="self-tag">自营</text>
              <text class="name">{{ item.spuName }}</text>
            </view>
            <text v-if="item.brandName" class="brand">{{ item.brandName }}</text>
            <view class="bottom">
              <text v-if="item.avgStar" class="rating">★ {{ item.avgStar }}</text>
              <text class="price"><text class="yen">¥</text>{{ formatMoney(item.price, '0.00') }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-if="hasMore" class="load-more">
        <button size="mini" class="load-btn" :loading="loadingMore" @click="loadMore">加载更多</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchHome} from '@/api/home'
import {productCover, searchProducts} from '@/api/product'
import type {PortalBrandBrief, PortalProductListItem} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {formatMoney} from '@/utils/money'
import {addSearchKeyword, clearSearchHistory, getSearchHistory, HOT_SEARCH_KEYWORDS} from '@/utils/search-history'

const keyword = ref('')
const catalogId = ref<number>()
const brandId = ref<number>()
const sortKeys = ['default', 'priceAsc', 'priceDesc', 'newest']
const sortLabels = ['默认排序', '价格升序', '价格降序', '最新上架']
const sortIndex = ref(0)

const products = ref<PortalProductListItem[]>([])
const pageNum = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const showHints = ref(true)
const history = ref<string[]>([])
const hotKeywords = HOT_SEARCH_KEYWORDS
const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
const brandNameMap = ref<Record<number, string>>({})

const { imageSrc, prefetchImages } = useGoodsImages()

const hasMore = computed(() => products.value.length < total.value)
const hasActiveFilter = computed(
  () => !!(keyword.value.trim() || catalogId.value != null || brandId.value != null)
)
const brandLabel = computed(() => {
  if (brandId.value == null) return '品牌'
  return brandNameMap.value[brandId.value] || '已选品牌'
})
const brandOptions = computed(() => {
  if (catalogId.value != null) {
    const list = categoryBrands.value[catalogId.value]
    if (list?.length) return list
  }
  const map = new Map<number, PortalBrandBrief>()
  for (const brands of Object.values(categoryBrands.value)) {
    for (const brand of brands || []) {
      if (brand.brandId != null) map.set(brand.brandId, brand)
    }
  }
  return [...map.values()]
})

function cover(item: PortalProductListItem) {
  return productCover(item)
}

function refreshHistory() {
  history.value = getSearchHistory()
}

async function loadBrands() {
  try {
    const home = await fetchHome()
    categoryBrands.value = home.categoryBrands || {}
    const map: Record<number, string> = {}
    for (const brands of Object.values(categoryBrands.value)) {
      for (const brand of brands || []) {
        if (brand.brandId != null && brand.name) map[brand.brandId] = brand.name
      }
    }
    brandNameMap.value = map
  } catch {
    categoryBrands.value = {}
  }
}

async function load(reset = false) {
  if (reset) {
    pageNum.value = 1
    products.value = []
  }
  const isFirst = pageNum.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true
  try {
    const page = await searchProducts({
      pageNum: pageNum.value,
      pageSize: 10,
      keyword: keyword.value || undefined,
      catalogId: catalogId.value,
      brandId: brandId.value,
      sort: sortKeys[sortIndex.value]
    })
    const rows = page.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    total.value = page.total || 0
    showHints.value = !hasActiveFilter.value && !products.value.length
    await prefetchImages(products.value.map((p) => cover(p)))
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function doSearch() {
  if (keyword.value.trim()) {
    addSearchKeyword(keyword.value)
    refreshHistory()
  }
  showHints.value = false
  load(true)
}

function applyKeyword(text: string) {
  keyword.value = text
  doSearch()
}

function clearKeyword() {
  keyword.value = ''
  showHints.value = true
}

function clearHistory() {
  clearSearchHistory()
  refreshHistory()
}

function onSortChange(e: { detail: { value: string } }) {
  sortIndex.value = Number(e.detail.value)
  load(true)
}

function clearCatalog() {
  catalogId.value = undefined
  load(true)
}

function clearBrand() {
  brandId.value = undefined
  load(true)
}

function selectBrand(id?: number) {
  brandId.value = id
  showHints.value = false
  load(true)
}

function loadMore() {
  pageNum.value += 1
  load(false)
}

function goDetail(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

onLoad((query) => {
  refreshHistory()
  loadBrands()
  if (query?.keyword) keyword.value = decodeURIComponent(String(query.keyword))
  if (query?.catalogId) catalogId.value = Number(query.catalogId)
  if (query?.brandId) brandId.value = Number(query.brandId)
  if (hasActiveFilter.value) {
    showHints.value = false
    load(true)
  } else {
    showHints.value = true
  }
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $sp-bg-page;
}

.search-bar {
  display: flex;
  gap: 16rpx;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  box-shadow: $sp-shadow-sm;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 24rpx;
  background: $sp-bg-page;
  border-radius: $sp-radius-pill;
}

.search-icon {
  font-size: 22rpx;
  font-weight: 600;
  color: $sp-text-muted;
}

.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
}

.clear-btn {
  font-size: 24rpx;
  color: $sp-text-muted;
  padding: 8rpx;
}

.search-btn {
  margin: 0;
  background: $sp-primary;
  color: #fff;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}

.hints {
  padding: 24rpx;
  background: #fff;
}

.hint-block + .hint-block {
  margin-top: 28rpx;
}

.hint-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.hint-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;
}

.hint-action {
  font-size: 24rpx;
  color: $sp-text-muted;
}

.hint-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.hint-tag {
  padding: 10rpx 24rpx;
  font-size: 24rpx;
  color: $sp-text-secondary;
  background: $sp-bg-page;
  border-radius: $sp-radius-pill;
}

.hint-tag.hot {
  color: $sp-primary;
  background: $sp-primary-light;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #fff;
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  justify-content: flex-end;
}

.filter-tag {
  padding: 6rpx 16rpx;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: $sp-radius-pill;
  font-size: 24rpx;
}

.brand-bar {
  white-space: nowrap;
  padding: 0 16rpx 16rpx;
  background: #fff;
}

.brand-chip {
  display: inline-block;
  margin-right: 12rpx;
  padding: 10rpx 22rpx;
  font-size: 24rpx;
  color: $sp-text-secondary;
  background: $sp-bg-page;
  border-radius: $sp-radius-pill;
}

.brand-chip.active {
  color: #fff;
  background: $sp-primary;
  font-weight: 600;
}

.list {
  padding: 16rpx 24rpx;
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.product-card {
  width: calc(50% - 10rpx);
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;
  box-shadow: $sp-shadow-sm;
}

.pic {
  width: 100%;
  height: 320rpx;
  background: #f8f8f8;
}

.info {
  padding: 16rpx 20rpx 20rpx;
}

.name-wrap {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  min-height: 72rpx;
  line-height: 1.4;
}

.self-tag {
  display: inline-block;
  padding: 0 8rpx;
  margin-right: 8rpx;
  font-size: 18rpx;
  line-height: 1.5;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: 4rpx;
  vertical-align: middle;
}

.name {
  display: inline;
  font-size: 26rpx;
  color: $sp-text;
}

.brand {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $sp-text-muted;
}

.bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12rpx;
}

.rating {
  font-size: 22rpx;
  color: #ffb400;
}

.price {
  font-size: 32rpx;
  font-weight: 700;
  color: $sp-accent;
}

.yen {
  font-size: 22rpx;
}

.hint,
.empty-wrap {
  padding: 48rpx 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx;
}

.empty-icon {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 600;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: 28rpx;
}

.empty-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;
}

.empty-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}

.load-more {
  padding: 32rpx;
  text-align: center;
}

.load-btn {
  background: #fff;
  color: $sp-primary;
  border: 1rpx solid $sp-primary;
  border-radius: $sp-radius-pill;
}
</style>

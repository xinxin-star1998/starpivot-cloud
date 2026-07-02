<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索商品"
          @confirm="doSearch"
        />
      </view>
      <button size="mini" class="search-btn" @click="doSearch">搜索</button>
    </view>

    <view class="toolbar">
      <picker :range="sortLabels" @change="onSortChange">
        <view class="sort">{{ sortLabels[sortIndex] }} ▾</view>
      </picker>
      <text v-if="catalogId" class="filter-tag" @click="clearCatalog">✕ 清除分类</text>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!products.length" class="empty-wrap">
      <view class="empty-state">
        <view class="empty-icon">🔍</view>
        <text class="empty-title">未找到相关商品</text>
        <text class="empty-desc">换个关键词试试吧</text>
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
              <text class="price"><text class="yen">¥</text>{{ item.price }}</text>
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
import {productCover, searchProducts} from '@/api/product'
import {useGoodsImages} from '@/composables/use-goods-images'
import type {PortalProductListItem} from '@/api/types'

const keyword = ref('')
const catalogId = ref<number>()
const sortKeys = ['default', 'priceAsc', 'priceDesc', 'newest']
const sortLabels = ['默认排序', '价格升序', '价格降序', '最新上架']
const sortIndex = ref(0)

const products = ref<PortalProductListItem[]>([])
const pageNum = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)

const { imageSrc, prefetchImages } = useGoodsImages()

const hasMore = computed(() => products.value.length < total.value)

function cover(item: PortalProductListItem) {
  return productCover(item)
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
      sort: sortKeys[sortIndex.value]
    })
    const rows = page.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    total.value = page.total || 0
    await prefetchImages(products.value.map((p) => cover(p)))
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function doSearch() {
  load(true)
}

function onSortChange(e: { detail: { value: string } }) {
  sortIndex.value = Number(e.detail.value)
  load(true)
}

function clearCatalog() {
  catalogId.value = undefined
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
  if (query?.keyword) keyword.value = decodeURIComponent(String(query.keyword))
  if (query?.catalogId) catalogId.value = Number(query.catalogId)
  load(true)
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
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
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

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #fff;
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.filter-tag {
  padding: 6rpx 16rpx;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: $sp-radius-pill;
  font-size: 24rpx;
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
  min-height: 72rpx;
  line-height: 1.45;
}

.self-tag {
  display: inline-block;
  padding: 2rpx 8rpx;
  margin-right: 8rpx;
  font-size: 20rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
  vertical-align: top;
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
  width: 140rpx;
  height: 140rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  background: linear-gradient(135deg, #fff5f5 0%, $sp-primary-light 100%);
  border-radius: 50%;
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

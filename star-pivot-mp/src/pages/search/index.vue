<template>
  <view class="page">
    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        confirm-type="search"
        placeholder="搜索商品"
        @confirm="doSearch"
      />
      <button size="mini" class="search-btn" @click="doSearch">搜索</button>
    </view>

    <view class="toolbar">
      <picker :range="sortLabels" @change="onSortChange">
        <view class="sort">{{ sortLabels[sortIndex] }} ▾</view>
      </picker>
      <text v-if="catalogId" class="filter-tag" @click="clearCatalog">清除分类筛选</text>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!products.length" class="hint">未找到相关商品</view>
    <view v-else class="list">
      <view v-for="item in products" :key="item.id" class="product-card" @click="goDetail(item.id)">
        <image class="pic" :src="imageSrc(cover(item))" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ item.spuName }}</text>
          <text v-if="item.brandName" class="brand">{{ item.brandName }}</text>
          <text v-if="item.avgStar" class="rating">{{ item.avgStar }}分 · {{ item.commentCount || 0 }}评</text>
          <text class="price">¥{{ item.price }}</text>
        </view>
      </view>
      <view v-if="hasMore" class="load-more">
        <button size="mini" :loading="loadingMore" @click="loadMore">加载更多</button>
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

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.search-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: #fff;
}
.search-input {
  flex: 1;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 999rpx;
  font-size: 28rpx;
}
.search-btn {
  background: #1677ff;
  color: #fff;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  font-size: 26rpx;
}
.filter-tag {
  color: #1677ff;
}
.list {
  padding: 16rpx;
}
.product-card {
  display: flex;
  gap: 20rpx;
  margin-bottom: 16rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}
.pic {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}
.info {
  flex: 1;
}
.name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
}
.brand,
.rating {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
.price {
  display: block;
  margin-top: 12rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #e64545;
}
.hint,
.load-more {
  padding: 48rpx;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}
</style>

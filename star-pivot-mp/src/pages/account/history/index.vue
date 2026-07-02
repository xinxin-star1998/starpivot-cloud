<template>
  <view class="page">
    <view v-if="items.length" class="toolbar">
      <button class="clear-btn" size="mini" @click="handleClearAll">清空足迹</button>
    </view>

    <view v-if="items.length" class="list">
      <view v-for="item in items" :key="item.spuId" class="card">
        <image
          class="cover"
          :src="coverUrl(item)"
          mode="aspectFill"
          @click="goDetail(item.spuId)"
        />
        <view class="body" @click="goDetail(item.spuId)">
          <text class="name">{{ item.spuName || `商品 #${item.spuId}` }}</text>
          <text v-if="item.price != null" class="price">¥{{ formatPrice(item.price) }}</text>
          <text class="time">{{ formatBrowseTime(item.viewedAt) }}</text>
        </view>
        <button class="remove-btn" size="mini" @click="handleRemove(item.spuId)">删除</button>
      </view>
    </view>

    <view v-else class="empty">
      <text>还没有浏览记录</text>
      <button class="go-btn" @click="goHome">去逛逛</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import type {PortalBrowseRecord} from '@/api/types'
import {clearBrowseHistory, formatBrowseTime, getBrowseHistory, removeBrowseRecord} from '@/utils/browse-history'
import {useGoodsImages} from '@/composables/use-goods-images'

const items = ref<PortalBrowseRecord[]>([])

const { imageSrc, prefetchImages } = useGoodsImages()

function formatPrice(p?: number) {
  return p != null ? Number(p).toFixed(2) : '--'
}

function coverUrl(item: PortalBrowseRecord) {
  return imageSrc(item.coverImg) || '/static/logo.png'
}

function loadList() {
  items.value = getBrowseHistory()
  prefetchImages(items.value.map((i) => i.coverImg))
}

function goDetail(spuId: number) {
  uni.navigateTo({ url: `/pages/product/detail?id=${spuId}` })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

function handleRemove(spuId: number) {
  removeBrowseRecord(spuId)
  loadList()
}

function handleClearAll() {
  uni.showModal({
    title: '提示',
    content: '确定清空全部浏览足迹吗？',
    success: (res) => {
      if (res.confirm) {
        clearBrowseHistory()
        items.value = []
      }
    }
  })
}

onShow(loadList)
</script>

<style scoped>
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16rpx;
}
.clear-btn {
  margin: 0;
  color: #e64545;
  background: transparent;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}
.cover {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  flex-shrink: 0;
}
.body {
  flex: 1;
  min-width: 0;
}
.name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.4;
}
.price {
  display: block;
  margin-top: 8rpx;
  color: #e64545;
  font-weight: 700;
  font-size: 28rpx;
}
.time {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #999;
}
.remove-btn {
  margin: 0;
  color: #e64545;
  background: #fff5f5;
}
.empty {
  padding: 160rpx 0;
  text-align: center;
  color: #999;
}
.go-btn {
  margin-top: 32rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
}
</style>

<template>
  <view class="page">
    <view v-if="items.length" class="toolbar">
      <text class="toolbar-tip">共 {{ items.length }} 条足迹</text>
      <button class="clear-btn" size="mini" @click="handleClearAll">清空</button>
    </view>

    <view v-if="items.length" class="grid">
      <view v-for="item in items" :key="item.spuId" class="card" @click="goDetail(item.spuId)">
        <image class="cover" :src="coverUrl(item)" mode="aspectFill" />
        <view class="body">
          <view class="name-wrap">
            <text class="self-tag">自营</text>
            <text class="name">{{ item.spuName || `商品 #${item.spuId}` }}</text>
          </view>
          <text v-if="item.price != null" class="price"><text class="yen">¥</text>{{ formatPrice(item.price) }}</text>
          <text class="time">{{ formatBrowseTime(item.viewedAt) }}</text>
        </view>
        <view class="remove" @click.stop="handleRemove(item.spuId)">×</view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-icon">👣</text>
      <text class="empty-text">还没有浏览记录</text>
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

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx;
  background: $sp-bg-page;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 8rpx 16rpx;
}

.toolbar-tip {
  font-size: 24rpx;
  color: $sp-text-muted;
}

.clear-btn {
  margin: 0;
  color: $sp-primary;
  background: transparent;
  border: none;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.card {
  position: relative;
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 340rpx;
  background: #f8f8f8;
}

.body {
  padding: 12rpx 16rpx 16rpx;
}

.self-tag {
  display: inline-block;
  padding: 2rpx 6rpx;
  margin-right: 6rpx;
  font-size: 18rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
  vertical-align: top;
}

.name {
  font-size: 24rpx;
  line-height: 1.4;
  color: $sp-text;
}

.price {
  display: block;
  margin-top: 8rpx;
  font-size: 32rpx;
  font-weight: 800;
  color: $sp-accent;
}

.yen {
  font-size: 22rpx;
}

.time {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $sp-text-muted;
}

.remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  font-size: 32rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 50%;
}

.empty {
  padding: 160rpx 0;
  text-align: center;
}

.empty-icon {
  display: block;
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  color: $sp-text-muted;
  font-size: 28rpx;
}

.go-btn {
  margin-top: 32rpx;
  padding: 0 64rpx;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}
</style>

<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!items.length" class="empty">
      <text class="empty-icon">❤</text>
      <text class="empty-text">暂无收藏商品</text>
    </view>
    <view v-else class="grid">
      <view v-for="item in items" :key="item.spuId" class="card">
        <image class="pic" :src="imageSrc(item.spuImg)" mode="aspectFill" @click="goDetail(item.spuId)" />
        <view class="info" @click="goDetail(item.spuId)">
          <view class="name-wrap">
            <text class="self-tag">自营</text>
            <text class="name">{{ item.spuName }}</text>
          </view>
          <text class="price"><text class="yen">¥</text>{{ formatMoney(item.price, '0.00') }}</text>
        </view>
        <button class="remove-btn" size="mini" @click.stop="remove(item.spuId!)">取消收藏</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchCollectList, removeCollect} from '@/api/collect'
import type {PortalCollectItem} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {isLogin} from '@/stores/member'
import {formatMoney} from '@/utils/money'

const loading = ref(false)
const items = ref<PortalCollectItem[]>([])
const { imageSrc, prefetchImages } = useGoodsImages()

async function loadList() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const page = await fetchCollectList(1, 50)
    items.value = page.rows || []
    await prefetchImages(items.value.map((i) => i.spuImg))
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function remove(spuId: number) {
  try {
    await removeCollect(spuId)
    uni.showToast({ title: '已取消' })
    await loadList()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

function goDetail(spuId?: number) {
  if (!spuId) return
  uni.navigateTo({ url: `/pages/product/detail?id=${spuId}` })
}

onShow(loadList)
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx;
  background: $sp-bg-page;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.card {
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;
}

.pic {
  width: 100%;
  height: 340rpx;
  background: #f8f8f8;
}

.info {
  padding: 12rpx 16rpx;
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

.remove-btn {
  display: block;
  width: calc(100% - 32rpx);
  margin: 0 16rpx 16rpx;
  color: $sp-text-secondary;
  background: $sp-bg-page;
  border-radius: $sp-radius-pill;
  border: none;
  font-size: 22rpx;

  &::after {
    border: none;
  }
}

.hint,
.empty {
  padding: 120rpx 0;
  text-align: center;
  color: $sp-text-muted;
}

.empty-icon {
  display: block;
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
}
</style>

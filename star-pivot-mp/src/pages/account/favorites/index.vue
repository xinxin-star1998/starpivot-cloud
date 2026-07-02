<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!items.length" class="hint">暂无收藏</view>
    <view v-else>
      <view v-for="item in items" :key="item.spuId" class="card" @click="goDetail(item.spuId)">
        <image class="pic" :src="imageSrc(item.spuImg)" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ item.spuName }}</text>
          <text class="price">¥{{ item.price }}</text>
        </view>
        <button size="mini" @click.stop="remove(item.spuId!)">取消收藏</button>
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

<style scoped>
.page {
  min-height: 100vh;
  padding: 16rpx;
  background: #f5f5f5;
}
.card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}
.pic {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}
.info {
  flex: 1;
}
.name {
  display: block;
  font-size: 28rpx;
}
.price {
  display: block;
  margin-top: 8rpx;
  color: #e64545;
  font-weight: 600;
}
.hint {
  padding: 120rpx 0;
  text-align: center;
  color: #999;
}
</style>

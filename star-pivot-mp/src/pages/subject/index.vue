<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else>
      <image v-if="subject.coverImg" class="banner" :src="imageSrc(subject.coverImg)" mode="aspectFill" />
      <view class="header">
        <text class="title">{{ subject.title || subject.name || '专题活动' }}</text>
        <text class="sub">{{ subject.subTitle }}</text>
        <button
          v-if="subjectId"
          size="mini"
          class="collect-btn"
          :loading="collectLoading"
          @click="toggleCollect"
        >
          {{ collected ? '已收藏专题' : '收藏专题' }}
        </button>
      </view>

      <view v-if="!products.length" class="hint">暂无商品</view>
      <view v-else class="list">
        <view v-for="item in products" :key="item.id" class="card" @click="goDetail(item.id)">
          <image class="pic" :src="imageSrc(cover(item))" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.spuName }}</text>
            <text class="price">¥{{ item.price }}</text>
          </view>
        </view>
        <view v-if="hasMore" class="load-more">
          <button size="mini" :loading="loadingMore" @click="loadMore">加载更多</button>
        </view>
      </view>
    </template>
    <button class="share-fab" open-type="share">分享</button>
  </view>
</template>

<script setup lang="ts">
import {onLoad, onShareAppMessage} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {addSubjectCollect, fetchSubjectCollectStatus, removeSubjectCollect} from '@/api/collect'
import {productCover} from '@/api/product'
import {useGoodsImages} from '@/composables/use-goods-images'
import {fetchSubjectDetail} from '@/api/subject'
import type {PortalProductListItem, PortalSubjectDetail} from '@/api/types'
import {buildSharePayload, subjectSharePath} from '@/utils/share'
import {isLogin} from '@/stores/member'

const loading = ref(true)
const loadingMore = ref(false)
const collectLoading = ref(false)
const subject = ref<PortalSubjectDetail>({})
const products = ref<PortalProductListItem[]>([])
const pageNum = ref(1)
const total = ref(0)
const subjectId = ref(0)
const collected = ref(false)

const { imageSrc, prefetchImages } = useGoodsImages()

const hasMore = computed(() => products.value.length < total.value)

function cover(item: PortalProductListItem) {
  return productCover(item)
}

async function loadSubject(reset = true) {
  if (!subjectId.value) return
  if (reset) {
    pageNum.value = 1
    products.value = []
  }
  const isFirst = pageNum.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true
  try {
    const data = await fetchSubjectDetail(subjectId.value, pageNum.value, 16)
    if (reset) subject.value = data
    const rows = data.products?.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    total.value = data.products?.total || 0
    await prefetchImages(
      [subject.value.coverImg],
      products.value.map((p) => cover(p))
    )
    if (isLogin()) {
      const status = await fetchSubjectCollectStatus(subjectId.value)
      collected.value = status.collected
    }
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  pageNum.value += 1
  loadSubject(false)
}

async function toggleCollect() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  collectLoading.value = true
  try {
    if (collected.value) {
      await removeSubjectCollect(subjectId.value)
      collected.value = false
      uni.showToast({ title: '已取消收藏' })
    } else {
      await addSubjectCollect(subjectId.value)
      collected.value = true
      uni.showToast({ title: '收藏成功' })
    }
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    collectLoading.value = false
  }
}

function goDetail(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

onLoad((query) => {
  subjectId.value = Number(query?.id || 0)
  loadSubject(true)
})

onShareAppMessage(() =>
  buildSharePayload({
    title: subject.value.title || subject.value.name || 'StarPivot 专题',
    path: subjectSharePath(subjectId.value),
    imageUrl: subject.value.coverImg
  })
)
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 120rpx;
  background: #f5f5f5;
}
.banner {
  width: 100%;
  height: 360rpx;
}
.header {
  padding: 24rpx;
  background: #fff;
}
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}
.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #666;
}
.collect-btn {
  margin-top: 16rpx;
  background: #1677ff;
  color: #fff;
}
.list {
  padding: 16rpx;
}
.card {
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
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.name {
  font-size: 28rpx;
}
.price {
  font-size: 32rpx;
  font-weight: 700;
  color: #e64545;
}
.hint,
.load-more {
  padding: 48rpx;
  text-align: center;
  color: #999;
}
.share-fab {
  position: fixed;
  right: 32rpx;
  bottom: 48rpx;
  width: 100rpx;
  height: 100rpx;
  line-height: 100rpx;
  padding: 0;
  font-size: 24rpx;
  color: #fff;
  background: #1677ff;
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(22, 119, 255, 0.35);
}
</style>

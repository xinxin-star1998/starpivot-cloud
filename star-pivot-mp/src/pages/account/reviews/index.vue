<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'pending' }" @click="switchTab('pending')">待评价</text>
      <text class="tab" :class="{ active: tab === 'mine' }" @click="switchTab('mine')">我的评价</text>
    </view>

    <view v-if="loading && tab === 'pending' && !pending.length" class="hint">加载中...</view>
    <view v-else-if="loading && tab === 'mine' && !myComments.length" class="hint">加载中...</view>

    <view v-else-if="tab === 'pending'">
      <view v-for="(item, idx) in pending" :key="`${item.orderSn}-${item.spuId}-${idx}`" class="card">
        <view class="goods-row" @click="goProduct(item.spuId)">
          <image
            v-if="item.coverImg"
            class="cover"
            :src="imageSrc(item.coverImg)"
            mode="aspectFill"
          />
          <view class="goods-meta">
            <text class="name">{{ item.spuName }}</text>
            <text class="sub">订单 {{ item.orderSn }}</text>
          </view>
        </view>
        <view class="stars">
          <text
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ on: (draftStars[idx] || 5) >= n }"
            @click="draftStars[idx] = n"
          >
            ★
          </text>
        </view>
        <textarea v-model="draftContent[idx]" placeholder="写下你的评价..." />
        <view class="img-row">
          <view v-for="(url, i) in draftPreviews[idx] || []" :key="url" class="img-item">
            <image class="preview" :src="url" mode="aspectFill" @click="previewDraft(idx, i)" />
            <text class="img-remove" @click="removeDraftImage(idx, i)">✕</text>
          </view>
          <view
            v-if="(draftKeys[idx] || []).length < 3"
            class="img-add"
            @click="chooseDraftImages(idx)"
          >
            <text>+</text>
            <text class="img-add-tip">晒图</text>
          </view>
        </view>
        <button size="mini" class="btn" :loading="submittingIdx === idx" @click="submitPending(idx, item)">
          提交评价
        </button>
      </view>
      <view v-if="!pending.length" class="hint">暂无待评价商品</view>
    </view>

    <view v-else>
      <view v-for="c in myComments" :key="c.id" class="card">
        <view class="stars readonly">
          <text v-for="n in 5" :key="n" class="star" :class="{ on: (c.star || 0) >= n }">★</text>
        </view>
        <text class="content">{{ c.content }}</text>
        <view v-if="commentImageMap[c.id!]?.length" class="img-row readonly">
          <image
            v-for="(url, i) in commentImageMap[c.id!]"
            :key="url"
            class="preview"
            :src="url"
            mode="aspectFill"
            @click="previewUrls(commentImageMap[c.id!], i)"
          />
        </view>
        <text class="time">{{ c.createTime }}</text>
      </view>
      <view v-if="hasMoreMine" class="load-more">
        <button size="mini" class="load-btn" :loading="loadingMore" @click="loadMoreMine">加载更多</button>
      </view>
      <view v-if="!myComments.length" class="hint">暂无评价记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchMyComments, fetchPendingReviews, submitComment} from '@/api/comment'
import {uploadImage} from '@/api/image'
import type {PortalComment, PortalPendingReview} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {requireLogin} from '@/utils/auth'
import {resolveCommentResourceUrls} from '@/utils/comment-resources'

const tab = ref<'pending' | 'mine'>('pending')
const loading = ref(false)
const loadingMore = ref(false)
const pending = ref<PortalPendingReview[]>([])
const myComments = ref<PortalComment[]>([])
const minePage = ref(1)
const mineTotal = ref(0)
const draftStars = ref<Record<number, number>>({})
const draftContent = ref<Record<number, string>>({})
const draftKeys = ref<Record<number, string[]>>({})
const draftPreviews = ref<Record<number, string[]>>({})
const commentImageMap = ref<Record<number, string[]>>({})
const submittingIdx = ref<number>()
const { imageSrc, prefetchImages } = useGoodsImages()

const hasMoreMine = computed(() => myComments.value.length < mineTotal.value)

function switchTab(next: 'pending' | 'mine') {
  tab.value = next
  load(true)
}

function goProduct(spuId?: number) {
  if (!spuId) return
  uni.navigateTo({ url: `/pages/product/detail?id=${spuId}&review=1` })
}

function previewUrls(urls: string[], index: number) {
  uni.previewImage({ urls, current: urls[index] })
}

function previewDraft(idx: number, index: number) {
  const urls = draftPreviews.value[idx] || []
  if (!urls.length) return
  previewUrls(urls, index)
}

function removeDraftImage(idx: number, index: number) {
  const keys = [...(draftKeys.value[idx] || [])]
  const previews = [...(draftPreviews.value[idx] || [])]
  keys.splice(index, 1)
  previews.splice(index, 1)
  draftKeys.value[idx] = keys
  draftPreviews.value[idx] = previews
}

function chooseDraftImages(idx: number) {
  const remain = 3 - (draftKeys.value[idx] || []).length
  if (remain <= 0) return
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      uni.showLoading({ title: '上传中' })
      try {
        const keys = [...(draftKeys.value[idx] || [])]
        const previews = [...(draftPreviews.value[idx] || [])]
        for (const filePath of res.tempFilePaths) {
          const result = await uploadImage(filePath)
          keys.push(result.objectName)
          previews.push(result.displayUrl || result.permanentUrl || result.objectName)
        }
        draftKeys.value[idx] = keys
        draftPreviews.value[idx] = previews
      } catch (e) {
        uni.showToast({ title: (e as Error).message, icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

async function resolveMineImages(list: PortalComment[]) {
  for (const row of list) {
    if (!row.id || !row.resources || commentImageMap.value[row.id]) continue
    const urls = await resolveCommentResourceUrls(row.resources)
    if (urls.length) commentImageMap.value[row.id] = urls
  }
}

async function load(reset = true) {
  if (!requireLogin()) return
  loading.value = true
  try {
    if (tab.value === 'pending') {
      pending.value = await fetchPendingReviews()
      pending.value.forEach((_, i) => {
        if (!draftStars.value[i]) draftStars.value[i] = 5
      })
      await prefetchImages(pending.value.map((i) => i.coverImg))
    } else {
      if (reset) {
        minePage.value = 1
        myComments.value = []
        commentImageMap.value = {}
      }
      const page = await fetchMyComments(minePage.value, 10)
      const rows = page.rows || []
      myComments.value = reset ? rows : [...myComments.value, ...rows]
      mineTotal.value = page.total || 0
      await resolveMineImages(rows)
    }
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadMoreMine() {
  if (!hasMoreMine.value || loadingMore.value) return
  loadingMore.value = true
  try {
    minePage.value += 1
    const page = await fetchMyComments(minePage.value, 10)
    const rows = page.rows || []
    myComments.value = [...myComments.value, ...rows]
    mineTotal.value = page.total || 0
    await resolveMineImages(rows)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loadingMore.value = false
  }
}

async function submitPending(idx: number, item: PortalPendingReview) {
  if (!item.spuId || !item.skuId) return
  const content = (draftContent.value[idx] || '').trim()
  if (!content) {
    uni.showToast({ title: '请填写评价内容', icon: 'none' })
    return
  }
  submittingIdx.value = idx
  try {
    const keys = draftKeys.value[idx] || []
    await submitComment({
      spuId: item.spuId,
      skuId: item.skuId,
      star: draftStars.value[idx] || 5,
      content,
      resources: keys.length ? keys.join(',') : undefined
    })
    uni.showToast({ title: '评价成功' })
    draftContent.value[idx] = ''
    draftKeys.value[idx] = []
    draftPreviews.value[idx] = []
    await load(true)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    submittingIdx.value = undefined
  }
}

onShow(() => load(true))
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx;
  background: $sp-bg-page;
}
.tabs {
  display: flex;
  margin-bottom: 16rpx;
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;
}
.tab {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  font-size: 28rpx;
  color: $sp-text-secondary;
  position: relative;
}
.tab.active {
  color: $sp-primary;
  font-weight: 700;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 48rpx;
    height: 4rpx;
    background: $sp-primary;
    border-radius: 2rpx;
  }
}
.card {
  margin-bottom: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}
.goods-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}
.cover {
  width: 100rpx;
  height: 100rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
  flex-shrink: 0;
}
.goods-meta {
  flex: 1;
  min-width: 0;
}
.name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}
.stars {
  margin: 16rpx 0;
}
.star {
  font-size: 40rpx;
  color: #ddd;
  margin-right: 8rpx;
}
.star.on {
  color: #ffb400;
}
textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 16rpx;
  background: $sp-bg-page;
  border-radius: $sp-radius-sm;
  font-size: 26rpx;
  box-sizing: border-box;
}
.img-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;
}
.img-item {
  position: relative;
  width: 140rpx;
  height: 140rpx;
}
.preview {
  width: 140rpx;
  height: 140rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
}
.img-remove {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  font-size: 22rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 50%;
}
.img-add {
  width: 140rpx;
  height: 140rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  font-size: 40rpx;
  color: $sp-text-muted;
  background: $sp-bg-page;
  border-radius: $sp-radius-sm;
  border: 1rpx dashed $sp-border;
}
.img-add-tip {
  font-size: 22rpx;
}
.btn {
  margin-top: 16rpx;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}
.content {
  display: block;
  font-size: 28rpx;
  line-height: 1.6;
}
.time {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}
.hint {
  padding: 80rpx 0;
  text-align: center;
  color: $sp-text-muted;
}
.load-more {
  padding: 24rpx;
  text-align: center;
}
.load-btn {
  background: #fff;
  color: $sp-primary;
  border: 1rpx solid $sp-primary;
  border-radius: $sp-radius-pill;
}
</style>

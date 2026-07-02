<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'pending' }" @click="tab = 'pending'; load()">待评价</text>
      <text class="tab" :class="{ active: tab === 'mine' }" @click="tab = 'mine'; load()">我的评价</text>
    </view>

    <view v-if="loading" class="hint">加载中...</view>

    <view v-else-if="tab === 'pending'">
      <view v-for="(item, idx) in pending" :key="idx" class="card">
        <text class="name">{{ item.spuName }}</text>
        <text class="sub">订单 {{ item.orderSn }}</text>
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
        <text class="time">{{ c.createTime }}</text>
      </view>
      <view v-if="!myComments.length" class="hint">暂无评价记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchMyComments, fetchPendingReviews, submitComment} from '@/api/comment'
import type {PortalComment, PortalPendingReview} from '@/api/types'
import {isLogin} from '@/stores/member'

const tab = ref<'pending' | 'mine'>('pending')
const loading = ref(false)
const pending = ref<PortalPendingReview[]>([])
const myComments = ref<PortalComment[]>([])
const draftStars = ref<Record<number, number>>({})
const draftContent = ref<Record<number, string>>({})
const submittingIdx = ref<number>()

async function load() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    if (tab.value === 'pending') {
      pending.value = await fetchPendingReviews()
      pending.value.forEach((_, i) => {
        if (!draftStars.value[i]) draftStars.value[i] = 5
      })
    } else {
      const page = await fetchMyComments(1, 30)
      myComments.value = page.rows || []
    }
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
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
    await submitComment({
      spuId: item.spuId,
      skuId: item.skuId,
      star: draftStars.value[idx] || 5,
      content
    })
    uni.showToast({ title: '评价成功' })
    await load()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    submittingIdx.value = undefined
  }
}

onShow(load)
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
</style>

<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'claim' }" @click="switchTab('claim')">领券中心</text>
      <text class="tab" :class="{ active: tab === 'mine' }" @click="switchTab('mine')">我的优惠券</text>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="tab === 'claim'">
      <view v-for="c in claimable" :key="c.couponId" class="coupon-card">
        <view class="left">
          <text class="amount">¥{{ c.amount }}</text>
          <text class="rule">满{{ c.minPoint || 0 }}可用</text>
        </view>
        <view class="body">
          <text class="name">{{ c.couponName }}</text>
          <text class="time">至 {{ formatDate(c.endTime) }}</text>
        </view>
        <button
          size="mini"
          class="btn"
          :disabled="!c.canReceive"
          :loading="receivingId === c.couponId"
          @click="handleReceive(c.couponId)"
        >
          {{ c.canReceive ? '领取' : '已领完' }}
        </button>
      </view>
      <view v-if="!claimable.length" class="hint">暂无可领优惠券</view>
    </view>

    <view v-else>
      <view v-for="c in mine" :key="c.historyId" class="coupon-card mine">
        <view class="left">
          <text class="amount" :class="{ disabled: c.status !== 0 }">¥{{ c.amount }}</text>
          <text class="rule">满{{ c.minPoint || 0 }}可用</text>
        </view>
        <view class="body">
          <text class="name">{{ c.couponName }}</text>
          <text class="time">至 {{ formatDate(c.endTime) }}</text>
          <text class="status">{{ statusLabel(c.status) }}</text>
        </view>
      </view>
      <view v-if="!mine.length" class="hint">暂无优惠券</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchClaimableCoupons, fetchMyCoupons, receiveCoupon} from '@/api/coupon'
import type {PortalClaimableCoupon, PortalMyCoupon} from '@/api/types'
import {isLogin} from '@/stores/member'

const tab = ref<'claim' | 'mine'>('claim')
const loading = ref(false)
const claimable = ref<PortalClaimableCoupon[]>([])
const mine = ref<PortalMyCoupon[]>([])
const receivingId = ref<number>()

function formatDate(v?: string) {
  if (!v) return '-'
  return v.slice(0, 10)
}

function statusLabel(s?: number) {
  if (s === 0) return '未使用'
  if (s === 1) return '已使用'
  if (s === 2) return '已过期'
  return '未知'
}

async function loadClaimable() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    claimable.value = await fetchClaimableCoupons()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadMine() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    mine.value = await fetchMyCoupons()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(t: 'claim' | 'mine') {
  tab.value = t
  if (t === 'claim') loadClaimable()
  else loadMine()
}

async function handleReceive(couponId: number) {
  receivingId.value = couponId
  try {
    await receiveCoupon(couponId)
    uni.showToast({ title: '领取成功' })
    await loadClaimable()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    receivingId.value = undefined
  }
}

onShow(() => loadClaimable())
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f5f5f5;
}
.tabs {
  display: flex;
  margin-bottom: 24rpx;
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}
.tab {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666;
}
.tab.active {
  color: #1677ff;
  font-weight: 600;
  background: #e6f4ff;
}
.coupon-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
}
.left {
  width: 140rpx;
  text-align: center;
}
.amount {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #e64545;
}
.amount.disabled {
  color: #ccc;
}
.rule {
  font-size: 22rpx;
  color: #999;
}
.body {
  flex: 1;
}
.name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
}
.time,
.status {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
.btn {
  background: #1677ff;
  color: #fff;
}
.hint {
  padding: 80rpx 0;
  text-align: center;
  color: #999;
}
</style>

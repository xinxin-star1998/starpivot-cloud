<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'claim' }" @click="switchTab('claim')">领券中心</text>
      <text class="tab" :class="{ active: tab === 'mine' }" @click="switchTab('mine')">我的优惠券</text>
    </view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="tab === 'claim'" class="list">
      <view v-for="c in claimable" :key="c.couponId" class="coupon-ticket">
        <view class="ticket-left">
          <text class="amount"><text class="yen">¥</text>{{ c.amount }}</text>
          <text class="rule">满{{ c.minPoint || 0 }}元可用</text>
        </view>
        <view class="ticket-right">
          <text class="name">{{ c.couponName }}</text>
          <text class="time">有效期至 {{ formatDate(c.endTime) }}</text>
          <button
            size="mini"
            class="claim-btn"
            :disabled="!c.canReceive"
            :loading="receivingId === c.couponId"
            @click="handleReceive(c.couponId)"
          >
            {{ c.canReceive ? '立即领取' : '已领完' }}
          </button>
        </view>
      </view>
      <view v-if="!claimable.length" class="hint">暂无可领优惠券</view>
    </view>

    <view v-else class="list">
      <view v-for="c in mine" :key="c.historyId" class="coupon-ticket" :class="{ disabled: c.status !== 0 }">
        <view class="ticket-left">
          <text class="amount"><text class="yen">¥</text>{{ c.amount }}</text>
          <text class="rule">满{{ c.minPoint || 0 }}元可用</text>
        </view>
        <view class="ticket-right">
          <text class="name">{{ c.couponName }}</text>
          <text class="time">有效期至 {{ formatDate(c.endTime) }}</text>
          <text class="status-badge">{{ statusLabel(c.status) }}</text>
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

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.coupon-ticket {
  display: flex;
  min-height: 180rpx;
  background: #fff;
  border-radius: $sp-radius-md;
  overflow: hidden;

  &.disabled {
    opacity: 0.55;
  }
}

.ticket-left {
  width: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $sp-primary 0%, $sp-primary-dark 100%);
  color: #fff;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    right: -8rpx;
    top: 50%;
    transform: translateY(-50%);
    width: 16rpx;
    height: 16rpx;
    background: $sp-bg-page;
    border-radius: 50%;
    box-shadow: 0 -40rpx 0 $sp-bg-page, 0 40rpx 0 $sp-bg-page;
  }
}

.amount {
  font-size: 52rpx;
  font-weight: 800;
  line-height: 1;
}

.yen {
  font-size: 28rpx;
}

.rule {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.9;
}

.ticket-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24rpx;
  gap: 8rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;
}

.time {
  font-size: 22rpx;
  color: $sp-text-muted;
}

.claim-btn {
  align-self: flex-start;
  margin-top: 8rpx;
  padding: 0 28rpx;
  background: $sp-primary;
  color: #fff;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}

.status-badge {
  align-self: flex-start;
  margin-top: 8rpx;
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  color: $sp-primary;
  border: 1rpx solid $sp-primary;
  border-radius: $sp-radius-pill;
}

.hint {
  padding: 80rpx 0;
  text-align: center;
  color: $sp-text-muted;
}
</style>

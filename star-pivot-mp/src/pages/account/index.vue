<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else-if="center">
      <view class="jd-header">
        <view class="header-inner">
          <view class="profile" @click="goProfile">
            <image class="avatar" :src="center.member.header || defaultAvatar" mode="aspectFill" />
            <view class="meta">
              <text class="name">{{ center.member.nickname || center.member.username || '京东用户' }}</text>
              <text v-if="center.levelName" class="level">{{ center.levelName }}</text>
              <text v-else class="sub">{{ center.member.mobile || '点击完善资料' }}</text>
            </view>
            <text class="profile-arrow">›</text>
          </view>
          <view class="assets">
            <view class="asset-item">
              <text class="asset-num">{{ center.member.integration ?? 0 }}</text>
              <text class="asset-label">京豆</text>
            </view>
            <view class="asset-item">
              <text class="asset-num">{{ center.couponCount ?? 0 }}</text>
              <text class="asset-label">优惠券</text>
            </view>
            <view class="asset-item">
              <text class="asset-num">{{ center.member.growth ?? 0 }}</text>
              <text class="asset-label">成长值</text>
            </view>
          </view>
        </view>
      </view>

      <view class="order-card">
        <view class="card-head" @click="goOrders()">
          <text class="card-title">我的订单</text>
          <text class="card-more">全部订单 ›</text>
        </view>
        <view class="order-icons">
          <view class="order-icon" @click="goOrders(0)">
            <text class="icon">💳</text>
            <text class="label">待付款</text>
            <text v-if="badgeCount('0')" class="badge">{{ badgeCount('0') }}</text>
          </view>
          <view class="order-icon" @click="goOrders(1)">
            <text class="icon">📦</text>
            <text class="label">待发货</text>
            <text v-if="badgeCount('1')" class="badge">{{ badgeCount('1') }}</text>
          </view>
          <view class="order-icon" @click="goOrders(2)">
            <text class="icon">🚚</text>
            <text class="label">待收货</text>
            <text v-if="badgeCount('2')" class="badge">{{ badgeCount('2') }}</text>
          </view>
          <view class="order-icon" @click="goReviews">
            <text class="icon">💬</text>
            <text class="label">待评价</text>
            <text v-if="badgeCount('review')" class="badge">{{ badgeCount('review') }}</text>
          </view>
          <view class="order-icon" @click="goAfterSales">
            <text class="icon">↩</text>
            <text class="label">退换/售后</text>
          </view>
        </view>
      </view>

      <view class="tool-card">
        <view class="tool-grid">
          <view class="tool-item" @click="goFavorites">
            <text class="tool-icon">❤</text>
            <text class="tool-label">商品收藏</text>
          </view>
          <view class="tool-item" @click="goHistory">
            <text class="tool-icon">👣</text>
            <text class="tool-label">浏览记录</text>
          </view>
          <view class="tool-item" @click="goCoupons">
            <text class="tool-icon">🎫</text>
            <text class="tool-label">优惠券</text>
          </view>
          <view class="tool-item" @click="goAddresses">
            <text class="tool-icon">📍</text>
            <text class="tool-label">收货地址</text>
          </view>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @click="goSecurity">
          <text class="menu-text">账号安全</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goReviews">
          <text class="menu-text">评价中心</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view v-if="isLogin()" class="logout-wrap">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </view>
    </template>

    <view v-else class="guest-page">
      <view class="jd-header guest-header">
        <view class="guest-profile" @click="goLogin">
          <view class="guest-avatar">👤</view>
          <view class="guest-meta">
            <text class="guest-title">登录 / 注册</text>
            <text class="guest-desc">登录享更多优惠</text>
          </view>
          <text class="profile-arrow">›</text>
        </view>
      </view>
      <view class="order-card">
        <view class="card-head" @click="goLogin">
          <text class="card-title">我的订单</text>
          <text class="card-more">请先登录 ›</text>
        </view>
        <view class="order-icons">
          <view class="order-icon" @click="goLogin"><text class="icon">💳</text><text class="label">待付款</text></view>
          <view class="order-icon" @click="goLogin"><text class="icon">📦</text><text class="label">待发货</text></view>
          <view class="order-icon" @click="goLogin"><text class="icon">🚚</text><text class="label">待收货</text></view>
          <view class="order-icon" @click="goLogin"><text class="icon">💬</text><text class="label">待评价</text></view>
          <view class="order-icon" @click="goLogin"><text class="icon">↩</text><text class="label">退换/售后</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchMemberCenter} from '@/api/member'
import {fetchOrderStatusCounts} from '@/api/order'
import type {PortalMemberCenter} from '@/api/types'
import {clearSession, isLogin} from '@/stores/member'

const loading = ref(false)
const center = ref<PortalMemberCenter | null>(null)
const statusCounts = ref<Record<string, number>>({})
const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg'

function badgeCount(key: string) {
  const n = statusCounts.value[key] || 0
  if (n <= 0) return ''
  return n > 99 ? '99+' : String(n)
}

async function loadStatusCounts() {
  if (!isLogin()) {
    statusCounts.value = {}
    return
  }
  try {
    statusCounts.value = await fetchOrderStatusCounts()
  } catch {
    statusCounts.value = {}
  }
}

async function refresh() {
  if (!isLogin()) {
    center.value = null
    return
  }
  loading.value = true
  try {
    center.value = await fetchMemberCenter()
    await loadStatusCounts()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goOrders(status?: number) {
  if (!isLogin()) {
    goLogin()
    return
  }
  const url = status != null ? `/pages/orders/index?status=${status}` : '/pages/orders/index'
  uni.navigateTo({ url })
}

function goAddresses() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/account/addresses/index' })
}

function goFavorites() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/account/favorites/index' })
}

function goCoupons() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/coupons/index' })
}

function goReviews() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/account/reviews/index' })
}

function goAfterSales() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/orders/index?afterSale=1' })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/account/profile/index' })
}

function goHistory() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/account/history/index' })
}

function goSecurity() {
  if (!isLogin()) { goLogin(); return }
  uni.navigateTo({ url: '/pages/account/security/index' })
}

function handleLogout() {
  clearSession()
  center.value = null
  uni.showToast({ title: '已退出' })
}

onShow(refresh)
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding-bottom: 40rpx;
  background: $sp-bg-page;
}

.jd-header {
  padding: calc(env(safe-area-inset-top) + 24rpx) 24rpx 48rpx;
  background: linear-gradient(180deg, $sp-primary 0%, $sp-primary-dark 100%);
}

.header-inner {
  position: relative;
}

.profile {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.meta {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.level {
  display: inline-block;
  margin-top: 8rpx;
  padding: 2rpx 12rpx;
  font-size: 20rpx;
  color: $sp-gold;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4rpx;
}

.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.profile-arrow {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.7);
}

.assets {
  display: flex;
  justify-content: space-around;
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.asset-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.asset-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.75);
}

.order-card,
.tool-card,
.menu-card {
  margin: 16rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}

.order-card {
  margin-top: -24rpx;
  position: relative;
  z-index: 1;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 24rpx 8rpx;
}

.card-title {
  font-size: 28rpx;
  font-weight: 700;
  color: $sp-text;
}

.card-more {
  font-size: 24rpx;
  color: $sp-text-muted;
}

.order-icons {
  display: flex;
  justify-content: space-around;
  padding: 16rpx 8rpx 28rpx;
}

.order-icon {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.badge {
  position: absolute;
  top: -4rpx;
  right: 8rpx;
  min-width: 28rpx;
  padding: 0 8rpx;
  font-size: 18rpx;
  line-height: 28rpx;
  text-align: center;
  color: #fff;
  background: $sp-accent;
  border-radius: 999rpx;
}

.order-icon .icon {
  font-size: 44rpx;
}

.order-icon .label {
  font-size: 22rpx;
  color: $sp-text-secondary;
}

.tool-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx 0;
}

.tool-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx 0;
}

.tool-icon {
  font-size: 40rpx;
}

.tool-label {
  font-size: 22rpx;
  color: $sp-text-secondary;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid $sp-border;

  &:last-child {
    border-bottom: none;
  }
}

.menu-text {
  font-size: 28rpx;
  color: $sp-text;
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}

.logout-wrap {
  padding: 0 16rpx;
  margin-top: 8rpx;
}

.logout-btn {
  background: #fff;
  color: $sp-text-secondary;
  border-radius: $sp-radius-md;
  font-size: 28rpx;
  border: none;

  &::after {
    border: none;
  }
}

.guest-header {
  padding-bottom: 32rpx;
}

.guest-profile {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.guest-avatar {
  width: 112rpx;
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.guest-meta {
  flex: 1;
}

.guest-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.guest-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.hint {
  padding: 120rpx;
  text-align: center;
  color: $sp-text-muted;
}
</style>

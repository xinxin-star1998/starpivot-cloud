<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else-if="center">
      <view class="profile card">
        <image class="avatar" :src="center.member.header || defaultAvatar" mode="aspectFill" />
        <view class="meta">
          <text class="name">{{ center.member.nickname || center.member.username || '会员' }}</text>
          <text v-if="center.levelName" class="level">{{ center.levelName }}</text>
          <text class="sub">{{ center.member.mobile || '未绑定手机' }}</text>
          <view class="points">
            <text>积分 {{ center.member.integration ?? 0 }}</text>
            <text>成长 {{ center.member.growth ?? 0 }}</text>
          </view>
        </view>
        <button size="mini" class="edit-btn" @click="goProfile">编辑资料</button>
      </view>

      <view class="stats card">
        <view class="stat" @click="goOrders()">
          <text class="num">{{ center.orderCount ?? 0 }}</text>
          <text class="label">订单</text>
        </view>
        <view class="stat" @click="goCoupons">
          <text class="num">{{ center.couponCount ?? 0 }}</text>
          <text class="label">优惠券</text>
        </view>
        <view class="stat" @click="goFavorites">
          <text class="num">{{ center.collectCount ?? 0 }}</text>
          <text class="label">收藏</text>
        </view>
        <view class="stat" @click="goReviews">
          <text class="num">{{ center.pendingReviewCount ?? 0 }}</text>
          <text class="label">待评价</text>
        </view>
      </view>

      <view class="menu card">
        <view class="menu-item" @click="goOrders()">全部订单</view>
        <view class="menu-item" @click="goOrders(0)">待付款</view>
        <view class="menu-item" @click="goFavorites">我的收藏</view>
        <view class="menu-item" @click="goCoupons">优惠券</view>
        <view class="menu-item" @click="goReviews">评价中心</view>
        <view class="menu-item" @click="goAddresses">收货地址</view>
        <view class="menu-item" @click="goHistory">浏览足迹</view>
        <view class="menu-item" @click="goSecurity">账号安全</view>
        <view class="menu-item" @click="handleLogout" v-if="isLogin()">退出登录</view>
      </view>
    </template>
    <view v-else class="card guest" @click="goLogin">
      <text>点击登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {fetchMemberCenter} from '@/api/member'
import type {PortalMemberCenter} from '@/api/types'
import {clearSession, isLogin} from '@/stores/member'

const loading = ref(false)
const center = ref<PortalMemberCenter | null>(null)
const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg'

async function refresh() {
  if (!isLogin()) {
    center.value = null
    return
  }
  loading.value = true
  try {
    center.value = await fetchMemberCenter()
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
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/account/addresses/index' })
}

function goFavorites() {
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/account/favorites/index' })
}

function goCoupons() {
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/coupons/index' })
}

function goReviews() {
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/account/reviews/index' })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/account/profile/index' })
}

function goHistory() {
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/account/history/index' })
}

function goSecurity() {
  if (!isLogin()) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/account/security/index' })
}

function handleLogout() {
  clearSession()
  center.value = null
  uni.showToast({ title: '已退出' })
}

onShow(refresh)
</script>

<style scoped>
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.card {
  margin-bottom: 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
}
.profile {
  display: flex;
  gap: 24rpx;
  align-items: center;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #f5f5f5;
}
.meta {
  flex: 1;
}
.name {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
}
.level {
  display: inline-block;
  margin-top: 8rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  color: #1677ff;
  background: #e6f4ff;
  border-radius: 8rpx;
}
.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #999;
}
.points {
  display: flex;
  gap: 24rpx;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #666;
}
.edit-btn {
  background: #f5f5f5;
  color: #333;
}
.stats {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 16rpx;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.num {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}
.label {
  font-size: 24rpx;
  color: #999;
}
.menu-item {
  padding: 28rpx 0;
  font-size: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.menu-item:last-child {
  border-bottom: none;
}
.guest {
  text-align: center;
  color: #1677ff;
  font-size: 30rpx;
}
.hint {
  padding: 80rpx;
  text-align: center;
  color: #999;
}
</style>

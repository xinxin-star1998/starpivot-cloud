<template>
  <view class="page">
    <view v-if="!isLogin()" class="empty-wrap">
      <view class="empty-state">
        <view class="empty-icon">🔐</view>
        <text class="empty-title">登录后查看购物车</text>
        <text class="empty-desc">登录即可同步您的购物车商品</text>
        <button class="empty-action" @click="goLogin">去登录</button>
      </view>
    </view>

    <view v-else-if="!items.length" class="empty-wrap">
      <view class="empty-state">
        <view class="empty-icon">🛒</view>
        <text class="empty-title">购物车还是空的</text>
        <text class="empty-desc">快去挑选心仪的商品吧</text>
        <button class="empty-action" @click="goHome">去逛逛</button>
      </view>
    </view>

    <view v-else class="list">
      <view v-for="item in items" :key="item.skuId" class="cart-item">
        <view class="check-wrap" @click="toggleCheck(item)">
          <view class="checkbox" :class="{ checked: !!item.checked, disabled: item.valid === false }">
            <text v-if="item.checked" class="check-mark">✓</text>
          </view>
        </view>
        <image class="pic" :src="imageSrc(item.skuDefaultImg)" mode="aspectFill" @click="goDetail(item)" />
        <view class="info">
          <view class="shop-row">
            <text class="self-tag">自营</text>
            <text class="shop-name">StarPivot商城</text>
          </view>
          <text class="name">{{ item.skuTitle }}</text>
          <text v-if="item.skuAttr" class="sku">{{ item.skuAttr }}</text>
          <view class="row">
            <text class="price"><text class="yen">¥</text>{{ formatMoney(item.price, '0.00') }}</text>
            <view class="qty-ctrl">
              <text class="qty-btn" :class="{ disabled: (item.quantity || 1) <= 1 }" @click="changeQty(item, -1)">−</text>
              <text class="qty">{{ item.quantity }}</text>
              <text class="qty-btn" @click="changeQty(item, 1)">+</text>
            </view>
          </view>
        </view>
        <view class="remove-wrap" @click="removeItem(item.skuId)">
          <text class="remove-icon">🗑</text>
        </view>
      </view>
    </view>

    <view v-if="isLogin() && items.length" class="footer">
      <view class="select-all" @click="toggleSelectAll">
        <view class="checkbox" :class="{ checked: allChecked }">
          <text v-if="allChecked" class="check-mark">✓</text>
        </view>
        <text class="select-label">全选</text>
      </view>
      <view class="summary">
        <text class="summary-amount"><text class="yen">¥</text>{{ formatMoney(checkedAmount, '0.00') }}</text>
        <text class="summary-count">已选 {{ checkedCount }} 件</text>
      </view>
      <button class="checkout-btn" :class="{ disabled: checkedCount === 0 }" :disabled="checkedCount === 0" @click="goCheckout">
        去结算({{ checkedCount }})
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchCart, removeFromCart, updateCartItem} from '@/api/cart'
import type {PortalCartItem} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {isLogin} from '@/stores/member'
import {syncCartBadgeFromItems} from '@/utils/tabbar-cart'
import {formatMoney} from '@/utils/money'

const items = ref<PortalCartItem[]>([])
const checkedCount = ref(0)
const checkedAmount = ref(0)
const { imageSrc, prefetchImages } = useGoodsImages()

const allChecked = computed(() => {
  const valid = items.value.filter((i) => i.valid !== false)
  return valid.length > 0 && valid.every((i) => i.checked)
})

async function loadCart() {
  if (!isLogin()) {
    items.value = []
    checkedCount.value = 0
    checkedAmount.value = 0
    syncCartBadgeFromItems([])
    return
  }
  try {
    const data = await fetchCart()
    items.value = data.items || []
    checkedCount.value = data.checkedCount || 0
    checkedAmount.value = data.checkedAmount || 0
    syncCartBadgeFromItems(items.value)
    await prefetchImages(items.value.map((i) => i.skuDefaultImg))
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function toggleSelectAll() {
  const next = !allChecked.value
  try {
    for (const item of items.value) {
      if (item.valid === false) continue
      await updateCartItem(item.skuId, item.quantity || 1, next)
    }
    await loadCart()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function toggleCheck(item: PortalCartItem) {
  if (item.valid === false) return
  try {
    await updateCartItem(item.skuId, item.quantity || 1, !item.checked)
    await loadCart()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function changeQty(item: PortalCartItem, delta: number) {
  const next = Math.max(1, (item.quantity || 1) + delta)
  try {
    await updateCartItem(item.skuId, next, item.checked)
    await loadCart()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function removeItem(skuId: number) {
  uni.showModal({
    title: '提示',
    content: '确定删除该商品吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await removeFromCart([skuId])
        await loadCart()
      } catch (e) {
        uni.showToast({ title: (e as Error).message, icon: 'none' })
      }
    }
  })
}

function goCheckout() {
  if (checkedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/checkout/index?mode=cart' })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

function goDetail(item: PortalCartItem) {
  if (item.spuId) {
    uni.navigateTo({ url: `/pages/product/detail?id=${item.spuId}` })
  }
}

onShow(loadCart)
</script>

<style scoped lang="scss">

.page {
  min-height: 100vh;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
  background: $sp-bg-page;
}

.empty-wrap {
  padding-top: 80rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 48rpx;
}

.empty-icon {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 72rpx;
  background: linear-gradient(135deg, #fff5f5 0%, $sp-primary-light 100%);
  border-radius: 50%;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $sp-text;
}

.empty-desc {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $sp-text-muted;
}

.empty-action {
  margin-top: 40rpx;
  padding: 0 64rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: linear-gradient(135deg, $sp-primary 0%, $sp-primary-dark 100%);
  color: #fff;
  border-radius: $sp-radius-pill;
  font-size: 28rpx;
  border: none;

  &::after {
    border: none;
  }
}

.list {
  padding: 16rpx 24rpx;
}

.cart-item {
  display: flex;
  gap: 16rpx;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
}

.check-wrap {
  padding-top: 48rpx;
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  transition: all 0.2s;

  &.checked {
    background: $sp-primary;
    border-color: $sp-primary;
  }
  &.disabled {
    opacity: 0.4;
    background: $sp-bg-page;
  }
}

.shop-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 8rpx;
}

.self-tag {
  padding: 2rpx 8rpx;
  font-size: 18rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
}

.shop-name {
  font-size: 22rpx;
  color: $sp-text-muted;
}

.check-mark {
  font-size: 24rpx;
  color: #fff;
  font-weight: 700;
}

.pic {
  width: 180rpx;
  height: 180rpx;
  border-radius: $sp-radius-md;
  background: #f8f8f8;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
  padding-top: 4rpx;
}

.name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  font-size: 28rpx;
  line-height: 1.4;
  color: $sp-text;
}

.sku {
  display: inline-block;
  margin-top: 10rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  color: $sp-text-muted;
  background: $sp-bg-page;
  border-radius: 6rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.price {
  font-size: 34rpx;
  font-weight: 700;
  color: $sp-accent;
}

.yen {
  font-size: 24rpx;
}

.qty-ctrl {
  display: flex;
  align-items: center;
  border: 1rpx solid $sp-border;
  border-radius: $sp-radius-sm;
  overflow: hidden;
}

.qty-btn {
  width: 56rpx;
  height: 56rpx;
  line-height: 56rpx;
  text-align: center;
  font-size: 32rpx;
  color: $sp-text;
  background: $sp-bg-page;

  &.disabled {
    color: #ccc;
  }
}

.qty {
  min-width: 64rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
}

.remove-wrap {
  padding: 8rpx;
  padding-top: 48rpx;
}

.remove-icon {
  font-size: 32rpx;
  opacity: 0.5;
}

.footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.select-all {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.select-label {
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2rpx;
}

.summary-amount {
  font-size: 40rpx;
  font-weight: 800;
  color: $sp-accent;
}

.summary-count {
  font-size: 22rpx;
  color: $sp-text-muted;
}

.checkout-btn {
  margin: 0;
  padding: 0 32rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
  border-radius: $sp-radius-pill;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  flex-shrink: 0;
  box-shadow: 0 8rpx 20rpx rgba(225, 37, 27, 0.3);

  &::after {
    border: none;
  }

  &.disabled {
    opacity: 0.45;
    box-shadow: none;
  }
}
</style>

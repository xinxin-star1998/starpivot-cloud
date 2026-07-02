<template>

  <view class="page">

    <view v-if="!isLogin()" class="empty" @click="goLogin">

      <text>请先登录</text>

    </view>

    <view v-else-if="!items.length" class="empty">

      <text>购物车是空的</text>

    </view>

    <view v-else class="list">

      <view v-for="item in items" :key="item.skuId" class="cart-item">

        <checkbox

          :checked="!!item.checked"

          :disabled="item.valid === false"

          @click="toggleCheck(item)"

        />

        <image class="pic" :src="imageSrc(item.skuDefaultImg)" mode="aspectFill" />

        <view class="info">

          <text class="name">{{ item.skuTitle }}</text>

          <text v-if="item.skuAttr" class="sku">{{ item.skuAttr }}</text>

          <view class="row">

            <text class="price">¥{{ item.price }}</text>

            <view class="qty-ctrl">

              <text class="qty-btn" @click="changeQty(item, -1)">-</text>

              <text class="qty">{{ item.quantity }}</text>

              <text class="qty-btn" @click="changeQty(item, 1)">+</text>

            </view>

          </view>

        </view>

        <button size="mini" @click="removeItem(item.skuId)">删</button>

      </view>

    </view>



    <view v-if="isLogin() && items.length" class="footer">

      <text>已选 {{ checkedCount }} 件 · ¥{{ checkedAmount.toFixed(2) }}</text>

      <button class="checkout-btn" :disabled="checkedCount === 0" @click="goCheckout">去结算</button>

    </view>

  </view>

</template>



<script setup lang="ts">

import {onShow} from '@dcloudio/uni-app'

import {ref} from 'vue'

import {fetchCart, removeFromCart, updateCartItem} from '@/api/cart'

import type {PortalCartItem} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'

import {isLogin} from '@/stores/member'


const items = ref<PortalCartItem[]>([])

const checkedCount = ref(0)

const checkedAmount = ref(0)

const { imageSrc, prefetchImages } = useGoodsImages()



async function loadCart() {

  if (!isLogin()) return

  try {

    const data = await fetchCart()

    items.value = data.items || []

    checkedCount.value = data.checkedCount || 0

    checkedAmount.value = data.checkedAmount || 0

    await prefetchImages(items.value.map((i) => i.skuDefaultImg))

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

}



async function toggleCheck(item: PortalCartItem) {

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

  try {

    await removeFromCart([skuId])

    await loadCart()

  } catch (e) {

    uni.showToast({ title: (e as Error).message, icon: 'none' })

  }

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



onShow(loadCart)

</script>



<style scoped>

.page {

  min-height: 100vh;

  padding-bottom: 140rpx;

  background: #f5f5f5;

}

.empty {

  padding: 120rpx 0;

  text-align: center;

  color: #999;

}

.cart-item {

  display: flex;

  gap: 16rpx;

  align-items: center;

  margin: 16rpx;

  padding: 24rpx;

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

.sku {

  display: block;

  margin-top: 8rpx;

  font-size: 24rpx;

  color: #999;

}

.row {

  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-top: 12rpx;

}

.price {

  color: #e64545;

  font-weight: 600;

}

.qty-ctrl {

  display: flex;

  align-items: center;

  gap: 16rpx;

}

.qty-btn {

  width: 48rpx;

  height: 48rpx;

  line-height: 48rpx;

  text-align: center;

  background: #f5f5f5;

  border-radius: 8rpx;

}

.footer {

  position: fixed;

  right: 0;

  bottom: 0;

  left: 0;

  display: flex;

  align-items: center;

  justify-content: space-between;

  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));

  background: #fff;

  font-size: 28rpx;

  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);

}

.checkout-btn {

  margin: 0;

  background: #1677ff;

  color: #fff;

  border-radius: 999rpx;

}

</style>


<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else>
      <view class="panel">
        <view class="panel-title">收货地址</view>
        <view v-if="addresses.length" class="address-list">
          <view
            v-for="addr in addresses"
            :key="addr.id"
            class="address-card"
            :class="{ active: selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <text class="user">{{ addr.name }} {{ addr.phone }}</text>
            <text class="detail">
              {{ addr.province }}{{ addr.city }}{{ addr.region }}{{ addr.detailAddress }}
            </text>
            <text v-if="addr.defaultStatus === 1" class="tag">默认</text>
          </view>
        </view>
        <view v-else class="hint">暂无收货地址，请先添加</view>
        <button class="link-btn" @click="goAddresses">管理收货地址</button>
      </view>

      <view class="panel">
        <view class="panel-title">优惠券</view>
        <picker
          v-if="checkoutCoupons.length"
          :range="couponLabels"
          @change="onCouponChange"
        >
          <view class="coupon-picker">{{ selectedCouponLabel }}</view>
        </picker>
        <view v-else class="hint-inline">暂无可用优惠券</view>
      </view>

      <view v-if="maxUsableIntegration > 0" class="panel">
        <view class="panel-title">积分抵扣</view>
        <text class="integration-hint">
          可用 {{ availableIntegration }} 积分，本单最多可用 {{ maxUsableIntegration }} 积分
        </text>
        <view class="integration-row">
          <button class="step-btn" @click="changeIntegration(-100)">-100</button>
          <input
            class="integration-input"
            type="number"
            :value="useIntegrationInput"
            @blur="onIntegrationInput"
          />
          <button class="step-btn" @click="changeIntegration(100)">+100</button>
        </view>
        <text v-if="integrationDiscount > 0" class="integration-discount">
          抵扣 -¥{{ formatPrice(integrationDiscount) }}（{{ useIntegrationInput }} 积分）
        </text>
      </view>

      <view class="panel">
        <view class="panel-title">商品清单</view>
        <view v-for="item in displayLines" :key="item.skuId" class="line-item">
          <text class="line-name">{{ item.skuTitle }}</text>
          <text class="line-qty">x{{ item.quantity }}</text>
          <text class="line-price">¥{{ formatPrice(item.lineAmount) }}</text>
        </view>
        <view class="total-row">
          <text v-if="priceTrial?.promotionAmount">促销 -¥{{ formatPrice(priceTrial.promotionAmount) }}</text>
          <text v-if="priceTrial?.couponAmount">优惠券 -¥{{ formatPrice(priceTrial.couponAmount) }}</text>
          <text v-if="priceTrial?.integrationAmount">积分 -¥{{ formatPrice(priceTrial.integrationAmount) }}</text>
          <text v-if="priceTrial?.freightAmount">运费 ¥{{ formatPrice(priceTrial.freightAmount) }}</text>
          <text class="pay">应付 ¥{{ formatPrice(priceTrial?.payAmount || 0) }}</text>
        </view>
      </view>

      <view class="panel">
        <view class="panel-title">备注</view>
        <input v-model="note" class="note-input" placeholder="选填：给商家留言" />
      </view>
    </template>

    <view class="submit-bar">
      <text class="amount">¥{{ formatPrice(priceTrial?.payAmount || 0) }}</text>
      <button class="submit-btn" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
        提交订单
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onLoad, onShow} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchAddressList} from '@/api/address'
import {fetchCart} from '@/api/cart'
import {fetchCheckoutCoupons} from '@/api/coupon'
import {fetchOrderPriceTrial, fetchOrderSubmit, fetchOrderSubmitToken} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import type {PortalAddress, PortalCartItem, PortalCheckoutCoupon, PortalOrderPriceTrial} from '@/api/types'
import {isLogin} from '@/stores/member'

const loading = ref(true)
const submitting = ref(false)
const addresses = ref<PortalAddress[]>([])
const checkoutItems = ref<PortalCartItem[]>([])
const checkoutCoupons = ref<PortalCheckoutCoupon[]>([])
const selectedCouponHistoryId = ref<number>()
const selectedAddressId = ref<number>()
const orderToken = ref('')
const note = ref('')
const priceTrial = ref<PortalOrderPriceTrial | null>(null)
const useIntegrationInput = ref(0)

const availableIntegration = computed(() => priceTrial.value?.availableIntegration ?? 0)
const maxUsableIntegration = computed(() => priceTrial.value?.maxUsableIntegration ?? 0)
const integrationDiscount = computed(() => priceTrial.value?.integrationAmount ?? 0)

const mode = ref<'cart' | 'buy'>('cart')
const buySkuId = ref(0)
const buyQuantity = ref(1)

const displayLines = computed(() => {
  if (priceTrial.value?.lines?.length) return priceTrial.value.lines
  return checkoutItems.value.map((item) => ({
    skuId: item.skuId,
    skuTitle: item.skuTitle || `SKU ${item.skuId}`,
    quantity: item.quantity || 1,
    lineAmount: (item.price || 0) * (item.quantity || 1)
  }))
})

const couponLabels = computed(() => {
  const list = ['不使用优惠券']
  checkoutCoupons.value.forEach((c) => {
    const label = `${c.couponName} -¥${c.amount}（满${c.minPoint || 0}）`
    list.push(c.usable ? label : `${label} · 不可用`)
  })
  return list
})

const selectedCouponLabel = computed(() => {
  if (!selectedCouponHistoryId.value) return '不使用优惠券'
  const c = checkoutCoupons.value.find((x) => x.historyId === selectedCouponHistoryId.value)
  return c ? `${c.couponName} -¥${c.amount}` : '不使用优惠券'
})

const canSubmit = computed(
  () => !!selectedAddressId.value && checkoutItems.value.length > 0 && !!orderToken.value
)

function formatPrice(v: number) {
  return Number(v || 0).toFixed(2)
}

function trialPayload() {
  if (mode.value === 'cart') {
    return { useCart: true }
  }
  return {
    useCart: false,
    items: [{ skuId: buySkuId.value, quantity: buyQuantity.value }]
  }
}

function couponTrialItems() {
  if (mode.value === 'cart') {
    return { useCart: true }
  }
  return {
    useCart: false,
    items: checkoutItems.value.map((i) => ({
      skuId: i.skuId,
      quantity: i.quantity || 1
    }))
  }
}

async function loadCoupons() {
  if (!checkoutItems.value.length) {
    checkoutCoupons.value = []
    selectedCouponHistoryId.value = undefined
    return
  }
  checkoutCoupons.value = await fetchCheckoutCoupons(couponTrialItems())
  if (
    selectedCouponHistoryId.value &&
    !checkoutCoupons.value.some(
      (c) => c.historyId === selectedCouponHistoryId.value && c.usable
    )
  ) {
    selectedCouponHistoryId.value = undefined
  }
}

async function refreshPriceTrial() {
  if (!checkoutItems.value.length && mode.value === 'cart') {
    priceTrial.value = null
    return
  }
  priceTrial.value = await fetchOrderPriceTrial({
    ...trialPayload(),
    couponHistoryId: selectedCouponHistoryId.value,
    useIntegration: useIntegrationInput.value || undefined
  })
  if (priceTrial.value?.useIntegration != null) {
    useIntegrationInput.value = priceTrial.value.useIntegration
  }
}

function changeIntegration(delta: number) {
  const next = Math.max(0, Math.min(maxUsableIntegration.value, useIntegrationInput.value + delta))
  useIntegrationInput.value = next
  refreshPriceTrial()
}

function onIntegrationInput(e: { detail: { value: string } }) {
  const raw = Number(e.detail.value)
  const next = Number.isFinite(raw)
    ? Math.max(0, Math.min(maxUsableIntegration.value, Math.floor(raw)))
    : 0
  useIntegrationInput.value = next
  refreshPriceTrial()
}

function onCouponChange(e: { detail: { value: string } }) {
  const idx = Number(e.detail.value)
  if (idx === 0) {
    selectedCouponHistoryId.value = undefined
  } else {
    const coupon = checkoutCoupons.value[idx - 1]
    if (!coupon?.usable) {
      uni.showToast({ title: coupon?.unusableReason || '优惠券不可用', icon: 'none' })
      return
    }
    selectedCouponHistoryId.value = coupon.historyId
  }
  refreshPriceTrial()
}

async function loadData() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const [addrList, tokenResult] = await Promise.all([
      fetchAddressList(),
      fetchOrderSubmitToken()
    ])
    addresses.value = addrList
    orderToken.value = tokenResult.orderToken
    const defaultAddr = addrList.find((a) => a.defaultStatus === 1) || addrList[0]
    selectedAddressId.value = defaultAddr?.id

    if (mode.value === 'buy' && buySkuId.value) {
      checkoutItems.value = [
        { skuId: buySkuId.value, quantity: buyQuantity.value, checked: true, valid: true }
      ]
    } else {
      const cart = await fetchCart()
      checkoutItems.value = (cart.items || []).filter((i) => i.checked && i.valid !== false)
    }
    await loadCoupons()
    await refreshPriceTrial()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function refreshSubmitToken() {
  const tokenResult = await fetchOrderSubmitToken()
  orderToken.value = tokenResult.orderToken
}

async function payOrder(orderId: number) {
  const params = await fetchWxJsapiPay(orderId)
  if (params.mock) {
    await mockWxPay(orderId)
    uni.showToast({ title: 'Mock 支付成功' })
    setTimeout(() => uni.redirectTo({ url: '/pages/orders/index' }), 600)
    return
  }
  await new Promise<void>((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: params.packageValue,
      signType: params.signType as 'RSA',
      paySign: params.paySign,
      success: () => resolve(),
      fail: (err) => reject(new Error(err.errMsg || '支付取消'))
    })
  })
  uni.showToast({ title: '支付成功' })
  setTimeout(() => uni.redirectTo({ url: '/pages/orders/index' }), 600)
}

async function handleSubmit() {
  if (!selectedAddressId.value || !orderToken.value) return
  submitting.value = true
  try {
    const payload = {
      addressId: selectedAddressId.value,
      payType: 2,
      note: note.value || undefined,
      orderToken: orderToken.value,
      couponHistoryId: selectedCouponHistoryId.value,
      useIntegration: useIntegrationInput.value || undefined,
      ...trialPayload()
    }
    const result = await fetchOrderSubmit(payload)
    uni.showToast({ title: '下单成功' })
    await payOrder(result.orderId)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
    await refreshSubmitToken()
  } finally {
    submitting.value = false
  }
}

function goAddresses() {
  uni.navigateTo({ url: '/pages/account/addresses/index' })
}

onLoad((query) => {
  mode.value = query?.mode === 'buy' ? 'buy' : 'cart'
  buySkuId.value = Number(query?.skuId || 0)
  buyQuantity.value = Number(query?.quantity || 1)
})

onShow(loadData)
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 160rpx;
  background: #f5f5f5;
}
.panel {
  margin: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
}
.panel-title {
  margin-bottom: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
}
.address-card {
  position: relative;
  margin-bottom: 16rpx;
  padding: 20rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
}
.address-card.active {
  border-color: #1677ff;
  background: #f0f7ff;
}
.user {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.detail {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #666;
}
.tag {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-size: 22rpx;
  color: #e64545;
}
.link-btn {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #1677ff;
  background: transparent;
}
.coupon-picker {
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.line-item {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 0;
  font-size: 26rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.line-name {
  flex: 1;
}
.line-qty {
  color: #999;
}
.line-price {
  color: #e64545;
}
.total-row {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
}
.pay {
  font-size: 32rpx;
  font-weight: 700;
  color: #e64545;
}
.note-input {
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.submit-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}
.amount {
  font-size: 36rpx;
  font-weight: 700;
  color: #e64545;
}
.submit-btn {
  min-width: 240rpx;
  margin: 0;
  background: #1677ff;
  color: #fff;
  border-radius: 999rpx;
}
.hint,
.hint-inline {
  padding: 16rpx 0;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}
.integration-hint {
  display: block;
  margin-bottom: 16rpx;
  font-size: 24rpx;
  color: #666;
}
.integration-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.integration-input {
  flex: 1;
  padding: 16rpx 20rpx;
  text-align: center;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.step-btn {
  margin: 0;
  padding: 0 24rpx;
  font-size: 26rpx;
  background: #f5f5f5;
  color: #333;
  border-radius: 12rpx;
}
.integration-discount {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #e64545;
}
</style>

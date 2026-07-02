<template>
  <view class="page">
    <view class="header">
      <text class="title">{{ page.title || '限时秒杀' }}</text>
      <text class="sub">{{ page.subTitle || '整点场 · 抢完即止' }}</text>
    </view>

    <scroll-view v-if="page.sessions?.length" scroll-x class="sessions">
      <view
        v-for="s in page.sessions"
        :key="s.id"
        class="session-tab"
        :class="{ active: activeSessionId === s.id, ongoing: s.state === 'ongoing' }"
        @click="selectSession(s.id!)"
      >
        <text class="time">{{ s.startLabel }}</text>
        <text class="label">{{ sessionLabel(s.state) }}</text>
      </view>
    </scroll-view>

    <view v-if="loading" class="hint">加载中...</view>
    <view v-else-if="!products.length" class="hint">当前场次暂无商品</view>
    <view v-else class="grid">
      <view v-for="item in products" :key="`${item.spuId}-${item.skuId}`" class="card">
        <image class="pic" :src="imageSrc(item.coverImg)" mode="aspectFill" />
        <view class="body">
          <text class="name">{{ item.spuName }}</text>
          <text v-if="item.seckillStockRemain != null" class="stock">
            剩余 {{ item.seckillStockRemain }} 件
          </text>
          <view class="price-row">
            <text class="promo">¥{{ item.promoPrice ?? item.price }}</text>
            <text v-if="item.promoPrice && item.price" class="origin">¥{{ item.price }}</text>
          </view>
          <button
            v-if="sessionState === 'ongoing'"
            size="mini"
            class="buy-btn"
            :disabled="!item.skuId || (item.seckillStockRemain != null && item.seckillStockRemain <= 0)"
            @click="openBuy(item)"
          >
            {{ item.seckillStockRemain != null && item.seckillStockRemain <= 0 ? '已抢光' : '立即抢购' }}
          </button>
        </view>
      </view>
    </view>

    <view v-if="buyVisible" class="mask" @click="buyVisible = false">
      <view class="dialog" @click.stop>
        <text class="dialog-title">确认秒杀</text>
        <text class="dialog-name">{{ buyTarget?.spuName }}</text>
        <text class="dialog-price">秒杀价 ¥{{ buyTarget?.promoPrice ?? buyTarget?.price }}</text>
        <view class="field">
          <text>数量</text>
          <view class="qty-ctrl">
            <text @click="changeQty(-1)">-</text>
            <text>{{ buyQuantity }}</text>
            <text @click="changeQty(1)">+</text>
          </view>
        </view>
        <picker :range="addressLabels" @change="onAddressChange">
          <view class="field picker">{{ addressLabels[addressIndex] || '选择收货地址' }}</view>
        </picker>
        <button class="submit" :loading="submitting" @click="submitBuy">提交订单</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchAddressList} from '@/api/address'
import {fetchOrderSubmitToken} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import {fetchSeckillPage, submitSeckillOrder} from '@/api/seckill'
import type {PortalAddress, PortalHomeProduct, PortalSeckillPage} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {isLogin} from '@/stores/member'

const loading = ref(false)
const page = ref<PortalSeckillPage>({})
const products = ref<PortalHomeProduct[]>([])
const activeSessionId = ref<number>()

const buyVisible = ref(false)
const buyTarget = ref<PortalHomeProduct | null>(null)
const buyQuantity = ref(1)
const addresses = ref<PortalAddress[]>([])
const addressIndex = ref(0)
const orderToken = ref('')
const submitting = ref(false)

const { imageSrc, prefetchImages } = useGoodsImages()

const sessionState = computed(
  () => page.value.sessions?.find((s) => s.id === activeSessionId.value)?.state
)

const addressLabels = computed(() =>
  addresses.value.map(
    (a) => `${a.name} ${a.phone} · ${a.province}${a.city}${a.region}${a.detailAddress}`
  )
)

function sessionLabel(state?: string) {
  if (state === 'ongoing') return '抢购中'
  if (state === 'upcoming') return '即将开始'
  return '已结束'
}

async function loadPage(sessionId?: number) {
  loading.value = true
  try {
    const data = await fetchSeckillPage(sessionId)
    page.value = data
    activeSessionId.value = data.activeSessionId
    products.value = data.products || []
    await prefetchImages(products.value.map((p) => p.coverImg))
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function selectSession(sessionId: number) {
  if (activeSessionId.value === sessionId) return
  await loadPage(sessionId)
}

function buyMaxQty() {
  const item = buyTarget.value
  if (!item) return 1
  const limits = [1]
  if (item.seckillLimit && item.seckillLimit > 0) limits.push(item.seckillLimit)
  if (item.seckillStockRemain != null && item.seckillStockRemain > 0) {
    limits.push(item.seckillStockRemain)
  }
  return Math.min(...limits)
}

function changeQty(delta: number) {
  buyQuantity.value = Math.max(1, Math.min(buyMaxQty(), buyQuantity.value + delta))
}

function onAddressChange(e: { detail: { value: string } }) {
  addressIndex.value = Number(e.detail.value)
}

async function openBuy(item: PortalHomeProduct) {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!item.skuId || !activeSessionId.value) return
  buyTarget.value = item
  buyQuantity.value = 1
  buyVisible.value = true
  const [addrList, tokenResult] = await Promise.all([fetchAddressList(), fetchOrderSubmitToken()])
  addresses.value = addrList
  orderToken.value = tokenResult.orderToken
  const defaultIdx = addrList.findIndex((a) => a.defaultStatus === 1)
  addressIndex.value = defaultIdx >= 0 ? defaultIdx : 0
}

async function payOrder(orderId: number) {
  const params = await fetchWxJsapiPay(orderId)
  if (params.mock) {
    await mockWxPay(orderId)
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
}

async function submitBuy() {
  const addr = addresses.value[addressIndex.value]
  if (!buyTarget.value?.skuId || !activeSessionId.value || !addr?.id || !orderToken.value) {
    uni.showToast({ title: '请完善信息', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const result = await submitSeckillOrder({
      sessionId: activeSessionId.value,
      skuId: buyTarget.value.skuId,
      quantity: buyQuantity.value,
      addressId: addr.id,
      orderToken: orderToken.value,
      payType: 2
    })
    buyVisible.value = false
    uni.showToast({ title: '下单成功' })
    await payOrder(result.orderId)
    uni.redirectTo({ url: '/pages/orders/index' })
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
    const tokenResult = await fetchOrderSubmitToken()
    orderToken.value = tokenResult.orderToken
  } finally {
    submitting.value = false
  }
}

onShow(() => loadPage())
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 24rpx;
  background: #f5f5f5;
}
.header {
  padding: 24rpx;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff;
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
  opacity: 0.9;
}
.sessions {
  white-space: nowrap;
  padding: 16rpx 24rpx;
  background: #fff;
}
.session-tab {
  display: inline-block;
  margin-right: 16rpx;
  padding: 16rpx 24rpx;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  text-align: center;
}
.session-tab.active {
  border-color: #1677ff;
  background: #e6f4ff;
}
.session-tab.ongoing .label {
  color: #1677ff;
}
.time {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.label {
  display: block;
  font-size: 22rpx;
  color: #999;
}
.grid {
  padding: 16rpx;
}
.card {
  margin-bottom: 16rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.pic {
  width: 100%;
  height: 320rpx;
  background: #f5f5f5;
}
.body {
  padding: 20rpx;
}
.name {
  display: block;
  font-size: 28rpx;
}
.stock {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
.price-row {
  margin-top: 12rpx;
}
.promo {
  font-size: 36rpx;
  font-weight: 700;
  color: #e64545;
}
.origin {
  margin-left: 12rpx;
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
}
.buy-btn {
  margin-top: 16rpx;
  background: #1677ff;
  color: #fff;
}
.hint {
  padding: 80rpx;
  text-align: center;
  color: #999;
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
}
.dialog {
  width: 100%;
  padding: 32rpx;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
}
.dialog-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
}
.dialog-name {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
}
.dialog-price {
  display: block;
  margin-top: 8rpx;
  color: #e64545;
  font-size: 32rpx;
  font-weight: 700;
}
.field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding: 16rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 26rpx;
}
.field.picker {
  color: #333;
}
.qty-ctrl {
  display: flex;
  gap: 24rpx;
}
.submit {
  margin-top: 24rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
}
</style>

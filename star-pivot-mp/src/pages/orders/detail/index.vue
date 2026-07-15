<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else-if="order">
      <view class="status-header">
        <text class="status">{{ statusText(order.status) }}</text>
        <text class="sn">订单号 {{ order.orderSn }}</text>
        <text class="time">{{ order.createTime }}</text>
      </view>

      <view v-if="canShowLogistics(order)" class="card logistics-card">
        <view class="section-title">物流信息</view>
        <text v-if="order.deliveryCompany" class="logistics-line">物流公司：{{ order.deliveryCompany }}</text>
        <text v-if="order.deliverySn" class="logistics-line">运单号：{{ order.deliverySn }}</text>
        <view class="logistics-actions">
          <button size="mini" class="btn-outline" @click="trackLogistics">查看物流</button>
          <button v-if="order.deliverySn" size="mini" class="btn-outline" @click="copySn">复制运单号</button>
        </view>
        <view v-if="logistics?.events?.length" class="track-list">
          <view v-for="(event, idx) in logistics.events" :key="idx" class="track-item">
            <text class="track-time">{{ event.eventTime }}</text>
            <text class="track-desc">{{ event.eventDesc }}</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="section-title">收货信息</view>
        <text class="receiver">{{ order.receiverName }} {{ order.receiverPhone }}</text>
        <text class="address">
          {{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverRegion
          }}{{ order.receiverDetailAddress }}
        </text>
      </view>

      <view class="card">
        <view class="section-title">商品清单</view>
        <view v-for="(item, idx) in order.orderItemList || []" :key="idx" class="item-row">
          <image
            v-if="itemPic(item)"
            class="item-img"
            :src="imageSrc(itemPic(item))"
            mode="aspectFill"
          />
          <view class="item-body">
            <view class="item-name-row">
              <text class="self-tag">自营</text>
              <text class="item-name">{{ item.spuName }}</text>
            </view>
            <text class="item-sku">{{ item.skuName }}</text>
            <view class="item-foot">
              <text class="item-price">¥{{ formatPrice(item.realAmount ?? item.skuPrice) }}</text>
              <text class="item-qty">x{{ item.skuQuantity }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="section-title">金额明细</view>
        <view class="amount-row">
          <text>商品总额</text>
          <text>¥{{ formatPrice(order.totalAmount) }}</text>
        </view>
        <view v-if="order.freightAmount" class="amount-row">
          <text>运费</text>
          <text>¥{{ formatPrice(order.freightAmount) }}</text>
        </view>
        <view class="amount-row total">
          <text>实付</text>
          <text class="pay">¥{{ formatPrice(order.payAmount) }}</text>
        </view>
        <text v-if="order.note" class="note">备注：{{ order.note }}</text>
      </view>

      <view v-if="showActions" class="action-bar">
        <button v-if="order.status === 0" class="btn-outline" @click="cancel">取消订单</button>
        <button v-if="order.status === 0" class="btn-primary" @click="payOrder">去支付</button>
        <button v-if="order.status === 2" class="btn-outline" @click="trackLogistics">查看物流</button>
        <button v-if="order.status === 2" class="btn-primary" @click="receive">确认收货</button>
        <button v-if="canReturn" class="btn-outline" @click="goReturn">申请退货</button>
      </view>
    </template>
    <view v-else class="hint">订单不存在</view>
  </view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {cancelOrder, confirmReceive, fetchOrderDetail, fetchOrderLogistics} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import type {PortalOrder, PortalOrderItem, PortalShipmentTracking} from '@/api/types'
import {isLogin} from '@/stores/member'
import {useGoodsImages} from '@/composables/use-goods-images'
import {canApplyReturn, canShowLogistics, openLogisticsTrack} from '@/utils/logistics'
import {formatMoney} from '@/utils/money'

const loading = ref(true)
const order = ref<PortalOrder | null>(null)
const logistics = ref<PortalShipmentTracking | null>(null)
let orderId = 0

const { imageSrc, prefetchImages } = useGoodsImages()

const STATUS_MAP: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已关闭',
  5: '无效订单'
}

const canReturn = computed(() => canApplyReturn(order.value))

const showActions = computed(() => {
  const status = order.value?.status
  return status === 0 || status === 2 || status === 3
})

function statusText(status?: number) {
  return status === undefined ? '' : STATUS_MAP[status] || String(status)
}

function formatPrice(v?: number) {
  return formatMoney(v, '0.00')
}

function itemPic(item: PortalOrderItem) {
  return item.skuPic || item.spuPic || ''
}

async function loadDetail() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!orderId) return
  loading.value = true
  try {
    order.value = await fetchOrderDetail(orderId)
    const pics = (order.value?.orderItemList || []).map((item) => itemPic(item))
    await prefetchImages(pics)
    if (canShowLogistics(order.value)) {
      try {
        logistics.value = await fetchOrderLogistics(orderId)
      } catch {
        logistics.value = null
      }
    } else {
      logistics.value = null
    }
  } catch (e) {
    order.value = null
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function cancel() {
  if (!orderId) return
  try {
    await cancelOrder(orderId)
    uni.showToast({ title: '已取消' })
    await loadDetail()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function payOrder() {
  if (!orderId) return
  try {
    const params = await fetchWxJsapiPay(orderId)
    if (params.mock) {
      await mockWxPay(orderId)
      uni.showToast({ title: 'Mock 支付成功' })
      await loadDetail()
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
    await loadDetail()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function receive() {
  if (!orderId) return
  try {
    await confirmReceive(orderId)
    uni.showToast({ title: '已确认收货' })
    await loadDetail()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

function trackLogistics() {
  if (!order.value) return
  openLogisticsTrack(order.value)
}

function copySn() {
  const sn = order.value?.deliverySn?.trim()
  if (!sn) return
  uni.setClipboardData({
    data: sn,
    success: () => uni.showToast({ title: '运单号已复制' })
  })
}

function goReturn() {
  if (!orderId) return
  uni.navigateTo({ url: `/pages/orders/return/index?orderId=${orderId}` })
}

onLoad((query) => {
  orderId = Number(query?.id || 0)
  loadDetail()
})
</script>

<style scoped lang="scss">
.page {
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
  min-height: 100vh;
  background: $sp-bg-page;
}
.status-header {
  padding: 40rpx 24rpx 48rpx;
  text-align: center;
  background: linear-gradient(135deg, $sp-primary 0%, $sp-primary-dark 100%);
  color: #fff;
}
.status {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
}
.sn,
.time {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  opacity: 0.85;
}
.card {
  margin: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}
.section-title {
  margin-bottom: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $sp-text;
}
.logistics-line {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
}
.logistics-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}
.track-list {
  margin-top: 24rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $sp-border;
}
.track-item {
  margin-bottom: 16rpx;
}
.track-time {
  display: block;
  font-size: 22rpx;
  color: $sp-text-muted;
}
.track-desc {
  display: block;
  margin-top: 4rpx;
  font-size: 24rpx;
  color: $sp-text-secondary;
  line-height: 1.4;
}
.receiver {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.address {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
  line-height: 1.5;
}
.item-row {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $sp-border;
}
.item-row:last-child {
  border-bottom: none;
}
.item-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
  flex-shrink: 0;
}
.item-body {
  flex: 1;
  min-width: 0;
}
.item-name-row {
  line-height: 1.45;
}

.self-tag {
  display: inline-block;
  padding: 2rpx 8rpx;
  margin-right: 8rpx;
  font-size: 20rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
  vertical-align: top;
}

.item-name {
  display: inline;
  font-size: 28rpx;
  font-weight: 600;
}
.item-sku {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}
.item-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}
.item-price {
  color: $sp-accent;
  font-weight: 700;
}
.item-qty {
  color: $sp-text-muted;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 0;
  font-size: 26rpx;
  color: $sp-text-secondary;
}
.amount-row.total {
  margin-top: 8rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $sp-border;
  font-size: 28rpx;
  color: $sp-text;
}
.pay {
  color: $sp-accent;
  font-weight: 800;
}
.note {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}
.action-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.btn-outline,
.btn-primary {
  margin: 0;
  padding: 0 40rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}

.btn-outline {
  color: $sp-text-secondary;
  background: #fff;
  border: 1rpx solid $sp-border;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  box-shadow: 0 8rpx 20rpx rgba(225, 37, 27, 0.25);

  &.full {
    flex: 1;
  }
}

.hint {
  padding: 120rpx 0;
  text-align: center;
  color: $sp-text-muted;
}
</style>

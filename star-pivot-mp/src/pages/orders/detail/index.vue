<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else-if="order">
      <view class="card status-card">
        <text class="status">{{ statusText(order.status) }}</text>
        <text class="sn">订单号 {{ order.orderSn }}</text>
        <text class="time">{{ order.createTime }}</text>
      </view>

      <view v-if="hasLogistics" class="card">
        <view class="section-title">物流信息</view>
        <text v-if="order.deliveryCompany">物流公司：{{ order.deliveryCompany }}</text>
        <text v-if="order.deliverySn">运单号：{{ order.deliverySn }}</text>
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
            <text class="item-name">{{ item.spuName }}</text>
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

      <view class="actions">
        <button v-if="order.status === 0" size="mini" @click="payOrder">去支付</button>
        <button v-if="order.status === 0" size="mini" @click="cancel">取消订单</button>
        <button v-if="order.status === 2" size="mini" @click="receive">确认收货</button>
      </view>
    </template>
    <view v-else class="hint">订单不存在</view>
  </view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {cancelOrder, confirmReceive, fetchOrderDetail} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import type {PortalOrder, PortalOrderItem} from '@/api/types'
import {isLogin} from '@/stores/member'
import {useGoodsImages} from '@/composables/use-goods-images'

const loading = ref(true)
const order = ref<PortalOrder | null>(null)
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

const hasLogistics = computed(
  () => !!(order.value?.deliveryCompany || order.value?.deliverySn)
)

function statusText(status?: number) {
  return status === undefined ? '' : STATUS_MAP[status] || String(status)
}

function formatPrice(v?: number) {
  return Number(v || 0).toFixed(2)
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

onLoad((query) => {
  orderId = Number(query?.id || 0)
  loadDetail()
})
</script>

<style scoped>
.page {
  padding: 24rpx;
  padding-bottom: 120rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
}
.status-card {
  text-align: center;
}
.status {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1677ff;
}
.sn,
.time {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #999;
}
.section-title {
  margin-bottom: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
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
  color: #666;
  line-height: 1.5;
}
.item-row {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.item-row:last-child {
  border-bottom: none;
}
.item-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  flex-shrink: 0;
}
.item-body {
  flex: 1;
  min-width: 0;
}
.item-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.item-sku {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #999;
}
.item-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}
.item-price {
  color: #e64545;
  font-weight: 600;
}
.item-qty {
  color: #666;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 0;
  font-size: 26rpx;
  color: #666;
}
.amount-row.total {
  margin-top: 8rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  font-size: 28rpx;
  color: #333;
}
.pay {
  color: #e64545;
  font-weight: 700;
}
.note {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #999;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  padding: 16rpx 0;
}
.hint {
  padding: 120rpx 0;
  text-align: center;
  color: #999;
}
</style>

<template>
  <view class="page">
    <scroll-view scroll-x class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.label"
        class="tab"
        :class="{ active: filterStatus === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </scroll-view>

    <view v-if="!orders.length && !loading" class="empty">暂无订单</view>
    <view
      v-for="order in orders"
      :key="order.id"
      class="order card"
      @click="goDetail(order.id)"
    >
      <view class="head">
        <text class="sn">{{ order.orderSn }}</text>
        <text class="status">{{ statusText(order.status) }}</text>
      </view>
      <view v-for="(item, idx) in order.orderItemList || []" :key="idx" class="item">
        <text>{{ item.spuName }} · {{ item.skuName }}</text>
        <text class="qty">x{{ item.skuQuantity }}</text>
      </view>
      <view class="foot">
        <text class="amount">¥{{ order.payAmount }}</text>
        <view class="actions" @click.stop>
          <button
            v-if="order.status === 0"
            size="mini"
            @click="payOrder(order.id)"
          >
            去支付
          </button>
          <button
            v-if="order.status === 0"
            size="mini"
            @click="cancel(order.id)"
          >
            取消
          </button>
          <button
            v-if="order.status === 2"
            size="mini"
            @click="receive(order.id)"
          >
            确认收货
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onLoad, onShow} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {cancelOrder, confirmReceive, fetchOrders} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import type {PortalOrder} from '@/api/types'
import {isLogin} from '@/stores/member'

const orders = ref<PortalOrder[]>([])
const loading = ref(false)
const filterStatus = ref<number | undefined>(undefined)

const tabs = [
  { label: '全部', value: undefined },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 }
]

const STATUS_MAP: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已关闭',
  5: '无效订单'
}

function statusText(status?: number) {
  return status === undefined ? '' : STATUS_MAP[status] || String(status)
}

function switchTab(status?: number) {
  filterStatus.value = status
  loadOrders()
}

function goDetail(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/orders/detail/index?id=${id}` })
}

async function loadOrders() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const page = await fetchOrders(1, 20, filterStatus.value)
    orders.value = page.rows || []
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function cancel(id?: number) {
  if (!id) return
  try {
    await cancelOrder(id)
    await loadOrders()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function payOrder(id?: number) {
  if (!id) return
  try {
    const params = await fetchWxJsapiPay(id)
    if (params.mock) {
      await mockWxPay(id)
      uni.showToast({ title: 'Mock 支付成功' })
      await loadOrders()
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
    await loadOrders()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function receive(id?: number) {
  if (!id) return
  try {
    await confirmReceive(id)
    uni.showToast({ title: '已确认收货' })
    await loadOrders()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

onLoad((query) => {
  if (query?.status !== undefined && query.status !== '') {
    filterStatus.value = Number(query.status)
  }
})

onShow(loadOrders)
</script>

<style scoped>
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.tabs {
  white-space: nowrap;
  margin: -8rpx -24rpx 16rpx;
  padding: 0 24rpx 16rpx;
}
.tab {
  display: inline-block;
  margin-right: 16rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: #666;
  background: #fff;
  border-radius: 999rpx;
}
.tab.active {
  color: #1677ff;
  background: #e6f4ff;
}
.card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
}
.head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
  font-size: 26rpx;
}
.sn {
  color: #666;
}
.status {
  color: #1677ff;
}
.item {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 0;
  font-size: 28rpx;
}
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}
.amount {
  color: #e64545;
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 12rpx;
}
.empty {
  padding: 120rpx 0;
  text-align: center;
  color: #999;
}
</style>

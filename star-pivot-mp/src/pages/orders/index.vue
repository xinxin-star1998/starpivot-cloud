<template>
  <view class="page">
    <view v-if="afterSaleMode" class="after-sale-tip">请选择需要售后的订单，进入详情后可继续申请退货</view>

    <scroll-view scroll-x class="tabs" :show-scrollbar="false">
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

    <view v-if="loading && !orders.length" class="hint">加载中...</view>
    <view v-else-if="!orders.length" class="empty">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无相关订单</text>
    </view>
    <view v-else class="list">
      <view v-for="order in orders" :key="order.id" class="order-card" @click="goDetail(order.id)">
        <view class="shop-row">
          <text class="shop-tag">自营</text>
          <text class="shop-name">StarPivot商城</text>
          <text class="status" :class="'s-' + order.status">{{ statusText(order.status) }}</text>
        </view>
        <view v-for="(item, idx) in order.orderItemList || []" :key="idx" class="item-row">
          <image class="item-pic" :src="itemPic(item)" mode="aspectFill" />
          <view class="item-info">
            <text class="item-name">{{ item.spuName }}</text>
            <text v-if="item.skuName" class="item-sku">{{ item.skuName }}</text>
            <view class="item-foot">
              <text class="item-price">¥{{ formatMoney(item.realAmount ?? item.skuPrice, '0.00') }}</text>
              <text class="item-qty">x{{ item.skuQuantity }}</text>
            </view>
          </view>
        </view>
        <view class="order-foot">
          <text class="total">共{{ itemCount(order) }}件 实付 <text class="amount">¥{{ formatMoney(order.payAmount, '0.00') }}</text></text>
          <view class="actions" @click.stop>
            <button v-if="order.status === 0" size="mini" class="btn-outline" @click="cancel(order.id)">取消</button>
            <button v-if="order.status === 0" size="mini" class="btn-primary" @click="payOrder(order.id)">去支付</button>
            <button v-if="canShowLogistics(order)" size="mini" class="btn-outline" @click="trackLogistics(order)">物流</button>
            <button v-if="order.status === 2" size="mini" class="btn-primary" @click="receive(order.id)">确认收货</button>
            <button v-if="canReview(order)" size="mini" class="btn-outline" @click="goReview(order)">去评价</button>
            <button v-if="canApplyReturn(order)" size="mini" class="btn-outline" @click="goReturn(order.id)">退货</button>
          </view>
        </view>
      </view>
      <view v-if="hasMore" class="load-more">
        <button size="mini" class="load-btn" :loading="loadingMore" @click="loadMore">加载更多</button>
      </view>
      <view v-else-if="orders.length" class="load-end">没有更多了</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onLoad, onReachBottom, onShow} from '@dcloudio/uni-app'
import {computed, ref} from 'vue'
import {fetchReviewableSpuIds} from '@/api/comment'
import {cancelOrder, confirmReceive, fetchOrders} from '@/api/order'
import {fetchWxJsapiPay, mockWxPay} from '@/api/pay'
import type {PortalOrder, PortalOrderItem} from '@/api/types'
import {useGoodsImages} from '@/composables/use-goods-images'
import {requireLogin} from '@/utils/auth'
import {canApplyReturn, canShowLogistics, openLogisticsTrack} from '@/utils/logistics'
import {formatMoney} from '@/utils/money'
import {requestOrderSubscribeMessage} from '@/utils/subscribe'

const PAGE_SIZE = 10
const orders = ref<PortalOrder[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const total = ref(0)
const filterStatus = ref<number | undefined>(undefined)
const afterSaleMode = ref(false)
const reviewableSpuIds = ref<Set<number>>(new Set())
const { imageSrc, prefetchImages } = useGoodsImages()

const hasMore = computed(() => orders.value.length < total.value)

const tabs = [
  { label: '全部', value: undefined },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
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

function itemCount(order: PortalOrder) {
  return (order.orderItemList || []).reduce((sum, i) => sum + (i.skuQuantity || 0), 0)
}

function itemPic(item: PortalOrderItem) {
  return imageSrc(item.skuPic || item.spuPic) || '/static/logo.png'
}

function canReview(order: PortalOrder) {
  if (order.status !== 2 && order.status !== 3) return false
  return (order.orderItemList || []).some(
    (item) => item.spuId != null && reviewableSpuIds.value.has(item.spuId)
  )
}

function findFirstReviewableSpuId(order: PortalOrder): number | undefined {
  return (order.orderItemList || []).find(
    (item) => item.spuId != null && reviewableSpuIds.value.has(item.spuId)
  )?.spuId
}

function switchTab(status?: number) {
  filterStatus.value = status
  loadOrders(true)
}

function goDetail(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/orders/detail/index?id=${id}` })
}

function goReview(order: PortalOrder) {
  const spuId = findFirstReviewableSpuId(order)
  if (!spuId) {
    uni.navigateTo({ url: '/pages/account/reviews/index' })
    return
  }
  uni.navigateTo({ url: `/pages/product/detail?id=${spuId}&review=1` })
}

async function refreshReviewable(list: PortalOrder[]) {
  const spuIds = [
    ...new Set(
      list
        .filter((o) => o.status === 2 || o.status === 3)
        .flatMap((o) => (o.orderItemList || []).map((i) => i.spuId).filter(Boolean) as number[])
    )
  ]
  if (!spuIds.length) {
    reviewableSpuIds.value = new Set()
    return
  }
  try {
    const res = await fetchReviewableSpuIds(spuIds)
    reviewableSpuIds.value = new Set(res.reviewableSpuIds || [])
  } catch {
    reviewableSpuIds.value = new Set()
  }
}

async function loadOrders(reset = true) {
  if (!requireLogin()) return
  if (reset) {
    pageNum.value = 1
    orders.value = []
    total.value = 0
  }
  const isFirst = pageNum.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true
  try {
    const page = await fetchOrders(
      pageNum.value,
      afterSaleMode.value ? 50 : PAGE_SIZE,
      filterStatus.value
    )
    let rows = page.rows || []
    if (afterSaleMode.value) {
      rows = rows.filter((o) => canApplyReturn(o))
      orders.value = rows
      total.value = rows.length
    } else {
      orders.value = reset ? rows : [...orders.value, ...rows]
      total.value = page.total || 0
    }
    await refreshReviewable(orders.value)
    const pics = orders.value.flatMap((o) => (o.orderItemList || []).map((i) => i.skuPic || i.spuPic))
    await prefetchImages(pics)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (!hasMore.value || loadingMore.value || loading.value) return
  pageNum.value += 1
  loadOrders(false)
}

async function cancel(id?: number) {
  if (!id) return
  try {
    await cancelOrder(id)
    await loadOrders(true)
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
      await requestOrderSubscribeMessage()
      uni.showToast({ title: 'Mock 支付成功' })
      await loadOrders(true)
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
    await requestOrderSubscribeMessage()
    uni.showToast({ title: '支付成功' })
    await loadOrders(true)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function receive(id?: number) {
  if (!id) return
  try {
    await confirmReceive(id)
    uni.showToast({ title: '已确认收货' })
    await loadOrders(true)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

function trackLogistics(order: PortalOrder) {
  openLogisticsTrack(order)
}

function goReturn(id?: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages/orders/return/index?orderId=${id}` })
}

onLoad((query) => {
  if (query?.afterSale === '1') {
    afterSaleMode.value = true
    filterStatus.value = undefined
  }
  if (query?.status !== undefined && query.status !== '') {
    filterStatus.value = Number(query.status)
  }
})

onShow(() => loadOrders(true))

onReachBottom(() => loadMore())
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $sp-bg-page;
}

.after-sale-tip {
  margin: 16rpx 16rpx 0;
  padding: 16rpx 20rpx;
  font-size: 24rpx;
  color: $sp-primary;
  background: $sp-primary-light;
  border-radius: $sp-radius-sm;
}

.tabs {
  white-space: nowrap;
  padding: 16rpx;
  background: #fff;
}

.tab {
  display: inline-block;
  margin-right: 12rpx;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
  background: $sp-bg-page;
  border-radius: $sp-radius-pill;
}

.tab.active {
  color: #fff;
  background: $sp-primary;
  font-weight: 600;
}

.hint,
.empty {
  padding: 120rpx 0;
  text-align: center;
  color: $sp-text-muted;
}

.empty-icon {
  display: block;
  font-size: 64rpx;
}

.empty-text {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
}

.list {
  padding: 0 16rpx 24rpx;
}

.order-card {
  margin-top: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}

.shop-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.shop-tag {
  padding: 2rpx 8rpx;
  font-size: 20rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
}

.shop-name {
  flex: 1;
  font-size: 26rpx;
  font-weight: 600;
}

.status {
  font-size: 26rpx;
  color: $sp-primary;
}

.item-row {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
}

.item-pic {
  width: 140rpx;
  height: 140rpx;
  border-radius: $sp-radius-sm;
  background: #f8f8f8;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-size: 28rpx;
  line-height: 1.45;
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

.order-foot {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $sp-border;
}

.total {
  display: block;
  text-align: right;
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.amount {
  color: $sp-accent;
  font-weight: 700;
}

.actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.btn-outline,
.btn-primary {
  margin: 0;
  font-size: 24rpx;
  border-radius: $sp-radius-pill;

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
  background: $sp-primary;
  border: none;
}

.load-more,
.load-end {
  padding: 24rpx;
  text-align: center;
}

.load-btn {
  background: #fff;
  color: $sp-primary;
  border: 1rpx solid $sp-primary;
  border-radius: $sp-radius-pill;
}

.load-end {
  font-size: 24rpx;
  color: $sp-text-muted;
}
</style>

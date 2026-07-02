<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else-if="order">
      <view class="card">
        <text class="card-title">订单 {{ order.orderSn }}</text>
        <text class="card-sub">请选择需要退货的商品</text>
      </view>

      <view v-if="!rows.length" class="hint">暂无可退商品</view>
      <view v-else class="card">
        <view v-for="row in rows" :key="row.skuId" class="item-row">
          <view class="check-wrap" @click="toggleRow(row)">
            <text class="check" :class="{ on: row.selected }">{{ row.selected ? '✓' : '' }}</text>
          </view>
          <view class="item-body">
            <text class="item-name">{{ row.skuName || row.spuName }}</text>
            <text class="item-meta">可退 {{ row.maxQty }} 件</text>
            <view v-if="row.selected" class="qty-row">
              <button class="qty-btn" size="mini" @click="changeQty(row, -1)">-</button>
              <text class="qty-num">{{ row.returnQty }}</text>
              <button class="qty-btn" size="mini" @click="changeQty(row, 1)">+</button>
            </view>
          </view>
        </view>
      </view>

      <view class="card">
        <text class="field-label">退货原因 *</text>
        <textarea v-model="reason" class="textarea" placeholder="如：商品质量问题" maxlength="200" />
        <text class="field-label optional">补充说明</text>
        <textarea v-model="description" class="textarea short" placeholder="选填" maxlength="200" />
      </view>

      <view class="footer">
        <button class="btn-submit" :loading="submitting" :disabled="!rows.length" @click="submit">
          提交申请
        </button>
      </view>
    </template>
    <view v-else class="hint">订单不存在或不可退货</view>
  </view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app'
import {ref} from 'vue'
import {applyOrderReturn, fetchOrderDetail} from '@/api/order'
import type {PortalOrder, PortalOrderItem} from '@/api/types'
import {canApplyReturn} from '@/utils/logistics'
import {isLogin} from '@/stores/member'

interface ReturnRow extends PortalOrderItem {
  maxQty: number
  returnQty: number
  selected: boolean
}

const loading = ref(true)
const submitting = ref(false)
const order = ref<PortalOrder | null>(null)
const rows = ref<ReturnRow[]>([])
const reason = ref('')
const description = ref('')
let orderId = 0

function buildRows(source: PortalOrder) {
  rows.value = (source.orderItemList || [])
    .filter((i) => i.skuId != null && (i.skuQuantity || 0) > 0)
    .map((i) => ({
      ...i,
      maxQty: i.skuQuantity || 1,
      returnQty: i.skuQuantity || 1,
      selected: false
    }))
}

function toggleRow(row: ReturnRow) {
  row.selected = !row.selected
}

function changeQty(row: ReturnRow, delta: number) {
  const next = row.returnQty + delta
  if (next < 1 || next > row.maxQty) return
  row.returnQty = next
}

async function loadOrder() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!orderId) return
  loading.value = true
  try {
    const data = await fetchOrderDetail(orderId)
    if (!canApplyReturn(data)) {
      order.value = null
      uni.showToast({ title: '当前订单不可退货', icon: 'none' })
      return
    }
    order.value = data
    buildRows(data)
  } catch (e) {
    order.value = null
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!order.value?.id) return
  const selected = rows.value.filter((r) => r.selected)
  if (!selected.length) {
    uni.showToast({ title: '请至少选择一件商品', icon: 'none' })
    return
  }
  const trimmed = reason.value.trim()
  if (!trimmed) {
    uni.showToast({ title: '请输入退货原因', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await applyOrderReturn({
      orderId: order.value.id,
      items: selected.map((r) => ({ skuId: r.skuId!, quantity: r.returnQty })),
      reason: trimmed,
      description: description.value.trim() || undefined
    })
    uni.showToast({ title: '申请已提交' })
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad((query) => {
  orderId = Number(query?.orderId || query?.id || 0)
  loadOrder()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  background: $sp-bg-page;
}

.card {
  margin: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}

.card-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: $sp-text;
}

.card-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}

.item-row {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid $sp-border;

  &:last-child {
    border-bottom: none;
  }
}

.check-wrap {
  flex-shrink: 0;
  padding-top: 4rpx;
}

.check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  font-size: 24rpx;
  color: #fff;
  border: 2rpx solid $sp-border;
  border-radius: 8rpx;
}

.check.on {
  background: $sp-primary;
  border-color: $sp-primary;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-size: 28rpx;
  color: $sp-text;
  line-height: 1.45;
}

.item-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.qty-btn {
  margin: 0;
  padding: 0 20rpx;
  line-height: 52rpx;
  font-size: 28rpx;
}

.qty-num {
  min-width: 48rpx;
  text-align: center;
  font-size: 28rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $sp-text;

  &.optional {
    margin-top: 24rpx;
    font-weight: 500;
    color: $sp-text-secondary;
  }
}

.textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 16rpx;
  font-size: 26rpx;
  background: $sp-bg-page;
  border-radius: $sp-radius-sm;
  box-sizing: border-box;
}

.textarea.short {
  min-height: 120rpx;
}

.footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.btn-submit {
  height: 80rpx;
  line-height: 80rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}

.hint {
  padding: 120rpx 24rpx;
  text-align: center;
  color: $sp-text-muted;
  font-size: 28rpx;
}
</style>

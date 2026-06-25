<!-- C 端优惠券中心 -->
<template>
  <div v-loading="loading" class="portal-coupons">
    <h1 class="page-title">优惠券中心</h1>

    <ElTabs v-model="activeTab" @tab-change="handleTabChange">
      <ElTabPane label="领券中心" name="claim" />
      <ElTabPane label="我的优惠券" name="mine" />
    </ElTabs>

    <div v-if="activeTab === 'claim'" class="coupon-grid">
      <div v-for="coupon in claimableList" :key="coupon.couponId" class="coupon-card">
        <div class="coupon-card__amount">
          <span class="symbol">¥</span>{{ formatAmount(coupon.amount) }}
        </div>
        <div class="coupon-card__body">
          <p class="coupon-card__name">{{ coupon.couponName }}</p>
          <p class="coupon-card__rule">满 {{ formatAmount(coupon.minPoint || 0) }} 可用</p>
          <p class="coupon-card__time">有效期至 {{ formatDate(coupon.endTime) }}</p>
          <p v-if="coupon.perLimit" class="coupon-card__limit">
            限领 {{ coupon.perLimit }} 张（已领 {{ coupon.receivedCount || 0 }}）
          </p>
        </div>
        <ElButton
          :disabled="!coupon.canReceive"
          :loading="receivingId === coupon.couponId"
          size="small"
          type="danger"
          @click="handleReceive(coupon.couponId!)"
        >
          {{ coupon.canReceive ? '立即领取' : '已领完' }}
        </ElButton>
      </div>
      <ElEmpty v-if="!claimableList.length" description="暂无可领取优惠券" />
    </div>

    <div v-else class="coupon-grid">
      <div v-for="coupon in myList" :key="coupon.historyId" class="coupon-card coupon-card--mine">
        <div :class="{ disabled: coupon.status !== 0 }" class="coupon-card__amount">
          <span class="symbol">¥</span>{{ formatAmount(coupon.amount) }}
        </div>
        <div class="coupon-card__body">
          <p class="coupon-card__name">{{ coupon.couponName }}</p>
          <p class="coupon-card__rule">满 {{ formatAmount(coupon.minPoint || 0) }} 可用</p>
          <p class="coupon-card__time">有效期至 {{ formatDate(coupon.endTime) }}</p>
          <ElTag :type="statusTagType(coupon.status)" size="small">{{
            statusLabel(coupon.status)
          }}</ElTag>
        </div>
      </div>
      <ElEmpty v-if="!myList.length" description="暂无优惠券">
        <ElButton type="primary" @click="activeTab = 'claim'">去领券</ElButton>
      </ElEmpty>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import {
    fetchPortalCouponClaimable,
    fetchPortalCouponMine,
    fetchPortalCouponReceive
  } from '@/api/portal/coupon'
  import type { PortalClaimableCoupon, PortalMyCoupon } from '@/api/portal/types'
  import { usePortalAuth } from '@/hooks/portal/usePortalAuth'

  defineOptions({ name: 'PortalCoupons' })

  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const activeTab = ref<'claim' | 'mine'>('claim')
  const claimableList = ref<PortalClaimableCoupon[]>([])
  const myList = ref<PortalMyCoupon[]>([])
  const receivingId = ref<number>()

  const formatAmount = (v?: number) => (v != null ? Number(v).toFixed(0) : '0')
  const formatDate = (v?: string) => (v ? v.slice(0, 10) : '-')

  const statusLabel = (status?: number) => {
    if (status === 1) return '已使用'
    if (status === 2) return '已过期'
    return '未使用'
  }

  const statusTagType = (status?: number) => {
    if (status === 1) return 'info'
    if (status === 2) return 'warning'
    return 'success'
  }

  async function loadClaimable() {
    claimableList.value = await fetchPortalCouponClaimable()
  }

  async function loadMine() {
    myList.value = await fetchPortalCouponMine()
  }

  async function handleTabChange() {
    loading.value = true
    try {
      if (activeTab.value === 'claim') {
        await loadClaimable()
      } else {
        await loadMine()
      }
    } finally {
      loading.value = false
    }
  }

  async function handleReceive(couponId: number) {
    receivingId.value = couponId
    try {
      await fetchPortalCouponReceive(couponId)
      await loadClaimable()
    } finally {
      receivingId.value = undefined
    }
  }

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    await handleTabChange()
  })
</script>

<style lang="scss" scoped>
  .page-title {
    margin: 0 0 16px;
    font-size: 22px;
  }

  .coupon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
    margin-top: 8px;
  }

  .coupon-card {
    display: flex;
    align-items: center;
    gap: 12px;
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    border: 1px solid #f0f0f0;

    &__amount {
      flex-shrink: 0;
      width: 72px;
      text-align: center;
      font-size: 28px;
      font-weight: 700;
      color: #e1251b;

      .symbol {
        font-size: 14px;
      }

      &.disabled {
        color: #bbb;
      }
    }

    &__body {
      flex: 1;
      min-width: 0;
    }

    &__name {
      margin: 0 0 4px;
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }

    &__rule,
    &__time,
    &__limit {
      margin: 0 0 4px;
      font-size: 12px;
      color: #999;
    }
  }
</style>

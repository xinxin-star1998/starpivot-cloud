<!-- C 端优惠券中心 -->
<template>
  <div v-loading="loading" class="portal-coupons">
    <PortalPageHeader title="优惠券中心" subtitle="领取优惠，畅享购物" />

    <div class="tab-switch">
      <button
        type="button"
        class="tab-switch__btn"
        :class="{ active: activeTab === 'claim' }"
        @click="activeTab = 'claim'; handleTabChange()"
      >
        <ArtSvgIcon icon="ri:gift-line" />
        领券中心
      </button>
      <button
        type="button"
        class="tab-switch__btn"
        :class="{ active: activeTab === 'mine' }"
        @click="activeTab = 'mine'; handleTabChange()"
      >
        <ArtSvgIcon icon="ri:coupon-3-line" />
        我的优惠券
      </button>
    </div>

    <div v-if="activeTab === 'claim'" class="coupon-grid">
      <div v-for="coupon in claimableList" :key="coupon.couponId" class="coupon-card">
        <div class="coupon-card__left">
          <div class="coupon-card__amount">
            <span class="symbol">¥</span>{{ formatAmount(coupon.amount) }}
          </div>
          <p class="coupon-card__rule">满 {{ formatAmount(coupon.minPoint || 0) }} 可用</p>
        </div>
        <div class="coupon-card__body">
          <p class="coupon-card__name">{{ coupon.couponName }}</p>
          <p class="coupon-card__time">有效期至 {{ formatDate(coupon.endTime) }}</p>
          <p v-if="coupon.perLimit" class="coupon-card__limit">
            限领 {{ coupon.perLimit }} 张（已领 {{ coupon.receivedCount || 0 }}）
          </p>
        </div>
        <ElButton
          :disabled="!coupon.canReceive"
          :loading="receivingId === coupon.couponId"
          size="small"
          type="primary"
          class="coupon-card__btn"
          @click="handleReceive(coupon.couponId!)"
        >
          {{ coupon.canReceive ? '立即领取' : '已领完' }}
        </ElButton>
      </div>
      <ElEmpty v-if="!claimableList.length" description="暂无可领取优惠券" />
    </div>

    <div v-else class="coupon-grid">
      <div v-for="coupon in myList" :key="coupon.historyId" class="coupon-card coupon-card--mine">
        <div class="coupon-card__left">
          <div class="coupon-card__amount" :class="{ disabled: coupon.status !== 0 }">
            <span class="symbol">¥</span>{{ formatAmount(coupon.amount) }}
          </div>
          <p class="coupon-card__rule">满 {{ formatAmount(coupon.minPoint || 0) }} 可用</p>
        </div>
        <div class="coupon-card__body">
          <p class="coupon-card__name">{{ coupon.couponName }}</p>
          <p class="coupon-card__time">有效期至 {{ formatDate(coupon.endTime) }}</p>
          <ElTag :type="statusTagType(coupon.status)" size="small">{{
            statusLabel(coupon.status)
          }}</ElTag>
        </div>
      </div>
      <ElEmpty v-if="!myList.length" description="暂无优惠券">
        <ElButton type="primary" @click="activeTab = 'claim'; handleTabChange()">去领券</ElButton>
      </ElEmpty>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {fetchPortalCouponClaimable, fetchPortalCouponMine, fetchPortalCouponReceive} from '@/api/portal/coupon'
import type {PortalClaimableCoupon, PortalMyCoupon} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import PortalPageHeader from '../components/portal-page-header.vue'

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

  .tab-switch {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;

    &__btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 10px 20px;
      border: 1px solid var(--portal-border);
      border-radius: 24px;
      background: var(--portal-bg-elevated);
      color: var(--portal-text-secondary);
      font-size: 14px;
      cursor: pointer;
      transition: all var(--portal-transition);

      svg {
        font-size: 18px;
      }

      &:hover {
        border-color: #ffb8b8;
        color: var(--portal-primary);
      }

      &.active {
        background: var(--portal-primary-gradient);
        border-color: transparent;
        color: #fff;
        font-weight: 600;
      }
    }
  }

  .coupon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    gap: 16px;
  }

  .coupon-card {
    display: flex;
    align-items: center;
    gap: 0;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius);
    overflow: hidden;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);
    transition: box-shadow var(--portal-transition);

    &:hover {
      box-shadow: var(--portal-shadow);
    }

    &__left {
      flex-shrink: 0;
      width: 100px;
      padding: 20px 12px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%);
      color: #fff;
      text-align: center;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        right: -6px;
        top: 50%;
        transform: translateY(-50%);
        width: 12px;
        height: 12px;
        background: var(--portal-bg-elevated);
        border-radius: 50%;
      }
    }

    &--mine &__left {
      background: linear-gradient(135deg, #636e72 0%, #2d3436 100%);
    }

    &__amount {
      font-size: 32px;
      font-weight: 800;
      line-height: 1;
      letter-spacing: -0.03em;

      .symbol {
        font-size: 14px;
      }

      &.disabled {
        opacity: 0.5;
      }
    }

    &__rule {
      margin: 6px 0 0;
      font-size: 11px;
      opacity: 0.85;
    }

    &__body {
      flex: 1;
      min-width: 0;
      padding: 16px;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 15px;
      font-weight: 700;
      color: var(--portal-text);
    }

    &__time,
    &__limit {
      margin: 0 0 4px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__btn {
      flex-shrink: 0;
      margin-right: 16px;
      border-radius: 16px;
      background: var(--portal-primary-gradient);
      border: none;
    }
  }
</style>

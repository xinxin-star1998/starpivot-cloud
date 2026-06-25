<!-- 首页右侧用户面板 -->
<template>
  <aside class="user-panel">
    <div class="user-panel__avatar">
      <ElAvatar :size="48" :src="avatarUrl">
        {{ avatarText }}
      </ElAvatar>
    </div>
    <p class="user-panel__greeting">{{ greeting }}</p>

    <template v-if="portalStore.isLogin">
      <p class="user-panel__name">{{ displayName }}</p>
      <div class="user-panel__actions">
        <ElButton type="danger" size="small" @click="router.push('/portal/orders')"
          >我的订单</ElButton
        >
        <ElButton size="small" @click="router.push('/portal/cart')">购物车</ElButton>
        <ElButton size="small" @click="router.push('/portal/coupons')">领券中心</ElButton>
      </div>
    </template>
    <template v-else>
      <p class="user-panel__tip">登录后享更多优惠</p>
      <ElButton type="danger" class="user-panel__login" @click="router.push('/portal/login')">
        立即登录
      </ElButton>
    </template>

    <div class="user-panel__coupons" @click="router.push('/portal/coupons')">
      <div v-for="item in couponHints" :key="item.label" class="coupon-item">
        <span class="coupon-item__amount">{{ item.amount }}</span>
        <span class="coupon-item__label">{{ item.label }}</span>
      </div>
    </div>

    <div class="user-panel__shortcuts">
      <a
        v-for="link in shortcuts"
        :key="link.label"
        class="shortcut-link"
        href="javascript:;"
        @click.prevent="link.action()"
      >
        {{ link.label }}
      </a>
    </div>
  </aside>
</template>

<script setup lang="ts">
  import { usePortalMemberStore } from '@/store/modules/portal-member'

  defineOptions({ name: 'PortalHomeUserPanel' })

  const router = useRouter()
  const portalStore = usePortalMemberStore()

  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return 'Hi~ 夜深了'
    if (hour < 12) return 'Hi~ 上午好'
    if (hour < 14) return 'Hi~ 中午好'
    if (hour < 18) return 'Hi~ 下午好'
    return 'Hi~ 晚上好'
  })

  const displayName = computed(
    () => portalStore.member?.nickname || portalStore.member?.username || '会员'
  )

  const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())

  const avatarUrl = computed(() => portalStore.member?.header || '')

  const couponHints = [
    { amount: '¥5', label: '新人券' },
    { amount: '¥10', label: '满减券' },
    { amount: '包邮', label: '运费券' }
  ]

  const shortcuts = [
    { label: '待付款', action: () => router.push('/portal/orders') },
    { label: '待收货', action: () => router.push('/portal/orders') },
    { label: '待评价', action: () => router.push('/portal/orders') },
    { label: '退换货', action: () => router.push('/portal/orders') }
  ]
</script>

<style scoped lang="scss">
  .user-panel {
    width: 190px;
    flex-shrink: 0;
    padding: 20px 14px;
    background: #fff;
    border-left: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-panel__avatar {
    margin-bottom: 8px;
  }

  .user-panel__greeting {
    margin: 0 0 4px;
    font-size: 14px;
    color: #666;
  }

  .user-panel__name {
    margin: 0 0 10px;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-panel__tip {
    margin: 0 0 10px;
    font-size: 12px;
    color: #999;
  }

  .user-panel__login {
    width: 100%;
    margin-bottom: 12px;
  }

  .user-panel__actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    margin-bottom: 12px;

    .el-button {
      width: 100%;
      margin: 0;
    }
  }

  .user-panel__coupons {
    display: flex;
    gap: 6px;
    width: 100%;
    margin: 12px 0;
    cursor: pointer;
  }

  .coupon-item {
    flex: 1;
    background: #fff5f5;
    border: 1px dashed #ffc9c9;
    border-radius: 4px;
    padding: 6px 2px;

    &__amount {
      display: block;
      font-size: 13px;
      font-weight: 700;
      color: #e1251b;
      line-height: 1.2;
    }

    &__label {
      display: block;
      font-size: 10px;
      color: #999;
      margin-top: 2px;
    }
  }

  .user-panel__shortcuts {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px 4px;
    width: 100%;
    margin-top: auto;
    padding-top: 12px;
    border-top: 1px solid #f5f5f5;
  }

  .shortcut-link {
    font-size: 12px;
    color: #666;
    text-decoration: none;

    &:hover {
      color: #e1251b;
    }
  }
</style>

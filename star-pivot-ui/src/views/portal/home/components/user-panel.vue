<!-- 首页右侧用户面板 -->
<template>
  <aside class="user-panel">
    <div class="user-panel__profile">
      <div class="user-panel__avatar-wrap">
        <ElAvatar :size="56" :src="avatarUrl" class="user-panel__avatar">
          {{ avatarText }}
        </ElAvatar>
      </div>
      <p class="user-panel__greeting">{{ greeting }}</p>
      <p v-if="portalStore.isLogin" class="user-panel__name">{{ displayName }}</p>
      <p v-else class="user-panel__tip">登录后享更多优惠</p>
    </div>

    <template v-if="portalStore.isLogin">
      <div class="user-panel__actions">
        <button type="button" class="action-btn action-btn--primary" @click="router.push('/portal/orders')">
          <ArtSvgIcon icon="ri:file-list-3-line" />
          我的订单
        </button>
        <button type="button" class="action-btn" @click="router.push('/portal/cart')">
          <ArtSvgIcon icon="ri:shopping-cart-2-line" />
          购物车
        </button>
        <button type="button" class="action-btn" @click="router.push('/portal/account/security')">
          <ArtSvgIcon icon="ri:shield-user-line" />
          账号安全
        </button>
      </div>
    </template>
    <template v-else>
      <div class="user-panel__auth">
        <ElButton type="primary" class="user-panel__login" @click="router.push('/portal/login')">
          立即登录
        </ElButton>
        <button type="button" class="user-panel__register" @click="router.push('/portal/register')">
          免费注册
        </button>
      </div>
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
        <ArtSvgIcon :icon="link.icon" />
        <span>{{ link.label }}</span>
        <span v-if="link.badge && pendingReviewCount > 0" class="shortcut-badge">
          {{ pendingReviewCount > 99 ? '99+' : pendingReviewCount }}
        </span>
      </a>
    </div>
  </aside>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {fetchPortalPendingReviewCount} from '@/api/portal/comment'
import {usePortalMemberStore} from '@/store/modules/portal-member'

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

  const pendingReviewCount = ref(0)

  async function loadPendingReviewCount() {
    if (!portalStore.isLogin) {
      pendingReviewCount.value = 0
      return
    }
    try {
      const res = await fetchPortalPendingReviewCount()
      pendingReviewCount.value = res.count ?? 0
    } catch {
      pendingReviewCount.value = 0
    }
  }

  watch(
    () => portalStore.isLogin,
    (login) => {
      if (login) loadPendingReviewCount()
      else pendingReviewCount.value = 0
    },
    { immediate: true }
  )

  onMounted(() => {
    window.addEventListener('portal-review-changed', loadPendingReviewCount)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-review-changed', loadPendingReviewCount)
  })

  const shortcuts = [
    {
      label: '待付款',
      icon: 'ri:wallet-3-line',
      action: () => router.push('/portal/orders?status=0')
    },
    {
      label: '待收货',
      icon: 'ri:truck-line',
      action: () => router.push('/portal/orders?status=2')
    },
    {
      label: '待评价',
      icon: 'ri:chat-smile-2-line',
      badge: true,
      action: () => router.push('/portal/orders?status=review')
    },
    {
      label: '退换货',
      icon: 'ri:exchange-line',
      action: () => router.push('/portal/orders?status=3')
    }
  ]
</script>

<style scoped lang="scss">

  .user-panel {
    width: 200px;
    flex-shrink: 0;
    padding: 20px 16px;
    background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
    border-left: 1px solid var(--portal-border);
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-panel__profile {
    width: 100%;
    margin-bottom: 12px;
  }

  .user-panel__avatar-wrap {
    margin-bottom: 10px;
  }

  .user-panel__avatar {
    border: 3px solid #fff;
    box-shadow: var(--portal-shadow-sm);
    background: var(--portal-primary-gradient);
    color: #fff;
    font-weight: 700;
  }

  .user-panel__greeting {
    margin: 0 0 4px;
    font-size: 13px;
    color: var(--portal-text-secondary);
  }

  .user-panel__name {
    margin: 0;
    font-size: 15px;
    font-weight: 700;
    color: var(--portal-text);
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-panel__tip {
    margin: 0;
    font-size: 12px;
    color: var(--portal-text-muted);
  }

  .user-panel__login {
    width: 100%;
    margin-bottom: 8px;
    border-radius: 20px;
    background: var(--portal-primary-gradient);
    border: none;
    font-weight: 600;
  }

  .user-panel__auth {
    width: 100%;
    margin-bottom: 12px;
  }

  .user-panel__register {
    display: block;
    width: 100%;
    padding: 0;
    border: none;
    background: none;
    color: var(--portal-primary);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    text-align: center;

    &:hover {
      text-decoration: underline;
    }
  }

  .user-panel__actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    margin-bottom: 12px;
  }

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    width: 100%;
    padding: 8px 12px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    background: #fff;
    color: var(--portal-text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all var(--portal-transition);

    svg {
      font-size: 16px;
    }

    &:hover {
      border-color: var(--portal-primary);
      color: var(--portal-primary);
      background: var(--portal-primary-light);
    }

    &--primary {
      background: var(--portal-primary-light);
      border-color: transparent;
      color: var(--portal-primary);
      font-weight: 600;
    }
  }

  .user-panel__coupons {
    display: flex;
    gap: 6px;
    width: 100%;
    margin: 8px 0 12px;
    cursor: pointer;
  }

  .coupon-item {
    flex: 1;
    background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
    border: 1px dashed #ffb8b8;
    border-radius: var(--portal-radius-sm);
    padding: 8px 4px;
    transition: transform var(--portal-transition);

    &:hover {
      transform: scale(1.03);
    }

    &__amount {
      display: block;
      font-size: 14px;
      font-weight: 800;
      color: var(--portal-primary);
      line-height: 1.2;
    }

    &__label {
      display: block;
      font-size: 10px;
      color: var(--portal-text-muted);
      margin-top: 2px;
    }
  }

  .user-panel__shortcuts {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    width: 100%;
    margin-top: auto;
    padding-top: 12px;
    border-top: 1px solid var(--portal-border);
  }

  .shortcut-link {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 4px;
    border-radius: var(--portal-radius-sm);
    font-size: 11px;
    color: var(--portal-text-secondary);
    text-decoration: none;
    transition: all var(--portal-transition);
    position: relative;

    .shortcut-badge {
      position: absolute;
      top: 2px;
      right: 2px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      border-radius: 8px;
      background: var(--portal-primary);
      color: #fff;
      font-size: 10px;
      line-height: 16px;
      font-weight: 700;
    }

    svg {
      font-size: 20px;
    }

    &:hover {
      color: var(--portal-primary);
      background: var(--portal-primary-light);
    }
  }
</style>

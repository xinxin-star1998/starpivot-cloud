<template>
  <header class="portal-header">
    <div class="portal-header__inner">
      <div class="portal-header__brand" @click="router.push('/portal')">
        <span class="logo">StarPivot</span>
        <span class="sub">商城</span>
      </div>

      <nav class="portal-header__nav">
        <RouterLink to="/portal" class="nav-link" active-class="is-active">首页</RouterLink>
        <RouterLink to="/portal/cart" class="nav-link" active-class="is-active">
          购物车
          <ElBadge v-if="cartCount > 0" :value="cartCount" class="cart-badge" />
        </RouterLink>
        <RouterLink to="/portal/orders" class="nav-link" active-class="is-active"
          >我的订单</RouterLink
        >
        <RouterLink active-class="is-active" class="nav-link" to="/portal/coupons"
          >优惠券</RouterLink
        >
      </nav>

      <div class="portal-header__actions">
        <template v-if="portalStore.isLogin">
          <span class="username">{{
            portalStore.member?.nickname || portalStore.member?.username
          }}</span>
          <ElButton link type="primary" @click="handleLogout">退出</ElButton>
        </template>
        <ElButton v-else type="primary" plain @click="router.push('/portal/login')">登录</ElButton>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
  import { fetchPortalCart } from '@/api/portal/cart'
  import { usePortalMemberStore } from '@/store/modules/portal-member'

  defineOptions({ name: 'PortalHeader' })

  const router = useRouter()
  const portalStore = usePortalMemberStore()
  const cartCount = ref(0)

  async function refreshCartCount() {
    if (!portalStore.isLogin) {
      cartCount.value = 0
      return
    }
    try {
      const cart = await fetchPortalCart()
      cartCount.value = cart.items?.reduce((sum, item) => sum + (item.quantity || 0), 0) || 0
    } catch {
      cartCount.value = 0
    }
  }

  function handleLogout() {
    portalStore.logOut()
    cartCount.value = 0
    router.push('/portal/login')
  }

  watch(
    () => portalStore.isLogin,
    () => refreshCartCount(),
    { immediate: true }
  )

  onMounted(() => {
    window.addEventListener('portal-cart-changed', refreshCartCount)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-cart-changed', refreshCartCount)
  })
</script>

<style scoped lang="scss">
  .portal-header {
    background: #fff;
    box-shadow: 0 1px 4px rgb(0 0 0 / 8%);
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .portal-header__inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 16px;
    height: 56px;
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .portal-header__brand {
    display: flex;
    align-items: baseline;
    gap: 6px;
    cursor: pointer;
    user-select: none;

    .logo {
      font-size: 20px;
      font-weight: 700;
      color: #e1251b;
    }

    .sub {
      font-size: 14px;
      color: #666;
    }
  }

  .portal-header__nav {
    flex: 1;
    display: flex;
    gap: 20px;

    .nav-link {
      color: #333;
      text-decoration: none;
      font-size: 15px;
      position: relative;

      &.is-active {
        color: #e1251b;
        font-weight: 600;
      }
    }

    .cart-badge {
      margin-left: 4px;
    }
  }

  .portal-header__actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .username {
      font-size: 14px;
      color: #666;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
</style>

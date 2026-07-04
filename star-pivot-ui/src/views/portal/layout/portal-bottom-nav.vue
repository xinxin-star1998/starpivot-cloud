<!-- 移动端底部导航 -->
<template>
  <nav class="portal-bottom-nav" aria-label="主导航">
    <button
      type="button"
      class="portal-bottom-nav__item"
      :class="{ 'is-active': isActive('/portal') && route.path === '/portal' }"
      @click="router.push('/portal')"
    >
      <ArtSvgIcon icon="ri:home-4-line" />
      <span>首页</span>
    </button>
    <button type="button" class="portal-bottom-nav__item" @click="openPortalCategoryDrawer()">
      <ArtSvgIcon icon="ri:apps-2-line" />
      <span>分类</span>
    </button>
    <button
      type="button"
      class="portal-bottom-nav__item"
      :class="{ 'is-active': isActive('/portal/cart') }"
      @click="router.push('/portal/cart')"
    >
      <span class="portal-bottom-nav__icon-wrap">
        <ArtSvgIcon icon="ri:shopping-cart-2-line" />
        <span v-if="cartCount > 0" class="portal-bottom-nav__badge">
          {{ cartCount > 99 ? '99+' : cartCount }}
        </span>
      </span>
      <span>购物车</span>
    </button>
    <button
      type="button"
      class="portal-bottom-nav__item"
      :class="{ 'is-active': isActive('/portal/account') }"
      @click="router.push('/portal/account')"
    >
      <ArtSvgIcon icon="ri:user-3-line" />
      <span>我的</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import { fetchPortalCart } from '@/api/portal/cart'
import { usePortalMemberStore } from '@/store/modules/portal-member'
import { openPortalCategoryDrawer } from '@/utils/portal/category-drawer'

defineOptions({ name: 'PortalBottomNav' })

  const route = useRoute()
  const router = useRouter()
  const portalStore = usePortalMemberStore()
  const cartCount = ref(0)

  function isActive(prefix: string) {
    if (prefix === '/portal') return route.path === '/portal'
    return route.path.startsWith(prefix)
  }

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
  .portal-bottom-nav {
    display: none;
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 190;
    height: 56px;
    padding-bottom: env(safe-area-inset-bottom, 0);
    background: rgb(255 255 255 / 96%);
    backdrop-filter: blur(12px);
    border-top: 1px solid var(--portal-border);
    box-shadow: 0 -4px 16px rgb(45 52 54 / 6%);

    @media (width <= 900px) {
      display: flex;
      align-items: stretch;
      justify-content: space-around;
    }

    &__item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 2px;
      border: none;
      background: none;
      color: var(--portal-text-secondary);
      font-size: 10px;
      cursor: pointer;
      transition: color var(--portal-transition);

      svg {
        font-size: 22px;
      }

      &.is-active {
        color: var(--portal-primary);
        font-weight: 600;
      }
    }

    &__icon-wrap {
      position: relative;
      display: flex;
    }

    &__badge {
      position: absolute;
      top: -4px;
      right: -10px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      border-radius: 8px;
      background: var(--portal-primary);
      color: #fff;
      font-size: 10px;
      line-height: 16px;
      font-weight: 700;
      text-align: center;
    }
  }
</style>

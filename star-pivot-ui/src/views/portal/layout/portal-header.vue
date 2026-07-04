<template>
  <header class="portal-header">
    <div class="portal-header__top">
      <div class="portal-header__top-inner">
        <span class="portal-header__slogan">欢迎来到 StarPivot 商城</span>
        <div class="portal-header__top-actions">
          <template v-if="portalStore.isLogin">
            <button type="button" class="portal-header__link" @click="router.push('/portal/account')">
              会员中心
            </button>
            <button type="button" class="portal-header__link" @click="router.push('/portal/account/security')">
              账号安全
            </button>
            <span class="portal-header__username">{{
              portalStore.member?.nickname || portalStore.member?.username
            }}</span>
            <button type="button" class="portal-header__link" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <button type="button" class="portal-header__link" @click="router.push('/portal/login')">登录</button>
            <span class="portal-header__sep">|</span>
            <button type="button" class="portal-header__link" @click="router.push('/portal/register')">
              注册
            </button>
          </template>
        </div>
      </div>
    </div>

    <div class="portal-header__main">
      <div class="portal-header__inner">
        <div class="portal-header__brand" @click="router.push('/portal')">
          <span class="portal-header__logo">StarPivot</span>
          <span class="portal-header__sub">商城</span>
        </div>

        <button
          type="button"
          class="portal-header__mobile-category"
          aria-label="全部分类"
          @click="openPortalCategoryDrawer()"
        >
          <ArtSvgIcon icon="ri:apps-2-line" />
        </button>

        <button
          type="button"
          class="portal-header__mobile-search"
          aria-label="搜索商品"
          @click="mobileSearchVisible = true"
        >
          <ArtSvgIcon icon="ri:search-line" />
        </button>

        <div class="portal-header__search">
          <ElInput
            v-model="searchKeyword"
            placeholder="搜索商品..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <ArtSvgIcon icon="ri:search-line" class="search-icon" />
            </template>
            <template #append>
              <ElButton type="primary" class="search-btn" @click="handleSearch">搜索</ElButton>
            </template>
          </ElInput>
        </div>

        <nav class="portal-header__nav">
          <RouterLink to="/portal" class="nav-item" active-class="is-active">
            <ArtSvgIcon icon="ri:home-4-line" />
            <span>首页</span>
          </RouterLink>
          <RouterLink to="/portal/account" class="nav-item" active-class="is-active">
            <span class="nav-item__icon-wrap">
              <ArtSvgIcon icon="ri:user-3-line" />
              <ElBadge
                v-if="pendingReviewCount > 0"
                :value="pendingReviewCount"
                :max="99"
                class="nav-badge"
              />
            </span>
            <span>我的</span>
          </RouterLink>
          <RouterLink to="/portal/cart" class="nav-item" active-class="is-active">
            <span class="nav-item__icon-wrap">
              <ArtSvgIcon icon="ri:shopping-cart-2-line" />
              <ElBadge v-if="cartCount > 0" :value="cartCount" :max="99" class="nav-badge" />
            </span>
            <span>购物车</span>
          </RouterLink>
          <RouterLink to="/portal/orders" class="nav-item" active-class="is-active">
            <ArtSvgIcon icon="ri:file-list-3-line" />
            <span>订单</span>
          </RouterLink>
          <RouterLink to="/portal/coupons" class="nav-item" active-class="is-active">
            <ArtSvgIcon icon="ri:coupon-3-line" />
            <span>优惠券</span>
          </RouterLink>
        </nav>
      </div>
    </div>

    <ElDrawer v-model="mobileSearchVisible" title="搜索商品" direction="ttb" size="auto" destroy-on-close @open="refreshMobileSearchHistory">
      <ElInput
        v-model="mobileSearchKeyword"
        placeholder="输入商品关键词"
        size="large"
        clearable
        autofocus
        @keyup.enter="handleMobileSearch"
      >
        <template #prefix>
          <ArtSvgIcon icon="ri:search-line" />
        </template>
      </ElInput>
      <ElButton type="primary" class="mobile-search-submit" @click="handleMobileSearch">搜索</ElButton>
      <div v-if="mobileSearchHistory.length" class="mobile-search-history">
        <span class="mobile-search-history__label">最近搜索</span>
        <div class="mobile-search-history__tags">
          <button
            v-for="item in mobileSearchHistory"
            :key="item"
            type="button"
            class="mobile-search-history__tag"
            @click="searchMobileHistory(item)"
          >
            {{ item }}
          </button>
        </div>
      </div>
    </ElDrawer>
  </header>
</template>

<script setup lang="ts">
import {fetchPortalCart} from '@/api/portal/cart'
import {fetchPortalPendingReviewCount} from '@/api/portal/comment'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {usePortalMemberStore} from '@/store/modules/portal-member'
import {addPortalSearchKeyword, getPortalSearchHistory} from '@/utils/portal/search-history'
import {openPortalCategoryDrawer} from '@/utils/portal/category-drawer'

defineOptions({ name: 'PortalHeader' })

  const route = useRoute()
  const router = useRouter()
  const portalStore = usePortalMemberStore()
  const cartCount = ref(0)
  const pendingReviewCount = ref(0)
  const searchKeyword = ref('')
  const mobileSearchVisible = ref(false)
  const mobileSearchKeyword = ref('')
  const mobileSearchHistory = ref<string[]>([])

  async function refreshPendingReviewCount() {
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

  function handleSearch() {
    const keyword = searchKeyword.value.trim()
    if (keyword) addPortalSearchKeyword(keyword)
    router.push({
      path: '/portal/search',
      query: {
        ...(keyword ? { keyword } : {}),
        ...(route.query.catalogId ? { catalogId: route.query.catalogId } : {}),
        ...(route.query.brandId ? { brandId: route.query.brandId } : {})
      }
    })
  }

  function handleMobileSearch() {
    const keyword = mobileSearchKeyword.value.trim()
    mobileSearchVisible.value = false
    if (keyword) addPortalSearchKeyword(keyword)
    router.push({
      path: '/portal/search',
      query: keyword ? { keyword } : undefined
    })
  }

  function refreshMobileSearchHistory() {
    mobileSearchHistory.value = getPortalSearchHistory()
  }

  function searchMobileHistory(keyword: string) {
    mobileSearchKeyword.value = keyword
    handleMobileSearch()
  }

  watch(
    () => route.query.keyword,
    (kw) => {
      if (route.path.startsWith('/portal/search')) {
        searchKeyword.value = typeof kw === 'string' ? kw : ''
      }
    },
    { immediate: true }
  )

  watch(
    () => portalStore.isLogin,
    () => {
      refreshCartCount()
      refreshPendingReviewCount()
    },
    { immediate: true }
  )

  onMounted(() => {
    window.addEventListener('portal-cart-changed', refreshCartCount)
    window.addEventListener('portal-review-changed', refreshPendingReviewCount)
    window.addEventListener('portal-search-history-changed', refreshMobileSearchHistory)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-cart-changed', refreshCartCount)
    window.removeEventListener('portal-review-changed', refreshPendingReviewCount)
    window.removeEventListener('portal-search-history-changed', refreshMobileSearchHistory)
  })
</script>

<style scoped lang="scss">

  .portal-header {
    position: sticky;
    top: 0;
    z-index: 200;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--portal-border);
    box-shadow: var(--portal-shadow-sm);
  }

  .portal-header__top {
    background: linear-gradient(90deg, #2d3436 0%, #636e72 100%);
    color: rgb(255 255 255 / 85%);
    font-size: 12px;
  }

  .portal-header__top-inner {
    max-width: var(--portal-max-width);
    margin: 0 auto;
    padding: 0 20px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .portal-header__top-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .portal-header__username {
    color: rgb(255 255 255 / 95%);
  }

  .portal-header__link {
    background: none;
    border: none;
    padding: 0;
    color: rgb(255 255 255 / 75%);
    cursor: pointer;
    font-size: 12px;
    transition: color var(--portal-transition);

    &:hover {
      color: #fff;
    }
  }

  .portal-header__sep {
    color: rgb(255 255 255 / 40%);
    font-size: 12px;
    user-select: none;
  }

  .portal-header__main {
    padding: 0 20px;
  }

  .portal-header__inner {
    max-width: var(--portal-max-width);
    margin: 0 auto;
    height: var(--portal-header-height);
    display: flex;
    align-items: center;
    gap: 28px;
  }

  .portal-header__brand {
    display: flex;
    align-items: baseline;
    gap: 6px;
    cursor: pointer;
    user-select: none;
    flex-shrink: 0;
  }

  .portal-header__logo {
    font-size: 24px;
    font-weight: 800;
    letter-spacing: -0.03em;
    background: var(--portal-primary-gradient);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .portal-header__sub {
    font-size: 13px;
    color: var(--portal-text-secondary);
    font-weight: 500;
  }

  .portal-header__search {
    flex: 1;
    max-width: 520px;

    :deep(.el-input-group__append) {
      padding: 0;
      border: none;
      background: transparent;
      box-shadow: none;
    }

    :deep(.el-input__wrapper) {
      border-radius: var(--portal-radius-sm) 0 0 var(--portal-radius-sm);
      box-shadow: 0 0 0 1px var(--portal-border-strong) inset;
    }

    .search-icon {
      font-size: 18px;
      color: var(--portal-text-muted);
    }

    .search-btn {
      border-radius: 0 var(--portal-radius-sm) var(--portal-radius-sm) 0;
      background: var(--portal-primary-gradient);
      border: none;
      padding: 0 20px;
      height: 40px;
      font-weight: 600;

      &:hover {
        opacity: 0.92;
      }
    }
  }

  .portal-header__nav {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
  }

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: 6px 12px;
    border-radius: var(--portal-radius-sm);
    color: var(--portal-text-secondary);
    text-decoration: none;
    font-size: 11px;
    transition: all var(--portal-transition);

    svg {
      font-size: 22px;
    }

    &__icon-wrap {
      position: relative;
      display: flex;
    }

    &:hover {
      color: var(--portal-primary);
      background: var(--portal-primary-light);
    }

    &.is-active {
      color: var(--portal-primary);
      font-weight: 600;
    }
  }

  .nav-badge {
    position: absolute;
    top: -6px;
    right: -10px;

    :deep(.el-badge__content) {
      background: var(--portal-primary);
      border: none;
    }
  }

  @media (width <= 900px) {
    .portal-header__search {
      display: none;
    }

    .portal-header__mobile-search,
    .portal-header__mobile-category {
      display: inline-flex;
    }

    .portal-header__inner {
      gap: 8px;
    }

    .nav-item span:not(.nav-item__icon-wrap) {
      display: none;
    }
  }

  .portal-header__mobile-category,
  .portal-header__mobile-search {
    display: none;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    background: #fff;
    color: var(--portal-text-secondary);
    cursor: pointer;
    flex-shrink: 0;

    svg {
      font-size: 20px;
    }

    &:hover {
      color: var(--portal-primary);
      border-color: var(--portal-primary);
      background: var(--portal-primary-light);
    }
  }

  .portal-header__mobile-search {
    margin-left: auto;
  }

  .mobile-search-submit {
    width: 100%;
    margin-top: 16px;
    background: var(--portal-primary-gradient);
    border: none;
    font-weight: 600;
  }

  .mobile-search-history {
    margin-top: 16px;

    &__label {
      display: block;
      margin-bottom: 8px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    &__tag {
      padding: 4px 12px;
      border: 1px solid var(--portal-border);
      border-radius: 14px;
      background: var(--portal-bg-elevated);
      color: var(--portal-text-secondary);
      font-size: 12px;
      cursor: pointer;
    }
  }
</style>

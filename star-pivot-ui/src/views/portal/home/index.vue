<!-- C 端商城首页 -->
<template>
  <div class="portal-home">
    <PortalHomeSkeleton v-if="loading" />

    <template v-else>
      <!-- 京东式 Hero：左分类 + 中轮播 + 右用户 -->
      <section class="portal-home__hero">
        <HomeCategoryNav
          class="portal-home__category-nav"
          :categories="categoryTree"
          :category-brands="categoryBrands"
          :brand-logos="brandLogoUrls"
          @select-category="selectCategory"
          @select-brand="selectBrand"
        />

        <div class="portal-home__banner-wrap">
          <ElCarousel v-if="banners.length" height="480px" class="portal-home__banner">
            <ElCarouselItem v-for="item in banners" :key="item.id">
              <a v-if="item.url" :href="item.url" target="_blank" rel="noopener" class="banner-link">
                <img :src="bannerUrls.get(item.pic || '') || ''" :alt="item.name" class="banner-img" />
              </a>
              <img
                v-else
                :src="bannerUrls.get(item.pic || '') || ''"
                :alt="item.name"
                class="banner-img"
              />
            </ElCarouselItem>
          </ElCarousel>
          <div v-else class="portal-home__banner-placeholder">
            <ElEmpty description="暂无轮播" />
          </div>
        </div>

        <div class="portal-home__user-wrap">
          <HomeUserPanel />
        </div>

        <button
          v-if="showHomeSearch"
          type="button"
          class="portal-home__category-entry"
          @click="openPortalCategoryDrawer()"
        >
          <ArtSvgIcon icon="ri:apps-2-line" />
          <span>全部分类</span>
          <ArtSvgIcon icon="ri:arrow-right-s-line" class="portal-home__category-entry-arrow" />
        </button>
      </section>

      <HomeMarketingGrid
        :blocks="homeBlocks"
        :image-urls="marketingImageUrls"
        :placeholder-img="placeholderImg"
        @go-product="goDetail"
        @open-link="openMarketingLink"
      />

      <HomeHotCategories
        :items="hotCategories"
        :icon-urls="hotCategoryIconUrls"
        @select="selectHotCategory"
      />

      <PortalRecentBrowse />

      <section v-if="showHomeSearch" class="portal-home__quick-search">
        <PortalSearchBar v-model="homeSearchKeyword" placeholder="搜一搜心仪好物" @search="handleHomeSearch" />
        <PortalSearchHints compact @select="searchFromHint" />
      </section>

      <section v-if="guessProducts.length" class="portal-home__section">
        <div class="section-head">
          <div class="section-head__left">
            <h2 class="section-title">猜你喜欢</h2>
            <p class="section-subtitle">根据你的浏览足迹推荐</p>
          </div>
        </div>
        <div class="product-grid">
          <PortalProductCard
            v-for="item in guessProducts"
            :key="`guess-${item.id}`"
            :spu-name="item.spuName"
            :brand-name="item.brandName"
            :price="item.price"
            :image-url="guessCoverUrls.get(item.coverImg || '') || placeholderImg"
            :avg-star="item.avgStar"
            :comment-count="item.commentCount"
            show-actions
            :collected="isCollected(item.id!)"
            :favorite-loading="isFavoriteLoading(item.id!)"
            :cart-loading="isCartLoading(item.id!)"
            @click="goDetail(item.id!)"
            @favorite="toggleFavorite(item)"
            @add-cart="addToCart(item)"
          />
        </div>
      </section>

      <section class="portal-home__section">
        <div class="section-head">
          <div class="section-head__left">
            <h2 class="section-title">精选商品</h2>
            <p v-if="filterHint" class="section-filter-hint">
              {{ filterHint }}
              <button type="button" class="clear-filter" @click="clearFilter">清除筛选</button>
            </p>
          </div>
          <ElSelect v-model="sort" placeholder="排序" style="width: 140px" @change="loadProducts">
            <ElOption label="默认" value="default" />
            <ElOption label="价格升序" value="priceAsc" />
            <ElOption label="价格降序" value="priceDesc" />
            <ElOption label="最新" value="newest" />
          </ElSelect>
        </div>

        <div v-if="products.length" class="product-grid">
          <PortalProductCard
            v-for="item in products"
            :key="item.id"
            :spu-name="item.spuName"
            :brand-name="item.brandName"
            :price="item.price"
            :image-url="coverUrls.get(item.coverImg || '') || placeholderImg"
            :avg-star="item.avgStar"
            :comment-count="item.commentCount"
            show-actions
            :collected="isCollected(item.id!)"
            :favorite-loading="isFavoriteLoading(item.id!)"
            :cart-loading="isCartLoading(item.id!)"
            @click="goDetail(item.id!)"
            @favorite="toggleFavorite(item)"
            @add-cart="addToCart(item)"
          />
        </div>
        <ElEmpty v-else description="暂无商品" />

        <div v-if="total > products.length" class="load-more">
          <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import { fetchPortalCartAdd } from '@/api/portal/cart'
import { fetchPortalCollectAdd, fetchPortalCollectRemove } from '@/api/portal/collect'
import { fetchPortalHome } from '@/api/portal/home'
import { fetchPortalProductDetail, fetchPortalProductRelated, fetchPortalProductSearch } from '@/api/portal/product'
import type {
  PortalBrandBrief,
  PortalCategory,
  PortalHomeBlock,
  PortalHotCategory,
  PortalProductListItem
} from '@/api/portal/types'
import { usePortalAuth } from '@/hooks/portal/usePortalAuth'
import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
import { getPortalBrowseHistory } from '@/utils/portal/browse-history'
import { notifyPortalCartChanged } from '@/utils/portal/cart-event'
import { openPortalCategoryDrawer } from '@/utils/portal/category-drawer'
import { PORTAL_PRODUCT_PLACEHOLDER_IMG } from '@/utils/portal/product-placeholder'
import { addPortalSearchKeyword } from '@/utils/portal/search-history'
import PortalHomeSkeleton from '@/views/portal/components/portal-home-skeleton.vue'
import PortalProductCard from '@/views/portal/components/portal-product-card.vue'
import PortalRecentBrowse from '@/views/portal/components/portal-recent-browse.vue'
import PortalSearchBar from '@/views/portal/components/portal-search-bar.vue'
import PortalSearchHints from '@/views/portal/components/portal-search-hints.vue'
import HomeCategoryNav from './components/category-nav.vue'
import HomeHotCategories from './components/hot-categories.vue'
import HomeMarketingGrid from './components/marketing-grid.vue'
import HomeUserPanel from './components/user-panel.vue'

defineOptions({ name: 'PortalHome' })

  const router = useRouter()
  const route = useRoute()
  const { requireLogin } = usePortalAuth()
  const loading = ref(true)
  const loadingMore = ref(false)
  const banners = ref<{ id?: number; name?: string; pic?: string; url?: string }[]>([])
  const bannerUrls = ref(new Map<string, string>())
  const categoryTree = ref<PortalCategory[]>([])
  const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
  const brandLogoUrls = ref(new Map<number, string>())
  const homeBlocks = ref<PortalHomeBlock[]>([])
  const hotCategories = ref<PortalHotCategory[]>([])
  const hotCategoryIconUrls = ref(new Map<string, string>())
  const marketingImageUrls = ref(new Map<string, string>())
  const products = ref<PortalProductListItem[]>([])
  const coverUrls = ref(new Map<string, string>())
  const guessProducts = ref<PortalProductListItem[]>([])
  const guessCoverUrls = ref(new Map<string, string>())
  const collectedIds = ref(new Set<number>())
  const favoriteLoadingSet = ref(new Set<number>())
  const cartLoadingSet = ref(new Set<number>())
  const searchKeyword = ref('')
  const homeSearchKeyword = ref('')
  const showHomeSearch = ref(false)
  const sort = ref('default')
  const pageNum = ref(1)
  const pageSize = 12
  const total = ref(0)
  const placeholderImg = PORTAL_PRODUCT_PLACEHOLDER_IMG

  const filterHint = computed(() => {
    if (!searchKeyword.value) return ''
    return `搜索「${searchKeyword.value}」`
  })

  function isCollected(spuId: number) {
    return collectedIds.value.has(spuId)
  }

  function isFavoriteLoading(spuId: number) {
    return favoriteLoadingSet.value.has(spuId)
  }

  function isCartLoading(spuId: number) {
    return cartLoadingSet.value.has(spuId)
  }

  async function resolveImages(items: { pic?: string; coverImg?: string }[], target: Ref<Map<string, string>>) {
    const keys = items.map((i) => i.pic || i.coverImg).filter(Boolean) as string[]
    if (!keys.length) return
    const map = await resolveGoodsImageDisplayUrls(keys)
    map.forEach((url, key) => target.value.set(key, url))
  }

  async function resolveBrandLogos(brandsMap: Record<number, PortalBrandBrief[]>) {
    const logos = Object.values(brandsMap)
      .flat()
      .map((b) => b.logo)
      .filter(Boolean) as string[]
    if (!logos.length) return
    const urlMap = await resolveGoodsImageDisplayUrls(logos)
    const logoToUrl = new Map<string, string>()
    urlMap.forEach((url, key) => logoToUrl.set(key, url))
    const result = new Map<number, string>()
    for (const brands of Object.values(brandsMap)) {
      for (const brand of brands) {
        if (brand.brandId != null && brand.logo) {
          const url = logoToUrl.get(brand.logo)
          if (url) result.set(brand.brandId, url)
        }
      }
    }
    brandLogoUrls.value = result
  }

  async function resolveMarketingImages(blocks: PortalHomeBlock[]) {
    const keys = new Set<string>()
    for (const block of blocks) {
      if (block.coverImg) keys.add(block.coverImg)
      const productLists = [
        ...(block.products || []),
        ...(block.sessions || []).flatMap((s) => s.products || [])
      ]
      for (const item of productLists) {
        if (item.coverImg) keys.add(item.coverImg)
      }
    }
    if (!keys.size) return
    const map = await resolveGoodsImageDisplayUrls([...keys])
    map.forEach((url, key) => marketingImageUrls.value.set(key, url))
  }

  async function resolveHotCategoryIcons(items: PortalHotCategory[]) {
    const icons = items.map((item) => item.icon?.trim()).filter(Boolean) as string[]
    if (!icons.length) return
    const map = await resolveGoodsImageDisplayUrls(icons)
    map.forEach((url, key) => hotCategoryIconUrls.value.set(key, url))
  }

  async function loadGuessProducts() {
    const history = getPortalBrowseHistory()
    const seedId = history[0]?.spuId
    if (!seedId) {
      guessProducts.value = []
      return
    }
    try {
      const related = await fetchPortalProductRelated(seedId, 8)
      guessProducts.value = related.filter((item) => item.id !== seedId).slice(0, 8)
      await resolveImages(guessProducts.value, guessCoverUrls)
    } catch {
      guessProducts.value = []
    }
  }

  async function loadHome() {
    const data = await fetchPortalHome()
    banners.value = data.banners || []
    categoryTree.value = data.categories || []
    categoryBrands.value = data.categoryBrands || {}
    homeBlocks.value = data.homeBlocks || []
    hotCategories.value = data.hotCategories || []
    await Promise.all([
      resolveImages(banners.value, bannerUrls),
      resolveBrandLogos(categoryBrands.value),
      resolveMarketingImages(homeBlocks.value),
      resolveHotCategoryIcons(hotCategories.value),
      loadGuessProducts()
    ])
  }

  async function loadProducts(reset = true) {
    if (reset) {
      pageNum.value = 1
      products.value = []
    }
    const res = await fetchPortalProductSearch({
      pageNum: pageNum.value,
      pageSize,
      keyword: searchKeyword.value || undefined,
      sort: sort.value === 'default' ? undefined : sort.value
    })
    total.value = res.total || 0
    const rows = res.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    await resolveImages(rows, coverUrls)
  }

  async function toggleFavorite(item: PortalProductListItem) {
    if (!item.id || !requireLogin()) return
    if (favoriteLoadingSet.value.has(item.id)) return
    favoriteLoadingSet.value.add(item.id)
    const next = !isCollected(item.id)
    try {
      if (next) {
        await fetchPortalCollectAdd(item.id, { silent: true })
        collectedIds.value = new Set([...collectedIds.value, item.id])
      } else {
        await fetchPortalCollectRemove(item.id)
        const nextSet = new Set(collectedIds.value)
        nextSet.delete(item.id)
        collectedIds.value = nextSet
      }
    } finally {
      favoriteLoadingSet.value.delete(item.id)
    }
  }

  async function addToCart(item: PortalProductListItem) {
    if (!item.id || !requireLogin()) return
    if (cartLoadingSet.value.has(item.id)) return
    cartLoadingSet.value.add(item.id)
    try {
      const detail = await fetchPortalProductDetail(item.id)
      const sku = detail.skus?.find((s) => (s.availableStock ?? 1) > 0) || detail.skus?.[0]
      if (!sku?.skuId) {
        ElMessage.warning('该商品暂不可购买')
        return
      }
      await fetchPortalCartAdd({ skuId: sku.skuId, quantity: 1 })
      notifyPortalCartChanged()
    } finally {
      cartLoadingSet.value.delete(item.id)
    }
  }

  function selectCategory(catId?: number) {
    if (catId == null) return
    router.push({ path: '/portal/search', query: { catalogId: String(catId) } })
  }

  function selectBrand(brandId?: number) {
    if (brandId == null) return
    router.push({ path: '/portal/search', query: { brandId: String(brandId) } })
  }

  function selectHotCategory(item: PortalHotCategory) {
    const url = item.url?.trim()
    if (url) {
      if (/^https?:\/\//i.test(url)) {
        window.open(url, '_blank', 'noopener')
        return
      }
      router.push(url)
      return
    }
    if (item.catId != null) {
      router.push({ path: '/portal/search', query: { catalogId: String(item.catId) } })
    }
  }

  function clearFilter() {
    searchKeyword.value = ''
    router.replace({ path: '/portal', query: {} })
    loadProducts(true)
  }

  function applySearchKeyword(keyword: string) {
    searchKeyword.value = keyword
    loadProducts(true)
  }

  function onPortalSearch(e: Event) {
    const keyword = (e as CustomEvent<string>).detail || ''
    applySearchKeyword(keyword)
    scrollToProducts()
  }

  function onBrowseChanged() {
    loadGuessProducts()
  }

  function scrollToProducts() {
    nextTick(() => {
      document.querySelector('.portal-home__section:last-of-type')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
    })
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadProducts(false).finally(() => {
      loadingMore.value = false
    })
  }

  function goDetail(id: number) {
    router.push(`/portal/product/${id}`)
  }

  function openMarketingLink(url: string) {
    if (url.startsWith('http')) {
      window.open(url, '_blank', 'noopener')
      return
    }
    router.push(url)
  }

  function handleHomeSearch(text: string) {
    const keyword = text.trim()
    if (keyword) addPortalSearchKeyword(keyword)
    router.push({
      path: '/portal/search',
      query: keyword ? { keyword } : undefined
    })
  }

  function searchFromHint(keyword: string) {
    homeSearchKeyword.value = keyword
    handleHomeSearch(keyword)
  }

  let homeSearchMq: MediaQueryList | null = null

  function syncHomeSearchVisibility() {
    showHomeSearch.value = homeSearchMq?.matches ?? false
  }

  watch(
    () => route.query.keyword,
    (kw) => {
      const keyword = typeof kw === 'string' ? kw : ''
      if (keyword !== searchKeyword.value) {
        applySearchKeyword(keyword)
      }
    }
  )

  onMounted(async () => {
    const kw = route.query.keyword
    if (typeof kw === 'string' && kw) {
      searchKeyword.value = kw
    }
    homeSearchMq = window.matchMedia('(width <= 900px)')
    syncHomeSearchVisibility()
    homeSearchMq.addEventListener('change', syncHomeSearchVisibility)
    window.addEventListener('portal-search', onPortalSearch)
    window.addEventListener('portal-browse-changed', onBrowseChanged)
    try {
      await Promise.all([loadHome(), loadProducts(true)])
    } finally {
      loading.value = false
    }
  })

  onUnmounted(() => {
    homeSearchMq?.removeEventListener('change', syncHomeSearchVisibility)
    window.removeEventListener('portal-search', onPortalSearch)
    window.removeEventListener('portal-browse-changed', onBrowseChanged)
  })
</script>

<style scoped lang="scss">
  .portal-home__hero {
    position: relative;
    display: flex;
    align-items: stretch;
    min-height: 480px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    overflow: visible;
    margin-bottom: 20px;
    box-shadow: var(--portal-shadow);
  }

  .portal-home__banner-wrap {
    flex: 1;
    min-width: 0;
    height: 480px;
    overflow: hidden;
    background: linear-gradient(135deg, #fafbfc 0%, #f0f2f5 100%);
  }

  .portal-home__banner {
    height: 480px;

    :deep(.el-carousel__container) {
      height: 480px;
    }

    :deep(.el-carousel__indicators) {
      bottom: 16px;
    }

    :deep(.el-carousel__button) {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      opacity: 0.5;
    }

    :deep(.el-carousel__indicator.is-active .el-carousel__button) {
      width: 24px;
      border-radius: 4px;
      opacity: 1;
      background: var(--portal-primary);
    }

    .banner-link,
    .banner-img {
      display: block;
      width: 100%;
      height: 480px;
      object-fit: cover;
    }
  }

  .portal-home__banner-placeholder {
    height: 480px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .portal-home__category-entry {
    display: none;
    align-items: center;
    gap: 8px;
    width: 100%;
    padding: 14px 16px;
    border: none;
    border-top: 1px solid var(--portal-border);
    background: linear-gradient(90deg, #2d3436 0%, #3d4f5f 100%);
    color: rgb(255 255 255 / 92%);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;

    svg {
      font-size: 18px;
    }

    &-arrow {
      margin-left: auto;
      opacity: 0.7;
    }
  }

  .portal-home__quick-search {
    margin-bottom: 20px;
    padding: 16px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    box-shadow: var(--portal-shadow-sm);
  }

  .portal-home__section {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: var(--portal-shadow-sm);
  }

  .section-head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 20px;
    gap: 16px;

    &__left {
      flex: 1;
      min-width: 0;
    }
  }

  .section-title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--portal-text);
    letter-spacing: -0.02em;
    position: relative;
    padding-left: 12px;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 18px;
      border-radius: 2px;
      background: var(--portal-primary-gradient);
    }
  }

  .section-subtitle {
    margin: 6px 0 0 12px;
    font-size: 13px;
    color: var(--portal-text-muted);
  }

  .section-filter-hint {
    margin: 6px 0 0 12px;
    font-size: 13px;
    color: var(--portal-text-secondary);
  }

  .clear-filter {
    margin-left: 8px;
    padding: 0;
    border: none;
    background: none;
    color: var(--portal-primary);
    cursor: pointer;
    font-size: 13px;

    &:hover {
      text-decoration: underline;
    }
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }

  .load-more {
    text-align: center;
    margin-top: 28px;

    :deep(.el-button) {
      min-width: 160px;
      border-radius: 20px;
    }
  }

  @media (width <= 900px) {
    .portal-home__hero {
      flex-direction: column;
      min-height: 0;
    }

    .portal-home__category-nav {
      display: none;
    }

    .portal-home__banner-wrap {
      order: 1;
      height: 220px;
    }

    .portal-home__user-wrap {
      order: 2;
    }

    .portal-home__category-entry {
      display: flex;
      order: 3;
      border-radius: 0 0 var(--portal-radius-lg) var(--portal-radius-lg);
    }

    .portal-home__banner,
    .portal-home__banner-wrap .banner-link,
    .portal-home__banner-wrap .banner-img,
    .portal-home__banner-placeholder {
      height: 220px;
    }

    .portal-home__banner :deep(.el-carousel__container) {
      height: 220px;
    }
  }
</style>

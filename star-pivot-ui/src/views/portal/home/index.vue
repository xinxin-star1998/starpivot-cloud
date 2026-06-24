<!-- C 端商城首页 -->
<template>
  <div v-loading="loading" class="portal-home">
    <!-- 京东式 Hero：左分类 + 中轮播 + 右用户 -->
    <section class="portal-home__hero">
      <HomeCategoryNav
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

      <HomeUserPanel />
    </section>

    <!-- 营销四宫格 -->
    <HomeMarketingGrid
      :blocks="homeBlocks"
      :image-urls="marketingImageUrls"
      :placeholder-img="placeholderImg"
      @go-product="goDetail"
      @open-link="openMarketingLink"
    />

    <!-- 商品列表 -->
    <section class="portal-home__section">
      <div class="section-head">
        <h2 class="section-title">精选商品</h2>
        <ElSelect v-model="sort" placeholder="排序" style="width: 140px" @change="loadProducts">
          <ElOption label="默认" value="default" />
          <ElOption label="价格升序" value="priceAsc" />
          <ElOption label="价格降序" value="priceDesc" />
          <ElOption label="最新" value="newest" />
        </ElSelect>
      </div>

      <div v-if="products.length" class="product-grid">
        <div
          v-for="item in products"
          :key="item.id"
          class="product-card"
          @click="goDetail(item.id!)"
        >
          <div class="product-card__img-wrap">
            <img
              :src="coverUrls.get(item.coverImg || '') || placeholderImg"
              :alt="item.spuName"
              class="product-card__img"
            />
          </div>
          <div class="product-card__body">
            <p class="product-card__name">{{ item.spuName }}</p>
            <p v-if="item.brandName" class="product-card__brand">{{ item.brandName }}</p>
            <p class="product-card__price">
              <span class="currency">¥</span>{{ formatPrice(item.price) }}
            </p>
          </div>
        </div>
      </div>
      <ElEmpty v-else description="暂无商品" />

      <div v-if="total > products.length" class="load-more">
        <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
  import { fetchPortalHome } from '@/api/portal/home'
  import { fetchPortalProductSearch } from '@/api/portal/product'
  import type {
    PortalBrandBrief,
    PortalCategory,
    PortalHomeBlock,
    PortalProductListItem
  } from '@/api/portal/types'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import HomeCategoryNav from './components/category-nav.vue'
  import HomeMarketingGrid from './components/marketing-grid.vue'
  import HomeUserPanel from './components/user-panel.vue'

  defineOptions({ name: 'PortalHome' })

  const router = useRouter()
  const loading = ref(true)
  const loadingMore = ref(false)
  const banners = ref<{ id?: number; name?: string; pic?: string; url?: string }[]>([])
  const bannerUrls = ref(new Map<string, string>())
  const categoryTree = ref<PortalCategory[]>([])
  const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
  const brandLogoUrls = ref(new Map<number, string>())
  const homeBlocks = ref<PortalHomeBlock[]>([])
  const marketingImageUrls = ref(new Map<string, string>())
  const products = ref<PortalProductListItem[]>([])
  const coverUrls = ref(new Map<string, string>())
  const selectedCatalogId = ref<number | undefined>()
  const selectedBrandId = ref<number | undefined>()
  const sort = ref('default')
  const pageNum = ref(1)
  const pageSize = 12
  const total = ref(0)
  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/><text x="50%" y="50%" fill="#999" font-size="14" text-anchor="middle" dy=".3em">暂无图片</text></svg>'
    )

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

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

  async function loadHome() {
    const data = await fetchPortalHome()
    banners.value = data.banners || []
    categoryTree.value = data.categories || []
    categoryBrands.value = data.categoryBrands || {}
    homeBlocks.value = data.homeBlocks || []
    await Promise.all([
      resolveImages(banners.value, bannerUrls),
      resolveBrandLogos(categoryBrands.value),
      resolveMarketingImages(homeBlocks.value)
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
      catalogId: selectedCatalogId.value,
      brandId: selectedBrandId.value,
      sort: sort.value === 'default' ? undefined : sort.value
    })
    total.value = res.total || 0
    const rows = res.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    await resolveImages(rows, coverUrls)
  }

  function selectCategory(catId?: number, toggle = false) {
    if (catId == null) return
    if (toggle && selectedCatalogId.value === catId && !selectedBrandId.value) {
      clearFilter()
      return
    }
    selectedBrandId.value = undefined
    selectedCatalogId.value = catId
    loadProducts(true)
    scrollToProducts()
  }

  function selectBrand(brandId?: number) {
    if (brandId == null) return
    selectedCatalogId.value = undefined
    selectedBrandId.value = brandId
    loadProducts(true)
    scrollToProducts()
  }

  function clearFilter() {
    selectedCatalogId.value = undefined
    selectedBrandId.value = undefined
    loadProducts(true)
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

  onMounted(async () => {
    try {
      await Promise.all([loadHome(), loadProducts(true)])
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .portal-home__hero {
    position: relative;
    display: flex;
    height: 480px;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 16px;
    box-shadow: 0 1px 4px rgb(0 0 0 / 6%);
  }

  .portal-home__banner-wrap {
    flex: 1;
    min-width: 0;
    background: #fafafa;
  }

  .portal-home__banner {
    height: 480px;

    :deep(.el-carousel__container) {
      height: 480px;
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

  .portal-home__section {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .section-title {
    margin: 0 0 16px;
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .section-head .section-title {
    margin-bottom: 0;
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 16px;
  }

  .product-card {
    border: 1px solid #eee;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: box-shadow 0.2s;
    background: #fff;

    &:hover {
      box-shadow: 0 4px 12px rgb(0 0 0 / 10%);
    }

    &__img-wrap {
      aspect-ratio: 1;
      background: #fafafa;
    }

    &__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &__body {
      padding: 12px;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 14px;
      color: #333;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 40px;
    }

    &__brand {
      margin: 0 0 8px;
      font-size: 12px;
      color: #999;
    }

    &__price {
      margin: 0;
      color: #e1251b;
      font-size: 18px;
      font-weight: 600;

      .currency {
        font-size: 13px;
      }
    }
  }

  .load-more {
    text-align: center;
    margin-top: 20px;
  }
</style>

<!-- C 端商品搜索 -->
<template>
  <div v-loading="loading" class="portal-search">
    <PortalPageHeader :title="pageTitle" subtitle="筛选、排序，快速找到心仪商品">
      <template #extra>
        <ElButton v-if="isMobile" class="filter-btn" @click="filterDrawerVisible = true">
          <ArtSvgIcon icon="ri:filter-3-line" />
          筛选
        </ElButton>
        <ElSelect v-model="sort" placeholder="排序" style="width: 140px" @change="loadProducts(true)">
          <ElOption label="默认" value="default" />
          <ElOption label="价格升序" value="priceAsc" />
          <ElOption label="价格降序" value="priceDesc" />
          <ElOption label="最新" value="newest" />
        </ElSelect>
      </template>
    </PortalPageHeader>

    <PortalSearchBar v-model="inputKeyword" class="portal-search__bar" @search="handleSearchSubmit" />

    <div class="portal-search__layout">
      <SearchCategorySidebar
        :categories="categories"
        :brands="visibleBrands"
        :selected-catalog-id="catalogId"
        :selected-brand-id="brandId"
        :brand-logo-urls="brandLogoUrls"
        @select-category="selectCategory"
        @select-brand="selectBrand"
      />

      <div class="portal-search__main">
        <PortalSearchHints v-if="showSearchHints" @select="searchByKeyword" />

        <div v-if="filterHint" class="filter-bar">
          <span>{{ filterHint }}</span>
          <button type="button" class="clear-filter" @click="clearFilter">清除筛选</button>
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
              <PortalProductRating :avg-star="item.avgStar" :comment-count="item.commentCount" />
              <p class="product-card__price">
                <span class="currency">¥</span>{{ formatPrice(item.price) }}
              </p>
            </div>
          </div>
        </div>
        <ElEmpty v-else description="未找到相关商品">
          <ElButton type="primary" @click="router.push('/portal')">返回首页</ElButton>
        </ElEmpty>

        <div v-if="total > products.length" class="load-more">
          <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
        </div>
      </div>
    </div>

    <ElDrawer v-model="filterDrawerVisible" title="筛选商品" direction="ltr" size="280px">
      <SearchCategorySidebar
        :categories="categories"
        :brands="visibleBrands"
        :selected-catalog-id="catalogId"
        :selected-brand-id="brandId"
        :brand-logo-urls="brandLogoUrls"
        class="search-sidebar--drawer"
        @select-category="onDrawerSelectCategory"
        @select-brand="onDrawerSelectBrand"
      />
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalHome} from '@/api/portal/home'
import {fetchPortalProductSearch} from '@/api/portal/product'
import type {PortalBrandBrief, PortalCategory, PortalProductListItem} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import PortalProductRating from '@/views/portal/components/portal-product-rating.vue'
import PortalSearchBar from '@/views/portal/components/portal-search-bar.vue'
import PortalSearchHints from '@/views/portal/components/portal-search-hints.vue'
import SearchCategorySidebar from './components/search-category-sidebar.vue'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {addPortalSearchKeyword} from '@/utils/portal/search-history'

defineOptions({ name: 'PortalSearch' })

  const router = useRouter()
  const route = useRoute()
  const loading = ref(true)
  const loadingMore = ref(false)
  const products = ref<PortalProductListItem[]>([])
  const categories = ref<PortalCategory[]>([])
  const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
  const brandLogoUrls = ref(new Map<number, string>())
  const categoryNameMap = ref<Record<number, string>>({})
  const brandNameMap = ref<Record<number, string>>({})
  const coverUrls = ref(new Map<string, string>())
  const sort = ref('default')
  const pageNum = ref(1)
  const pageSize = 16
  const total = ref(0)

  const keyword = ref('')
  const inputKeyword = ref('')
  const catalogId = ref<number | undefined>()
  const brandId = ref<number | undefined>()
  const filterDrawerVisible = ref(false)
  const isMobile = ref(false)

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/><text x="50%" y="50%" fill="#999" font-size="14" text-anchor="middle" dy=".3em">暂无图片</text></svg>'
    )

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  const pageTitle = computed(() => {
    if (keyword.value) return `搜索「${keyword.value}」`
    if (catalogId.value != null) {
      const name = categoryNameMap.value[catalogId.value]
      return name ? `${name}` : '分类商品'
    }
    return '商品搜索'
  })

  const showSearchHints = computed(() => !keyword.value && catalogId.value == null && brandId.value == null)

  const filterHint = computed(() => {
    const parts: string[] = []
    if (keyword.value) parts.push(`关键词：${keyword.value}`)
    if (catalogId.value != null) {
      parts.push(`分类：${categoryNameMap.value[catalogId.value] || '已选分类'}`)
    }
    if (brandId.value != null) {
      parts.push(`品牌：${brandNameMap.value[brandId.value] || '已选品牌'}`)
    }
    return parts.join(' · ')
  })

  function findL1CatId(nodes: PortalCategory[], targetId: number): number | undefined {
    for (const l1 of nodes) {
      if (l1.catId === targetId) return l1.catId
      for (const l2 of l1.children || []) {
        if (l2.catId === targetId) return l1.catId
        for (const l3 of l2.children || []) {
          if (l3.catId === targetId) return l1.catId
        }
      }
    }
    return undefined
  }

  const visibleBrands = computed(() => {
    if (catalogId.value != null) {
      const l1Id = findL1CatId(categories.value, catalogId.value)
      if (l1Id != null) return categoryBrands.value[l1Id] || []
    }
    const map = new Map<number, PortalBrandBrief>()
    for (const brands of Object.values(categoryBrands.value)) {
      for (const brand of brands || []) {
        if (brand.brandId != null) map.set(brand.brandId, brand)
      }
    }
    return [...map.values()]
  })

  function buildCategoryNameMap(nodes: PortalCategory[], map: Record<number, string> = {}) {
    for (const node of nodes) {
      if (node.catId != null && node.name) map[node.catId] = node.name
      if (node.children?.length) buildCategoryNameMap(node.children, map)
    }
    return map
  }

  async function loadCategories() {
    const home = await fetchPortalHome()
    categories.value = home.categories || []
    categoryBrands.value = home.categoryBrands || {}
    categoryNameMap.value = buildCategoryNameMap(categories.value)
    const brandMap: Record<number, string> = {}
    for (const brands of Object.values(categoryBrands.value)) {
      for (const brand of brands || []) {
        if (brand.brandId != null && brand.name) brandMap[brand.brandId] = brand.name
      }
    }
    brandNameMap.value = brandMap
    const logos = Object.values(categoryBrands.value)
      .flat()
      .map((b) => b.logo)
      .filter(Boolean) as string[]
    if (logos.length) {
      const urlMap = await resolveGoodsImageDisplayUrls(logos)
      const logoToUrl = new Map<string, string>()
      urlMap.forEach((url, key) => logoToUrl.set(key, url))
      const result = new Map<number, string>()
      for (const brands of Object.values(categoryBrands.value)) {
        for (const brand of brands || []) {
          if (brand.brandId != null && brand.logo) {
            const url = logoToUrl.get(brand.logo)
            if (url) result.set(brand.brandId, url)
          }
        }
      }
      brandLogoUrls.value = result
    }
  }

  function buildQuery(extra: { catalogId?: number; brandId?: number; keyword?: string }) {
    const kw = extra.keyword !== undefined ? extra.keyword : keyword.value
    return {
      ...(kw ? { keyword: kw } : {}),
      ...(extra.catalogId != null ? { catalogId: String(extra.catalogId) } : {}),
      ...(extra.brandId != null ? { brandId: String(extra.brandId) } : {})
    }
  }

  function selectCategory(id?: number) {
    router.push({ path: '/portal/search', query: buildQuery({ catalogId: id, brandId: brandId.value }) })
  }

  function selectBrand(id?: number) {
    router.push({ path: '/portal/search', query: buildQuery({ catalogId: catalogId.value, brandId: id }) })
  }

  function onDrawerSelectCategory(id?: number) {
    selectCategory(id)
    filterDrawerVisible.value = false
  }

  function onDrawerSelectBrand(id?: number) {
    selectBrand(id)
    filterDrawerVisible.value = false
  }

  function syncQueryFromRoute() {
    const q = route.query
    keyword.value = typeof q.keyword === 'string' ? q.keyword : ''
    inputKeyword.value = keyword.value
    catalogId.value = q.catalogId ? Number(q.catalogId) : undefined
    brandId.value = q.brandId ? Number(q.brandId) : undefined
    if (catalogId.value != null && !Number.isFinite(catalogId.value)) catalogId.value = undefined
    if (brandId.value != null && !Number.isFinite(brandId.value)) brandId.value = undefined
  }

  async function loadProducts(reset = true) {
    if (reset) {
      pageNum.value = 1
      products.value = []
    }
    const res = await fetchPortalProductSearch({
      pageNum: pageNum.value,
      pageSize,
      keyword: keyword.value || undefined,
      catalogId: catalogId.value,
      brandId: brandId.value,
      sort: sort.value === 'default' ? undefined : sort.value
    })
    if (keyword.value.trim()) {
      addPortalSearchKeyword(keyword.value)
    }
    total.value = res.total || 0
    const rows = res.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    const map = await resolveGoodsImageDisplayUrls(
      rows.map((r) => r.coverImg).filter(Boolean) as string[]
    )
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  function clearFilter() {
    router.replace({ path: '/portal/search' })
  }

  function searchByKeyword(text: string) {
    router.push({ path: '/portal/search', query: { keyword: text } })
  }

  function handleSearchSubmit(text: string) {
    router.push({
      path: '/portal/search',
      query: buildQuery({ catalogId: catalogId.value, brandId: brandId.value, keyword: text || undefined })
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

  watch(
    () => route.query,
    async () => {
      syncQueryFromRoute()
      loading.value = true
      try {
        await loadProducts(true)
      } finally {
        loading.value = false
      }
    },
    { immediate: true }
  )

  onMounted(() => {
    loadCategories().catch(() => {})
    const mq = window.matchMedia('(width <= 900px)')
    const syncMobile = () => {
      isMobile.value = mq.matches
    }
    syncMobile()
    mq.addEventListener('change', syncMobile)
  })
</script>

<style scoped lang="scss">
  @import '../styles/variables.scss';

  .portal-search__layout {
    display: flex;
    gap: 20px;
    align-items: flex-start;
  }

  .portal-search__bar {
    margin-bottom: 16px;
  }

  .filter-btn {
    margin-right: 8px;
  }

  :deep(.search-sidebar--drawer) {
    display: block !important;
    width: 100%;
    max-height: none;
    position: static;
    border: none;
    padding: 0;
  }

  .portal-search__main {
    flex: 1;
    min-width: 0;
  }

  .filter-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    padding: 12px 16px;
    background: var(--portal-primary-light);
    border-radius: var(--portal-radius);
    font-size: 14px;
    color: var(--portal-text-secondary);
  }

  .clear-filter {
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

  .product-card {
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--portal-transition);
    background: var(--portal-bg-elevated);

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--portal-shadow-lg);
      border-color: transparent;
    }

    &__img-wrap {
      aspect-ratio: 1;
      background: #fafbfc;
      overflow: hidden;
    }

    &__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.35s ease;
    }

    &:hover &__img {
      transform: scale(1.05);
    }

    &__body {
      padding: 14px 16px 16px;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 14px;
      color: var(--portal-text);
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 42px;
    }

    &__brand {
      margin: 0 0 10px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__price {
      margin: 0;
      color: var(--portal-primary);
      font-size: 20px;
      font-weight: 700;

      .currency {
        font-size: 13px;
      }
    }
  }

  .load-more {
    text-align: center;
    margin-top: 28px;
  }
</style>

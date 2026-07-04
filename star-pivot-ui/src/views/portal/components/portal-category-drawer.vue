<!-- 移动端全部分类抽屉 -->
<template>
  <ElDrawer
    v-model="visible"
    title="全部分类"
    direction="ltr"
    size="min(320px, 88vw)"
    destroy-on-close
    @open="ensureLoaded"
  >
    <div v-loading="loading" class="portal-category-drawer">
      <ul v-if="categories.length" class="portal-category-drawer__list">
        <li
          v-for="cat in categories"
          :key="cat.catId"
          class="portal-category-drawer__group"
        >
          <button
            type="button"
            class="portal-category-drawer__l1"
            @click="toggleExpand(cat.catId!)"
          >
            <span>{{ cat.name }}</span>
            <ArtSvgIcon
              :icon="expandedIds.has(cat.catId!) ? 'ri:arrow-up-s-line' : 'ri:arrow-down-s-line'"
            />
          </button>

          <div v-show="expandedIds.has(cat.catId!)" class="portal-category-drawer__panel">
            <button
              type="button"
              class="portal-category-drawer__link portal-category-drawer__link--all"
              @click="goCategory(cat.catId)"
            >
              查看「{{ cat.name }}」全部
            </button>

            <template v-for="l2 in cat.children || []" :key="l2.catId">
              <p class="portal-category-drawer__l2">{{ l2.name }}</p>
              <div class="portal-category-drawer__l3-wrap">
                <button
                  v-for="l3 in l2.children || []"
                  :key="l3.catId"
                  type="button"
                  class="portal-category-drawer__link"
                  @click="goCategory(l3.catId)"
                >
                  {{ l3.name }}
                </button>
                <button
                  v-if="!(l2.children?.length)"
                  type="button"
                  class="portal-category-drawer__link"
                  @click="goCategory(l2.catId)"
                >
                  查看全部
                </button>
              </div>
            </template>

            <div v-if="brandsFor(cat.catId!).length" class="portal-category-drawer__brands">
              <p class="portal-category-drawer__brands-title">热门品牌</p>
              <div class="portal-category-drawer__brand-grid">
                <button
                  v-for="brand in brandsFor(cat.catId!)"
                  :key="brand.brandId"
                  type="button"
                  class="portal-category-drawer__brand"
                  @click="goBrand(brand.brandId)"
                >
                  <img
                    v-if="brandLogo(brand.brandId!)"
                    :src="brandLogo(brand.brandId!)"
                    :alt="brand.name"
                  />
                  <span v-else>{{ brand.name }}</span>
                </button>
              </div>
            </div>
          </div>
        </li>
      </ul>
      <ElEmpty v-else-if="!loading" description="暂无分类" />
    </div>
  </ElDrawer>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import { fetchPortalHome } from '@/api/portal/home'
import type { PortalBrandBrief, PortalCategory } from '@/api/portal/types'
import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
import {
  closePortalCategoryDrawer,
  portalCategoryDrawerVisible
} from '@/utils/portal/category-drawer'

defineOptions({ name: 'PortalCategoryDrawer' })

  const router = useRouter()
  const visible = portalCategoryDrawerVisible
  const loading = ref(false)
  const loaded = ref(false)
  const categories = ref<PortalCategory[]>([])
  const categoryBrands = ref<Record<number, PortalBrandBrief[]>>({})
  const brandLogoUrls = ref(new Map<number, string>())
  const expandedIds = ref(new Set<number>())

  function brandsFor(catId: number) {
    return categoryBrands.value[catId] || []
  }

  function brandLogo(brandId: number) {
    return brandLogoUrls.value.get(brandId) || ''
  }

  function toggleExpand(catId: number) {
    const next = new Set(expandedIds.value)
    if (next.has(catId)) next.delete(catId)
    else next.add(catId)
    expandedIds.value = next
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

  async function ensureLoaded() {
    if (loaded.value) return
    loading.value = true
    try {
      const data = await fetchPortalHome()
      categories.value = data.categories || []
      categoryBrands.value = data.categoryBrands || {}
      if (categories.value[0]?.catId != null) {
        expandedIds.value = new Set([categories.value[0].catId])
      }
      await resolveBrandLogos(categoryBrands.value)
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  function goCategory(catId?: number) {
    if (catId == null) return
    closePortalCategoryDrawer()
    router.push({ path: '/portal/search', query: { catalogId: String(catId) } })
  }

  function goBrand(brandId?: number) {
    if (brandId == null) return
    closePortalCategoryDrawer()
    router.push({ path: '/portal/search', query: { brandId: String(brandId) } })
  }
</script>

<style scoped lang="scss">
  .portal-category-drawer {
    min-height: 120px;

    &__list {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    &__group {
      border-bottom: 1px solid var(--portal-border);
    }

    &__l1 {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding: 14px 4px;
      border: none;
      background: none;
      font-size: 15px;
      font-weight: 600;
      color: var(--portal-text);
      cursor: pointer;
      text-align: left;

      svg {
        font-size: 18px;
        color: var(--portal-text-muted);
      }
    }

    &__panel {
      padding: 0 4px 12px;
    }

    &__l2 {
      margin: 8px 0 6px;
      font-size: 13px;
      font-weight: 600;
      color: var(--portal-text-secondary);
    }

    &__l3-wrap {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 8px;
    }

    &__link {
      padding: 6px 12px;
      border: 1px solid var(--portal-border);
      border-radius: 16px;
      background: var(--portal-bg-elevated);
      color: var(--portal-text-secondary);
      font-size: 12px;
      cursor: pointer;
      transition: all var(--portal-transition);

      &:hover,
      &:active {
        border-color: var(--portal-primary);
        color: var(--portal-primary);
        background: var(--portal-primary-light);
      }

      &--all {
        width: 100%;
        margin-bottom: 8px;
        border-style: dashed;
        border-radius: var(--portal-radius-sm);
        text-align: center;
      }
    }

    &__brands {
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px dashed var(--portal-border);
    }

    &__brands-title {
      margin: 0 0 8px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__brand-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 8px;
    }

    &__brand {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 40px;
      padding: 4px;
      border: 1px solid var(--portal-border);
      border-radius: var(--portal-radius-sm);
      background: #fff;
      cursor: pointer;

      img {
        max-width: 100%;
        max-height: 28px;
        object-fit: contain;
      }

      span {
        font-size: 11px;
        color: var(--portal-text-secondary);
      }
    }
  }
</style>

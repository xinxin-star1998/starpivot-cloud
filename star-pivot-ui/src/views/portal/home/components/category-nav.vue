<!-- 首页左侧分类导航 + Mega Menu -->
<template>
  <div
    class="category-nav"
    @mouseleave="handleNavLeave"
  >
    <ul class="category-nav__list">
      <li
        v-for="cat in categories"
        :key="cat.catId"
        class="category-nav__item"
        :class="{ active: activeCatId === cat.catId }"
        @mouseenter="handleItemEnter(cat)"
      >
        <span class="category-nav__name">{{ cat.name }}</span>
        <span v-if="subHint(cat)" class="category-nav__hint">{{ subHint(cat) }}</span>
      </li>
    </ul>

    <Transition name="mega-fade">
      <div
        v-if="activeCategory"
        class="category-mega"
        @mouseenter="megaHovered = true"
        @mouseleave="handleMegaLeave"
      >
        <div class="category-mega__body">
          <div
            v-for="l2 in activeCategory.children || []"
            :key="l2.catId"
            class="category-mega__group"
          >
            <span class="category-mega__l2">{{ l2.name }}</span>
            <div class="category-mega__l3-list">
              <template v-if="l2.children?.length">
                <a
                  v-for="l3 in l2.children"
                  :key="l3.catId"
                  class="category-mega__l3"
                  href="javascript:;"
                  @click.prevent="emitSelectCategory(l3.catId)"
                >
                  {{ l3.name }}
                </a>
              </template>
              <a
                v-else
                class="category-mega__l3"
                href="javascript:;"
                @click.prevent="emitSelectCategory(l2.catId)"
              >
                查看全部
              </a>
            </div>
          </div>
          <ElEmpty
            v-if="!(activeCategory.children?.length)"
            description="暂无子分类"
            :image-size="60"
          />
        </div>

        <aside v-if="activeBrands.length" class="category-mega__brands">
          <p class="category-mega__brands-title">热门品牌</p>
          <div class="category-mega__brand-grid">
            <a
              v-for="brand in activeBrands"
              :key="brand.brandId"
              class="category-mega__brand"
              href="javascript:;"
              @click.prevent="emitSelectBrand(brand.brandId)"
            >
              <img
                v-if="brandLogo(brand.brandId!)"
                :src="brandLogo(brand.brandId!)"
                :alt="brand.name"
                class="category-mega__brand-logo"
              />
              <span v-else class="category-mega__brand-text">{{ brand.name }}</span>
            </a>
          </div>
        </aside>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import type {PortalBrandBrief, PortalCategory} from '@/api/portal/types'

defineOptions({ name: 'PortalHomeCategoryNav' })

  const props = defineProps<{
    categories: PortalCategory[]
    categoryBrands?: Record<number, PortalBrandBrief[]>
    brandLogos?: Map<number, string>
  }>()

  const emit = defineEmits<{
    selectCategory: [catId: number]
    selectBrand: [brandId: number]
  }>()

  const activeCatId = ref<number | undefined>()
  const megaHovered = ref(false)

  const activeCategory = computed(() =>
    props.categories.find((c) => c.catId === activeCatId.value)
  )

  const activeBrands = computed(() => {
    if (activeCatId.value == null) return []
    return props.categoryBrands?.[activeCatId.value] || []
  })

  function subHint(cat: PortalCategory) {
    const names = (cat.children || [])
      .slice(0, 3)
      .map((c) => c.name)
      .filter(Boolean)
    return names.join(' / ')
  }

  function brandLogo(brandId: number) {
    return props.brandLogos?.get(brandId) || ''
  }

  function handleItemEnter(cat: PortalCategory) {
    activeCatId.value = cat.catId
    megaHovered.value = true
  }

  function handleNavLeave() {
    if (!megaHovered.value) {
      activeCatId.value = undefined
    }
  }

  function handleMegaLeave() {
    megaHovered.value = false
    activeCatId.value = undefined
  }

  function emitSelectCategory(catId?: number) {
    if (catId != null) {
      emit('selectCategory', catId)
      activeCatId.value = undefined
      megaHovered.value = false
    }
  }

  function emitSelectBrand(brandId?: number) {
    if (brandId != null) {
      emit('selectBrand', brandId)
      activeCatId.value = undefined
      megaHovered.value = false
    }
  }
</script>

<style scoped lang="scss">

  .category-nav {
    width: 200px;
    flex-shrink: 0;
    align-self: stretch;
    min-height: 480px;
    background: linear-gradient(180deg, #2d3436 0%, #3d4f5f 100%);
    border-radius: var(--portal-radius-lg) 0 0 var(--portal-radius-lg);
    z-index: 2;
  }

  .category-nav__list {
    list-style: none;
    margin: 0;
    padding: 8px 0;
    max-height: 480px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgb(255 255 255 / 20%);
      border-radius: 2px;
    }
  }

  .category-nav__item {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 10px 16px;
    cursor: pointer;
    transition: all var(--portal-transition);
    border-left: 3px solid transparent;

    &:hover,
    &.active {
      background: rgb(255 255 255 / 8%);
      border-left-color: var(--portal-primary);
    }

    &.active .category-nav__name {
      color: #fff;
    }
  }

  .category-nav__name {
    font-size: 14px;
    font-weight: 600;
    color: rgb(255 255 255 / 92%);
    line-height: 1.3;
  }

  .category-nav__hint {
    font-size: 11px;
    color: rgb(255 255 255 / 45%);
    line-height: 1.3;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .category-mega {
    position: absolute;
    left: 200px;
    top: 0;
    bottom: 0;
    right: 200px;
    background: var(--portal-bg-elevated);
    border-left: 1px solid var(--portal-border);
    box-shadow: var(--portal-shadow);
    display: flex;
    z-index: 10;
    overflow: hidden;
  }

  .category-mega__body {
    flex: 1;
    padding: 16px 20px;
    overflow-y: auto;
  }

  .category-mega__group {
    display: flex;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px dashed #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .category-mega__l2 {
    flex-shrink: 0;
    width: 88px;
    font-size: 13px;
    font-weight: 600;
    color: #333;
    line-height: 1.6;
  }

  .category-mega__l3-list {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 4px 0;
    line-height: 1.8;
  }

  .category-mega__l3 {
    font-size: 13px;
    color: #666;
    text-decoration: none;
    padding: 0 10px;
    border-right: 1px solid #e8e8e8;

    &:last-child {
      border-right: none;
    }

    &:hover {
      color: var(--portal-primary);
    }
  }

  .category-mega__brands {
    width: 180px;
    flex-shrink: 0;
    padding: 16px 12px;
    border-left: 1px solid var(--portal-border);
    background: #fafbfc;
  }

  .category-mega__brands-title {
    margin: 0 0 12px;
    font-size: 13px;
    font-weight: 600;
    color: #333;
  }

  .category-mega__brand-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .category-mega__brand {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 44px;
    background: #fff;
    border: 1px solid #eee;
    border-radius: 4px;
    overflow: hidden;
    text-decoration: none;
    transition: border-color 0.15s;

    &:hover {
      border-color: var(--portal-primary);
    }
  }

  .category-mega__brand-logo {
    max-width: 90%;
    max-height: 32px;
    object-fit: contain;
  }

  .category-mega__brand-text {
    font-size: 12px;
    color: #666;
    padding: 0 4px;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mega-fade-enter-active,
  .mega-fade-leave-active {
    transition: opacity 0.15s ease;
  }

  .mega-fade-enter-from,
  .mega-fade-leave-to {
    opacity: 0;
  }

  @media (width <= 900px) {
    .category-nav {
      display: none;
    }
  }
</style>

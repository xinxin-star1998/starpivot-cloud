<!-- 搜索页分类 + 品牌侧栏 -->
<template>
  <aside class="search-sidebar">
    <h3 class="search-sidebar__title">商品分类</h3>
    <ul class="search-sidebar__list">
      <li
        class="search-sidebar__item"
        :class="{ active: !selectedCatalogId }"
        @click="emitSelectCategory(undefined)"
      >
        全部分类
      </li>
      <li v-for="l1 in categories" :key="l1.catId" class="search-sidebar__group">
        <div
          class="search-sidebar__item search-sidebar__item--l1"
          :class="{ active: selectedCatalogId === l1.catId }"
          @click="emitSelectCategory(l1.catId)"
        >
          {{ l1.name }}
        </div>
        <ul v-if="l1.children?.length" class="search-sidebar__sub">
          <template v-for="l2 in l1.children" :key="l2.catId">
            <li
              class="search-sidebar__item search-sidebar__item--l2"
              :class="{ active: selectedCatalogId === l2.catId }"
              @click="emitSelectCategory(l2.catId)"
            >
              {{ l2.name }}
            </li>
            <li
              v-for="l3 in l2.children || []"
              :key="l3.catId"
              class="search-sidebar__item search-sidebar__item--l3"
              :class="{ active: selectedCatalogId === l3.catId }"
              @click="emitSelectCategory(l3.catId)"
            >
              {{ l3.name }}
            </li>
          </template>
        </ul>
      </li>
    </ul>

    <template v-if="brands.length">
      <h3 class="search-sidebar__title search-sidebar__title--brand">品牌筛选</h3>
      <ul class="search-sidebar__list search-sidebar__brand-list">
        <li
          class="search-sidebar__item search-sidebar__item--brand"
          :class="{ active: !selectedBrandId }"
          @click="emitSelectBrand(undefined)"
        >
          全部品牌
        </li>
        <li
          v-for="brand in brands"
          :key="brand.brandId"
          class="search-sidebar__item search-sidebar__item--brand"
          :class="{ active: selectedBrandId === brand.brandId }"
          @click="emitSelectBrand(brand.brandId)"
        >
          <img
            v-if="brandLogoUrls?.get(brand.brandId!)"
            :src="brandLogoUrls.get(brand.brandId!)"
            :alt="brand.name"
            class="search-sidebar__brand-logo"
          />
          <span>{{ brand.name }}</span>
        </li>
      </ul>
    </template>
  </aside>
</template>

<script setup lang="ts">
import type {PortalBrandBrief, PortalCategory} from '@/api/portal/types'

defineOptions({ name: 'SearchCategorySidebar' })

  defineProps<{
    categories: PortalCategory[]
    brands: PortalBrandBrief[]
    selectedCatalogId?: number
    selectedBrandId?: number
    brandLogoUrls?: Map<number, string>
  }>()

  const emit = defineEmits<{
    'select-category': [catalogId?: number]
    'select-brand': [brandId?: number]
  }>()

  function emitSelectCategory(catalogId?: number) {
    emit('select-category', catalogId)
  }

  function emitSelectBrand(brandId?: number) {
    emit('select-brand', brandId)
  }
</script>

<style scoped lang="scss">
  .search-sidebar {
    width: 200px;
    flex-shrink: 0;
    background: var(--portal-bg-elevated);
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    padding: 12px 0;
    max-height: calc(100vh - 220px);
    overflow-y: auto;
    position: sticky;
    top: calc(var(--portal-header-height) + 48px);

    &__title {
      margin: 0 0 8px;
      padding: 0 16px 8px;
      font-size: 14px;
      font-weight: 700;
      color: var(--portal-text);
      border-bottom: 1px solid var(--portal-border);

      &--brand {
        margin-top: 16px;
      }
    }

    &__list {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    &__brand-list {
      max-height: 240px;
      overflow-y: auto;
    }

    &__group {
      margin: 0;
    }

    &__sub {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    &__item {
      padding: 8px 16px;
      font-size: 13px;
      color: var(--portal-text-secondary);
      cursor: pointer;
      transition: all var(--portal-transition);

      &:hover {
        color: var(--portal-primary);
        background: var(--portal-primary-light);
      }

      &.active {
        color: var(--portal-primary);
        background: var(--portal-primary-light);
        font-weight: 600;
      }

      &--l1 {
        font-weight: 600;
        color: var(--portal-text);
      }

      &--l2 {
        padding-left: 28px;
      }

      &--l3 {
        padding-left: 40px;
        font-size: 12px;
      }

      &--brand {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
      }
    }

    &__brand-logo {
      width: 20px;
      height: 20px;
      object-fit: contain;
      flex-shrink: 0;
    }
  }

  @media (width <= 900px) {
    .search-sidebar:not(.search-sidebar--drawer) {
      display: none;
    }
  }
</style>

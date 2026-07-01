<!-- 首页分类热门入口 -->
<template>
  <section v-if="items.length" class="hot-categories">
    <h2 class="hot-categories__title">分类热门</h2>
    <div class="hot-categories__grid">
      <a
        v-for="item in items"
        :key="item.id ?? item.catId"
        class="hot-categories__item"
        href="javascript:;"
        @click.prevent="emit('select', item)"
      >
        <div class="hot-categories__icon-wrap">
          <img
            v-if="iconUrl(item)"
            :src="iconUrl(item)"
            :alt="displayTitle(item)"
            class="hot-categories__icon"
          />
          <span v-else class="hot-categories__icon-fallback">{{ fallbackText(item) }}</span>
        </div>
        <span class="hot-categories__name">{{ displayTitle(item) }}</span>
      </a>
    </div>
  </section>
</template>

<script setup lang="ts">
import type {PortalHotCategory} from '@/api/portal/types'

defineOptions({ name: 'PortalHomeHotCategories' })

  const props = defineProps<{
    items: PortalHotCategory[]
    iconUrls?: Map<string, string>
  }>()

  const emit = defineEmits<{
    select: [item: PortalHotCategory]
  }>()

  function displayTitle(item: PortalHotCategory) {
    return item.title || item.catName || '分类'
  }

  function iconUrl(item: PortalHotCategory) {
    const raw = item.icon?.trim()
    if (!raw) return ''
    return props.iconUrls?.get(raw) || ''
  }

  function fallbackText(item: PortalHotCategory) {
    const name = displayTitle(item)
    return name.slice(0, 1)
  }
</script>

<style scoped lang="scss">
  @import '../../styles/variables.scss';

  .hot-categories {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 20px 24px;
    margin-bottom: 20px;
    box-shadow: var(--portal-shadow-sm);
  }

  .hot-categories__title {
    margin: 0 0 16px;
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .hot-categories__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(88px, 1fr));
    gap: 16px 12px;
  }

  .hot-categories__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    text-decoration: none;
    transition: transform 0.15s ease;

    &:hover {
      transform: translateY(-2px);

      .hot-categories__name {
        color: var(--portal-primary);
      }
    }
  }

  .hot-categories__icon-wrap {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    border: 1px solid #eee;
  }

  .hot-categories__icon {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .hot-categories__icon-fallback {
    font-size: 22px;
    font-weight: 600;
    color: var(--portal-primary);
  }

  .hot-categories__name {
    font-size: 13px;
    color: #555;
    text-align: center;
    line-height: 1.3;
    max-width: 88px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>

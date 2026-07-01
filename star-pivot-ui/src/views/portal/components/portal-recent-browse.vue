<!-- 首页最近浏览横滑 -->
<template>
  <section v-if="items.length" class="portal-recent-browse">
    <div class="portal-recent-browse__head">
      <h2 class="portal-recent-browse__title">最近浏览</h2>
      <button type="button" class="portal-recent-browse__more" @click="router.push('/portal/account/history')">
        查看更多
      </button>
    </div>
    <div class="portal-recent-browse__scroll">
      <div
        v-for="item in items"
        :key="item.spuId"
        class="recent-card"
        @click="router.push(`/portal/product/${item.spuId}`)"
      >
        <img
          :src="coverUrls.get(item.coverImg || '') || placeholderImg"
          :alt="item.spuName"
          class="recent-card__img"
        />
        <p class="recent-card__name">{{ item.spuName || `商品 #${item.spuId}` }}</p>
        <p v-if="item.price != null" class="recent-card__price">¥{{ formatPrice(item.price) }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {getPortalBrowseHistory, type PortalBrowseRecord} from '@/utils/portal/browse-history'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'PortalRecentBrowse' })

  const props = withDefaults(
    defineProps<{
      limit?: number
    }>(),
    {
      limit: 8
    }
  )

  const router = useRouter()
  const items = ref<PortalBrowseRecord[]>([])
  const coverUrls = ref(new Map<string, string>())

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160"><rect fill="#f0f0f0" width="160" height="160"/></svg>'
    )

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  async function loadItems() {
    items.value = getPortalBrowseHistory().slice(0, props.limit)
    const keys = items.value.map((item) => item.coverImg).filter(Boolean) as string[]
    if (keys.length) {
      const map = await resolveGoodsImageDisplayUrls(keys)
      map.forEach((url, key) => coverUrls.value.set(key, url))
    }
  }

  function onBrowseChanged() {
    loadItems()
  }

  onMounted(() => {
    loadItems()
    window.addEventListener('portal-browse-changed', onBrowseChanged)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-browse-changed', onBrowseChanged)
  })
</script>

<style scoped lang="scss">
  @import '../styles/variables.scss';

  .portal-recent-browse {
    margin-bottom: 24px;
    padding: 20px;
    background: var(--portal-bg-elevated);
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-lg);

    &__head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }

    &__title {
      margin: 0;
      font-size: 18px;
      font-weight: 700;
    }

    &__more {
      padding: 0;
      border: none;
      background: none;
      color: var(--portal-primary);
      font-size: 13px;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }

    &__scroll {
      display: flex;
      gap: 14px;
      overflow-x: auto;
      padding-bottom: 4px;
      scrollbar-width: thin;

      &::-webkit-scrollbar {
        height: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: var(--portal-border);
        border-radius: 3px;
      }
    }
  }

  .recent-card {
    width: 132px;
    flex-shrink: 0;
    cursor: pointer;

    &__img {
      width: 132px;
      height: 132px;
      object-fit: cover;
      border-radius: var(--portal-radius-sm);
      background: #fafbfc;
      border: 1px solid var(--portal-border);
    }

    &__name {
      margin: 8px 0 4px;
      font-size: 12px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    &__price {
      margin: 0;
      color: var(--portal-primary);
      font-size: 14px;
      font-weight: 700;
    }
  }
</style>

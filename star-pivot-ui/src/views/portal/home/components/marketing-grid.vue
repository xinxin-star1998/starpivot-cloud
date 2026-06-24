<!-- 首页营销四宫格：新品 / 秒杀 / 包邮 / 专题 -->
<template>
  <section v-if="blocks.length" class="marketing-grid">
    <article
      v-for="block in blocks"
      :key="block.code"
      class="marketing-card"
      :class="`marketing-card--${block.code}`"
    >
      <header class="marketing-card__head">
        <div class="marketing-card__titles">
          <h3 class="marketing-card__title">{{ block.title }}</h3>
          <p v-if="block.subTitle" class="marketing-card__subtitle">{{ block.subTitle }}</p>
        </div>
        <a
          v-if="block.url"
          class="marketing-card__more"
          href="javascript:;"
          @click.prevent="emit('openLink', block.url!)"
        >
          更多
        </a>
      </header>

      <!-- 秒杀场次切换 -->
      <div v-if="block.code === 'seckill' && block.sessions?.length" class="seckill-tabs">
        <button
          v-for="session in block.sessions"
          :key="session.id"
          type="button"
          class="seckill-tab"
          :class="{
            active: activeSessionMap[block.code] === session.id,
            ongoing: session.state === 'ongoing'
          }"
          @click="selectSession(block, session)"
        >
          <span class="seckill-tab__time">{{ session.startLabel }}</span>
          <span class="seckill-tab__label">
            {{ sessionStateLabel(session.state) }}
          </span>
        </button>
      </div>

      <div v-if="displayProducts(block).length" class="marketing-card__products">
        <div
          v-for="item in displayProducts(block)"
          :key="`${item.spuId}-${item.skuId || 0}`"
          class="marketing-product"
          @click="emit('goProduct', item.spuId!)"
        >
          <div class="marketing-product__img-wrap">
            <img
              :src="imageUrl(item.coverImg) || placeholderImg"
              :alt="item.spuName"
              class="marketing-product__img"
            />
          </div>
          <p class="marketing-product__price">
            <span v-if="item.promoPrice != null" class="promo">
              <span class="currency">¥</span>{{ formatPrice(item.promoPrice) }}
            </span>
            <span v-else class="promo">
              <span class="currency">¥</span>{{ formatPrice(item.price) }}
            </span>
            <span
              v-if="item.promoPrice != null && item.price != null && item.promoPrice < item.price"
              class="origin"
            >
              ¥{{ formatPrice(item.price) }}
            </span>
          </p>
        </div>
      </div>
      <ElEmpty v-else :image-size="48" description="暂无商品" />
    </article>
  </section>
</template>

<script setup lang="ts">
  import type { PortalHomeBlock, PortalHomeProduct, PortalSeckillSession } from '@/api/portal/types'

  defineOptions({ name: 'PortalHomeMarketingGrid' })

  const props = defineProps<{
    blocks: PortalHomeBlock[]
    imageUrls: Map<string, string>
    placeholderImg: string
  }>()

  const emit = defineEmits<{
    goProduct: [spuId: number]
    openLink: [url: string]
  }>()

  const activeSessionMap = reactive<Record<string, number | undefined>>({})

  watch(
    () => props.blocks,
    (blocks) => {
      for (const block of blocks) {
        if (block.code === 'seckill') {
          activeSessionMap.seckill = block.activeSessionId
        }
      }
    },
    { immediate: true, deep: true }
  )

  function imageUrl(key?: string) {
    if (!key) return ''
    return props.imageUrls.get(key) || key
  }

  function formatPrice(price?: number) {
    return price != null ? Number(price).toFixed(2) : '--'
  }

  function sessionStateLabel(state?: string) {
    if (state === 'ongoing') return '抢购中'
    if (state === 'upcoming') return '即将开始'
    return '已结束'
  }

  function selectSession(block: PortalHomeBlock, session: PortalSeckillSession) {
    if (session.id != null) {
      activeSessionMap.seckill = session.id
    }
  }

  function displayProducts(block: PortalHomeBlock): PortalHomeProduct[] {
    if (block.code === 'seckill' && block.sessions?.length) {
      const sessionId = activeSessionMap.seckill ?? block.activeSessionId
      const session = block.sessions.find((s) => s.id === sessionId)
      return session?.products?.length ? session.products : block.products || []
    }
    return block.products || []
  }
</script>

<style scoped lang="scss">
  .marketing-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 16px;
  }

  .marketing-card {
    background: #fff;
    border-radius: 8px;
    padding: 14px;
    min-height: 220px;
    box-shadow: 0 1px 4px rgb(0 0 0 / 6%);
    display: flex;
    flex-direction: column;

    &--seckill {
      .marketing-card__title {
        color: #e1251b;
      }
    }

    &--new .marketing-card__title {
      color: #333;
    }

    &--budget .marketing-card__title {
      color: #f08420;
    }

    &--subject .marketing-card__title {
      color: #2f9bff;
    }
  }

  .marketing-card__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 10px;
  }

  .marketing-card__title {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.2;
  }

  .marketing-card__subtitle {
    margin: 4px 0 0;
    font-size: 12px;
    color: #999;
    line-height: 1.3;
  }

  .marketing-card__more {
    flex-shrink: 0;
    font-size: 12px;
    color: #999;
    text-decoration: none;

    &:hover {
      color: #e1251b;
    }
  }

  .seckill-tabs {
    display: flex;
    gap: 6px;
    margin-bottom: 10px;
    overflow-x: auto;
  }

  .seckill-tab {
    flex: 1;
    min-width: 0;
    border: 1px solid #eee;
    border-radius: 4px;
    background: #fafafa;
    padding: 4px 2px;
    cursor: pointer;
    transition: all 0.15s;

    &.active {
      border-color: #e1251b;
      background: #fff5f5;
    }

    &.ongoing.active .seckill-tab__label {
      color: #e1251b;
    }

    &__time {
      display: block;
      font-size: 12px;
      font-weight: 600;
      color: #333;
      line-height: 1.2;
    }

    &__label {
      display: block;
      font-size: 10px;
      color: #999;
      margin-top: 2px;
    }
  }

  .marketing-card__products {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    flex: 1;
  }

  .marketing-product {
    cursor: pointer;
    text-align: center;

    &__img-wrap {
      aspect-ratio: 1;
      background: #fafafa;
      border-radius: 6px;
      overflow: hidden;
      margin-bottom: 6px;
    }

    &__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.2s;
    }

    &:hover &__img {
      transform: scale(1.04);
    }

    &__price {
      margin: 0;
      line-height: 1.2;

      .promo {
        color: #e1251b;
        font-size: 14px;
        font-weight: 700;
      }

      .currency {
        font-size: 11px;
      }

      .origin {
        display: block;
        font-size: 11px;
        color: #bbb;
        text-decoration: line-through;
        margin-top: 2px;
      }
    }
  }

  @media (width <= 992px) {
    .marketing-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (width <= 640px) {
    .marketing-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

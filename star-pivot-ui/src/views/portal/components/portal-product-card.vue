<!-- 统一商品卡片 -->
<template>
  <div class="portal-product-card" @click="emit('click')">
    <div class="portal-product-card__img-wrap">
      <img
        :src="imageSrc"
        :alt="spuName"
        class="portal-product-card__img"
        loading="lazy"
      />
      <span v-if="badge" class="portal-product-card__badge">{{ badge }}</span>
      <div v-if="showActions" class="portal-product-card__actions" @click.stop>
        <button
          type="button"
          class="portal-product-card__action-btn"
          :class="{ 'is-active': collected }"
          :disabled="favoriteLoading"
          aria-label="收藏"
          @click="emit('favorite')"
        >
          <ArtSvgIcon :icon="collected ? 'ri:heart-fill' : 'ri:heart-line'" />
        </button>
        <button
          type="button"
          class="portal-product-card__action-btn"
          :disabled="cartLoading"
          aria-label="加入购物车"
          @click="emit('add-cart')"
        >
          <ArtSvgIcon icon="ri:shopping-cart-line" />
        </button>
      </div>
    </div>
    <div class="portal-product-card__body">
      <p class="portal-product-card__name">{{ spuName }}</p>
      <p v-if="showBrand && brandName" class="portal-product-card__brand">{{ brandName }}</p>
      <PortalProductRating
        v-if="showRating"
        :avg-star="avgStar"
        :comment-count="commentCount"
      />
      <p v-if="stockText" class="portal-product-card__stock">{{ stockText }}</p>
      <p class="portal-product-card__price">
        <span v-if="promoPrice != null" class="promo">
          <span class="currency">¥</span>{{ formatPrice(promoPrice) }}
        </span>
        <span v-else class="promo">
          <span class="currency">¥</span>{{ formatPrice(price) }}
        </span>
        <span
          v-if="promoPrice != null && price != null && promoPrice < price"
          class="origin"
        >
          ¥{{ formatPrice(price) }}
        </span>
      </p>
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import PortalProductRating from '@/views/portal/components/portal-product-rating.vue'
import { PORTAL_PRODUCT_PLACEHOLDER_IMG } from '@/utils/portal/product-placeholder'
import { formatMoney } from '@/utils/mall/money'

defineOptions({ name: 'PortalProductCard' })

  const props = withDefaults(
    defineProps<{
      spuName?: string
      brandName?: string
      price?: number
      promoPrice?: number
      coverImg?: string
      imageUrl?: string
      placeholderImg?: string
      avgStar?: number
      commentCount?: number
      showRating?: boolean
      showBrand?: boolean
      badge?: string
      stockText?: string
      showActions?: boolean
      collected?: boolean
      favoriteLoading?: boolean
      cartLoading?: boolean
    }>(),
    {
      showRating: true,
      showBrand: true,
      placeholderImg: PORTAL_PRODUCT_PLACEHOLDER_IMG,
      showActions: false,
      collected: false,
      favoriteLoading: false,
      cartLoading: false
    }
  )

  const emit = defineEmits<{
    click: []
    favorite: []
    'add-cart': []
  }>()

  const imageSrc = computed(
    () => props.imageUrl || props.coverImg || props.placeholderImg
  )

  function formatPrice(p?: number) {
    return formatMoney(p)
  }
</script>

<style scoped lang="scss">
  .portal-product-card {
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--portal-transition);
    background: var(--portal-bg-elevated);

    @media (hover: hover) {
      &:hover {
        transform: translateY(-4px);
        box-shadow: var(--portal-shadow-lg);
        border-color: transparent;

        .portal-product-card__img {
          transform: scale(1.05);
        }

        .portal-product-card__actions {
          opacity: 1;
        }
      }
    }

    &__img-wrap {
      position: relative;
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

    &__badge {
      position: absolute;
      top: 10px;
      left: 10px;
      padding: 2px 8px;
      border-radius: 10px;
      background: var(--portal-primary);
      color: #fff;
      font-size: 11px;
      font-weight: 600;
      z-index: 1;
    }

    &__actions {
      position: absolute;
      right: 8px;
      bottom: 8px;
      display: flex;
      gap: 6px;
      opacity: 0;
      transition: opacity var(--portal-transition);
      z-index: 2;

      @media (hover: none) {
        opacity: 1;
      }
    }

    &__action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border: none;
      border-radius: 50%;
      background: rgb(255 255 255 / 92%);
      color: var(--portal-text-secondary);
      box-shadow: var(--portal-shadow-sm);
      cursor: pointer;
      transition: all var(--portal-transition);

      svg {
        font-size: 16px;
      }

      &:hover:not(:disabled),
      &.is-active {
        color: var(--portal-primary);
        background: #fff;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
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

    &__stock {
      margin: 0 0 8px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__price {
      margin: 0;

      .promo {
        color: var(--portal-primary);
        font-size: 20px;
        font-weight: 700;
        letter-spacing: -0.02em;
      }

      .currency {
        font-size: 13px;
        margin-right: 1px;
      }

      .origin {
        margin-left: 8px;
        color: var(--portal-text-muted);
        font-size: 13px;
        text-decoration: line-through;
        font-weight: 400;
      }
    }
  }
</style>

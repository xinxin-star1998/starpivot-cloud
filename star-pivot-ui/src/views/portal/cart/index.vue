<!-- C 端购物车 -->
<template>
  <div v-loading="loading" class="portal-cart">
    <h1 class="page-title">购物车</h1>

    <template v-if="cart.items?.length">
      <div class="cart-list">
        <div v-for="item in cart.items" :key="item.skuId" class="cart-item">
          <ElCheckbox v-model="item.checked" @change="(v) => handleCheckChange(item, v as boolean)" />
          <img
            :src="coverUrls.get(item.skuDefaultImg || '') || placeholderImg"
            class="cart-item__img"
            alt=""
          />
          <div class="cart-item__info">
            <p class="cart-item__title">{{ item.skuTitle }}</p>
            <p v-if="item.skuAttr" class="cart-item__attr">{{ item.skuAttr }}</p>
            <p v-if="!item.valid" class="cart-item__invalid">商品已失效</p>
          </div>
          <div class="cart-item__price">¥{{ formatPrice(item.price) }}</div>
          <ElInputNumber
            :model-value="item.quantity"
            :min="1"
            :max="item.stock || 99"
            :disabled="!item.valid"
            @change="(v) => handleQtyChange(item, Number(v))"
          />
          <ElButton link type="danger" @click="handleRemove(item.skuId)">删除</ElButton>
        </div>
      </div>

      <div class="cart-footer">
        <div class="cart-footer__summary">
          已选 <strong>{{ cart.checkedCount }}</strong> 件，合计
          <span class="total">¥{{ formatPrice(cart.checkedAmount) }}</span>
        </div>
        <ElButton type="danger" size="large" :disabled="!cart.checkedCount" @click="goCheckout">
          去结算
        </ElButton>
      </div>
    </template>

    <ElEmpty v-else description="购物车是空的">
      <ElButton type="primary" @click="router.push('/portal')">去逛逛</ElButton>
    </ElEmpty>
  </div>
</template>

<script setup lang="ts">
  import { fetchPortalCart, fetchPortalCartRemove, fetchPortalCartUpdate } from '@/api/portal/cart'
  import type { PortalCart, PortalCartItem } from '@/api/portal/types'
  import { usePortalAuth } from '@/hooks/portal/usePortalAuth'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import { notifyPortalCartChanged } from '@/utils/portal/cart-event'

  defineOptions({ name: 'PortalCart' })

  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const cart = ref<PortalCart>({ items: [] })
  const coverUrls = ref(new Map<string, string>())
  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="#f0f0f0" width="80" height="80"/></svg>')

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '0.00')

  async function loadCart() {
    cart.value = await fetchPortalCart()
    const imgs = (cart.value.items || []).map((i) => i.skuDefaultImg).filter(Boolean) as string[]
    if (imgs.length) {
      coverUrls.value = await resolveGoodsImageDisplayUrls(imgs)
    }
  }

  async function handleQtyChange(item: PortalCartItem, quantity: number) {
    if (!quantity || quantity === item.quantity) return
    await fetchPortalCartUpdate({ skuId: item.skuId, quantity })
    await loadCart()
    notifyPortalCartChanged()
  }

  async function handleCheckChange(item: PortalCartItem, checked: boolean) {
    await fetchPortalCartUpdate({ skuId: item.skuId, quantity: item.quantity || 1, checked })
    await loadCart()
  }

  async function handleRemove(skuId: number) {
    await fetchPortalCartRemove([skuId])
    await loadCart()
    notifyPortalCartChanged()
  }

  function goCheckout() {
    router.push('/portal/checkout')
  }

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    try {
      await loadCart()
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .page-title {
    margin: 0 0 16px;
    font-size: 22px;
  }

  .cart-list {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
  }

  .cart-item {
    display: grid;
    grid-template-columns: auto 80px 1fr 100px 140px auto;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;

    @media (width <= 768px) {
      grid-template-columns: auto 64px 1fr;
      grid-template-rows: auto auto;
    }

    &__img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 4px;
      background: #fafafa;
    }

    &__title {
      margin: 0 0 4px;
      font-size: 14px;
    }

    &__attr {
      margin: 0;
      font-size: 12px;
      color: #999;
    }

    &__invalid {
      margin: 4px 0 0;
      color: #e1251b;
      font-size: 12px;
    }

    &__price {
      color: #e1251b;
      font-weight: 600;
    }
  }

  .cart-footer {
    margin-top: 16px;
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 24px;

    &__summary {
      font-size: 14px;

      .total {
        color: #e1251b;
        font-size: 22px;
        font-weight: 700;
        margin-left: 4px;
      }
    }
  }
</style>

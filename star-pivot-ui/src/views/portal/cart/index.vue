<!-- C 端购物车 -->
<template>
  <div v-loading="loading" class="portal-cart">
    <PortalPageHeader title="购物车" :subtitle="cart.items?.length ? `共 ${cart.items.length} 件商品` : undefined" />

    <template v-if="cart.items?.length">
      <div class="cart-toolbar">
        <ElCheckbox
          :model-value="allChecked"
          :indeterminate="indeterminate"
          @change="(v) => handleSelectAll(Boolean(v))"
        >
          全选
        </ElCheckbox>
        <div class="cart-toolbar__actions">
          <ElButton
            link
            type="danger"
            :disabled="!checkedCount"
            :loading="removingChecked"
            @click="removeChecked"
          >
            删除已选
          </ElButton>
          <ElButton
            link
            type="primary"
            :disabled="!checkedValidCount"
            :loading="movingToFavorites"
            @click="moveCheckedToFavorites"
          >
            移入收藏
          </ElButton>
          <ElButton v-if="invalidCount" link type="danger" :loading="clearingInvalid" @click="clearInvalid">
            清理失效({{ invalidCount }})
          </ElButton>
        </div>
      </div>

      <div class="cart-list">
        <div class="cart-list__head">
          <span class="col-check">选择</span>
          <span class="col-img">图片</span>
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-action">操作</span>
        </div>
        <div v-for="item in cart.items" :key="item.skuId" class="cart-item" :class="{ invalid: !item.valid }">
          <ElCheckbox
            :model-value="item.checked"
            :disabled="!item.valid"
            @change="(v) => handleCheckChange(item, v as boolean)"
          />
          <img
            :src="coverUrls.get(item.skuDefaultImg || '') || placeholderImg"
            class="cart-item__img"
            alt=""
            @click="goProduct(item)"
          />
          <div class="cart-item__info">
            <p class="cart-item__title" @click="goProduct(item)">{{ item.skuTitle }}</p>
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
          <div class="cart-item__actions">
            <ElButton
              v-if="item.valid && item.spuId"
              link
              type="primary"
              @click="handleCollect(item)"
            >
              收藏
            </ElButton>
            <ElButton link type="danger" @click="handleRemove(item.skuId)">删除</ElButton>
          </div>
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
import {fetchPortalCart, fetchPortalCartRemove, fetchPortalCartUpdate} from '@/api/portal/cart'
import {fetchPortalCollectAdd} from '@/api/portal/collect'
import type {PortalCart, PortalCartItem} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {notifyPortalCartChanged} from '@/utils/portal/cart-event'
import {ElMessage, ElMessageBox} from 'element-plus'
import PortalPageHeader from '../components/portal-page-header.vue'

defineOptions({ name: 'PortalCart' })

  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const movingToFavorites = ref(false)
  const clearingInvalid = ref(false)
  const removingChecked = ref(false)
  const cart = ref<PortalCart>({ items: [] })
  const coverUrls = ref(new Map<string, string>())
  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="#f0f0f0" width="80" height="80"/></svg>'
    )

  const validItems = computed(() => (cart.value.items || []).filter((item) => item.valid))
  const invalidCount = computed(() => (cart.value.items || []).filter((item) => !item.valid).length)
  const checkedValidCount = computed(
    () => (cart.value.items || []).filter((item) => item.valid && item.checked).length
  )
  const checkedCount = computed(() => (cart.value.items || []).filter((item) => item.checked).length)
  const allChecked = computed(
    () => validItems.value.length > 0 && validItems.value.every((item) => item.checked)
  )
  const indeterminate = computed(() => {
    const checked = validItems.value.filter((item) => item.checked).length
    return checked > 0 && checked < validItems.value.length
  })

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '0.00')

  async function loadCart() {
    cart.value = await fetchPortalCart()
    const imgs = (cart.value.items || []).map((i) => i.skuDefaultImg).filter(Boolean) as string[]
    if (imgs.length) {
      coverUrls.value = await resolveGoodsImageDisplayUrls(imgs)
    }
  }

  function goProduct(item: PortalCartItem) {
    if (!item.valid || !item.spuId) return
    router.push(`/portal/product/${item.spuId}`)
  }

  async function handleSelectAll(checked: boolean) {
    const targets = validItems.value
    if (!targets.length) return
    await Promise.all(
      targets.map((item) =>
        fetchPortalCartUpdate({ skuId: item.skuId, quantity: item.quantity || 1, checked })
      )
    )
    await loadCart()
  }

  async function moveCheckedToFavorites() {
    const items = (cart.value.items || []).filter(
      (item) => item.valid && item.checked && item.spuId
    )
    const spuIds = [...new Set(items.map((item) => item.spuId!))]
    if (!spuIds.length) {
      ElMessage.warning('请先选择要收藏的商品')
      return
    }

    let removeAfter = false
    try {
      await ElMessageBox.confirm(
        `确定将已选的 ${spuIds.length} 件商品移入收藏吗？`,
        '移入收藏',
        {
          confirmButtonText: '收藏并移除',
          cancelButtonText: '仅收藏',
          distinguishCancelAndClose: true,
          type: 'info'
        }
      )
      removeAfter = true
    } catch (action) {
      if (action !== 'cancel') return
    }

    movingToFavorites.value = true
    try {
      const results = await Promise.allSettled(
        spuIds.map((spuId) => fetchPortalCollectAdd(spuId, { silent: true }))
      )
      const success = results.filter((r) => r.status === 'fulfilled').length
      if (success > 0) {
        ElMessage.success(
          removeAfter ? `已收藏 ${success} 件商品，并从购物车移除` : `已收藏 ${success} 件商品`
        )
      } else {
        ElMessage.info('所选商品已在收藏中')
      }
      if (removeAfter && success > 0) {
        const skuIds = items.map((item) => item.skuId)
        await fetchPortalCartRemove(skuIds)
        notifyPortalCartChanged()
      }
      await loadCart()
    } finally {
      movingToFavorites.value = false
    }
  }

  async function clearInvalid() {
    const skuIds = (cart.value.items || []).filter((item) => !item.valid).map((item) => item.skuId)
    if (!skuIds.length) return
    clearingInvalid.value = true
    try {
      await fetchPortalCartRemove(skuIds)
      await loadCart()
      notifyPortalCartChanged()
      ElMessage.success('已清理失效商品')
    } finally {
      clearingInvalid.value = false
    }
  }

  async function removeChecked() {
    const skuIds = (cart.value.items || []).filter((item) => item.checked).map((item) => item.skuId)
    if (!skuIds.length) {
      ElMessage.warning('请先选择要删除的商品')
      return
    }
    try {
      await ElMessageBox.confirm(`确定删除已选的 ${skuIds.length} 件商品吗？`, '提示', { type: 'warning' })
      removingChecked.value = true
      await fetchPortalCartRemove(skuIds)
      await loadCart()
      notifyPortalCartChanged()
      ElMessage.success('已删除选中商品')
    } catch {
      // cancelled
    } finally {
      removingChecked.value = false
    }
  }

  async function handleCollect(item: PortalCartItem) {
    if (!item.spuId) return

    let removeAfter = false
    try {
      await ElMessageBox.confirm('收藏该商品？', '移入收藏', {
        confirmButtonText: '收藏并移除',
        cancelButtonText: '仅收藏',
        distinguishCancelAndClose: true,
        type: 'info'
      })
      removeAfter = true
    } catch (action) {
      if (action !== 'cancel') return
    }

    await fetchPortalCollectAdd(item.spuId, { silent: removeAfter })
    if (removeAfter) {
      await fetchPortalCartRemove([item.skuId])
      notifyPortalCartChanged()
      await loadCart()
      ElMessage.success('已收藏并从购物车移除')
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
  @import '../styles/variables.scss';

  .cart-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 12px 16px;
    background: var(--portal-bg-elevated);
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);

    &__actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .cart-list {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    overflow: hidden;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);

    &__head {
      display: grid;
      grid-template-columns: 40px 80px 1fr 100px 140px 100px;
      gap: 16px;
      padding: 12px 24px;
      background: #fafbfc;
      font-size: 13px;
      color: var(--portal-text-secondary);
      font-weight: 500;
      border-bottom: 1px solid var(--portal-border);

      @media (width <= 768px) {
        display: none;
      }
    }
  }

  .cart-item {
    display: grid;
    grid-template-columns: auto 80px 1fr 100px 140px 100px;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    border-bottom: 1px solid var(--portal-border);
    transition: background var(--portal-transition);

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafbfc;
    }

    &.invalid {
      opacity: 0.65;
    }

    @media (width <= 768px) {
      grid-template-columns: auto 64px 1fr;
      grid-template-rows: auto auto auto;
      padding: 16px;
    }

    &__img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: var(--portal-radius-sm);
      background: #fafbfc;
      border: 1px solid var(--portal-border);
      cursor: pointer;
    }

    &__info {
      min-width: 0;
    }

    &__title {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 500;
      color: var(--portal-text);
      line-height: 1.4;
      cursor: pointer;

      &:hover {
        color: var(--portal-primary);
      }
    }

    &__attr {
      margin: 0;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__invalid {
      margin: 4px 0 0;
      color: var(--portal-primary);
      font-size: 12px;
    }

    &__price {
      color: var(--portal-primary);
      font-weight: 700;
      font-size: 15px;
    }

    &__actions {
      display: flex;
      flex-direction: column;
      gap: 2px;
      align-items: flex-start;
    }

    @media (width <= 768px) {
      &__price,
      .el-input-number,
      &__actions {
        grid-column: 2 / -1;
      }

      &__actions {
        flex-direction: row;
        gap: 8px;
      }
    }
  }

  .cart-footer {
    margin-top: 20px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 20px 24px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 24px;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);
    position: sticky;
    bottom: 16px;

    &__summary {
      font-size: 14px;
      color: var(--portal-text-secondary);

      strong {
        color: var(--portal-primary);
        font-size: 16px;
      }

      .total {
        color: var(--portal-primary);
        font-size: 26px;
        font-weight: 800;
        margin-left: 4px;
        letter-spacing: -0.02em;
      }
    }

    :deep(.el-button) {
      min-width: 140px;
      border-radius: 24px;
      font-weight: 600;
      background: var(--portal-primary-gradient);
      border: none;
    }
  }

  :deep(.el-empty) {
    padding: 60px 0;
  }
</style>

<!-- C 端限时秒杀页 -->
<template>
  <div v-loading="loading" class="portal-seckill">
    <PortalPageHeader :title="page.title || '限时秒杀'" :subtitle="page.subTitle || '整点场 · 抢完即止'" />

    <div v-if="page.sessions?.length" class="seckill-tabs">
      <button
        v-for="session in page.sessions"
        :key="session.id"
        type="button"
        class="seckill-tab"
        :class="{
          active: activeSessionId === session.id,
          ongoing: session.state === 'ongoing'
        }"
        @click="selectSession(session.id!)"
      >
        <span class="seckill-tab__time">{{ session.startLabel }}</span>
        <span class="seckill-tab__label">{{ sessionStateLabel(session.state) }}</span>
        <PortalSeckillCountdown
          v-if="activeSessionId === session.id"
          :session="session"
          class="seckill-tab__countdown"
        />
      </button>
    </div>

    <div v-if="products.length" class="product-grid">
      <PortalProductCard
        v-for="item in products"
        :key="`${item.spuId}-${item.skuId || 0}`"
        :spu-name="item.spuName"
        :price="item.price"
        :promo-price="item.promoPrice ?? item.price"
        :image-url="coverUrls.get(item.coverImg || '') || placeholderImg"
        :badge="sessionState === 'ongoing' ? '抢购中' : undefined"
        :stock-text="stockText(item)"
        :show-rating="false"
        :show-brand="false"
        @click="goProduct(item)"
      >
        <template v-if="sessionState === 'ongoing'" #footer>
          <ElButton
            type="primary"
            size="small"
            class="product-card__buy"
            :disabled="!item.skuId || (item.seckillStockRemain != null && item.seckillStockRemain <= 0)"
            @click.stop="openBuyDialog(item)"
          >
            {{ item.seckillStockRemain != null && item.seckillStockRemain <= 0 ? '已抢光' : '立即抢购' }}
          </ElButton>
        </template>
      </PortalProductCard>
    </div>
    <ElEmpty v-else description="当前场次暂无秒杀商品" />

    <ElDialog v-model="buyDialogVisible" title="确认秒杀" width="480px" destroy-on-close>
      <div v-if="buyTarget" class="buy-dialog">
        <p class="buy-dialog__name">{{ buyTarget.spuName }}</p>
        <p class="buy-dialog__price">
          秒杀价 ¥{{ formatPrice(buyTarget.promoPrice ?? buyTarget.price) }}
        </p>
        <ElForm label-width="80px">
          <ElFormItem label="数量">
            <ElInputNumber
              v-model="buyQuantity"
              :min="1"
              :max="buyMaxQuantity"
              :disabled="submitting"
            />
          </ElFormItem>
          <ElFormItem label="收货地址">
            <ElSelect v-model="selectedAddressId" placeholder="选择地址" style="width: 100%">
              <ElOption
                v-for="addr in addresses"
                :key="addr.id"
                :label="`${addr.name} ${addr.phone} · ${addr.province}${addr.city}${addr.region}${addr.detailAddress}`"
                :value="addr.id!"
              />
            </ElSelect>
          </ElFormItem>
        </ElForm>
      </div>
      <template #footer>
        <ElButton @click="buyDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSeckillSubmit">提交订单</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { fetchPortalAddressList } from '@/api/portal/address'
import { fetchPortalOrderSubmitToken } from '@/api/portal/order'
import { fetchPortalSeckillOrder, fetchPortalSeckillPage } from '@/api/portal/seckill'
import type { PortalAddress, PortalHomeProduct, PortalSeckillPage } from '@/api/portal/types'
import { usePortalMemberStore } from '@/store/modules/portal-member'
import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
import { formatMoney } from '@/utils/mall/money'
import { PORTAL_PRODUCT_PLACEHOLDER_IMG } from '@/utils/portal/product-placeholder'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import PortalProductCard from '@/views/portal/components/portal-product-card.vue'
import PortalSeckillCountdown from '@/views/portal/components/portal-seckill-countdown.vue'

defineOptions({ name: 'PortalSeckill' })

  const router = useRouter()
  const portalStore = usePortalMemberStore()
  const loading = ref(true)
  const page = ref<PortalSeckillPage>({})
  const products = ref<PortalHomeProduct[]>([])
  const coverUrls = ref(new Map<string, string>())
  const activeSessionId = ref<number>()

  const buyDialogVisible = ref(false)
  const buyTarget = ref<PortalHomeProduct | null>(null)
  const buyQuantity = ref(1)
  const addresses = ref<PortalAddress[]>([])
  const selectedAddressId = ref<number>()
  const orderToken = ref('')
  const submitting = ref(false)

  const placeholderImg = PORTAL_PRODUCT_PLACEHOLDER_IMG

  const sessionState = computed(
    () => page.value.sessions?.find((item) => item.id === activeSessionId.value)?.state
  )

  const buyMaxQuantity = computed(() => {
    const item = buyTarget.value
    if (!item) return 1
    const limits: number[] = [1]
    if (item.seckillLimit && item.seckillLimit > 0) limits.push(item.seckillLimit)
    if (item.seckillStockRemain != null && item.seckillStockRemain > 0) {
      limits.push(item.seckillStockRemain)
    }
    return Math.min(...limits)
  })

  const formatPrice = (p?: number) => formatMoney(p)

  function sessionStateLabel(state?: string) {
    if (state === 'ongoing') return '抢购中'
    if (state === 'upcoming') return '即将开始'
    return '已结束'
  }

  function stockText(item: PortalHomeProduct) {
    if (item.seckillStockRemain == null) return ''
    let text = `剩余 ${item.seckillStockRemain} 件`
    if (item.seckillLimit) text += ` · 限购 ${item.seckillLimit} 件`
    return text
  }

  async function resolveCoverImages(items: PortalHomeProduct[]) {
    const keys = items.map((item) => item.coverImg).filter(Boolean) as string[]
    if (!keys.length) return
    const map = await resolveGoodsImageDisplayUrls(keys)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function loadPage(sessionId?: number) {
    const data = await fetchPortalSeckillPage(sessionId)
    page.value = data
    activeSessionId.value = data.activeSessionId
    products.value = data.products || []
    await resolveCoverImages(products.value)
  }

  async function selectSession(sessionId: number) {
    if (activeSessionId.value === sessionId) return
    loading.value = true
    try {
      await loadPage(sessionId)
    } finally {
      loading.value = false
    }
  }

  function goProduct(item: PortalHomeProduct) {
    if (!item.spuId) return
    router.push({
      path: `/portal/product/${item.spuId}`,
      query: item.skuId ? { skuId: String(item.skuId) } : undefined
    })
  }

  async function ensureLogin(): Promise<boolean> {
    if (portalStore.token) return true
    router.push({ path: '/portal/login', query: { redirect: router.currentRoute.value.fullPath } })
    return false
  }

  async function openBuyDialog(item: PortalHomeProduct) {
    if (!(await ensureLogin())) return
    if (!item.skuId || !activeSessionId.value) return
    buyTarget.value = item
    buyQuantity.value = 1
    buyDialogVisible.value = true
    const [addrList, tokenResult] = await Promise.all([
      fetchPortalAddressList(),
      fetchPortalOrderSubmitToken()
    ])
    addresses.value = addrList
    orderToken.value = tokenResult.orderToken
    const defaultAddr = addrList.find((a) => a.defaultStatus === 1) || addrList[0]
    selectedAddressId.value = defaultAddr?.id
  }

  async function handleSeckillSubmit() {
    if (!buyTarget.value?.skuId || !activeSessionId.value || !selectedAddressId.value || !orderToken.value) {
      ElMessage.warning('请完善收货地址')
      return
    }
    submitting.value = true
    try {
      const result = await fetchPortalSeckillOrder({
        sessionId: activeSessionId.value,
        skuId: buyTarget.value.skuId,
        quantity: buyQuantity.value,
        addressId: selectedAddressId.value,
        orderToken: orderToken.value
      })
      buyDialogVisible.value = false
      ElMessage.success(`下单成功，订单号 ${result.orderSn}`)
      router.push('/portal/orders')
    } catch {
      const tokenResult = await fetchPortalOrderSubmitToken()
      orderToken.value = tokenResult.orderToken
    } finally {
      submitting.value = false
    }
  }

  onMounted(async () => {
    try {
      await loadPage()
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .seckill-tabs {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    margin-bottom: 20px;
    padding-bottom: 4px;
  }

  .seckill-tab {
    flex-shrink: 0;
    min-width: 100px;
    padding: 10px 14px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    background: var(--portal-bg-elevated);
    cursor: pointer;
    transition: all var(--portal-transition);

    &__time {
      display: block;
      font-size: 16px;
      font-weight: 700;
      color: var(--portal-text);
    }

    &__label {
      display: block;
      margin-top: 4px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__countdown {
      display: block;
      margin-top: 4px;
    }

    &.ongoing &__label {
      color: var(--portal-primary);
    }

    &.active {
      border-color: var(--portal-primary);
      background: var(--portal-primary-light);
    }
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }

  .product-card__buy {
    width: 100%;
    margin-top: 12px;
  }

  .buy-dialog {
    &__name {
      margin: 0 0 8px;
      font-weight: 600;
    }

    &__price {
      margin: 0 0 16px;
      color: var(--portal-primary);
      font-size: 18px;
      font-weight: 700;
    }
  }
</style>

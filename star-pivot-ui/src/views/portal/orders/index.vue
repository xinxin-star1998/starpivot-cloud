<!-- C 端我的订单 -->
<template>
  <div v-loading="loading" class="portal-orders">
    <PortalPageHeader title="我的订单" subtitle="查看与管理您的全部订单" />

    <div class="status-tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.key"
        type="button"
        class="status-tab"
        :class="{ active: activeStatus === tab.key }"
        @click="activeStatus = tab.key; handleTabChange()"
      >
        {{ tab.label }}
        <span v-if="tabBadge(tab.key)" class="status-tab__badge">{{ formatTabBadge(tab.key) }}</span>
      </button>
    </div>

    <div v-if="displayOrders.length" class="order-list">
      <div v-for="order in displayOrders" :key="order.id" class="order-card">
        <div class="order-card__head">
          <span class="order-sn">{{ order.orderSn }}</span>
          <span class="order-time">{{ order.createTime }}</span>
          <ElTag :type="getPortalOrderStatusType(order.status)" size="small">
            {{ getPortalOrderStatusLabel(order.status) }}
          </ElTag>
        </div>

        <div class="order-card__items">
          <div v-for="item in previewItems(order)" :key="item.id" class="order-item-line">
            <img
              :src="coverUrls.get(item.skuPic || item.spuPic || '') || placeholderImg"
              class="order-item-line__img"
              alt=""
            />
            <span class="order-item-line__name">{{ item.skuName || item.spuName }}</span>
            <span class="order-item-line__qty">x{{ item.skuQuantity }}</span>
            <span class="order-item-line__price">¥{{ formatPrice(item.realAmount) }}</span>
          </div>
          <p v-if="(order.orderItemList?.length || 0) > 2" class="more-hint">
            共 {{ order.orderItemList?.length }} 件商品
          </p>
        </div>

        <div class="order-card__foot">
          <span class="pay-amount">
            实付：<strong>¥{{ formatPrice(order.payAmount) }}</strong>
          </span>
          <div class="order-card__actions">
            <ElButton
              v-if="hasLogistics(order)"
              size="small"
              type="primary"
              plain
              @click="openLogisticsTrack(order)"
            >
              查看物流
            </ElButton>
            <ElButton size="small" @click="openDetail(order.id!)">订单详情</ElButton>
            <ElButton
              v-if="order.status === 0"
              size="small"
              type="danger"
              :loading="actionOrderId === order.id && actionType === 'pay'"
              @click="handlePay(order.id!)"
            >
              去支付
            </ElButton>
            <ElButton
              v-if="order.status === 0"
              size="small"
              :loading="actionOrderId === order.id && actionType === 'cancel'"
              @click="handleCancel(order.id!)"
            >
              取消订单
            </ElButton>
            <ElButton
              v-if="hasReviewableItem(order)"
              size="small"
              type="primary"
              plain
              @click="goReview(order)"
            >
              去评价
            </ElButton>
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
      </div>
    </div>

    <ElEmpty v-else :description="emptyDescription">
      <ElButton v-if="isReviewTab" type="primary" @click="router.push('/portal/account/pending-reviews')">
        查看待评价商品
      </ElButton>
      <ElButton v-else type="primary" @click="router.push('/portal')">去逛逛</ElButton>
    </ElEmpty>

    <ElDrawer v-model="detailVisible" title="订单详情" size="480px" destroy-on-close>
      <template v-if="detailOrder">
        <div class="detail-block">
          <p><strong>订单号：</strong>{{ detailOrder.orderSn }}</p>
          <p>
            <strong>状态：</strong>
            <ElTag :type="getPortalOrderStatusType(detailOrder.status)" size="small">
              {{ getPortalOrderStatusLabel(detailOrder.status) }}
            </ElTag>
          </p>
          <p><strong>下单时间：</strong>{{ detailOrder.createTime }}</p>
        </div>
        <div class="detail-block">
          <p><strong>收货信息</strong></p>
          <p>{{ detailOrder.receiverName }} {{ detailOrder.receiverPhone }}</p>
          <p>
            {{ detailOrder.receiverProvince }}{{ detailOrder.receiverCity
            }}{{ detailOrder.receiverRegion }}{{ detailOrder.receiverDetailAddress }}
          </p>
        </div>
        <div v-if="hasLogistics(detailOrder)" class="detail-block detail-logistics">
          <p><strong>物流信息</strong></p>
          <p v-if="detailOrder.deliveryCompany">物流公司：{{ detailOrder.deliveryCompany }}</p>
          <p class="detail-logistics__sn">
            运单号：{{ detailOrder.deliverySn }}
            <ElButton link type="primary" size="small" @click="copyDeliverySn(detailOrder.deliverySn!)">
              复制
            </ElButton>
          </p>
          <ElButton size="small" type="primary" plain @click="openLogisticsTrack(detailOrder)">
            查询物流
          </ElButton>
        </div>
        <div class="detail-block">
          <p><strong>商品明细</strong></p>
          <div v-for="item in detailOrder.orderItemList || []" :key="item.id" class="detail-item">
            <span>{{ item.skuName }}</span>
            <span>x{{ item.skuQuantity }}</span>
            <span class="price">¥{{ formatPrice(item.realAmount) }}</span>
            <ElButton
              v-if="item.spuId && reviewableSpuIds.has(item.spuId)"
              link
              type="primary"
              size="small"
              @click="goReviewProduct(item.spuId)"
            >
              评价
            </ElButton>
          </div>
        </div>
        <div class="detail-block detail-total">
          实付：<strong>¥{{ formatPrice(detailOrder.payAmount) }}</strong>
        </div>
        <div v-if="detailOrder.status === 0" class="detail-actions">
          <ElButton type="danger" @click="handlePay(detailOrder.id!)">
            {{ alipayEnabled ? '支付宝支付' : 'Mock 支付' }}
          </ElButton>
          <ElButton @click="handleCancel(detailOrder.id!)">取消订单</ElButton>
        </div>
        <div v-if="detailOrder.status === 2" class="detail-actions">
          <ElButton type="primary" @click="handleConfirmReceive(detailOrder.id!)"
            >确认收货</ElButton
          >
        </div>
        <div v-if="detailOrder.status === 2 || detailOrder.status === 3" class="detail-actions">
          <ElButton @click="openReturnDialog">申请退货</ElButton>
        </div>
      </template>
    </ElDrawer>

    <ReturnDialog v-model="returnDialogVisible" :order="returnOrder" @success="onReturnSuccess" />
    <LogisticsDialog v-model:visible="logisticsVisible" v-model:order-id="logisticsOrderId" />
  </div>
</template>

<script setup lang="ts">
import {
  fetchPortalOrderCancel,
  fetchPortalOrderConfirmReceive,
  fetchPortalOrderDetail,
  fetchPortalOrderList,
  fetchPortalOrderMockPay,
  fetchPortalOrderStatusCounts
} from '@/api/portal/order'
import {fetchPortalAlipayEnabled, fetchPortalAlipayPay} from '@/api/portal/pay'
import {fetchPortalReviewableSpuIds} from '@/api/portal/comment'
import type {PortalOrder, PortalOrderItem} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {submitAlipayPayForm} from '@/utils/portal/alipay-pay'
import {handleMutationError} from '@/utils/http/mutation'
import {getPortalOrderStatusLabel, getPortalOrderStatusType} from '@/utils/portal/order-status'
import LogisticsDialog from './modules/logistics-dialog.vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import ReturnDialog from './modules/return-dialog.vue'
import PortalPageHeader from '../components/portal-page-header.vue'

defineOptions({ name: 'PortalOrders' })

  const router = useRouter()
  const route = useRoute()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const loadingMore = ref(false)
  const activeStatus = ref('all')
  const orders = ref<PortalOrder[]>([])
  const pageNum = ref(1)
  const pageSize = 10
  const total = ref(0)
  const coverUrls = ref(new Map<string, string>())
  const detailVisible = ref(false)
  const detailOrder = ref<PortalOrder | null>(null)
  const actionOrderId = ref<number>()
  const actionType = ref<'pay' | 'cancel'>()
  const alipayEnabled = ref(false)
  const returnDialogVisible = ref(false)
  const logisticsVisible = ref(false)
  const logisticsOrderId = ref<number>()
  const returnOrder = ref<PortalOrder | null>(null)
  const reviewableSpuIds = ref(new Set<number>())
  const tabCounts = ref<Record<string, number>>({})

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect fill="#f0f0f0" width="48" height="48"/></svg>'
    )

  const statusTabs = [
    { key: 'all', label: '全部', status: undefined as number | undefined },
    { key: '0', label: '待付款', status: 0 },
    { key: '1', label: '待发货', status: 1 },
    { key: '2', label: '已发货', status: 2 },
    { key: '3', label: '已完成', status: 3 },
    { key: 'review', label: '待评价', status: undefined as number | undefined },
    { key: '4', label: '已关闭', status: 4 }
  ]

  const isReviewTab = computed(() => activeStatus.value === 'review')

  const displayOrders = computed(() => {
    if (!isReviewTab.value) return orders.value
    return orders.value.filter((order) => hasReviewableItem(order))
  })

  const emptyDescription = computed(() =>
    isReviewTab.value ? '暂无待评价订单' : '暂无订单'
  )

  const hasMore = computed(() => {
    if (isReviewTab.value) return false
    return orders.value.length < total.value
  })

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '0.00')

  function tabBadge(key: string) {
    if (key === 'all' || key === '4') return 0
    return tabCounts.value[key] ?? 0
  }

  function formatTabBadge(key: string) {
    const count = tabBadge(key)
    return count > 99 ? '99+' : count
  }

  async function loadTabCounts() {
    try {
      tabCounts.value = await fetchPortalOrderStatusCounts()
    } catch {
      tabCounts.value = {}
    }
  }

  function currentStatusFilter() {
    const tab = statusTabs.find((t) => t.key === activeStatus.value)
    return tab?.status
  }

  function previewItems(order: PortalOrder): PortalOrderItem[] {
    return (order.orderItemList || []).slice(0, 2)
  }

  function collectReviewableSpuIds(list: PortalOrder[]): number[] {
    const ids = new Set<number>()
    for (const order of list) {
      if (order.status !== 2 && order.status !== 3) continue
      for (const item of order.orderItemList || []) {
        if (item.spuId) ids.add(item.spuId)
      }
    }
    return [...ids]
  }

  async function refreshReviewableSpuIds(list: PortalOrder[]) {
    const spuIds = collectReviewableSpuIds(list)
    if (!spuIds.length) {
      reviewableSpuIds.value = new Set()
      return
    }
    try {
      const res = await fetchPortalReviewableSpuIds(spuIds)
      reviewableSpuIds.value = new Set(res.reviewableSpuIds || [])
    } catch {
      reviewableSpuIds.value = new Set()
    }
  }

  function hasReviewableItem(order: PortalOrder) {
    if (order.status !== 2 && order.status !== 3) return false
    return (order.orderItemList || []).some(
      (item) => item.spuId != null && reviewableSpuIds.value.has(item.spuId)
    )
  }

  function findFirstReviewableSpuId(order: PortalOrder): number | undefined {
    return (order.orderItemList || []).find(
      (item) => item.spuId != null && reviewableSpuIds.value.has(item.spuId)
    )?.spuId
  }

  function goReviewProduct(spuId: number) {
    detailVisible.value = false
    router.push(`/portal/product/${spuId}?review=1`)
  }

  function goReview(order: PortalOrder) {
    const spuId = findFirstReviewableSpuId(order)
    if (spuId) goReviewProduct(spuId)
  }

  async function resolveOrderImages(list: PortalOrder[]) {
    const keys = list.flatMap((o) =>
      (o.orderItemList || []).flatMap((i) => [i.skuPic, i.spuPic].filter(Boolean) as string[])
    )
    if (!keys.length) return
    const map = await resolveGoodsImageDisplayUrls(keys)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function loadReviewOrders() {
    pageNum.value = 1
    orders.value = []
    try {
      const [shipped, completed] = await Promise.all([
        fetchPortalOrderList({ pageNum: 1, pageSize: 100, status: 2 }),
        fetchPortalOrderList({ pageNum: 1, pageSize: 100, status: 3 })
      ])
      const merged = [...(shipped.rows || []), ...(completed.rows || [])]
      merged.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
      orders.value = merged
      total.value = merged.length
      await resolveOrderImages(merged)
      await refreshReviewableSpuIds(merged)
      await loadTabCounts()
    } catch (error) {
      handleMutationError(error, '加载待评价订单失败')
      orders.value = []
      total.value = 0
    }
  }

  async function loadOrders(reset = true) {
    if (isReviewTab.value) {
      await loadReviewOrders()
      return
    }
    if (reset) {
      pageNum.value = 1
      orders.value = []
    }
    try {
      const res = await fetchPortalOrderList({
        pageNum: pageNum.value,
        pageSize,
        status: currentStatusFilter()
      })
      total.value = res.total || 0
      const rows = res.rows || []
      orders.value = reset ? rows : [...orders.value, ...rows]
      await resolveOrderImages(rows)
      await refreshReviewableSpuIds(orders.value)
      await loadTabCounts()
    } catch (error) {
      handleMutationError(error, '加载订单失败')
      if (reset) {
        orders.value = []
        total.value = 0
      }
    }
  }

  function syncStatusFromRoute() {
    const status = route.query.status
    if (typeof status === 'string' && statusTabs.some((tab) => tab.key === status)) {
      activeStatus.value = status
    }
  }

  function handleTabChange() {
    const nextQuery = { ...route.query }
    if (activeStatus.value === 'all') {
      delete nextQuery.status
    } else {
      nextQuery.status = activeStatus.value
    }
    router.replace({ query: nextQuery })
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadOrders(false).finally(() => {
      loadingMore.value = false
    })
  }

  async function openDetail(orderId: number) {
    try {
      detailOrder.value = await fetchPortalOrderDetail(orderId)
      detailVisible.value = true
      await resolveOrderImages([detailOrder.value])
    } catch (error) {
      handleMutationError(error, '加载订单详情失败')
    }
  }

  function hasLogistics(order?: PortalOrder | null) {
    if (!order?.deliverySn?.trim()) return false
    return order.status === 2 || order.status === 3
  }

  function openLogisticsTrack(order: PortalOrder) {
    if (!order.id || !order.deliverySn?.trim()) return
    logisticsOrderId.value = order.id
    logisticsVisible.value = true
  }

  async function copyDeliverySn(deliverySn: string) {
    try {
      await navigator.clipboard.writeText(deliverySn)
      ElMessage.success('运单号已复制')
    } catch {
      ElMessage.info(deliverySn)
    }
  }

  async function handlePay(orderId: number) {
    actionOrderId.value = orderId
    actionType.value = 'pay'
    try {
      if (alipayEnabled.value) {
        const result = await fetchPortalAlipayPay(orderId)
        submitAlipayPayForm(result.payForm)
        ElMessage.info('正在跳转支付宝，支付完成后请刷新订单列表')
      } else {
        await fetchPortalOrderMockPay(orderId)
        ElMessage.success('支付成功')
        detailVisible.value = false
        await loadOrders(true)
      }
    } catch (err) {
      if (alipayEnabled.value) {
        handleMutationError(err, '支付宝下单失败')
      } else {
        handleMutationError(err, '支付失败')
      }
    } finally {
      actionOrderId.value = undefined
      actionType.value = undefined
    }
  }

  async function handleConfirmReceive(orderId: number) {
    try {
      await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', { type: 'info' })
      await fetchPortalOrderConfirmReceive(orderId)
      detailVisible.value = false
      await loadOrders(true)
    } catch (error) {
      handleMutationError(error, '确认收货失败')
    }
  }

  function openReturnDialog() {
    if (!detailOrder.value) return
    returnOrder.value = detailOrder.value
    returnDialogVisible.value = true
  }

  async function onReturnSuccess() {
    detailVisible.value = false
    await loadOrders(true)
  }

  async function handleCancel(orderId: number) {
    try {
      await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
      actionOrderId.value = orderId
      actionType.value = 'cancel'
      await fetchPortalOrderCancel(orderId)
      ElMessage.success('订单已取消')
      detailVisible.value = false
      await loadOrders(true)
    } catch (error) {
      handleMutationError(error, '取消订单失败')
    } finally {
      actionOrderId.value = undefined
      actionType.value = undefined
    }
  }

  onMounted(async () => {
    window.addEventListener('portal-review-changed', loadTabCounts)
    if (!requireLogin()) {
      loading.value = false
      return
    }
    syncStatusFromRoute()
    try {
      alipayEnabled.value = await fetchPortalAlipayEnabled()
      await loadOrders(true)
    } finally {
      loading.value = false
    }
  })

  onUnmounted(() => {
    window.removeEventListener('portal-review-changed', loadTabCounts)
  })

  watch(
    () => route.query.status,
    () => {
      if (!requireLogin()) return
      syncStatusFromRoute()
      loadOrders(true)
    }
  )
</script>

<style scoped lang="scss">

  .status-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .status-tab {
    padding: 8px 18px;
    border: 1px solid var(--portal-border);
    border-radius: 20px;
    background: var(--portal-bg-elevated);
    color: var(--portal-text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all var(--portal-transition);

    &:hover {
      border-color: #ffb8b8;
      color: var(--portal-primary);
    }

    &.active {
      background: var(--portal-primary-gradient);
      border-color: transparent;
      color: #fff;
      font-weight: 600;
    }

    &__badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 18px;
      height: 18px;
      margin-left: 6px;
      padding: 0 5px;
      border-radius: 9px;
      background: #fff;
      color: var(--portal-primary);
      font-size: 11px;
      font-weight: 700;
      line-height: 1;
    }

    &.active &__badge {
      background: rgb(255 255 255 / 25%);
      color: #fff;
    }
  }

  .order-list {
    margin-top: 0;
  }

  .order-card {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    margin-bottom: 16px;
    overflow: hidden;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);
    transition: box-shadow var(--portal-transition);

    &:hover {
      box-shadow: var(--portal-shadow);
    }

    &__head {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 20px;
      background: #fafbfc;
      border-bottom: 1px solid var(--portal-border);
      font-size: 13px;

      .order-sn {
        font-weight: 600;
        color: var(--portal-text);
      }

      .order-time {
        flex: 1;
        color: var(--portal-text-muted);
      }
    }

    &__items {
      padding: 16px 20px;
    }

    &__foot {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 20px;
      border-top: 1px solid var(--portal-border);
      background: #fafbfc;

      .pay-amount {
        font-size: 14px;
        color: var(--portal-text-secondary);

        strong {
          color: var(--portal-primary);
          font-size: 20px;
          font-weight: 800;
        }
      }
    }

    &__actions {
      display: flex;
      gap: 8px;

      :deep(.el-button) {
        border-radius: 16px;
      }
    }
  }

  .order-item-line {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    font-size: 13px;

    &__img {
      width: 56px;
      height: 56px;
      object-fit: cover;
      border-radius: var(--portal-radius-sm);
      background: #fafbfc;
      border: 1px solid var(--portal-border);
    }

    &__name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: var(--portal-text);
    }

    &__qty {
      color: var(--portal-text-muted);
    }

    &__price {
      color: var(--portal-text-secondary);
      min-width: 72px;
      text-align: right;
      font-weight: 500;
    }
  }

  .more-hint {
    margin: 4px 0 0;
    font-size: 12px;
    color: var(--portal-text-muted);
  }

  .load-more {
    text-align: center;
    padding: 20px;

    :deep(.el-button) {
      border-radius: 20px;
      min-width: 140px;
    }
  }

  .detail-block {
    margin-bottom: 16px;
    font-size: 14px;
    line-height: 1.6;
    color: var(--portal-text);

    p {
      margin: 4px 0;
    }
  }

  .detail-logistics__sn {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 4px;
  }

  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    padding: 10px 0;
    border-bottom: 1px solid var(--portal-border);
    font-size: 13px;
    flex-wrap: wrap;

    .price {
      color: var(--portal-primary);
      font-weight: 600;
    }
  }

  .detail-total {
    text-align: right;
    font-size: 16px;

    strong {
      color: var(--portal-primary);
      font-size: 22px;
      font-weight: 800;
    }
  }

  .detail-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 16px;
  }
</style>

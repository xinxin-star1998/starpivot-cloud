<!-- C 端我的订单 -->
<template>
  <div v-loading="loading" class="portal-orders">
    <h1 class="page-title">我的订单</h1>

    <ElTabs v-model="activeStatus" @tab-change="handleTabChange">
      <ElTabPane v-for="tab in statusTabs" :key="tab.key" :label="tab.label" :name="tab.key" />
    </ElTabs>

    <div v-if="orders.length" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
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
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
      </div>
    </div>

    <ElEmpty v-else description="暂无订单">
      <ElButton type="primary" @click="router.push('/portal')">去逛逛</ElButton>
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
            {{ detailOrder.receiverProvince }}{{ detailOrder.receiverCity }}{{ detailOrder.receiverRegion
            }}{{ detailOrder.receiverDetailAddress }}
          </p>
        </div>
        <div class="detail-block">
          <p><strong>商品明细</strong></p>
          <div v-for="item in detailOrder.orderItemList || []" :key="item.id" class="detail-item">
            <span>{{ item.skuName }}</span>
            <span>x{{ item.skuQuantity }}</span>
            <span class="price">¥{{ formatPrice(item.realAmount) }}</span>
          </div>
        </div>
        <div class="detail-block detail-total">
          实付：<strong>¥{{ formatPrice(detailOrder.payAmount) }}</strong>
        </div>
        <div v-if="detailOrder.status === 0" class="detail-actions">
          <ElButton type="danger" @click="handlePay(detailOrder.id!)">Mock 支付</ElButton>
          <ElButton @click="handleCancel(detailOrder.id!)">取消订单</ElButton>
        </div>
      </template>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import {
    fetchPortalOrderCancel,
    fetchPortalOrderDetail,
    fetchPortalOrderList,
    fetchPortalOrderMockPay
  } from '@/api/portal/order'
  import type { PortalOrder, PortalOrderItem } from '@/api/portal/types'
  import { usePortalAuth } from '@/hooks/portal/usePortalAuth'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import {
    getPortalOrderStatusLabel,
    getPortalOrderStatusType
  } from '@/utils/portal/order-status'
  import { ElMessage, ElMessageBox } from 'element-plus'

  defineOptions({ name: 'PortalOrders' })

  const router = useRouter()
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

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect fill="#f0f0f0" width="48" height="48"/></svg>')

  const statusTabs = [
    { key: 'all', label: '全部', status: undefined },
    { key: '0', label: '待付款', status: 0 },
    { key: '1', label: '待发货', status: 1 },
    { key: '2', label: '已发货', status: 2 },
    { key: '3', label: '已完成', status: 3 },
    { key: '4', label: '已关闭', status: 4 }
  ]

  const hasMore = computed(() => orders.value.length < total.value)

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '0.00')

  function currentStatusFilter() {
    const tab = statusTabs.find((t) => t.key === activeStatus.value)
    return tab?.status
  }

  function previewItems(order: PortalOrder): PortalOrderItem[] {
    return (order.orderItemList || []).slice(0, 2)
  }

  async function resolveOrderImages(list: PortalOrder[]) {
    const keys = list.flatMap((o) =>
      (o.orderItemList || []).flatMap((i) => [i.skuPic, i.spuPic].filter(Boolean) as string[])
    )
    if (!keys.length) return
    const map = await resolveGoodsImageDisplayUrls(keys)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function loadOrders(reset = true) {
    if (reset) {
      pageNum.value = 1
      orders.value = []
    }
    const res = await fetchPortalOrderList({
      pageNum: pageNum.value,
      pageSize,
      status: currentStatusFilter()
    })
    total.value = res.total || 0
    const rows = res.rows || []
    orders.value = reset ? rows : [...orders.value, ...rows]
    await resolveOrderImages(rows)
  }

  function handleTabChange() {
    loadOrders(true)
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadOrders(false).finally(() => {
      loadingMore.value = false
    })
  }

  async function openDetail(orderId: number) {
    detailOrder.value = await fetchPortalOrderDetail(orderId)
    detailVisible.value = true
    await resolveOrderImages([detailOrder.value])
  }

  async function handlePay(orderId: number) {
    actionOrderId.value = orderId
    actionType.value = 'pay'
    try {
      await fetchPortalOrderMockPay(orderId)
      ElMessage.success('支付成功')
      detailVisible.value = false
      await loadOrders(true)
    } finally {
      actionOrderId.value = undefined
      actionType.value = undefined
    }
  }

  async function handleCancel(orderId: number) {
    await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
    actionOrderId.value = orderId
    actionType.value = 'cancel'
    try {
      await fetchPortalOrderCancel(orderId)
      ElMessage.success('订单已取消')
      detailVisible.value = false
      await loadOrders(true)
    } finally {
      actionOrderId.value = undefined
      actionType.value = undefined
    }
  }

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    try {
      await loadOrders(true)
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

  .order-list {
    margin-top: 8px;
  }

  .order-card {
    background: #fff;
    border-radius: 8px;
    margin-bottom: 12px;
    overflow: hidden;

    &__head {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
      font-size: 13px;

      .order-sn {
        font-weight: 600;
        color: #333;
      }

      .order-time {
        flex: 1;
        color: #999;
      }
    }

    &__items {
      padding: 12px 16px;
    }

    &__foot {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      border-top: 1px solid #f0f0f0;

      .pay-amount strong {
        color: #e1251b;
        font-size: 18px;
      }
    }

    &__actions {
      display: flex;
      gap: 8px;
    }
  }

  .order-item-line {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 0;
    font-size: 13px;

    &__img {
      width: 48px;
      height: 48px;
      object-fit: cover;
      border-radius: 4px;
      background: #f5f5f5;
    }

    &__name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__qty {
      color: #999;
    }

    &__price {
      color: #666;
      min-width: 72px;
      text-align: right;
    }
  }

  .more-hint {
    margin: 4px 0 0;
    font-size: 12px;
    color: #999;
  }

  .load-more {
    text-align: center;
    padding: 16px;
  }

  .detail-block {
    margin-bottom: 16px;
    font-size: 14px;
    line-height: 1.6;
    color: #333;

    p {
      margin: 4px 0;
    }
  }

  .detail-item {
    display: flex;
    justify-content: space-between;
    gap: 8px;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;
    font-size: 13px;

    .price {
      color: #e1251b;
    }
  }

  .detail-total {
    text-align: right;
    font-size: 16px;

    strong {
      color: #e1251b;
      font-size: 20px;
    }
  }

  .detail-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 16px;
  }
</style>

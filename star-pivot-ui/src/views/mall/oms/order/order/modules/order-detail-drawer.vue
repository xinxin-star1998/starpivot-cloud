<template>
  <ElDrawer v-model="drawerVisible" title="订单详情" size="640px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="2" border size="small" class="mb-4">
        <ElDescriptionsItem label="订单号" :span="2">{{ detail.orderSn }}</ElDescriptionsItem>
        <ElDescriptionsItem label="会员">{{ detail.memberUsername || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getPortalOrderStatusType(detail.status)" size="small">
            {{ getPortalOrderStatusLabel(detail.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="订单总额">¥{{ formatAmount(detail.totalAmount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="应付金额">¥{{ formatAmount(detail.payAmount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="运费">¥{{ formatAmount(detail.freightAmount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="优惠">¥{{ formatAmount(detail.discountAmount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="下单时间" :span="2">{{ detail.createTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="支付时间" :span="2">{{ detail.paymentTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="发货时间" :span="2">{{ detail.deliveryTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物流公司">{{ detail.deliveryCompany || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="物流单号">{{ detail.deliverySn || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="收货人">{{ detail.receiverName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="联系电话">{{ detail.receiverPhone || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="收货地址" :span="2">
          {{ formatAddress(detail) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="备注" :span="2">{{ detail.note || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <h4 class="section-title">商品明细</h4>
      <ElTable :data="detail.orderItemList || []" border size="small">
        <ElTableColumn prop="skuName" label="SKU" min-width="140" show-overflow-tooltip />
        <ElTableColumn prop="skuAttrsVals" label="规格" min-width="100" show-overflow-tooltip />
        <ElTableColumn prop="skuPrice" label="单价" width="90">
          <template #default="{ row }">¥{{ formatAmount(row.skuPrice) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="skuQuantity" label="数量" width="70" />
        <ElTableColumn prop="realAmount" label="小计" width="90">
          <template #default="{ row }">¥{{ formatAmount(row.realAmount) }}</template>
        </ElTableColumn>
      </ElTable>

      <div v-if="showActions" class="drawer-actions">
        <ElButton
          v-if="hasAuth('mall:order:deliver') && detail.status === 1"
          type="primary"
          @click="emit('deliver', detail.id)"
        >
          发货
        </ElButton>
        <ElButton
          v-if="hasAuth('mall:order:close') && (detail.status === 0 || detail.status === 1)"
          type="danger"
          @click="handleClose"
        >
          关闭订单
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { fetchOmsOrderById, fetchOmsOrderClose, type OmsOrderVo } from '@/api/mall/order'
  import { getPortalOrderStatusLabel, getPortalOrderStatusType } from '@/utils/portal/order-status'
  import { useAuth } from '@/hooks/core/useAuth'
  import { ElMessageBox } from 'element-plus'

  interface Props {
    visible: boolean
    orderId?: number
    showActions?: boolean
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'deliver', orderId?: number): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    showActions: true
  })
  const emit = defineEmits<Emits>()

  const { hasAuth } = useAuth()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const detail = ref<OmsOrderVo | null>(null)

  watch(
    () => [props.visible, props.orderId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        detail.value = await fetchOmsOrderById(id)
      } finally {
        loading.value = false
      }
    }
  )

  function formatAmount(val?: number) {
    if (val == null) return '0.00'
    return Number(val).toFixed(2)
  }

  function formatAddress(order: OmsOrderVo) {
    const parts = [
      order.receiverProvince,
      order.receiverCity,
      order.receiverRegion,
      order.receiverDetailAddress
    ].filter(Boolean)
    return parts.length ? parts.join('') : '-'
  }

  async function handleClose() {
    if (detail.value?.id == null) return
    await ElMessageBox.confirm('确定关闭该订单？', '关闭订单', { type: 'warning' })
    await fetchOmsOrderClose({ orderId: detail.value.id })
    emit('submit')
    drawerVisible.value = false
  }
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }
  .section-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
  }
  .drawer-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
  }
</style>

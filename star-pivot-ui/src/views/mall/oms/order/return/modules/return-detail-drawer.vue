<template>
  <ElDrawer v-model="drawerVisible" title="退货详情" size="560px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="订单号">{{ detail.orderSn }}</ElDescriptionsItem>
        <ElDescriptionsItem label="会员">{{ detail.memberUsername || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ RETURN_STATUS_MAP[detail.status ?? 0] ?? detail.status }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="退款金额">¥{{ formatAmount(detail.returnAmount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="申请时间">{{ detail.createTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货人">{{ detail.returnName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="联系电话">{{ detail.returnPhone || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货原因">{{ detail.reason || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="问题描述">{{ detail.description || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理人">{{ detail.handleMan || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理时间">{{ detail.handleTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理备注">{{ detail.handleNote || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <h4 class="section-title">商品信息</h4>
      <ElDescriptions :column="1" border size="small">
        <ElDescriptionsItem label="SKU">{{ detail.skuName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="品牌">{{ detail.skuBrand || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="规格">{{ detail.skuAttrsVals || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="数量">{{ detail.skuCount ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="单价">¥{{ formatAmount(detail.skuPrice) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="实付">¥{{ formatAmount(detail.skuRealPrice) }}</ElDescriptionsItem>
      </ElDescriptions>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { fetchReturnById, RETURN_STATUS_MAP, type ReturnVo } from '@/api/mall/order-return'

  interface Props {
    visible: boolean
    returnId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const detail = ref<ReturnVo | null>(null)

  watch(
    () => [props.visible, props.returnId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        detail.value = await fetchReturnById(id)
      } finally {
        loading.value = false
      }
    }
  )

  function formatAmount(val?: number) {
    if (val == null) return '0.00'
    return Number(val).toFixed(2)
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
</style>

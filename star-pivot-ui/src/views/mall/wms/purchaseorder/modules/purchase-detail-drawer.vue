<template>
  <ElDrawer v-model="drawerVisible" title="采购单详情" size="520px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="6" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="采购单 ID">{{ detail.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="采购人">{{ detail.assigneeName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="电话">{{ detail.phone || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ PURCHASE_STATUS_MAP[detail.status ?? 0] ?? detail.status }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">{{ detail.wareId ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="金额">{{ detail.amount ?? '-' }}</ElDescriptionsItem>
      </ElDescriptions>
      <ElTable :data="detail.details || []" border size="small">
        <ElTableColumn prop="id" label="明细ID" width="80" />
        <ElTableColumn prop="skuId" label="SKU" width="80" />
        <ElTableColumn prop="skuNum" label="数量" width="70" />
        <ElTableColumn prop="skuPrice" label="单价" width="80" />
        <ElTableColumn prop="wareId" label="仓库" width="70" />
        <ElTableColumn prop="status" label="状态">
          <template #default="{ row }">
            {{ PURCHASE_DETAIL_STATUS_MAP[row.status ?? 0] ?? row.status }}
          </template>
        </ElTableColumn>
      </ElTable>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import {
    fetchPurchaseById,
    PURCHASE_DETAIL_STATUS_MAP,
    PURCHASE_STATUS_MAP,
    type PurchaseVo
  } from '@/api/mall/purchase'

  interface Props {
    visible: boolean
    purchaseId?: number
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
  const detail = ref<PurchaseVo | null>(null)

  watch(
    () => [props.visible, props.purchaseId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        detail.value = await fetchPurchaseById(id)
      } finally {
        loading.value = false
      }
    }
  )
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }
</style>

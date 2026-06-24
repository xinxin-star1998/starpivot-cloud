<template>
  <ElDialog v-model="dialogVisible" title="完成采购" width="640px" align-center destroy-on-close>
    <ElSkeleton v-if="loading" :rows="4" animated />
    <template v-else>
      <p class="hint">采购单 #{{ purchaseId }} — 请确认每条明细的采购结果</p>
      <ElTable :data="items" border size="small">
        <ElTableColumn prop="skuId" label="SKU" width="80" />
        <ElTableColumn prop="skuNum" label="数量" width="70" />
        <ElTableColumn prop="wareId" label="仓库" width="70" />
        <ElTableColumn label="结果" min-width="160">
          <template #default="{ row }">
            <ElRadioGroup v-model="row.resultStatus">
              <ElRadio :value="3">成功入库</ElRadio>
              <ElRadio :value="4">采购失败</ElRadio>
            </ElRadioGroup>
          </template>
        </ElTableColumn>
      </ElTable>
    </template>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" :disabled="loading" @click="handleSubmit">
        确认完成
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchPurchaseById, fetchPurchaseDone, type PurchaseDetailVo } from '@/api/mall/purchase'

  interface Props {
    visible: boolean
    purchaseId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const submitting = ref(false)
  const items = ref<(PurchaseDetailVo & { resultStatus: number })[]>([])

  watch(
    () => [props.visible, props.purchaseId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        items.value = []
        return
      }
      loading.value = true
      try {
        const detail = await fetchPurchaseById(id)
        items.value = (detail.details || [])
          .filter((d) => d.status === 2)
          .map((d) => ({ ...d, resultStatus: 3 }))
      } finally {
        loading.value = false
      }
    }
  )

  async function handleSubmit() {
    if (props.purchaseId == null || !items.value.length) return
    submitting.value = true
    try {
      await fetchPurchaseDone({
        id: props.purchaseId,
        items: items.value.map((item) => ({
          itemId: item.id!,
          status: item.resultStatus
        }))
      })
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .hint {
    margin: 0 0 12px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
</style>

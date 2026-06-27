<template>
  <ElDrawer v-model="drawerVisible" destroy-on-close size="640px" title="采购单详情">
    <ElSkeleton v-if="loading" :rows="6" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="采购单 ID">{{ detail.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="采购人">{{ detail.assigneeName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="电话">{{ detail.phone || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="业务状态">
          {{ PURCHASE_STATUS_MAP[detail.status ?? 0] ?? detail.status }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批状态">
          <ElTag :type="auditTagType(detail.auditStatus)" size="small">
            {{ PURCHASE_AUDIT_STATUS_MAP[detail.auditStatus || ''] || detail.auditStatus || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">
          {{ detail.wareName || (detail.wareId != null ? `仓库#${detail.wareId}` : '-') }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="金额">{{ detail.amount ?? '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <div
        v-if="hasAuth('mall:purchase:edit') && canSubmitPurchaseAudit(detail.auditStatus)"
        class="action-bar"
      >
        <ElButton :loading="submitting" type="primary" @click="handleSubmitApproval">
          提交审批
        </ElButton>
      </div>

      <ApprovalTimeline
        v-if="detail.approvalInstanceId"
        :instance-id="detail.approvalInstanceId"
        class="mb-4"
      />

      <ElTable :data="detail.details || []" border size="small">
        <ElTableColumn prop="id" label="明细ID" width="80" />
        <ElTableColumn prop="skuId" label="SKU" width="80" />
        <ElTableColumn label="SKU 名称" min-width="140" prop="skuName" show-overflow-tooltip />
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
    canSubmitPurchaseAudit,
    fetchPurchaseById,
    fetchPurchaseSubmitApproval,
    PURCHASE_AUDIT_STATUS_MAP,
    PURCHASE_DETAIL_STATUS_MAP,
    PURCHASE_STATUS_MAP,
    type PurchaseVo
  } from '@/api/mall/purchase'
  import ApprovalTimeline from '@/views/approval/components/ApprovalTimeline.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { ElMessage, ElMessageBox } from 'element-plus'

  interface Props {
    visible: boolean
    purchaseId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'changed'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { hasAuth } = useAuth()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const submitting = ref(false)
  const detail = ref<PurchaseVo | null>(null)

  function auditTagType(status?: string) {
    if (status === 'APPROVED') return 'success'
    if (status === 'REJECTED') return 'danger'
    if (status === 'PENDING') return 'warning'
    return 'info'
  }

  async function loadDetail() {
    if (props.purchaseId == null) {
      detail.value = null
      return
    }
    loading.value = true
    try {
      detail.value = await fetchPurchaseById(props.purchaseId)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.visible, props.purchaseId] as const,
    async ([visible]) => {
      if (!visible) {
        detail.value = null
        return
      }
      await loadDetail()
    }
  )

  async function handleSubmitApproval() {
    if (!detail.value?.id) return
    await ElMessageBox.confirm('确定提交该采购单审批吗？', '提交审批', { type: 'info' })
    submitting.value = true
    try {
      await fetchPurchaseSubmitApproval(detail.value.id)
      ElMessage.success('已提交审批')
      await loadDetail()
      emit('changed')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }

  .action-bar {
    margin-bottom: 16px;
  }
</style>

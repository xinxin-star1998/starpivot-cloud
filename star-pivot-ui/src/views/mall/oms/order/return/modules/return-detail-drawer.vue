<template>
  <ElDrawer v-model="drawerVisible" destroy-on-close size="640px" title="退货详情">
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="订单号">{{ detail.orderSn }}</ElDescriptionsItem>
        <ElDescriptionsItem label="会员">{{ detail.memberUsername || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="业务状态">
          {{ RETURN_STATUS_MAP[detail.status ?? 0] ?? detail.status }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批状态">
          <ElTag :type="auditTagType(detail.auditStatus)" size="small">
            {{ RETURN_AUDIT_STATUS_MAP[detail.auditStatus || ''] || detail.auditStatus || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="退款金额"
          >¥{{ formatAmount(detail.returnAmount) }}</ElDescriptionsItem
        >
        <ElDescriptionsItem label="申请时间">{{ formatDateTime(detail.createTime) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货人">{{ detail.returnName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="联系电话">{{ detail.returnPhone || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货原因">{{ detail.reason || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="问题描述">{{ detail.description || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理人">{{ detail.handleMan || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理时间">{{ formatDateTime(detail.handleTime) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="处理备注">{{ detail.handleNote || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <div v-if="hasAuth('mall:return:audit') && canSubmitReturnAudit(detail)" class="action-bar">
        <ElButton :loading="submitting" type="primary" @click="handleSubmitApproval">
          提交审批
        </ElButton>
      </div>

      <ApprovalTimeline
        v-if="detail.approvalInstanceId"
        :instance-id="detail.approvalInstanceId"
        class="mb-4"
      />

      <h4 class="section-title">商品信息</h4>
      <ElDescriptions :column="1" border size="small">
        <ElDescriptionsItem label="SKU">{{ detail.skuName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="品牌">{{ detail.skuBrand || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="规格">{{ detail.skuAttrsVals || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="数量">{{ detail.skuCount ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="单价">¥{{ formatAmount(detail.skuPrice) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="实付"
          >¥{{ formatAmount(detail.skuRealPrice) }}</ElDescriptionsItem
        >
      </ElDescriptions>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {
  canSubmitReturnAudit,
  fetchReturnById,
  fetchReturnSubmitApproval,
  RETURN_AUDIT_STATUS_MAP,
  RETURN_STATUS_MAP,
  type ReturnVo
} from '@/api/mall/order-return'
import ApprovalTimeline from '@/views/approval/components/ApprovalTimeline.vue'
import {useAuth} from '@/hooks/core/useAuth'
import {ElMessage, ElMessageBox} from 'element-plus'
import {formatDateTime} from '@/utils/common/datetime'

interface Props {
    visible: boolean
    returnId?: number
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
  const detail = ref<ReturnVo | null>(null)

  function auditTagType(status?: string) {
    if (status === 'APPROVED') return 'success'
    if (status === 'REJECTED') return 'danger'
    if (status === 'PENDING') return 'warning'
    return 'info'
  }

  function formatAmount(val?: number) {
    if (val == null) return '0.00'
    return Number(val).toFixed(2)
  }

  async function loadDetail() {
    if (props.returnId == null) {
      detail.value = null
      return
    }
    loading.value = true
    try {
      detail.value = await fetchReturnById(props.returnId)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.visible, props.returnId] as const,
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
    await ElMessageBox.confirm('确定提交该退货申请审批吗？', '提交审批', { type: 'info' })
    submitting.value = true
    try {
      await fetchReturnSubmitApproval(detail.value.id)
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
  .section-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
  }
</style>

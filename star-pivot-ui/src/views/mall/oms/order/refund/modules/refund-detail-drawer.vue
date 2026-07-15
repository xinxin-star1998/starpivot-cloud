<template>
  <ElDrawer v-model="drawerVisible" destroy-on-close size="720px" title="退款详情">
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="退款单号">{{ detail.refundSn || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="订单号">{{ detail.orderSn || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货单ID">{{ detail.orderReturnId ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退款金额">¥{{ formatAmount(detail.refund) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="statusTagType(detail.refundStatus)" size="small">
            {{ REFUND_STATUS_MAP[detail.refundStatus ?? 0] ?? detail.refundStatus ?? '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="渠道">
          {{ REFUND_CHANNEL_MAP[detail.refundChannel ?? 0] ?? detail.refundChannel ?? '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <div v-if="hasAuth('mall:refund:query')" class="action-bar mb-4">
        <ElButton
          v-if="canSync(detail)"
          :loading="syncing"
          type="primary"
          @click="handleSync"
        >
          同步渠道状态
        </ElButton>
        <ElButton
          v-if="canRetry(detail)"
          :loading="retrying"
          type="warning"
          @click="handleRetry"
        >
          重试原路退款
        </ElButton>
      </div>

      <h4 class="section-title">渠道回调 / 查询结果</h4>
      <pre class="callback-json">{{ formattedContent }}</pre>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {
  fetchRefundAckAlert,
  fetchRefundById,
  fetchRefundRetry,
  fetchRefundSync,
  REFUND_CHANNEL_MAP,
  REFUND_STATUS_MAP,
  type RefundVo
} from '@/api/mall/refund'
import {useAuth} from '@/hooks/core/useAuth'
import {ElMessage} from 'element-plus'
import {formatMoney} from '@/utils/mall/money'

interface Props {
  visible: boolean
  refundId?: number
}
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'changed'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { hasAuth } = useAuth()

const loading = ref(false)
const syncing = ref(false)
const retrying = ref(false)
const detail = ref<RefundVo>()

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const formattedContent = computed(() => formatJson(detail.value?.refundContent))

watch(
  () => [props.visible, props.refundId] as const,
  ([visible, id]) => {
    if (visible && id) {
      loadDetail(id)
    }
  },
  { immediate: true }
)

async function loadDetail(id: number) {
  loading.value = true
  try {
    detail.value = await fetchRefundById(id)
    if (detail.value?.refundStatus === 3 && detail.value.id) {
      await fetchRefundAckAlert(detail.value.id)
    }
  } finally {
    loading.value = false
  }
}

function canSync(row?: RefundVo) {
  const status = row?.refundStatus ?? 0
  return status === 0 || status === 1
}

function canRetry(row?: RefundVo) {
  const status = row?.refundStatus ?? 0
  return status === 0 || status === 3
}

function statusTagType(status?: number) {
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  if (status === 1) return 'warning'
  return 'info'
}

function formatAmount(value?: number) {
  return formatMoney(value, '-')
}

function formatJson(raw?: string) {
  if (!raw) {
    return '暂无回调内容'
  }
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

async function handleSync() {
  if (!detail.value?.id) {
    return
  }
  syncing.value = true
  try {
    detail.value = await fetchRefundSync(detail.value.id)
    ElMessage.success('退款状态已同步')
    emit('changed')
  } finally {
    syncing.value = false
  }
}

async function handleRetry() {
  if (!detail.value?.id) {
    return
  }
  retrying.value = true
  try {
    detail.value = await fetchRefundRetry(detail.value.id)
    ElMessage.success('已重新发起原路退款')
    emit('changed')
  } finally {
    retrying.value = false
  }
}
</script>

<style scoped>
.section-title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.callback-json {
  margin: 0;
  padding: 12px;
  max-height: 420px;
  overflow: auto;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.action-bar {
  display: flex;
  gap: 8px;
}
</style>

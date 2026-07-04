<template>
  <ElDrawer v-model="drawerVisible" title="库存工作单" size="560px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="6" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="工作单 ID">{{ detail.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="订单号">{{ detail.orderSn || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="收货人">{{ detail.consignee || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="电话">{{ detail.consigneeTel || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="地址">{{ detail.deliveryAddress || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ TASK_STATUS_MAP[detail.taskStatus ?? 0] ?? detail.taskStatus }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="仓库">{{ detail.wareId ?? '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElTable :data="detail.details || []" border size="small" class="mb-4">
        <ElTableColumn prop="skuId" label="SKU" width="80" />
        <ElTableColumn prop="skuName" label="名称" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="skuNum" label="数量" width="70" />
        <ElTableColumn prop="wareId" label="仓库" width="70" />
        <ElTableColumn prop="lockStatus" label="锁状态" width="80">
          <template #default="{ row }">
            {{ LOCK_STATUS_MAP[row.lockStatus ?? 0] ?? row.lockStatus ?? '-' }}
          </template>
        </ElTableColumn>
      </ElTable>

      <ElSpace v-if="hasAuth('mall:task:edit')">
        <ElButton
          type="warning"
          :disabled="detail.taskStatus !== 0 && detail.taskStatus !== 1"
          :loading="actionLoading === 'lock'"
          @click="doLock"
        >
          锁定库存
        </ElButton>
        <ElButton
          type="success"
          :disabled="detail.taskStatus !== 1"
          :loading="actionLoading === 'deduct'"
          @click="doDeduct"
        >
          扣减出库
        </ElButton>
        <ElButton
          type="danger"
          :disabled="detail.taskStatus !== 1"
          :loading="actionLoading === 'unlock'"
          @click="doUnlock"
        >
          解锁库存
        </ElButton>
      </ElSpace>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {
  fetchWareTaskById,
  fetchWareTaskDeduct,
  fetchWareTaskLock,
  fetchWareTaskUnlock,
  LOCK_STATUS_MAP,
  TASK_STATUS_MAP,
  type WareOrderTaskVo
} from '@/api/mall/ware-task'
import {useAuth} from '@/hooks/core/useAuth'
import {ElMessageBox} from 'element-plus'

interface Props {
    visible: boolean
    taskId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { hasAuth } = useAuth()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const actionLoading = ref<'lock' | 'deduct' | 'unlock' | ''>('')
  const detail = ref<WareOrderTaskVo | null>(null)

  async function loadDetail(id: number) {
    loading.value = true
    try {
      detail.value = await fetchWareTaskById(id)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.visible, props.taskId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      await loadDetail(id)
    }
  )

  async function doLock() {
    if (props.taskId == null) return
    await ElMessageBox.confirm('确定为该工作单锁定库存？', '锁定库存', { type: 'warning' })
    actionLoading.value = 'lock'
    try {
      await fetchWareTaskLock(props.taskId)
      await loadDetail(props.taskId)
      emit('submit')
    } finally {
      actionLoading.value = ''
    }
  }

  async function doDeduct() {
    if (props.taskId == null) return
    await ElMessageBox.confirm('确定扣减库存并完成出库？', '扣减出库', { type: 'warning' })
    actionLoading.value = 'deduct'
    try {
      await fetchWareTaskDeduct(props.taskId)
      await loadDetail(props.taskId)
      emit('submit')
    } finally {
      actionLoading.value = ''
    }
  }

  async function doUnlock() {
    if (props.taskId == null) return
    await ElMessageBox.confirm('确定解锁已锁定的库存？', '解锁库存', { type: 'warning' })
    actionLoading.value = 'unlock'
    try {
      await fetchWareTaskUnlock(props.taskId)
      await loadDetail(props.taskId)
      emit('submit')
    } finally {
      actionLoading.value = ''
    }
  }
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }
</style>

<template>
  <ElDialog v-model="visible" title="物流轨迹" width="520px" destroy-on-close @closed="reset">
    <div v-loading="loading" class="logistics-dialog">
      <template v-if="tracking">
        <p><strong>承运商：</strong>{{ tracking.carrierName || '-' }}</p>
        <p><strong>运单号：</strong>{{ tracking.trackingNo || '-' }}</p>
        <ElTimeline v-if="tracking.events?.length" class="logistics-timeline">
          <ElTimelineItem
            v-for="(event, index) in tracking.events"
            :key="index"
            :timestamp="event.eventTime"
            placement="top"
          >
            <p>{{ event.eventDesc }}</p>
            <p v-if="event.location" class="event-location">{{ event.location }}</p>
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else description="暂无轨迹明细">
          <ElButton v-if="tracking.trackingNo" type="primary" plain @click="openExternal">
            前往快递100查询
          </ElButton>
        </ElEmpty>
      </template>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
import type {ShipmentTracking} from '@/api/tms/types'
import {fetchPortalOrderLogistics} from '@/api/tms/shipment'
import {buildLogisticsTrackUrl} from '@/utils/portal/logistics'
import {handleMutationError} from '@/utils/http/mutation'

const visible = defineModel<boolean>('visible', { default: false })
const orderId = defineModel<number | undefined>('orderId')

const loading = ref(false)
const tracking = ref<ShipmentTracking>()

watch(
  () => [visible.value, orderId.value] as const,
  async ([open, id]) => {
    if (!open || id == null) return
    loading.value = true
    try {
      tracking.value = await fetchPortalOrderLogistics(id)
    } catch (error) {
      handleMutationError(error, '加载物流轨迹失败')
      visible.value = false
    } finally {
      loading.value = false
    }
  }
)

function openExternal() {
  if (!tracking.value?.trackingNo) return
  window.open(
    buildLogisticsTrackUrl(tracking.value.trackingNo, tracking.value.kuaidi100Com || tracking.value.carrierName),
    '_blank',
    'noopener'
  )
}

function reset() {
  tracking.value = undefined
}
</script>

<style scoped>
.logistics-dialog p {
  margin: 0 0 8px;
}

.logistics-timeline {
  margin-top: 16px;
}

.event-location {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

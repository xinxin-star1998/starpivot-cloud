<template>
  <div class="tms-shipment-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="订单号">
          <ElInput v-model="searchForm.orderSn" clearable placeholder="订单号" />
        </ElFormItem>
        <ElFormItem label="运单号">
          <ElInput v-model="searchForm.trackingNo" clearable placeholder="物流单号" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" clearable placeholder="全部" style="width: 140px">
            <ElOption v-for="(label, key) in shipmentStatusLabel" :key="key" :label="label" :value="key" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />
      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDrawer v-model="detailVisible" title="运单详情" size="520px" destroy-on-close>
      <template v-if="detail">
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem label="运单号">{{ detail.shipmentSn }}</ElDescriptionsItem>
          <ElDescriptionsItem label="订单号">{{ detail.orderSn }}</ElDescriptionsItem>
          <ElDescriptionsItem label="承运商">{{ detail.carrierName }}</ElDescriptionsItem>
          <ElDescriptionsItem label="物流单号">{{ detail.trackingNo }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="shipmentStatusTagType[detail.status || ''] || 'info'">
              {{ shipmentStatusLabel[detail.status || ''] || detail.status }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发货时间">{{ detail.shipTime || '-' }}</ElDescriptionsItem>
        </ElDescriptions>
        <div class="track-header">
          <h4>物流轨迹</h4>
          <ElButton
            v-auth="'tms:shipment:track'"
            size="small"
            :loading="refreshing"
            @click="handleRefreshTrack"
          >
            刷新轨迹
          </ElButton>
        </div>
        <ElTimeline v-if="detail.events?.length">
          <ElTimelineItem
            v-for="(event, index) in detail.events"
            :key="index"
            :timestamp="event.eventTime"
            placement="top"
          >
            <p>{{ event.eventDesc }}</p>
            <p v-if="event.location" class="track-location">{{ event.location }}</p>
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else description="暂无轨迹，可点击刷新或配置快递100 API" />
      </template>
    </ElDrawer>
  </div>
</template>

<script lang="ts" setup>
import {h} from 'vue'
import {ElButton, ElMessage, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchTmsShipmentDetail,
  fetchTmsShipmentList,
  fetchTmsShipmentRefreshTrack,
  type TmsShipment
} from '@/api/tms/shipment'
import {shipmentStatusLabel, shipmentStatusTagType} from '@/api/tms/types'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'TmsShipment' })

const searchForm = ref({ orderSn: '', trackingNo: '', status: '' })
const detailVisible = ref(false)
const detail = ref<TmsShipment>()
const refreshing = ref(false)

const {
  columns,
  columnChecks,
  data,
  loading,
  pagination,
  searchParams,
  getData,
  handleSizeChange,
  handleCurrentChange,
  refreshData
} = useTable({
  core: {
    apiFn: fetchTmsShipmentList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'orderSn', label: '订单号', minWidth: 160 },
      { prop: 'carrierName', label: '承运商', minWidth: 110 },
      { prop: 'trackingNo', label: '物流单号', minWidth: 160 },
      {
        prop: 'status',
        label: '状态',
        width: 100,
        formatter: (row: TmsShipment) =>
          h(ElTag, { type: shipmentStatusTagType[row.status || ''] || 'info' }, () =>
            shipmentStatusLabel[row.status || ''] || row.status
          )
      },
      { prop: 'shipTime', label: '发货时间', minWidth: 170 },
      {
        prop: 'actions',
        label: '操作',
        width: 100,
        fixed: 'right',
        formatter: (row: TmsShipment) =>
          h(
            ElButton,
            { link: true, type: 'primary', onClick: () => openDetail(row.id!) },
            () => '详情'
          )
      }
    ]
  }
})

function handleSearch() {
  Object.assign(searchParams, searchForm.value, { pageNum: 1 })
  getData()
}

function resetSearch() {
  searchForm.value = { orderSn: '', trackingNo: '', status: '' }
  handleSearch()
}

async function openDetail(id: number) {
  try {
    detail.value = await fetchTmsShipmentDetail(id)
    detailVisible.value = true
  } catch (error) {
    handleMutationError(error, '加载运单详情失败')
  }
}

async function handleRefreshTrack() {
  if (!detail.value?.id) return
  refreshing.value = true
  try {
    detail.value = await fetchTmsShipmentRefreshTrack(detail.value.id)
    ElMessage.success('轨迹已刷新')
  } catch (error) {
    handleMutationError(error, '刷新轨迹失败')
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped>
.track-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 20px 0 12px;
}

.track-location {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

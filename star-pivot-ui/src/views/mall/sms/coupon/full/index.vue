<!-- 营销-满减折扣（满减 + 阶梯价） -->
<template>
  <div class="sms-reduction-page art-full-height">
    <ElTabs v-model="activeTab">
      <ElTabPane label="满减" name="full">
        <ElCard shadow="never">
          <ElForm :inline="true" @submit.prevent="searchFull">
            <ElFormItem label="SKU ID">
              <ElInputNumber v-model="fullSearch.skuId" :min="1" controls-position="right" />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" @click="searchFull">查询</ElButton>
              <ElButton @click="resetFull">重置</ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>
        <ElCard shadow="never" style="margin-top: 12px">
          <div class="toolbar">
            <ElButton type="primary" @click="openFullDialog('add')">新增满减</ElButton>
          </div>
          <ArtTable
            :loading="fullLoading"
            :data="fullData"
            :columns="fullColumns"
            :pagination="fullPagination"
            @pagination:size-change="fullSizeChange"
            @pagination:current-change="fullPageChange"
          />
        </ElCard>
      </ElTabPane>

      <ElTabPane label="阶梯价" name="ladder">
        <ElCard shadow="never">
          <ElForm :inline="true" @submit.prevent="searchLadder">
            <ElFormItem label="SKU ID">
              <ElInputNumber v-model="ladderSearch.skuId" :min="1" controls-position="right" />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" @click="searchLadder">查询</ElButton>
              <ElButton @click="resetLadder">重置</ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>
        <ElCard shadow="never" style="margin-top: 12px">
          <div class="toolbar">
            <ElButton type="primary" @click="openLadderDialog('add')">新增阶梯价</ElButton>
          </div>
          <ArtTable
            :loading="ladderLoading"
            :data="ladderData"
            :columns="ladderColumns"
            :pagination="ladderPagination"
            @pagination:size-change="ladderSizeChange"
            @pagination:current-change="ladderPageChange"
          />
        </ElCard>
      </ElTabPane>
    </ElTabs>

    <FullReductionDialog
      v-model:visible="fullDialogVisible"
      :type="fullDialogType"
      :record-id="fullRecordId"
      @success="loadFull"
    />
    <LadderDialog
      v-model:visible="ladderDialogVisible"
      :type="ladderDialogType"
      :record-id="ladderRecordId"
      @success="loadLadder"
    />
  </div>
</template>

<script setup lang="ts">
  import { h, onMounted } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import {
    fetchSkuFullReductionList,
    fetchSkuFullReductionRemove,
    fetchSkuLadderList,
    fetchSkuLadderRemove,
    type SkuFullReductionVo,
    type SkuLadderVo
  } from '@/api/mall/sku-promotion'
  import FullReductionDialog from './modules/full-reduction-dialog.vue'
  import LadderDialog from './modules/ladder-dialog.vue'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'SmsFullReduction' })

  const activeTab = ref('full')

  const fullSearch = ref({ skuId: undefined as number | undefined })
  const fullData = ref<SkuFullReductionVo[]>([])
  const fullLoading = ref(false)
  const fullPagination = reactive({ current: 1, size: 10, total: 0 })
  const fullDialogVisible = ref(false)
  const fullDialogType = ref<DialogType>('add')
  const fullRecordId = ref<number>()

  const ladderSearch = ref({ skuId: undefined as number | undefined })
  const ladderData = ref<SkuLadderVo[]>([])
  const ladderLoading = ref(false)
  const ladderPagination = reactive({ current: 1, size: 10, total: 0 })
  const ladderDialogVisible = ref(false)
  const ladderDialogType = ref<DialogType>('add')
  const ladderRecordId = ref<number>()

  const fullColumns = [
    { prop: 'skuId', label: 'SKU ID', width: 100 },
    { prop: 'skuName', label: 'SKU 名称', minWidth: 160 },
    { prop: 'fullPrice', label: '满(元)', width: 100 },
    { prop: 'reducePrice', label: '减(元)', width: 100 },
    {
      prop: 'operation',
      label: '操作',
      width: 160,
      formatter: (row: SkuFullReductionVo) =>
        h('div', [
          h(ArtButtonTable, {
            label: '编辑',
            type: 'edit',
            onClick: () => openFullDialog('edit', row.id)
          }),
          h(ArtButtonTable, {
            label: '删除',
            type: 'delete',
            onClick: () => deleteFull(row)
          })
        ])
    }
  ]

  const ladderColumns = [
    { prop: 'skuId', label: 'SKU ID', width: 100 },
    { prop: 'skuName', label: 'SKU 名称', minWidth: 160 },
    { prop: 'fullCount', label: '满(件)', width: 90 },
    { prop: 'discount', label: '折扣', width: 90 },
    { prop: 'price', label: '折后价', width: 100 },
    {
      prop: 'operation',
      label: '操作',
      width: 160,
      formatter: (row: SkuLadderVo) =>
        h('div', [
          h(ArtButtonTable, {
            label: '编辑',
            type: 'edit',
            onClick: () => openLadderDialog('edit', row.id)
          }),
          h(ArtButtonTable, {
            label: '删除',
            type: 'delete',
            onClick: () => deleteLadder(row)
          })
        ])
    }
  ]

  const loadFull = async () => {
    fullLoading.value = true
    try {
      const res = await fetchSkuFullReductionList({
        pageNum: fullPagination.current,
        pageSize: fullPagination.size,
        skuId: fullSearch.value.skuId
      })
      fullData.value = res.rows || []
      fullPagination.total = res.total || 0
    } finally {
      fullLoading.value = false
    }
  }

  const loadLadder = async () => {
    ladderLoading.value = true
    try {
      const res = await fetchSkuLadderList({
        pageNum: ladderPagination.current,
        pageSize: ladderPagination.size,
        skuId: ladderSearch.value.skuId
      })
      ladderData.value = res.rows || []
      ladderPagination.total = res.total || 0
    } finally {
      ladderLoading.value = false
    }
  }

  onMounted(() => {
    loadFull()
    loadLadder()
  })

  const searchFull = () => {
    fullPagination.current = 1
    loadFull()
  }
  const resetFull = () => {
    fullSearch.value = { skuId: undefined }
    searchFull()
  }
  const fullSizeChange = (size: number) => {
    fullPagination.size = size
    loadFull()
  }
  const fullPageChange = (page: number) => {
    fullPagination.current = page
    loadFull()
  }

  const searchLadder = () => {
    ladderPagination.current = 1
    loadLadder()
  }
  const resetLadder = () => {
    ladderSearch.value = { skuId: undefined }
    searchLadder()
  }
  const ladderSizeChange = (size: number) => {
    ladderPagination.size = size
    loadLadder()
  }
  const ladderPageChange = (page: number) => {
    ladderPagination.current = page
    loadLadder()
  }

  const openFullDialog = (type: DialogType, id?: number) => {
    fullDialogType.value = type
    fullRecordId.value = id
    fullDialogVisible.value = true
  }

  const openLadderDialog = (type: DialogType, id?: number) => {
    ladderDialogType.value = type
    ladderRecordId.value = id
    ladderDialogVisible.value = true
  }

  const deleteFull = (row: SkuFullReductionVo) => {
    if (!row.id) return
    ElMessageBox.confirm('确定删除该满减规则吗？', '删除', { type: 'warning' })
      .then(async () => {
        await fetchSkuFullReductionRemove([row.id!])
        loadFull()
      })
      .catch(() => {})
  }

  const deleteLadder = (row: SkuLadderVo) => {
    if (!row.id) return
    ElMessageBox.confirm('确定删除该阶梯价规则吗？', '删除', { type: 'warning' })
      .then(async () => {
        await fetchSkuLadderRemove([row.id!])
        loadLadder()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .sms-reduction-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
  .toolbar {
    margin-bottom: 12px;
  }
</style>
